#!/bin/bash
#
# Generate diffs showing what changed in doc between two Solution versions
#
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <old Solution version git tag> <new Solution version git tag>"
    echo "For example: $0 1.0.0 1.1.0"
    echo "** Be sure the new tag has been pushed to github"
    exit -1
fi

oldVersion="$1"
newVersion="$2"
userDir=$(pwd)
workingDir=$(mktemp -d -t docDiff-XXXXXXXXXX)
diffOneScript=/tmp/diffOneDocFile.sh
outputFilename=doc_diff_${newVersion}_vs_${oldVersion}.txt
intermediateOutputFilePath="${workingDir}/${outputFilename}"

echo "Working in ${workingDir}"
cd "${workingDir}"

echo "#!/bin/bash" > ${diffOneScript}
echo "# Args: otherVersion filepath" >> ${diffOneScript}
echo "echo \"=====================================================\"" >> ${diffOneScript}
echo "echo \"\$2 vs. \$1\"" >> ${diffOneScript}
echo "echo \"(old lines are preceded by '<'; new lines by '>')\"" >> ${diffOneScript}
echo "echo \"-----------------------------------------------------\"" >> ${diffOneScript}
echo "if test -f \"../../../../\$1/blackduck-coverity-on-polaris-plugin/docs/generated/\$2\" ; then" >> ${diffOneScript}
echo "    diff \"../../../../\$1/blackduck-coverity-on-polaris-plugin/docs/generated/\$2\" \"\$2\"" >> ${diffOneScript}
echo "else" >> ${diffOneScript}
echo "    echo \"************ \$2\" is NEW" >> ${diffOneScript}
echo "fi" >> ${diffOneScript}
echo "echo \"\"" >> ${diffOneScript}

chmod +x ${diffOneScript}

mkdir ${oldVersion}
cd ${oldVersion}
git clone https://github.com/jenkinsci/blackduck-coverity-on-polaris-plugin.git
cd blackduck-coverity-on-polaris-plugin
git checkout tags/${oldVersion}

# Write the header
echo "Generating diffs from:" > ${intermediateOutputFilePath}
git status >> ${intermediateOutputFilePath}
git show >>${intermediateOutputFilePath}
echo "" >> ${intermediateOutputFilePath}

./mvnw docs -x test

cd "${workingDir}"
mkdir ${newVersion}
cd ${newVersion}
git clone https://github.com/jenkinsci/blackduck-coverity-on-polaris-plugin.git
cd blackduck-coverity-on-polaris-plugin
git checkout tags/${newVersion}

# Write the header
echo "Generating diffs up through:" >> ${intermediateOutputFilePath}
git status >> ${intermediateOutputFilePath}
git show >>${intermediateOutputFilePath}
echo "" >> ${intermediateOutputFilePath}

./mvnw docs -x test
cd docs/generated

# Do the diffing
find . -name "*.md" -exec ${diffOneScript} ${oldVersion} {} \; >> ${intermediateOutputFilePath}

# Provide output file
cd "${userDir}"
cp ${intermediateOutputFilePath} .
echo "Output file: ${outputFilename}"

# Clean up
rm -rf "${workingDir}"

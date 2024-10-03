/*
 * blackduck-coverity-on-polaris
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.jenkins.polaris.extensions.pipeline;

import com.blackduck.integration.jenkins.polaris.extensions.tools.PolarisCli;
import com.blackduck.integration.jenkins.polaris.service.PolarisCommandsFactory;
import com.synopsys.integration.jenkins.annotations.HelpMarkdown;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tools.ToolInstallation;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class ExecutePolarisCliStep extends Step implements Serializable {
    public static final String DISPLAY_NAME = "Execute Coverity on Polaris CLI";
    public static final String PIPELINE_NAME = "polaris";
    private static final long serialVersionUID = -2698425344634481146L;

    @HelpMarkdown("The command line arguments to pass to the Coverity on Polaris CLI")
    private final String arguments;

    @Nullable
    @HelpMarkdown("The Coverity on Polaris CLI installation to execute")
    private String polarisCli;

    @Nullable
    @HelpMarkdown(
            "If true (checked), returns the status code of the Coverity on Polaris CLI run instead of throwing an exception")
    private Boolean returnStatus;

    @Nullable
    @HelpMarkdown(
            "Creates a file at $CHANGE_SET_FILE_PATH (by default, the workspace directory) containing a list of files generated from the Jenkins-provided SCM changeset.  \r\n"
                    + "Used for Incremental analysis (--incremental) as the file containing the list of changed files for analysis.")
    private PipelineCreateChangeSetFile createChangeSetFile;

    @DataBoundConstructor
    public ExecutePolarisCliStep(String arguments) {
        this.arguments = arguments;
    }

    @Nullable
    public String getPolarisCli() {
        return polarisCli;
    }

    @DataBoundSetter
    public void setPolarisCli(String polarisCli) {
        this.polarisCli = polarisCli;
    }

    public String getArguments() {
        return arguments;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(context);
    }

    @Nullable
    public Boolean getReturnStatus() {
        if (!Boolean.TRUE.equals(returnStatus)) {
            return null;
        }
        return returnStatus;
    }

    @DataBoundSetter
    public void setReturnStatus(Boolean returnStatus) {
        this.returnStatus = returnStatus;
    }

    @Nullable
    public PipelineCreateChangeSetFile getCreateChangeSetFile() {
        return createChangeSetFile;
    }

    @DataBoundSetter
    public void setCreateChangeSetFile(@Nullable PipelineCreateChangeSetFile createChangeSetFile) {
        this.createChangeSetFile = createChangeSetFile;
    }

    @Symbol(PIPELINE_NAME)
    @Extension(optional = true)
    public static final class DescriptorImpl extends StepDescriptor {
        public DescriptorImpl() {
            // Nothing to do here, but we must provide an explicit default constructor or else some versions of the
            // Pipeline syntax generator will break
            // -rotte JAN 2020
        }

        public ListBoxModel doFillPolarisCliItems() {
            PolarisCli.DescriptorImpl polarisCliToolInstallationDescriptor =
                    ToolInstallation.all().get(PolarisCli.DescriptorImpl.class);

            if (polarisCliToolInstallationDescriptor == null) {
                return new ListBoxModel();
            }

            return Stream.of(polarisCliToolInstallationDescriptor.getInstallations())
                    .map(PolarisCli::getName)
                    .map(ListBoxModel.Option::new)
                    .collect(Collectors.toCollection(ListBoxModel::new));
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return new HashSet<>(Arrays.asList(
                    TaskListener.class, EnvVars.class, FilePath.class, Launcher.class, Node.class, Run.class));
        }

        @Override
        public String getFunctionName() {
            return PIPELINE_NAME;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return DISPLAY_NAME;
        }
    }

    public class Execution extends SynchronousNonBlockingStepExecution<Integer> {
        private static final long serialVersionUID = -3799159740768688972L;

        @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
        private final transient TaskListener listener;

        @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
        private final transient EnvVars envVars;

        @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
        private final transient FilePath workspace;

        private final transient Launcher launcher;
        private final transient Node node;
        private final transient Run<?, ?> run;

        protected Execution(@Nonnull StepContext context) throws InterruptedException, IOException {
            super(context);
            listener = context.get(TaskListener.class);
            envVars = context.get(EnvVars.class);
            workspace = context.get(FilePath.class);
            launcher = context.get(Launcher.class);
            node = context.get(Node.class);
            run = context.get(Run.class);
        }

        @Override
        protected Integer run() throws Exception {
            return PolarisCommandsFactory.fromPipeline(listener, envVars, launcher, node, run, workspace)
                    .runPolarisCli(polarisCli, arguments, returnStatus, createChangeSetFile);
        }
    }
}

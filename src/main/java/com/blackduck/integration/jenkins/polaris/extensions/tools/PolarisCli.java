/*
 * blackduck-coverity-on-polaris
 *
 * Copyright ©2024 Black Duck Software, Inc. All rights reserved.
 * Black Duck® is a trademark of Black Duck Software, Inc. in the United States and other countries.
 */
package com.blackduck.integration.jenkins.polaris.extensions.tools;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Util;
import hudson.model.EnvironmentSpecific;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolProperty;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class PolarisCli extends ToolInstallation implements NodeSpecific<PolarisCli>, EnvironmentSpecific<PolarisCli> {
    private static final long serialVersionUID = -3838254855454518440L;

    @DataBoundConstructor
    public PolarisCli(String name, String home, List<? extends ToolProperty<?>> properties) {
        super(Util.fixEmptyAndTrim(name), Util.fixEmptyAndTrim(home), properties);
    }

    @Override
    public PolarisCli forNode(@Nonnull Node node, TaskListener log) throws IOException, InterruptedException {
        return new PolarisCli(
                getName(), translateFor(node, log), getProperties().toList());
    }

    @Override
    public PolarisCli forEnvironment(EnvVars environment) {
        return new PolarisCli(
                getName(), environment.expand(getHome()), getProperties().toList());
    }

    @Override
    public void buildEnvVars(EnvVars env) {
        // This is what the gradle plugin does, so it's probably good enough for us --rotte (JAN 2020)
        env.putIfNotNull("PATH+POLARIS", getHome() + "/bin");
    }

    @Extension
    @Symbol("polarisCli")
    public static final class DescriptorImpl extends ToolDescriptor<PolarisCli> {
        @Override
        public String getDisplayName() {
            return "Coverity on Polaris CLI";
        }

        @Override
        public PolarisCli[] getInstallations() {
            load();
            return super.getInstallations();
        }

        @Override
        public void setInstallations(PolarisCli... installations) {
            super.setInstallations(installations);
            save();
        }

        @Override
        public List<? extends ToolInstaller> getDefaultInstallers() {
            return Collections.singletonList(new PolarisCliInstaller(null));
        }
    }
}

package com.synopsys.integration.jenkins.polaris.extensions.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import hudson.util.FormValidation;

@PowerMockIgnore({"javax.crypto.*", "javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
public class PolarisGlobalConfigTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void testMissingCredentials() {
        final PolarisGlobalConfig detectGlobalConfig = new PolarisGlobalConfig();
        final FormValidation formValidation = detectGlobalConfig.doTestPolarisConnection("https://blackduck.domain.com", "123", "30");

        assertEquals(FormValidation.Kind.ERROR, formValidation.kind);
        assertTrue(formValidation.getMessage().contains("token"));
        System.out.printf("Message: %s\n", formValidation.getMessage());
    }
}

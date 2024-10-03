/*
 * blackduck-coverity-on-polaris
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.jenkins.polaris;

public enum PolarisJenkinsEnvironmentVariable {
    CHANGE_SET_FILE_PATH("CHANGE_SET_FILE_PATH");

    private final String environmentVariable;

    PolarisJenkinsEnvironmentVariable(String environmentVariable) {
        this.environmentVariable = environmentVariable;
    }

    public String stringValue() {
        return environmentVariable;
    }
}
/*
 * blackduck-coverity-on-polaris
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.jenkins.polaris.service;

import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.util.OperatingSystemType;
import java.util.ArrayList;
import java.util.List;

public class PolarisCliArgumentService {
    private final IntLogger logger;

    public PolarisCliArgumentService(IntLogger logger) {
        this.logger = logger;
    }

    public List<String> finalizePolarisCliArguments(
            OperatingSystemType operatingSystemType,
            String pathToPolarisCli,
            List<String> tokenizedPolarisArgumentString) {
        List<String> escapedArguments = new ArrayList<>();
        escapedArguments.add(pathToPolarisCli);

        if (OperatingSystemType.WINDOWS.equals(operatingSystemType)) {
            boolean isJson = false;
            for (String argument : tokenizedPolarisArgumentString) {
                if (isJson) {
                    escapedArguments.add(argument.replace("\"", "\\\""));
                } else {
                    escapedArguments.add(argument);
                }
                isJson = "--co".equals(argument);
            }
        } else {
            escapedArguments.addAll(tokenizedPolarisArgumentString);
        }

        logger.alwaysLog("Executing " + String.join(" ", escapedArguments));

        return escapedArguments;
    }
}

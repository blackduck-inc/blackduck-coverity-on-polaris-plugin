/*
 * blackduck-coverity-on-polaris
 *
 * Copyright ©2024 Black Duck Software, Inc. All rights reserved.
 * Black Duck® is a trademark of Black Duck Software, Inc. in the United States and other countries.
 */
package com.blackduck.integration.jenkins.polaris.service;

import com.blackduck.integration.jenkins.extensions.JenkinsIntLogger;
import com.blackduck.integration.jenkins.wrapper.JenkinsVersionHelper;
import com.blackduck.integration.phonehome.PhoneHomeClient;
import com.blackduck.integration.phonehome.PhoneHomeResponse;
import com.blackduck.integration.phonehome.PhoneHomeService;
import com.blackduck.integration.phonehome.request.PhoneHomeRequestBody;
import com.blackduck.integration.phonehome.request.PhoneHomeRequestBodyBuilder;
import com.blackduck.integration.polaris.common.api.PolarisResource;
import com.blackduck.integration.polaris.common.api.model.ContextAttributes;
import com.blackduck.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.blackduck.integration.polaris.common.service.ContextsService;
import com.google.gson.Gson;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.impl.client.HttpClientBuilder;

public class PolarisPhoneHomeService {
    private final JenkinsIntLogger logger;
    private final JenkinsVersionHelper jenkinsVersionHelper;
    private final ContextsService contextsService;
    private final AccessTokenPolarisHttpClient accessTokenPolarisHttpClient;

    public PolarisPhoneHomeService(
            JenkinsIntLogger logger,
            JenkinsVersionHelper jenkinsVersionHelper,
            ContextsService contextsService,
            AccessTokenPolarisHttpClient accessTokenPolarisHttpClient) {
        this.logger = logger;
        this.jenkinsVersionHelper = jenkinsVersionHelper;
        this.contextsService = contextsService;
        this.accessTokenPolarisHttpClient = accessTokenPolarisHttpClient;
    }

    public Optional<PhoneHomeResponse> phoneHome() {
        try {
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            Gson gson = new Gson();
            PhoneHomeClient phoneHomeClient = new PhoneHomeClient(logger, httpClientBuilder, gson, null, (String) null);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            PhoneHomeService phoneHomeService =
                    PhoneHomeService.createAsynchronousPhoneHomeService(logger, phoneHomeClient, executor);

            PhoneHomeRequestBody phoneHomeRequestBody = buildPhoneHomeRequest();

            return Optional.ofNullable(phoneHomeService.phoneHome(phoneHomeRequestBody));
        } catch (Exception e) {
            logger.trace("Phone home failed due to an unexpected exception:", e);
        }

        return Optional.empty();
    }

    private PhoneHomeRequestBody buildPhoneHomeRequest() {
        String organizationName;
        try {
            organizationName = contextsService
                    .getCurrentContext()
                    .map(PolarisResource::getAttributes)
                    .map(ContextAttributes::getOrganizationname)
                    .orElse(PhoneHomeRequestBody.UNKNOWN_FIELD_VALUE);
        } catch (Exception ex) {
            organizationName = PhoneHomeRequestBody.UNKNOWN_FIELD_VALUE;
        }

        PhoneHomeRequestBodyBuilder phoneHomeRequestBodyBuilder = PhoneHomeRequestBodyBuilder.createForPolaris(
                "blackduck-coverity-on-polaris-plugin",
                organizationName,
                accessTokenPolarisHttpClient.getPolarisServerUrl().string(),
                jenkinsVersionHelper
                        .getPluginVersion("blackduck-coverity-on-polaris")
                        .orElse(PhoneHomeRequestBody.UNKNOWN_FIELD_VALUE),
                PhoneHomeRequestBody.UNKNOWN_FIELD_VALUE);

        jenkinsVersionHelper
                .getJenkinsVersion()
                .ifPresent(jenkinsVersionString ->
                        phoneHomeRequestBodyBuilder.addToMetaData("jenkins.version", jenkinsVersionString));

        return phoneHomeRequestBodyBuilder.build();
    }
}

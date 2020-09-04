/**
 * synopsys-polaris
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.polaris.common.service;

import java.util.List;
import java.util.Optional;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.api.PolarisPagedResourceResponse;
import com.synopsys.integration.polaris.common.api.auth.model.ContextAttributes;
import com.synopsys.integration.polaris.common.api.auth.model.ContextResource;
import com.synopsys.integration.polaris.common.request.PolarisRequestFactory;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.request.Request;

public class ContextsService {
    private static final TypeToken<PolarisPagedResourceResponse<ContextResource>> CONTEXT_RESOURCES = new TypeToken<PolarisPagedResourceResponse<ContextResource>>() {};
    private final PolarisService polarisService;
    private final AccessTokenPolarisHttpClient polarisHttpClient;

    public ContextsService(PolarisService polarisService, AccessTokenPolarisHttpClient polarisHttpClient) {
        this.polarisService = polarisService;
        this.polarisHttpClient = polarisHttpClient;
    }

    public List<ContextResource> getAllContexts() throws IntegrationException {
        Request request = PolarisRequestFactory.createDefaultPolarisGetRequest(polarisHttpClient.getPolarisServerUrl() + "/api/auth/contexts");
        return polarisService.getAllResponses(request, CONTEXT_RESOURCES.getType());
    }

    public Optional<ContextResource> getCurrentContext() throws IntegrationException {
        return getAllContexts().stream()
                   .filter(this::isCurrentContext)
                   .findFirst();
    }

    private Boolean isCurrentContext(ContextResource context) {
        return Optional.ofNullable(context)
                   .map(ContextResource::getAttributes)
                   .map(ContextAttributes::getCurrent)
                   .orElse(Boolean.FALSE);
    }

}
/*
 * blackduck-coverity-on-polaris
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.polaris.common.api;

import com.google.gson.annotations.SerializedName;

public class PolarisResource<A extends PolarisAttributes> extends PolarisResponse {
    private static final long serialVersionUID = 7914255365770861000L;

    @SerializedName("type")
    private String type;

    @SerializedName("id")
    private String id;

    @SerializedName("attributes")
    private A attributes = null;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public A getAttributes() {
        return attributes;
    }

    public void setAttributes(A attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}

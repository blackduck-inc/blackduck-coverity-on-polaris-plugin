/*
 * synopsys-polaris
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.polaris.common.api;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

public class PolarisPaginationMeta extends PolarisResponse {
    @SerializedName("offset")
    private BigDecimal offset;

    @SerializedName("limit")
    private BigDecimal limit;

    @SerializedName("total")
    private BigDecimal total;

    /**
     * The offset used for this request.  If null, no offset was applied.
     * @return offset
     */
    public BigDecimal getOffset() {
        return offset;
    }

    public void setOffset(BigDecimal offset) {
        this.offset = offset;
    }

    /**
     * The maximum number of elements returned for this request.  If null, no limit was applied.
     * @return limit
     */
    public BigDecimal getLimit() {
        return limit;
    }

    public void setLimit(BigDecimal limit) {
        this.limit = limit;
    }

    /**
     * The total number of results matching the provided criteria, without regard to the provided offset or limit.
     * @return total
     */
    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}

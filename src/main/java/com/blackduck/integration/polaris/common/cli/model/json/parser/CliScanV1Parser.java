/*
 * synopsys-polaris
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.polaris.common.cli.model.json.parser;

import com.blackduck.integration.polaris.common.cli.model.CliCommonResponseModel;
import com.blackduck.integration.polaris.common.cli.model.CommonToolInfo;
import com.blackduck.integration.polaris.common.cli.model.json.v1.CliScanV1;
import com.blackduck.integration.polaris.common.cli.model.json.v1.ToolInfoV1;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.exception.IntegrationException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CliScanV1Parser extends CliScanParser<CliScanV1> {
    public CliScanV1Parser(Gson gson) {
        super(gson);
    }

    @Override
    public TypeToken<CliScanV1> getTypeToken() {
        return new TypeToken<CliScanV1>() {};
    }

    @Override
    @SuppressFBWarnings(value = "UWF_UNWRITTEN_PUBLIC_OR_PROTECTED_FIELD")
    public CliCommonResponseModel fromCliScan(JsonObject versionlessModel) throws IntegrationException {
        CliScanV1 cliScanV1 = fromJson(versionlessModel);
        CliCommonResponseModel cliCommonResponseModel =
                createResponseModel(cliScanV1.issueSummary, cliScanV1.projectInfo, cliScanV1.scanInfo);

        List<CommonToolInfo> tools = new ArrayList<>();
        // TODO verify case of tool names
        fromToolInfoV1(cliScanV1.blackDuckScaToolInfo, "sca", tools::add);
        fromToolInfoV1(cliScanV1.coverityToolInfo, "Coverity", tools::add);

        cliCommonResponseModel.setTools(tools);

        return cliCommonResponseModel;
    }

    private void fromToolInfoV1(ToolInfoV1 toolInfoV1, String toolName, Consumer<CommonToolInfo> consumer)
            throws IntegrationException {
        if (toolInfoV1 != null) {
            CommonToolInfo commonToolInfo = createCommonToolInfo(toolInfoV1);
            commonToolInfo.setToolName(toolName);

            consumer.accept(commonToolInfo);
        }
    }
}

/*
 * synopsys-polaris
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.polaris.common.service;

import com.blackduck.integration.polaris.common.api.PolarisResource;
import com.blackduck.integration.polaris.common.api.model.FailureInfo;
import com.blackduck.integration.polaris.common.api.model.JobAttributes;
import com.blackduck.integration.polaris.common.api.model.JobStatus;
import com.blackduck.integration.polaris.common.exception.PolarisIntegrationException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.wait.WaitJob;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class JobService {
    public static final long DEFAULT_TIMEOUT = 30 * 60L;
    public static final int DEFAULT_WAIT_INTERVAL = 5;

    private final IntLogger logger;
    private final PolarisService polarisService;

    public JobService(IntLogger logger, PolarisService polarisService) {
        this.logger = logger;
        this.polarisService = polarisService;
    }

    public PolarisResource<JobAttributes> getJobByUrl(HttpUrl jobApiUrl) throws IntegrationException {
        return polarisService.get(jobApiUrl, JobAttributes.class);
    }

    public void waitForJobStateIsCompletedOrDieByUrl(
            HttpUrl jobApiUrl, long timeoutInSeconds, int waitIntervalInSeconds)
            throws IntegrationException, InterruptedException {
        WaitJob waitJob = WaitJob.createUsingSystemTimeWhenInvoked(
                logger, timeoutInSeconds, waitIntervalInSeconds, () -> hasJobEnded(jobApiUrl));
        if (!waitJob.waitFor()) {
            String maximumDurationString = DurationFormatUtils.formatDurationHMS(timeoutInSeconds * 1000);
            throw new PolarisIntegrationException(String.format(
                    "Job at url %s did not end in the provided timeout of %s", jobApiUrl, maximumDurationString));
        }

        PolarisResource<JobAttributes> jobResource = this.getJobByUrl(jobApiUrl);
        JobStatus.StateEnum jobState = Optional.ofNullable(jobResource)
                .map(PolarisResource::getAttributes)
                .map(JobAttributes::getStatus)
                .map(JobStatus::getState)
                .orElseThrow(() -> new PolarisIntegrationException(
                        String.format("Job at url %s ended but its state cannot be determined.", jobApiUrl)));

        if (!JobStatus.StateEnum.COMPLETED.equals(jobState)) {
            StringBuilder errorMessageBuilder = new StringBuilder();
            errorMessageBuilder.append(String.format(
                    "Job at url %s ended with state %s instead of %s",
                    jobApiUrl, jobState, JobStatus.StateEnum.COMPLETED));
            if (JobStatus.StateEnum.FAILED.equals(jobState)) {
                // Niether Data nor Attributes can be null because they were validated above -- rotte MAR 2020
                FailureInfo failureInfo = jobResource.getAttributes().getFailureInfo();
                if (failureInfo != null && StringUtils.isNotBlank(failureInfo.getUserFriendlyFailureReason())) {
                    errorMessageBuilder.append(
                            String.format(" because: %s", failureInfo.getUserFriendlyFailureReason()));
                }
            }
            errorMessageBuilder.append(
                    "\r\nCheck the job status in the Polaris Software Integrity Platform for more details.");

            throw new PolarisIntegrationException(errorMessageBuilder.toString());
        }
    }

    private boolean hasJobEnded(HttpUrl jobApiUrl) throws IntegrationException {
        String jobStatusPrefix = "Job at url " + jobApiUrl;

        try {
            Optional<JobStatus> optionalJobStatus = Optional.ofNullable(getJobByUrl(jobApiUrl))
                    .map(PolarisResource::getAttributes)
                    .map(JobAttributes::getStatus);

            if (!optionalJobStatus.isPresent()) {
                logger.info(jobStatusPrefix + " was found but the job status could not be determined.");
                return false;
            }

            JobStatus jobStatus = optionalJobStatus.get();
            JobStatus.StateEnum stateEnum = jobStatus.getState();
            if (JobStatus.StateEnum.QUEUED.equals(stateEnum)
                    || JobStatus.StateEnum.RUNNING.equals(stateEnum)
                    || JobStatus.StateEnum.DISPATCHED.equals(stateEnum)) {
                logger.info(jobStatusPrefix + " was found with status " + stateEnum.toString() + ". Progress: "
                        + jobStatus.getProgress());
                return false;
            }

        } catch (IntegrationException e) {
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                logger.info(jobStatusPrefix + " could not be found.");
            } else {
                throw e;
            }
        }

        return true;
    }
}

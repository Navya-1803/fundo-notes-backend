package com.fundoo.notes.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.PositiveOrZero;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Starts the trash cleanup batch job on demand and reports its result.
 *
 * @author Navya Raj
 */
@RestController
@RequestMapping("/api/batch")
@Validated
public class BatchJobController {

    private static final Logger log = LoggerFactory.getLogger(BatchJobController.class);

    private final JobOperator jobOperator;
    private final Job trashCleanupJob;

    public BatchJobController(JobOperator jobOperator, @Qualifier("trashCleanupJob") Job trashCleanupJob) {
        this.jobOperator = jobOperator;
        this.trashCleanupJob = trashCleanupJob;
    }

    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runTrashCleanup(
            @RequestParam(required = false) @PositiveOrZero Long retentionDays) throws Exception {
        // a timestamp makes every run a new job instance
        JobParametersBuilder parameters = new JobParametersBuilder().addLong("startedAt", System.currentTimeMillis());
        if (retentionDays != null) {
            parameters.addLong("retentionDays", retentionDays);
        }
        JobParameters jobParameters = parameters.toJobParameters();
        log.info("Starting job {} with retentionDays: {}", trashCleanupJob.getName(), retentionDays);
        JobExecution execution = jobOperator.start(trashCleanupJob, jobParameters);
        return ResponseEntity.ok(toResponse(execution));
    }

    private Map<String, Object> toResponse(JobExecution execution) {
        long read = execution.getStepExecutions().stream().mapToLong(StepExecution::getReadCount).sum();
        long written = execution.getStepExecutions().stream().mapToLong(StepExecution::getWriteCount).sum();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("jobName", trashCleanupJob.getName());
        body.put("jobExecutionId", execution.getId());
        body.put("status", execution.getStatus().toString());
        body.put("notesRead", read);
        body.put("notesDeleted", written);
        log.info("Job {} finished with status {}, deleted {} note(s)", trashCleanupJob.getName(), execution.getStatus(), written);
        return body;
    }
}

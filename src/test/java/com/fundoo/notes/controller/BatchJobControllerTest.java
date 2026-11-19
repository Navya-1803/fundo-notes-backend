package com.fundoo.notes.controller;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BatchJobController.class)
class BatchJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobOperator jobOperator;

    @MockitoBean(name = "trashCleanupJob")
    private Job trashCleanupJob;

    private JobExecution completedExecution() {
        JobExecution execution = new JobExecution(1L, new JobInstance(1L, "trashCleanupJob"), new JobParameters());
        execution.setStatus(BatchStatus.COMPLETED);
        StepExecution step = new StepExecution(1L, "trashCleanupStep", execution);
        step.setReadCount(3);
        step.setWriteCount(3);
        execution.addStepExecution(step);
        return execution;
    }

    @Test
    void testRunJob_ReturnsSummary() throws Exception {
        when(trashCleanupJob.getName()).thenReturn("trashCleanupJob");
        when(jobOperator.start(eq(trashCleanupJob), any(JobParameters.class))).thenReturn(completedExecution());
        mockMvc.perform(post("/api/batch/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobName").value("trashCleanupJob"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.notesDeleted").value(3));
    }

    @Test
    void testRunJob_PassesRetentionDays() throws Exception {
        when(trashCleanupJob.getName()).thenReturn("trashCleanupJob");
        when(jobOperator.start(eq(trashCleanupJob), any(JobParameters.class))).thenReturn(completedExecution());
        mockMvc.perform(post("/api/batch/run").param("retentionDays", "7")).andExpect(status().isOk());
        verify(jobOperator).start(eq(trashCleanupJob), argThat((JobParameters p) -> Long.valueOf(7L).equals(p.getLong("retentionDays"))));
    }

    @Test
    void testRunJob_NegativeRetention_Returns400() throws Exception {
        mockMvc.perform(post("/api/batch/run").param("retentionDays", "-1")).andExpect(status().isBadRequest());
        verifyNoInteractions(jobOperator);
    }
}

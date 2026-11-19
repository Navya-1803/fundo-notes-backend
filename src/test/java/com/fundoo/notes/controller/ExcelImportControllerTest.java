package com.fundoo.notes.controller;

import com.fundoo.notes.support.ExcelTestFiles;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExcelImportController.class)
class ExcelImportControllerTest {

    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobOperator jobOperator;

    @MockitoBean(name = "excelNoteImportJob")
    private Job excelNoteImportJob;

    private JobExecution execution(BatchStatus status) {
        JobExecution execution = new JobExecution(1L, new JobInstance(1L, "excelNoteImportJob"), new JobParameters());
        execution.setStatus(status);
        StepExecution step = new StepExecution(1L, "excelNoteImportStep", execution);
        step.setReadCount(3);
        step.setWriteCount(2);
        step.setFilterCount(1);
        execution.addStepExecution(step);
        return execution;
    }

    @Test
    void testImport_ReturnsSummary() throws Exception {
        when(excelNoteImportJob.getName()).thenReturn("excelNoteImportJob");
        when(jobOperator.start(eq(excelNoteImportJob), any(JobParameters.class))).thenReturn(execution(BatchStatus.COMPLETED));
        MockMultipartFile file = new MockMultipartFile("file", "notes.xlsx", XLSX,
                ExcelTestFiles.workbook(new String[]{"T", "D", "navya@example.com", "no", "no"}));

        mockMvc.perform(multipart("/api/batch/import/notes").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.notesImported").value(2))
                .andExpect(jsonPath("$.rowsSkipped").value(1));
    }

    @Test
    void testImport_FailedJob_Returns400() throws Exception {
        when(excelNoteImportJob.getName()).thenReturn("excelNoteImportJob");
        when(jobOperator.start(eq(excelNoteImportJob), any(JobParameters.class))).thenReturn(execution(BatchStatus.FAILED));
        MockMultipartFile file = new MockMultipartFile("file", "notes.xlsx", XLSX, new byte[]{1, 2, 3});
        mockMvc.perform(multipart("/api/batch/import/notes").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    void testImport_WrongExtension_Returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "notes.csv", "text/csv", "a,b".getBytes());
        mockMvc.perform(multipart("/api/batch/import/notes").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only .xlsx files are supported"));
        verifyNoInteractions(jobOperator);
    }

    @Test
    void testImport_EmptyFile_Returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "notes.xlsx", XLSX, new byte[0]);
        mockMvc.perform(multipart("/api/batch/import/notes").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Uploaded file is empty"));
    }

    @Test
    void testImport_MissingFile_Returns400() throws Exception {
        mockMvc.perform(multipart("/api/batch/import/notes"))
                .andExpect(status().isBadRequest());
    }
}

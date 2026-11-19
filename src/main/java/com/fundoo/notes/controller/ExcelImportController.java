package com.fundoo.notes.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Upload endpoint for importing notes from an Excel (.xlsx) file.
 *
 * @author Navya Raj
 */
@RestController
@RequestMapping("/api/batch")
public class ExcelImportController {

    private static final Logger log = LoggerFactory.getLogger(ExcelImportController.class);

    private final JobOperator jobOperator;
    private final Job excelNoteImportJob;

    public ExcelImportController(JobOperator jobOperator, @Qualifier("excelNoteImportJob") Job excelNoteImportJob) {
        this.jobOperator = jobOperator;
        this.excelNoteImportJob = excelNoteImportJob;
    }

    @PostMapping(value = "/import/notes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> importNotes(@RequestParam("file") MultipartFile file) throws Exception {
        validateFile(file);
        log.info("Excel import requested for file {}", file.getOriginalFilename());
        // the batch reader works on a file, so the upload is stored in a temporary file first
        Path tempFile = Files.createTempFile("notes-import-", ".xlsx");
        try {
            file.transferTo(tempFile);
            JobExecution execution = jobOperator.start(excelNoteImportJob, new JobParametersBuilder()
                    .addString("filePath", tempFile.toAbsolutePath().toString())
                    .addLong("startedAt", System.currentTimeMillis())
                    .toJobParameters());
            HttpStatus status = execution.getStatus() == BatchStatus.COMPLETED ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(toResponse(execution, file.getOriginalFilename()));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Only .xlsx files are supported");
        }
    }

    private Map<String, Object> toResponse(JobExecution execution, String fileName) {
        long read = execution.getStepExecutions().stream().mapToLong(StepExecution::getReadCount).sum();
        long written = execution.getStepExecutions().stream().mapToLong(StepExecution::getWriteCount).sum();
        long skipped = execution.getStepExecutions().stream().mapToLong(StepExecution::getFilterCount).sum();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("jobName", excelNoteImportJob.getName());
        body.put("fileName", new File(fileName).getName());
        body.put("status", execution.getStatus().toString());
        body.put("rowsRead", read);
        body.put("notesImported", written);
        body.put("rowsSkipped", skipped);
        log.info("Excel import finished with status {}: {} imported, {} skipped", execution.getStatus(), written, skipped);
        return body;
    }
}

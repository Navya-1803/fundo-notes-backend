package com.fundoo.notes.config;

import com.fundoo.notes.dto.request.NoteExcelRow;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.repository.UserRepository;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Batch job importing notes from an uploaded Excel file:
 * read row -> validate row -> convert to Note -> save. Invalid rows are
 * logged and skipped, valid rows are saved in chunks.
 *
 * @author Navya Raj
 */
@Configuration
public class ExcelNoteImportConfig {

    private static final Logger log = LoggerFactory.getLogger(ExcelNoteImportConfig.class);

    public static final String EXCEL_IMPORT_JOB = "excelNoteImportJob";
    private static final int CHUNK_SIZE = 50;
    private static final int TITLE_MAX_LENGTH = 100;

    @Bean
    public Job excelNoteImportJob(JobRepository jobRepository, Step excelNoteImportStep) {
        return new JobBuilder(EXCEL_IMPORT_JOB, jobRepository)
                .start(excelNoteImportStep)
                .build();
    }

    @Bean
    public Step excelNoteImportStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                    ListItemReader<NoteExcelRow> excelNoteReader,
                                    ItemProcessor<NoteExcelRow, Note> excelNoteProcessor,
                                    ItemWriter<Note> excelNoteWriter) {
        return new StepBuilder("excelNoteImportStep", jobRepository)
                .<NoteExcelRow, Note>chunk(CHUNK_SIZE, transactionManager)
                .reader(excelNoteReader)
                .processor(excelNoteProcessor)
                .writer(excelNoteWriter)
                .build();
    }

    // the file path is passed as a job parameter by the upload endpoint
    @Bean
    @StepScope
    public ListItemReader<NoteExcelRow> excelNoteReader(@Value("#{jobParameters['filePath']}") String filePath) throws IOException {
        return new ListItemReader<>(readRows(new File(filePath)));
    }

    @Bean
    public ItemProcessor<NoteExcelRow, Note> excelNoteProcessor(UserRepository userRepository) {
        return row -> {
            if (row.getTitle() == null || row.getTitle().isBlank() || row.getTitle().length() > TITLE_MAX_LENGTH) {
                log.warn("Skipping row {}: title is missing or longer than {} characters", row.getRowNumber(), TITLE_MAX_LENGTH);
                return null;
            }
            if (row.getOwnerEmail() == null || row.getOwnerEmail().isBlank()) {
                log.warn("Skipping row {}: owner email is missing", row.getRowNumber());
                return null;
            }
            Optional<User> owner = userRepository.findByEmail(row.getOwnerEmail().trim().toLowerCase());
            if (owner.isEmpty()) {
                log.warn("Skipping row {}: no user with email {}", row.getRowNumber(), row.getOwnerEmail());
                return null;
            }
            Note note = new Note(row.getTitle().trim(), row.getDescription(), owner.get());
            note.setPinned(row.isPinned() && !row.isArchived());
            note.setArchived(row.isArchived());
            return note;
        };
    }

    @Bean
    public ItemWriter<Note> excelNoteWriter(NoteRepository noteRepository) {
        return chunk -> {
            noteRepository.saveAll(chunk.getItems());
            log.info("Imported {} note(s) from Excel", chunk.size());
        };
    }

    /**
     * Reads every data row of the first sheet, the first row is the header.
     */
    static List<NoteExcelRow> readRows(File file) throws IOException {
        List<NoteExcelRow> rows = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();
        try (Workbook workbook = WorkbookFactory.create(file, null, true)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0 || isEmpty(row, formatter)) {
                    continue;
                }
                rows.add(new NoteExcelRow(row.getRowNum() + 1,
                        text(row, 0, formatter), text(row, 1, formatter), text(row, 2, formatter),
                        flag(row, 3, formatter), flag(row, 4, formatter)));
            }
        }
        log.info("Read {} data row(s) from {}", rows.size(), file.getName());
        return rows;
    }

    private static String text(Row row, int column, DataFormatter formatter) {
        Cell cell = row.getCell(column);
        String value = cell == null ? "" : formatter.formatCellValue(cell).trim();
        return value.isEmpty() ? null : value;
    }

    // TRUE, true, yes, y and 1 all count as true
    private static boolean flag(Row row, int column, DataFormatter formatter) {
        String value = text(row, column, formatter);
        return value != null && List.of("true", "yes", "y", "1").contains(value.toLowerCase());
    }

    private static boolean isEmpty(Row row, DataFormatter formatter) {
        for (int i = 0; i < 5; i++) {
            if (text(row, i, formatter) != null) {
                return false;
            }
        }
        return true;
    }
}

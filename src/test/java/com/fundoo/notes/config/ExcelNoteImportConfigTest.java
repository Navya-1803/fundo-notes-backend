package com.fundoo.notes.config;

import com.fundoo.notes.dto.request.NoteExcelRow;
import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.repository.UserRepository;
import com.fundoo.notes.support.ExcelTestFiles;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ExcelNoteImportConfigTest {

    @TempDir
    Path tempDir;

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    @Qualifier("excelNoteImportJob")
    private Job excelNoteImportJob;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        noteRepository.deleteAll();
        userRepository.deleteAll();
    }

    private Path excel(String[]... rows) throws Exception {
        Path file = tempDir.resolve("notes.xlsx");
        Files.write(file, ExcelTestFiles.workbook(rows));
        return file;
    }

    @Test
    void testReadRows_ParsesColumnsAndSkipsHeader() throws Exception {
        Path file = excel(new String[]{"Groceries", "Milk", "navya@example.com", "yes", "no"},
                new String[]{null, null, null, null, null},
                new String[]{"Ideas", null, "navya@example.com", "0", "TRUE"});
        List<NoteExcelRow> rows = ExcelNoteImportConfig.readRows(file.toFile());
        assertEquals(2, rows.size());
        assertEquals("Groceries", rows.get(0).getTitle());
        assertTrue(rows.get(0).isPinned());
        assertEquals(2, rows.get(0).getRowNumber());
        assertTrue(rows.get(1).isArchived());
        assertNull(rows.get(1).getDescription());
    }

    @Test
    void testImportJob_SavesValidRowsAndSkipsInvalidOnes() throws Exception {
        userRepository.save(new User("Navya", "Raj", "navya@example.com", "hash"));
        Path file = excel(
                new String[]{"Groceries", "Milk", "NAVYA@example.com", "yes", "no"},
                new String[]{"", "no title", "navya@example.com", "no", "no"},
                new String[]{"Unknown owner", "", "nobody@example.com", "no", "no"},
                new String[]{"Archived idea", "", "navya@example.com", "yes", "yes"});

        JobExecution execution = jobOperator.start(excelNoteImportJob, new JobParametersBuilder()
                .addString("filePath", file.toString())
                .addLong("startedAt", System.currentTimeMillis())
                .toJobParameters());

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        List<Note> notes = noteRepository.findAll();
        assertEquals(2, notes.size());
        Note archived = notes.stream().filter(n -> n.getTitle().equals("Archived idea")).findFirst().orElseThrow();
        // an archived note is never pinned
        assertTrue(archived.isArchived());
        assertFalse(archived.isPinned());
    }

    @Test
    void testImportJob_InvalidFile_Fails() throws Exception {
        Path file = tempDir.resolve("broken.xlsx");
        Files.writeString(file, "this is not an excel file");
        JobExecution execution = jobOperator.start(excelNoteImportJob, new JobParametersBuilder()
                .addString("filePath", file.toString())
                .addLong("startedAt", System.currentTimeMillis())
                .toJobParameters());
        assertEquals(BatchStatus.FAILED, execution.getStatus());
    }
}

package com.fundoo.notes.config;

import com.fundoo.notes.entity.Note;
import com.fundoo.notes.entity.User;
import com.fundoo.notes.repository.NoteRepository;
import com.fundoo.notes.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BatchConfigTest {

    @Autowired
    private JobOperator jobOperator;

    @Autowired
    @Qualifier("trashCleanupJob")
    private Job trashCleanupJob;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        noteRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testTrashCleanupJob_DeletesOnlyOldTrashedNotes() throws Exception {
        User user = userRepository.save(new User("Navya", "Raj", "batch@example.com", "hash"));
        Note trashed = new Note("Old", "", user);
        trashed.setTrashed(true);
        noteRepository.save(trashed);
        noteRepository.save(new Note("Keep me", "", user));

        // retention of 0 days: everything already in trash is old enough
        JobExecution execution = jobOperator.start(trashCleanupJob, new JobParametersBuilder()
                .addLong("startedAt", System.currentTimeMillis())
                .addLong("retentionDays", 0L)
                .toJobParameters());

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(1, noteRepository.count());
        assertEquals("Keep me", noteRepository.findAll().get(0).getTitle());
    }

    @Test
    void testTrashCleanupJob_RecentTrashIsKept() throws Exception {
        User user = userRepository.save(new User("Navya", "Raj", "batch2@example.com", "hash"));
        Note trashed = new Note("Recent", "", user);
        trashed.setTrashed(true);
        noteRepository.save(trashed);

        JobExecution execution = jobOperator.start(trashCleanupJob, new JobParametersBuilder()
                .addLong("startedAt", System.currentTimeMillis()).toJobParameters());

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(1, noteRepository.count());
    }

    @Test
    void testProcessor_SkipsRestoredNotes() throws Exception {
        BatchConfig config = new BatchConfig();
        Note restored = new Note("Restored", "", null);
        assertNull(config.trashedNoteProcessor().process(restored));
        restored.setTrashed(true);
        assertSame(restored, config.trashedNoteProcessor().process(restored));
    }
}

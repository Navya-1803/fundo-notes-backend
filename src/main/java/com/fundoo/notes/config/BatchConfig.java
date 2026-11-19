package com.fundoo.notes.config;

import com.fundoo.notes.entity.Note;
import com.fundoo.notes.repository.NoteRepository;

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

import java.time.LocalDateTime;

/**
 * Batch job that permanently deletes notes which stayed in trash longer than
 * the retention period. Reader -> processor -> writer, in chunks of 20.
 *
 * @author Navya Raj
 */
@Configuration
public class BatchConfig {

    private static final Logger log = LoggerFactory.getLogger(BatchConfig.class);

    public static final String TRASH_CLEANUP_JOB = "trashCleanupJob";
    private static final int CHUNK_SIZE = 20;

    @Bean
    public Job trashCleanupJob(JobRepository jobRepository, Step trashCleanupStep) {
        return new JobBuilder(TRASH_CLEANUP_JOB, jobRepository)
                .start(trashCleanupStep)
                .build();
    }

    @Bean
    public Step trashCleanupStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                 ListItemReader<Note> trashedNoteReader, ItemProcessor<Note, Note> trashedNoteProcessor,
                                 ItemWriter<Note> trashedNoteWriter) {
        return new StepBuilder("trashCleanupStep", jobRepository)
                .<Note, Note>chunk(CHUNK_SIZE, transactionManager)
                .reader(trashedNoteReader)
                .processor(trashedNoteProcessor)
                .writer(trashedNoteWriter)
                .build();
    }

    // step scoped so the notes are loaded when the job runs, with the retention days of that run
    @Bean
    @StepScope
    public ListItemReader<Note> trashedNoteReader(NoteRepository noteRepository,
                                                  @Value("#{jobParameters['retentionDays'] ?: ${app.batch.trash-retention-days}}") Long retentionDays) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        log.info("Reading notes trashed before {}", cutoff);
        return new ListItemReader<>(noteRepository.findByTrashedTrueAndUpdatedAtBefore(cutoff));
    }

    @Bean
    public ItemProcessor<Note, Note> trashedNoteProcessor() {
        return note -> {
            // a note restored meanwhile is skipped by returning null
            if (!note.isTrashed()) {
                return null;
            }
            log.debug("Note id: {} will be deleted permanently", note.getId());
            return note;
        };
    }

    @Bean
    public ItemWriter<Note> trashedNoteWriter(NoteRepository noteRepository) {
        return chunk -> {
            noteRepository.deleteAll(chunk.getItems());
            log.info("Permanently deleted {} trashed note(s)", chunk.size());
        };
    }
}

package com.fundoo.notes.repository;

import com.fundoo.notes.entity.Note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Data access for notes. Every query is scoped to a user so one user can
 * never read another user's notes.
 *
 * @author Navya Raj
 */
@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByUserId(Long userId);

    // notes shown on the main dashboard
    List<Note> findByUserIdAndArchivedFalseAndTrashedFalse(Long userId);

    List<Note> findByUserIdAndArchivedTrueAndTrashedFalse(Long userId);

    List<Note> findByUserIdAndTrashedTrue(Long userId);

    List<Note> findByUserIdAndTitleContainingIgnoreCase(Long userId, String keyword);

    Optional<Note> findByIdAndUserId(Long id, Long userId);

    // trashed notes not touched since the given time, used by the cleanup batch job
    List<Note> findByTrashedTrueAndUpdatedAtBefore(LocalDateTime cutoff);
}

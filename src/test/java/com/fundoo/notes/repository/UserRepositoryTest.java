package com.fundoo.notes.repository;

import com.fundoo.notes.entity.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSave_GeneratesIdAndTimestamps() {
        User saved = userRepository.save(new User("Navya", "Raj", "navya@example.com", "secret"));
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void testFindByEmail_Found() {
        userRepository.save(new User("Navya", "Raj", "navya@example.com", "secret"));
        assertTrue(userRepository.findByEmail("navya@example.com").isPresent());
    }

    @Test
    void testFindByEmail_NotFound() {
        assertTrue(userRepository.findByEmail("missing@example.com").isEmpty());
    }

    @Test
    void testExistsByEmail() {
        userRepository.save(new User("Navya", "Raj", "navya@example.com", "secret"));
        assertTrue(userRepository.existsByEmail("navya@example.com"));
        assertFalse(userRepository.existsByEmail("other@example.com"));
    }

    @Test
    void testDuplicateEmail_Rejected() {
        userRepository.saveAndFlush(new User("Navya", "Raj", "navya@example.com", "secret"));
        assertThrows(DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(new User("Other", "User", "navya@example.com", "pwd")));
    }
}

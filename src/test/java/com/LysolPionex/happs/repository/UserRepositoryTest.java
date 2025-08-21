package com.LysolPionex.happs.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import com.LysolPionex.happs.User;

@DataJpaTest
public class UserRepositoryTest {

    String email1 = "email@domain.com";
    String email2 = "email2@domain.com";
    
    String username1 = "testuser";
    String username2 = "testuser2";
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void testSaveAndFindByEmail() {
        
        User user = new User();
        user.setEmail(email1);
        user.setUsername(username1);
        userRepository.save(user);
        
        Optional<User> foundUser = userRepository.findByEmail(email1);
        assertTrue(foundUser.isPresent());
        assertEquals(email1, foundUser.get().getEmail());
        assertEquals(username1, foundUser.get().getUsername());
    }
    
    @Test
    void testEmailUnique() {
        User user1 = new User();
        user1.setEmail(email1);
        user1.setUsername(username1);
        userRepository.save(user1);
        
        User userDuplicateEmail = new User();
        userDuplicateEmail.setEmail(email1);
        userDuplicateEmail.setUsername(username2);
        
        
        assertThrows(DataIntegrityViolationException.class, () ->{
            userRepository.save(userDuplicateEmail);
            userRepository.flush(); // Force flush to execute it immediately ensuring violation is triggered
        });
    }
    
    
    @Test
    void testSaveAndFindByUsername() {
        
        User user = new User();
        user.setEmail(email1);
        user.setUsername(username1);
        userRepository.save(user);
        
        Optional<User> foundUser = userRepository.findByUsername(username1);
        assertTrue(foundUser.isPresent());
        assertEquals(email1, foundUser.get().getEmail());
        assertEquals(username1, foundUser.get().getUsername());
    }
    
    @Test
    void testUsernameUnique() {
        User user1 = new User();
        user1.setEmail(email1);
        user1.setUsername(username1);
        userRepository.save(user1);
        
        User userDuplicateUsername = new User();
        userDuplicateUsername.setEmail(email2);
        userDuplicateUsername.setUsername(username1);
        
        
        assertThrows(DataIntegrityViolationException.class, () ->{
            userRepository.save(userDuplicateUsername);
            userRepository.flush(); // Force flush to execute it immediately ensuring violation is triggered
        });
    }    
}

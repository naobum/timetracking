package com.kfu.timetracking.services;

import com.kfu.timetracking.models.Role;
import com.kfu.timetracking.models.User;
import com.kfu.timetracking.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("STUDENT");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(Set.of(testRole));
    }

    @Test
    void testLoadUserByUsernameSuccess() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testLoadUserByUsernameNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, 
                () -> customUserDetailsService.loadUserByUsername("nonexistent"));
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    @Test
    void testLoadUserByUsernameWithMultipleRoles() {
        Role role2 = new Role();
        role2.setId(2L);
        role2.setName("TEACHER");

        testUser.setRoles(Set.of(testRole, role2));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(2, result.getAuthorities().size());
    }

    @Test
    void testLoadUserByUsernameWithEmptyRoles() {
        testUser.setRoles(Set.of());

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(0, result.getAuthorities().size());
    }

    @Test
    void testLoadUserByUsernameCaseSensitive() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // First call should return empty
        assertThrows(UsernameNotFoundException.class, 
                () -> customUserDetailsService.loadUserByUsername("testUser"));

        // Second call should return the user
        UserDetails result = customUserDetailsService.loadUserByUsername("testuser");
        assertNotNull(result);
    }

    @Test
    void testLoadUserByUsernameWithSpecialCharacters() {
        User specialUser = new User();
        specialUser.setId(2L);
        specialUser.setUsername("user@example.com");
        specialUser.setPassword("encodedPassword");
        specialUser.setRoles(Set.of(testRole));

        when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.of(specialUser));

        UserDetails result = customUserDetailsService.loadUserByUsername("user@example.com");

        assertNotNull(result);
        assertEquals("user@example.com", result.getUsername());
    }

    @Test
    void testLoadUserByUsernameWithLongUsername() {
        String longUsername = "a".repeat(255);
        User longUser = new User();
        longUser.setId(3L);
        longUser.setUsername(longUsername);
        longUser.setPassword("encodedPassword");
        longUser.setRoles(Set.of(testRole));

        when(userRepository.findByUsername(longUsername)).thenReturn(Optional.of(longUser));

        UserDetails result = customUserDetailsService.loadUserByUsername(longUsername);

        assertNotNull(result);
        assertEquals(longUsername, result.getUsername());
    }

    @Test
    void testLoadUserByUsernameExceptionMessage() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("testuser"));

        assertTrue(exception.getMessage().contains("testuser"));
        assertTrue(exception.getMessage().contains("Пользователь не найден"));
    }

    @Test
    void testLoadUserByUsernameCalledOncePerRequest() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        customUserDetailsService.loadUserByUsername("testuser");

        verify(userRepository, times(1)).findByUsername("testuser");
    }
}

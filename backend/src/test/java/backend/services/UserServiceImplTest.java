package backend.services;

import backend.exceptions.AuthenticationException;
import backend.exceptions.ConflictException;
import backend.exceptions.ResourceNotFoundException;
import backend.models.User;
import backend.repositories.UserRepository;
import backend.services.impl.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
    }

    // Test for login method
    @Test
    void login_validCredentials_returnsUser() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        User result = userService.login("test@example.com", "password");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password", "encodedPassword");
    }

    @Test
    void login_userNotFound_throwsAuthenticationException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            userService.login("invalid@example.com", "password");
        });

        assertEquals("User doesn't exist", exception.getMessage());
    }

    @Test
    void login_invalidPassword_throwsAuthenticationException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            userService.login("test@example.com", "wrongpassword");
        });

        assertEquals("Invalid username or password", exception.getMessage());
    }

    // Test for signup method
    @Test
    void signup_userAlreadyExists_throwsConflictException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        ConflictException exception = assertThrows(ConflictException.class, () -> {
            userService.signup("test@example.com", "password", "USER");
        });

        assertEquals("User already exists with email: test@example.com", exception.getMessage());
    }

    @Test
    void signup_successful_returnsTrue() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        boolean result = userService.signup("new@example.com", "password", "USER");

        assertTrue(result);
        verify(userRepository).save(any(User.class));
    }

    // Test for changePassword method
    @Test
    void changePassword_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.changePassword(99L, "newpassword");
        });

        assertEquals("User not found with id: 99", exception.getMessage());
    }

    @Test
    void changePassword_successful_returnsTrue() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        boolean result = userService.changePassword(1L, "newpassword");

        assertTrue(result);
        verify(userRepository).save(any(User.class));
    }

    // Test for delete method
    @Test
    void delete_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.delete(99L);
        });

        assertEquals("User not found with id: 99", exception.getMessage());
    }

    @Test
    void delete_successful_returnsTrue() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        boolean result = userService.delete(1L);

        assertTrue(result);
        verify(userRepository).delete(any(User.class));
    }

    // Test for getUserByID method
    @Test
    void getUserByID_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserByID(99L);
        });

        assertEquals("User not found with id: 99", exception.getMessage());
    }

    @Test
    void getUserByID_successful_returnsUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        User result = userService.getUserByID(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
    }
}

package backend.controllers;

import backend.dtos.*;
import backend.exceptions.AuthenticationException;
import backend.interfaces.UserService;
import backend.models.User;
import backend.services.AuthServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthServiceImpl authService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_successful_returnsUserResponseDto() {
        LoginRequestDto loginRequest = new LoginRequestDto("test@example.com", "password");
        User user = new User(1L, "test@example.com", "hashedPass", "USER");

        when(userService.login(loginRequest.getEmail(), loginRequest.getPassword())).thenReturn(user);
        when(authService.generateToken(1, "USER")).thenReturn("mocked-jwt-token");

        ResponseEntity<?> response = userController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof UserResponseDto);
        UserResponseDto dto = (UserResponseDto) response.getBody();
        assertEquals("mocked-jwt-token", dto.getToken());
        assertEquals("test@example.com", dto.getEmail());
    }

    @Test
    void login_invalidCredentials_returnsUnauthorized() {
        LoginRequestDto loginRequest = new LoginRequestDto("wrong@example.com", "wrongpass");

        when(userService.login(loginRequest.getEmail(), loginRequest.getPassword()))
                .thenThrow(new AuthenticationException("Invalid credentials"));

        ResponseEntity<?> response = userController.login(loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof MessageResponseDto);
        assertEquals("Invalid credentials", ((MessageResponseDto) response.getBody()).getMessage());
    }

    @Test
    void signup_success_returnsCreatedStatus() {
        SignupRequestDto signupRequest = new SignupRequestDto("new@example.com", "password", "USER");

        ResponseEntity<?> response = userController.signup(signupRequest);

        verify(userService).signup("new@example.com", "password", "USER");
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("New user created", ((MessageResponseDto) response.getBody()).getMessage());
    }

    @Test
    void deleteUser_success_returnsOkStatus() {
        long userId = 1L;

        ResponseEntity<?> response = userController.deleteUser(userId);

        verify(userService).delete(userId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User deleted successfully.", ((MessageResponseDto) response.getBody()).getMessage());
    }

    @Test
    void changePassword_success_returnsOkStatus() {
        long userId = 1L;
        PasswordChangeRequestDto requestDto = new PasswordChangeRequestDto("newPassword");

        ResponseEntity<?> response = userController.changePassword(userId, requestDto);

        verify(userService).changePassword(userId, "newPassword");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password changed successfully.", ((MessageResponseDto) response.getBody()).getMessage());
    }

    @Test
    void signup_userAlreadyExists_returnsBadRequest() {
        SignupRequestDto signupRequest = new SignupRequestDto("duplicate@example.com", "password", "USER");

        doThrow(new RuntimeException("User already exists")).when(userService)
                .signup(anyString(), anyString(), anyString());

        ResponseEntity<?> response = userController.signup(signupRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("User already exists", ((MessageResponseDto) response.getBody()).getMessage());
    }

    @Test
    void changePassword_userNotFound_returnsNotFound() {
        long userId = 99L;
        PasswordChangeRequestDto requestDto = new PasswordChangeRequestDto("newPassword");

        doThrow(new RuntimeException("User not found")).when(userService)
                .changePassword(userId, "newPassword");

        ResponseEntity<?> response = userController.changePassword(userId, requestDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", ((MessageResponseDto) response.getBody()).getMessage());
    }

    @Test
    void deleteUser_invalidId_returnsNotFound() {
        long invalidId = 123L;

        doThrow(new RuntimeException("User not found")).when(userService).delete(invalidId);

        ResponseEntity<?> response = userController.deleteUser(invalidId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", ((MessageResponseDto) response.getBody()).getMessage());
    }
}

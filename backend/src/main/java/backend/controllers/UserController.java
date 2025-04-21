package backend.controllers;

// Import java classes
import backend.interfaces.UserService;
import backend.services.AuthServiceImpl;
import backend.models.User;
import backend.dtos.MessageResponseDto;
import backend.dtos.LoginRequestDto;
import backend.dtos.SignupRequestDto;
import backend.dtos.UserResponseDto;
import backend.exceptions.AuthenticationException;
import backend.dtos.PasswordChangeRequestDto;

// Spring imports
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Other imports
import jakarta.validation.Valid;

// Controller listening for /user routes
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final AuthServiceImpl authService;

    // Constructor to inject dependencies
    public UserController(UserService userService, AuthServiceImpl authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * Handles user login. It accepts the email and password, validates them,
     * and returns a JWT token along with user details on success.
     * 
     * @param request LoginRequest containing email and password.
     * @return ResponseEntity containing a JWT token and user information, or a bad
     *         request message on failure.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        try {
            User user = userService.login(request.getEmail(), request.getPassword());
            String token = authService.generateToken(user.getId().intValue(), user.getUsertype());

            return ResponseEntity.ok(new UserResponseDto(token, user.getEmail(), user.getUsertype(), user.getId()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponseDto(e.getMessage()));
        }
    }

    /**
     * Handles user signup. It takes the user's email, password, and usertype,
     * creates a new user, and returns a success or failure message.
     * 
     * @param request SignupRequest containing email, password, and usertype.
     * @return ResponseEntity with a message indicating success or failure.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDto request) {
        try {
            userService.signup(request.getEmail(), request.getPassword(), request.getUsertype());
            return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponseDto("New user created"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponseDto(e.getMessage()));
        }
    }

    /**
     * Deletes a user by their ID.
     * 
     * @param id The ID of the user to be deleted.
     * @return ResponseEntity indicating success or failure of the operation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable long id) {
        try {
            userService.delete(id);
            return ResponseEntity.ok(new MessageResponseDto("User deleted successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDto(e.getMessage()));
        }
    }

    /**
     * Changes the password of a user by their ID.
     * 
     * @param id      The ID of the user whose password is to be changed.
     * @param request PasswordChangeRequest containing the new password.
     * @return ResponseEntity indicating success or failure of the password change.
     */
    @PutMapping("/change-password/{id}")
    public ResponseEntity<?> changePassword(@PathVariable long id, @RequestBody PasswordChangeRequestDto request) {
        try {
            userService.changePassword(id, request.getNewPassword());
            return ResponseEntity.ok(new MessageResponseDto("Password changed successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDto(e.getMessage()));
        }
    }

    /**
     * A simple test endpoint to return a greeting message for authorized users.
     * 
     * @return A greeting message.
     */
    @GetMapping("/hello")
    public String Hello() {
        return "hello authorized user!";
    }
}

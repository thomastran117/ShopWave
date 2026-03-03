package backend.services.impl;

import java.util.Optional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import backend.exceptions.http.ConflictException;
import backend.exceptions.http.ResourceNotFoundException;
import backend.exceptions.http.UnauthorizedException;
import backend.models.User;
import backend.repositories.UserRepository;
import backend.services.intf.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User login(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            throw new UnauthorizedException("User doesn't exist");
        }

        if (!passwordEncoder.matches(password, user.get().getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        return user.get();
    }

    @Override
    public boolean signup(String email, String password, String usertype) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("User already exists with email: " + email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setUsertype("USER");
        user.setProvider("LOCAL");

        userRepository.save(user);
        return true;
    }

    @Override
    public User getUserByID(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public long getID(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email))
                .getId();
    }

    @Override
    public boolean changePassword(long id, String password) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return true;
    }

    @Override
    public boolean delete(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
        return true;
    }

    @Override
    public User loginOrSignupGoogle(String email) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            user.setPassword(null);
            user.setUsertype("USER");
            user.setProvider("GOOGLE");
            return userRepository.save(user);
        });
    }
}
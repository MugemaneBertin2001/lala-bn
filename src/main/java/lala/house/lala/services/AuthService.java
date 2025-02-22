package lala.house.lala.services;

import org.springframework.stereotype.Service;
import lala.house.lala.entities.User;
import lala.house.lala.enums.UserRole;
import lala.house.lala.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import lala.house.lala.dto.AuthResponse;
import java.util.Optional;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    public User getCurrentUser(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUserRole(String email, UserRole newRole) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(newRole);
        return userRepository.save(user);
    }

    public User getUserProfile(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public AuthResponse handleGoogleLogin(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");
        String pictureUrl = oauth2User.getAttribute("picture");
        String googleId = oauth2User.getAttribute("sub");

        // Check if user exists
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            // Existing user - generate token and return normal response
            User user = existingUser.get();
            // Update user info if needed
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPictureUrl(pictureUrl);
            User savedUser = userRepository.save(user);

            return new AuthResponse(
                    jwtService.generateToken(savedUser),
                    "Login successful",
                    savedUser);
        } else {
            // New user - return user info without token
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setPictureUrl(pictureUrl);
            newUser.setGoogleId(googleId);
            newUser.setRole(UserRole.RENTER);
            User savedUser = userRepository.save(newUser);
            return new AuthResponse(
                    null, 
                    "Please complete registration",
                    savedUser);
        }
    }

    // Add a new method to update user role based on email
    public AuthResponse completeRegistration(String email, UserRole role) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (!existingUser.isPresent()) {
            throw new RuntimeException("User not found");
        }

        User user = existingUser.get();
        user.setRole(role);
        user.setIsNewUser(false);

        User savedUser = userRepository.save(user);

        return new AuthResponse(
                jwtService.generateToken(savedUser),
                "Registration completed successfully",
                savedUser);
    }

    public Optional<User> getUserById(Long hostId) {
        return userRepository.findById(hostId);
    }
}
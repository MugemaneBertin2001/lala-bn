package lala.house.lala.services;

import org.springframework.stereotype.Service;
import lala.house.lala.entities.User;
import lala.house.lala.enums.UserRole;
import lala.house.lala.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import lala.house.lala.dto.AuthResponse;

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

        // Find or create user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFirstName(firstName);
                    newUser.setLastName(lastName);
                    newUser.setGoogleId(googleId);
                    newUser.setPictureUrl(pictureUrl);
                    newUser.setRole(UserRole.RENTER);
                    return userRepository.save(newUser);
                });

        return new AuthResponse(jwtService.generateToken(user), "Login successful");
    }
}
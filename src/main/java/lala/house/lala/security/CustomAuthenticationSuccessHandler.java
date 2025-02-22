package lala.house.lala.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;
import lala.house.lala.entities.User;
import lala.house.lala.enums.UserRole;
import lala.house.lala.repository.UserRepository;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Extract user info from Google OAuth2
        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        String googleId = oAuth2User.getAttribute("sub");
        String pictureUrl = oAuth2User.getAttribute("picture");

        userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFirstName(firstName);
                    newUser.setLastName(lastName);
                    newUser.setGoogleId(googleId);
                    newUser.setPictureUrl(pictureUrl);
                    newUser.setRole(UserRole.RENTER); // Default role
                    return userRepository.save(newUser);
                });
        response.sendRedirect("/api/auth/success");
    }
}
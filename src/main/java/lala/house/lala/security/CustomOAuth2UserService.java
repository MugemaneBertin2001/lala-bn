package lala.house.lala.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import lala.house.lala.entities.User;
import lala.house.lala.enums.UserRole;
import lala.house.lala.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception ex) {
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        // Extract OAuth2 details
        String email = oauth2User.getAttribute("email");
        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");
        String googleId = oauth2User.getAttribute("sub");
        String pictureUrl = oauth2User.getAttribute("picture");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        // Check if user exists
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Update existing user's information
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPictureUrl(pictureUrl);
            user = userRepository.save(user);
        } else {
            // Register new user
            user = new User();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setGoogleId(googleId);
            user.setPictureUrl(pictureUrl);
            user.setRole(UserRole.RENTER); // Default role
            user = userRepository.save(user);
        }

        // Create user's authorities based on their role
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        // Return custom OAuth2User with additional details
        return new DefaultOAuth2User(
                authorities,
                oauth2User.getAttributes(),
                "sub" // Use "sub" as the primary key in the OAuth2 attributes
        );
    }
}
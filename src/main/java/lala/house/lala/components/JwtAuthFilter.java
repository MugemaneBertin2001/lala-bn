package lala.house.lala.components;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lala.house.lala.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import lala.house.lala.entities.User;
import lala.house.lala.repository.UserRepository;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No JWT token found in the request header");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            String userEmail = jwtService.extractUsername(token);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.debug("Validating JWT token for user: {}", userEmail);
                User user = userRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

                if (jwtService.isTokenValid(token, user)) {
                    logger.debug("JWT token is valid for user: {}", userEmail);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    logger.warn("Invalid JWT token for user: {}", userEmail);
                    SecurityContextHolder.clearContext();
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.warn("JWT token expired: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            logger.error("Error validating JWT token: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
package lala.house.lala.services;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lala.house.lala.entities.User;
import lala.house.lala.exceptions.UnauthorizedException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import io.jsonwebtoken.Claims;
import java.util.function.Function;

import java.util.Date;
import java.nio.charset.StandardCharsets;



@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    private AuthService userService;
    private long expirationTime;
    private org.slf4j.Logger logger = LoggerFactory.getLogger(JwtService.class);

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole().name())
                .claim("userId", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, User user) {
        final String username = extractUsername(token);
        return (username.equals(user.getEmail())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid token");
        }
    }

    public User getUserFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.info("Authorization header is missing");
            throw new UnauthorizedException("Authorization header is missing");
        }
        String token = authHeader.substring(7);
        String userEmail = extractUsername(token);
        User user = userService.getUserProfile(userEmail);
        if (user == null) {
            throw new UnauthorizedException("User not found in database");
        }

        if (!isTokenValid(token, user)) {
            throw new UnauthorizedException("Invalid token");
        }
        return user;
    }
}
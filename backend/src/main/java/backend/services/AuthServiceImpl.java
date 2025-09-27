package backend.services;

import backend.configs.EnvConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

@Component
public class AuthServiceImpl implements Serializable {

    private final EnvConfig env;

    public AuthServiceImpl(EnvConfig env) {
        this.env = env;
    }

    // ------------------- TOKEN EXTRACTION -------------------

    public int getUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token is missing or improperly formatted");
        }
        String tokenWithoutBearer = token.substring(7);
        return Integer.parseInt(getClaimFromToken(tokenWithoutBearer, Claims::getSubject));
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(env.getJwtSecret())
                .parseClaimsJws(token)
                .getBody();
    }

    // ------------------- ACCESS TOKEN -------------------

    public String generateAccessToken(int userId, String role) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + env.getJwtValidity() * 1000)) // e.g. 15min - 1hr
                .signWith(SignatureAlgorithm.HS512, env.getJwtSecret())
                .compact();
    }

    // ------------------- REFRESH TOKEN -------------------

    public String generateRefreshToken(int userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + env.getJwtRefreshValidity() * 1000)) // e.g. days
                .signWith(SignatureAlgorithm.HS512, env.getJwtSecret())
                .compact();
    }

    public boolean validateRefreshToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String rotateRefreshToken(String oldToken) {
        Claims claims = getAllClaimsFromToken(oldToken);

        return Jwts.builder()
                .setSubject(claims.getSubject())
                .setIssuedAt(new Date())
                .setExpiration(claims.getExpiration())
                .signWith(SignatureAlgorithm.HS512, env.getJwtSecret())
                .compact();
    }
    // ------------------- AUTHENTICATION -------------------

    public Authentication getAuthentication(String token) {
        Claims claims = getAllClaimsFromToken(token);
        int userId = Integer.parseInt(claims.getSubject());
        String role = claims.get("role", String.class);

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        if (role != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        return new UsernamePasswordAuthenticationToken(userId, null, authorities);
    }

    public Map<String, Object> generateTokenPair(int userId, String role) {
        String accessToken = generateAccessToken(userId, role);
        String refreshToken = generateRefreshToken(userId);

        Map<String, Object> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        tokens.put("tokenType", "Bearer");
        tokens.put("expiresIn", env.getJwtValidity());

        return tokens;
    }
}

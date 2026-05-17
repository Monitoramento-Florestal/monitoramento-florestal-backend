package com.example.arbor.security;

import com.example.arbor.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    private static final long ACCESS_EXPIRATION = 1000L * 60 * 60;           // 1 hora
    private static final long REFRESH_EXPIRATION = 1000L * 60 * 60 * 24 * 7; // 7 dias

    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";
    private static final String CLAIM_REFRESH_VERSION = "rtv";

    @PostConstruct
    void validateJwtSecret() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                    "Configuracao ausente: defina JWT_SECRET (min. 32 caracteres) ou rode com perfil dev (--spring.profiles.active=dev).");
        }

        if (secretKey.length() < 32) {
            throw new IllegalStateException(
                    "Configuracao invalida: JWT_SECRET muito curta. Use pelo menos 32 caracteres.");
        }
    }



    public String gerarTokenAcesso(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        return gerarTokenAcesso(extraClaims, userDetails);
    }

    public String gerarTokenRefresh(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        return gerarTokenRefresh(extraClaims, userDetails);
    }



    public String gerarToken(UserDetails userDetails) {
        return gerarTokenAcesso(userDetails);
    }

    public String gerarTokenAcesso(Map<String, Object> extraClaims, UserDetails userDetails) {
        extraClaims.put(CLAIM_TYPE, TYPE_ACCESS);
        return buildToken(extraClaims, userDetails, ACCESS_EXPIRATION);
    }

    public String gerarTokenRefresh(Map<String, Object> extraClaims, UserDetails userDetails) {
        extraClaims.put(CLAIM_TYPE, TYPE_REFRESH);
        extraClaims.put(CLAIM_REFRESH_VERSION, resolveRefreshTokenVersion(userDetails));
        return buildToken(extraClaims, userDetails, REFRESH_EXPIRATION);
    }



    public boolean isTokenValido(String token, UserDetails userDetails) {
        final String username = extrairUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpirado(token) && userDetails.isEnabled();
    }

    /** Retorna true se o token contem a claim type=refresh. */
    public boolean isRefreshToken(String token) {
        String type = extrairClaim(token, claims -> claims.get(CLAIM_TYPE, String.class));
        return TYPE_REFRESH.equals(type);
    }

    /** Retorna true se o token contem a claim type=access. */
    public boolean isAccessToken(String token) {
        String type = extrairClaim(token, claims -> claims.get(CLAIM_TYPE, String.class));
        return TYPE_ACCESS.equals(type);
    }

    public boolean isRefreshTokenValido(String token, Usuario usuario) {
        if (usuario == null) {
            return false;
        }

        if (!isTokenValido(token, usuario) || !isRefreshToken(token)) {
            return false;
        }

        Integer tokenVersion = extrairClaim(token, claims -> claims.get(CLAIM_REFRESH_VERSION, Integer.class));
        int tokenVersionValue = tokenVersion == null ? 0 : tokenVersion;
        int expectedVersion = resolveRefreshTokenVersion(usuario);
        return tokenVersionValue == expectedVersion;
    }


    public String extrairUsername(String token) {
        return extrairClaim(token, Claims::getSubject);
    }

    public <T> T extrairClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extrairTodosClaims(token);
        return claimsResolver.apply(claims);
    }



    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    private boolean isTokenExpirado(String token) {
        return extrairExpiracao(token).before(new Date());
    }

    private Date extrairExpiracao(String token) {
        return extrairClaim(token, Claims::getExpiration);
    }

    private Claims extrairTodosClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private int resolveRefreshTokenVersion(UserDetails userDetails) {
        if (userDetails instanceof Usuario usuario) {
            Integer version = usuario.getRefreshTokenVersion();
            return version == null ? 0 : version;
        }

        return 0;
    }
}

package com.example.WebApp.Security;

import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SecureTokenService {

    private static final BytesKeyGenerator DEFAULT_TOKEN_GENERATOR = KeyGenerators.secureRandom(15);

    private static final Charset US_ASCII = StandardCharsets.US_ASCII;

    @Value("${webApp.secure.token.validity}")
    private int tokenValidityInSeconds;

    private SecureTokenRepositories tokenRepositories;

    public SecureToken createSecureToken(){
        String tokenValue = new String(Base64.encodeBase64URLSafe(DEFAULT_TOKEN_GENERATOR.generateKey()), US_ASCII);
        SecureToken secureToken = new SecureToken();
        secureToken.setToken(tokenValue);
        secureToken.setExpireAt(LocalDateTime.now().plusSeconds(getTokenValidityInSeconds()));
        this.saveSecureToken(secureToken);
        return secureToken;
    }

    public void saveSecureToken(SecureToken token) {
        tokenRepositories.save(token);
    }

    public SecureToken findByToken(String token) {
        return tokenRepositories.findByToken(token);
    }

    public void removeToken(SecureToken token) {
        tokenRepositories.delete(token);
    }

    public int getTokenValidityInSeconds() {
        return tokenValidityInSeconds;
    }
}
package com.example.car_manager_api.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtIssuerConfig {

    @Bean
    public JwtEncoder jwtEncoder(@Value("${spring.security.oauth2.resourceserver.jwt.secret-key}") String secret) {
        byte[] bytes = secretBytes(secret);
        return new NimbusJwtEncoder(new ImmutableSecret<>(bytes));
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.secret-key}") String secret) {
        byte[] bytes = secretBytes(secret);
        SecretKeySpec key = new SecretKeySpec(bytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    private static byte[] secretBytes(String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if(bytes.length < 32){
            throw new IllegalStateException("JWT secret deve ter pelo menos 32 bytes para HS256");
        }
        return bytes;
    }
}

package com.example.car_manager_api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtTokenServiceTest {

    @Mock
    JwtEncoder jwtEncoder;

    @InjectMocks
    JwtTokenService jwtTokenService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(jwtTokenService, "issuer", "car-manager-api");
        ReflectionTestUtils.setField(jwtTokenService, "expirationMinutes", 60L);
    }

    @Test
    @DisplayName("Deve gerar token JWT a partir da autenticacao")
    public void createTokenTest() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("token-xyz");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin",
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        String token = jwtTokenService.createToken(auth);

        assertThat(token).isEqualTo("token-xyz");
        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }
}

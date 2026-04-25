package com.example.car_manager_api.api.resource;

import com.example.car_manager_api.api.dto.LoginDTO;
import com.example.car_manager_api.api.dto.TokenDTO;
import com.example.car_manager_api.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    @Value("${app.jwt.expiration-minutes}")
    private long expirationMinutes;

    @PostMapping("/login")
    public TokenDTO login(@RequestBody @Valid LoginDTO dto){
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(dto.getUsername(), dto.getPassword()));
        String token = jwtTokenService.createToken(authentication);
        return TokenDTO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresInSeconds(expirationMinutes * 60)
                .build();
    }
}

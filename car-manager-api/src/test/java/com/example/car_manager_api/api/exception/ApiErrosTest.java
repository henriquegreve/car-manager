package com.example.car_manager_api.api.exception;

import com.example.car_manager_api.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApiErrosTest {

    @Test
    @DisplayName("Deve montar erros a partir do BindingResult")
    public void bindingResultTest() {
        BindingResult br = mock(BindingResult.class);
        ObjectError err = new ObjectError("x", "campo invalido");
        when(br.getAllErrors()).thenReturn(Collections.singletonList(err));

        ApiErros apiErros = new ApiErros(br);

        assertThat(apiErros.getErrors()).containsExactly("campo invalido");
    }

    @Test
    @DisplayName("Deve montar erro a partir de BusinessException")
    public void businessExceptionTest() {
        ApiErros apiErros = new ApiErros(new BusinessException("regra violada"));
        assertThat(apiErros.getErrors()).containsExactly("regra violada");
    }

    @Test
    @DisplayName("Deve montar erro a partir de ResponseStatusException")
    public void responseStatusExceptionTest() {
        ApiErros apiErros = new ApiErros(new ResponseStatusException(HttpStatus.NOT_FOUND, "nao achou"));
        assertThat(apiErros.getErrors()).containsExactly("nao achou");
    }

    @Test
    @DisplayName("Deve montar mensagem padrao para AuthenticationException")
    public void authenticationExceptionTest() {
        AuthenticationException ex = mock(AuthenticationException.class);
        ApiErros apiErros = new ApiErros(ex);
        assertThat(apiErros.getErrors()).containsExactly("Autenticação necessária");
    }

    @Test
    @DisplayName("Deve montar mensagem padrao para AccessDeniedException")
    public void accessDeniedExceptionTest() {
        ApiErros apiErros = new ApiErros(new AccessDeniedException("x"));
        assertThat(apiErros.getErrors()).containsExactly("Permissão insuficiente para este recurso");
    }

    @Test
    @DisplayName("Deve montar erro a partir de string simples")
    public void stringErrorTest() {
        assertThat(new ApiErros("falhou").getErrors()).containsExactly("falhou");
    }

}

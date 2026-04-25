package com.example.car_manager_api.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class RestAuthenticationEntryPointTest {

    @Test
    @DisplayName("Deve responder 401 JSON com ApiErros")
    public void commenceTest() throws Exception {
        ObjectMapper mapper = JsonMapper.builder().build();
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint(mapper);
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(new MockHttpServletRequest(), response, new BadCredentialsException("x"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(mapper.readTree(response.getContentAsByteArray()).path("errors").get(0).asText())
                .isEqualTo("Autenticação necessária");
    }
}

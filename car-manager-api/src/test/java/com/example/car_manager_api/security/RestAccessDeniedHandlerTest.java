package com.example.car_manager_api.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

public class RestAccessDeniedHandlerTest {

    @Test
    @DisplayName("Deve responder 403 JSON com ApiErros")
    public void handleTest() throws Exception {
        ObjectMapper mapper = JsonMapper.builder().build();
        RestAccessDeniedHandler handler = new RestAccessDeniedHandler(mapper);
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(new MockHttpServletRequest(), response, new AccessDeniedException("negado"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(mapper.readTree(response.getContentAsByteArray()).path("errors").get(0).asText())
                .isEqualTo("Permissão insuficiente para este recurso");
    }
}

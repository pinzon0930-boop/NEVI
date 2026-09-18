package com.nevi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ESC-05 / QA-05 — Detecta regresiones en el flujo registro -> login -> recurso protegido.
    @Test
    void registroLoginYAccesoAPerfil_debeFuncionarDeExtremoAExtremo() throws Exception {
        Map<String, String> registerBody = new HashMap<>();
        registerBody.put("email", "test.ci@novi.com");
        registerBody.put("password", "clave123");
        registerBody.put("name", "Usuario CI");
        registerBody.put("role", "student");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("email", "test.ci@novi.com");
        loginBody.put("password", "clave123");

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();

        String token = objectMapper.readTree(response).get("token").asText();

        mockMvc.perform(get("/api/auth/perfil")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test.ci@novi.com"));
    }

    @Test
    void accesoAPerfilSinToken_debeRechazarse() throws Exception {
        // ESC-03 / QA-03 — Sin token, el sistema no debe exponer datos.
        mockMvc.perform(get("/api/auth/perfil"))
                .andExpect(status().isUnauthorized());
    }
}

package com.nevi;

import com.nevi.dto.AuthResponse;
import com.nevi.dto.LoginRequest;
import com.nevi.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ESC-05 — Testeabilidad (dossier/04-escenarios-calidad.md, QA-05).
 * Cubre el flujo exigido por el escenario: registro -> login -> acceso
 * a un recurso protegido. Antes de este archivo, R-04 (09-inventario-riesgos.md)
 * estaba abierto porque no existía ninguna prueba de integración en el backend.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthFlowIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void registroLoginYAccesoAGrupos_debeFuncionarDeExtremoAExtremo() {
        String email = "test-" + UUID.randomUUID() + "@nevi.test";

        // 1. Registro
        RegisterRequest registro = new RegisterRequest();
        registro.setEmail(email);
        registro.setPassword("clave123");
        registro.setName("Usuario de Prueba");
        registro.setRole("student");

        ResponseEntity<AuthResponse> registroResp = rest.postForEntity(
            baseUrl() + "/api/auth/register", registro, AuthResponse.class);
        assertThat(registroResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(registroResp.getBody()).isNotNull();
        assertThat(registroResp.getBody().getToken()).isNotBlank();

        // 2. Login
        LoginRequest login = new LoginRequest();
        login.setEmail(email);
        login.setPassword("clave123");

        ResponseEntity<AuthResponse> loginResp = rest.postForEntity(
            baseUrl() + "/api/auth/login", login, AuthResponse.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResp.getBody()).isNotNull();
        String token = loginResp.getBody().getToken();
        assertThat(token).isNotBlank();

        // 3. Acceso a recurso protegido CON token -> 200
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<String> gruposResp = rest.exchange(
            baseUrl() + "/api/grupos", HttpMethod.GET,
            new HttpEntity<>(headers), String.class);
        assertThat(gruposResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void accesoAGrupos_sinToken_debeRechazarseCon401() {
        // Cubre además ESC-03 — Seguridad: acceso sin token JWT.
        ResponseEntity<String> resp = rest.getForEntity(baseUrl() + "/api/grupos", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}

package com.nevi.service;

import com.nevi.dto.MensajeResponse;
import com.nevi.entity.Grupo;
import com.nevi.entity.Mensaje;
import com.nevi.entity.User;
import com.nevi.repository.GrupoRepository;
import com.nevi.repository.MensajeRepository;
import com.nevi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MensajeService {

    private final MensajeRepository mensajeRepository;
    private final GrupoRepository grupoRepository;
    private final UserRepository userRepository;

    // SimpMessagingTemplate permite enviar mensajes a través de WebSocket a todos los suscriptores.
    private final SimpMessagingTemplate messagingTemplate;

    // Obtiene los últimos 50 mensajes de un grupo, del más antiguo al más reciente.
    @Transactional(readOnly = true)
    public List<MensajeResponse> obtener(UUID grupoId) {
        Grupo grupo = obtenerGrupo(grupoId);

        // Trae los últimos 50 ordenados DESC, luego los invertimos para mostrar cronológicamente.
        List<Mensaje> mensajes = mensajeRepository.findTopByGrupo(grupo, PageRequest.of(0, 50));
        Collections.reverse(mensajes);

        return mensajes.stream().map(MensajeResponse::desde).toList();
    }

    // Persiste un nuevo mensaje y lo difunde a todos los clientes conectados al grupo vía WebSocket.
    @Transactional
    public MensajeResponse enviar(UUID grupoId, String emailUsuario, String contenido) {
        User user  = obtenerUsuario(emailUsuario);
        Grupo grupo = obtenerGrupo(grupoId);

        Mensaje mensaje = Mensaje.builder()
            .grupo(grupo)
            .user(user)
            .content(contenido)
            .build();

        MensajeResponse response = MensajeResponse.desde(mensajeRepository.save(mensaje));

        // Envía el mensaje a todos los clientes suscritos al topic del grupo.
        // El frontend escucha en: /topic/grupo/{grupoId}
        messagingTemplate.convertAndSend("/topic/grupo/" + grupoId, response);

        return response;
    }

    // ── Utilidades privadas ──────────────────────────────────────────────────

    private User obtenerUsuario(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
    }

    private Grupo obtenerGrupo(UUID id) {
        return grupoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado."));
    }
}

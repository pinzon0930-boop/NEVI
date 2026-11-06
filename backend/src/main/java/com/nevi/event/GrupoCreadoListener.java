package com.nevi.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class GrupoCreadoListener {

    @EventListener
    public void recibirEvento(GrupoCreadoEvent evento) {
        System.out.println("[SPIKE-01] EVENTO GRUPOCREADO RECIBIDO");
        System.out.println("[SPIKE-01] ID: " + evento.grupoId());
        System.out.println("[SPIKE-01] Nombre: " + evento.nombre());
        System.out.println("[SPIKE-01] ProfesorId: " + evento.profesorId());
        System.out.println("[SPIKE-01] Fecha: " + evento.fechaCreacion());
    }
}
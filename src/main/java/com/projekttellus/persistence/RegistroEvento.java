package com.projekttellus.persistence;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Registro imutável do evento aplicado a um mundo, inclusive seu instante na simulação. */
public record RegistroEvento(UUID id, UUID mundoId, Instant instante, String descricao,
                             double intensidade, double anos) {
    public RegistroEvento {
        Objects.requireNonNull(id, "Identificador do evento");
        Objects.requireNonNull(mundoId, "Identificador do mundo");
        Objects.requireNonNull(instante, "Instante do evento");
        if (descricao == null || descricao.isBlank())
            throw new IllegalArgumentException("A descrição do evento é obrigatória.");
        if (!Double.isFinite(intensidade) || intensidade < 0 || intensidade > 1)
            throw new IllegalArgumentException("A intensidade deve estar entre zero e um.");
        if (!Double.isFinite(anos) || anos < 0)
            throw new IllegalArgumentException("O tempo do evento deve ser finito e não negativo.");
    }
}

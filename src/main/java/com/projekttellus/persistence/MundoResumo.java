package com.projekttellus.persistence;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Dados necessários para a biblioteca de mundos, sem materializar todo o estado. */
public record MundoResumo(UUID id, String nome, double anos, Instant atualizadoEm) {
    public MundoResumo {
        Objects.requireNonNull(id, "Identificador do mundo");
        Objects.requireNonNull(atualizadoEm, "Instante de atualização");
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("O nome do mundo é obrigatório.");
        if (!Double.isFinite(anos) || anos < 0)
            throw new IllegalArgumentException("O tempo deve ser finito e não negativo.");
    }
}

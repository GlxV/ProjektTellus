package com.projekttellus.patterns.memento;

import com.projekttellus.domain.EstadoPlanetario;
import com.projekttellus.domain.Validacao;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Retrato imutável; nenhuma estrutura mutável é compartilhada com o originador. */
public record TerraMemento(UUID id, UUID mundoId, String rotulo, String nomeMundo,
                          Instant criadoEm, EstadoPlanetario estado) {
    public TerraMemento {
        Objects.requireNonNull(id); Objects.requireNonNull(mundoId);
        rotulo = Validacao.texto("Nome do retrato", rotulo);
        nomeMundo = Validacao.texto("Nome do mundo", nomeMundo);
        Objects.requireNonNull(criadoEm); Objects.requireNonNull(estado);
    }
    @Override public String toString() { return rotulo; }
}

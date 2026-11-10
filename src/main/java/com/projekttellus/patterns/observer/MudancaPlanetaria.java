package com.projekttellus.patterns.observer;
import com.projekttellus.domain.EstadoPlanetario;
import com.projekttellus.simulation.Indicadores;
import java.util.UUID;
import java.util.Objects;
public record MudancaPlanetaria(UUID mundoId, EstadoPlanetario estado, Indicadores indicadores, String descricao) {
    public MudancaPlanetaria {
        Objects.requireNonNull(mundoId); Objects.requireNonNull(estado);
        Objects.requireNonNull(indicadores); Objects.requireNonNull(descricao);
    }
}

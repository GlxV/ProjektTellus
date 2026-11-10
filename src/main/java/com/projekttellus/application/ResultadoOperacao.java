package com.projekttellus.application;
import com.projekttellus.domain.*;
import com.projekttellus.simulation.Indicadores;
import java.util.List;
import java.util.Objects;

public record ResultadoOperacao(Terra terra, EstadoPlanetario antes, EstadoPlanetario depois,
                                List<Passo> passos, Indicadores indicadores, String descricao) {
    public ResultadoOperacao {
        Objects.requireNonNull(terra); Objects.requireNonNull(antes); Objects.requireNonNull(depois);
        passos = List.copyOf(passos); Objects.requireNonNull(indicadores); Objects.requireNonNull(descricao);
    }
}

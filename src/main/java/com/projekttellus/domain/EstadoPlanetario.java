package com.projekttellus.domain;
import java.util.Objects;

/** Grafo de valores profundamente imutável, sem dependências de interface ou banco. */
public record EstadoPlanetario(Atmosfera atmosfera, Clima clima, Biosfera biosfera,
                               Hidrosfera hidrosfera, Civilizacao civilizacao, double anos) {
    public EstadoPlanetario {
        Objects.requireNonNull(atmosfera); Objects.requireNonNull(clima);
        Objects.requireNonNull(biosfera); Objects.requireNonNull(hidrosfera);
        Objects.requireNonNull(civilizacao);
        Validacao.intervalo("Tempo", anos, 0, 1_000_000_000);
    }
}

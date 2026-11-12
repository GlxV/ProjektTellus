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
    /** Percentual seco e não glacial da superfície total, disponível para ocupação. */
    public double superficieContinental() { return Math.max(0, 100 - hidrosfera.agua() - hidrosfera.gelo()); }
    /** Vegetação é uma fração do continente, nunca uma segunda cobertura planetária sobre o oceano. */
    public double superficieVegetada() { return superficieContinental() * biosfera.vegetacao() / 100; }
}

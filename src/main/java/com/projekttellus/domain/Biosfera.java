package com.projekttellus.domain;
/** Percentual vegetado do continente disponível. Sem continente, a área efetiva é zero.
 * Conserva a semântica e os valores dos saves V001; não representa vida marinha. */
public record Biosfera(double vegetacao) {
    public Biosfera { Validacao.intervalo("Vegetação", vegetacao, 0, 100); }
}

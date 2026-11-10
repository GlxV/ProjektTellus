package com.projekttellus.domain;
public record Biosfera(double vegetacao) {
    public Biosfera { Validacao.intervalo("Vegetação", vegetacao, 0, 100); }
}

package com.projekttellus.domain;

public record Clima(double temperatura, double aerossol, double radiacao) {
    public Clima {
        Validacao.intervalo("Temperatura", temperatura, -150, 200);
        Validacao.intervalo("Aerossóis", aerossol, 0, 100);
        Validacao.intervalo("Radiação", radiacao, 0, 100);
    }
}

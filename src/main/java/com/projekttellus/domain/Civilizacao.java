package com.projekttellus.domain;
public record Civilizacao(long populacao) {
    public Civilizacao {
        if (populacao < 0 || populacao > 100_000_000_000L)
            throw new IllegalArgumentException("População deve estar entre zero e 100 bilhões.");
    }
}

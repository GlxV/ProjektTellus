package com.projekttellus.domain;
/** Água líquida e gelo são coberturas distintas da superfície total. */
public record Hidrosfera(double agua, double gelo) {
    public Hidrosfera {
        Validacao.intervalo("Água", agua, 0, 100);
        Validacao.intervalo("Gelo", gelo, 0, 100);
        if (agua + gelo > 100 + 1e-6)
            throw new IllegalArgumentException("Água líquida e gelo juntos não podem exceder 100%.");
        if (agua + gelo > 100) agua = Math.max(0, 100 - gelo);
    }
}

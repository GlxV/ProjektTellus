package com.projekttellus.domain;

/** Frações percentuais de quatro gases; pressão total em atmosferas. */
public record Atmosfera(double oxigenio, double nitrogenio, double co2, double helio, double pressao) {
    public Atmosfera {
        Validacao.intervalo("Oxigênio", oxigenio, 0, 100);
        Validacao.intervalo("Nitrogênio", nitrogenio, 0, 100);
        Validacao.intervalo("CO₂", co2, 0, 100);
        Validacao.intervalo("Hélio", helio, 0, 100);
        Validacao.intervalo("Pressão", pressao, 0, 20);
        double total = oxigenio + nitrogenio + co2 + helio;
        if (Math.abs(total - 100) > 1e-6)
            throw new IllegalArgumentException("A soma dos quatro gases deve ser 100%.");
        // A tolerância aceita ruído numérico, nunca uma reserva extra de gás.
        if (Math.abs(total - 100) > 1e-12) {
            double[] gases = {oxigenio, nitrogenio, co2, helio};
            int maior = 0;
            for (int i = 1; i < gases.length; i++) if (gases[i] > gases[maior]) maior = i;
            double outros = 0;
            for (int i = 0; i < gases.length; i++) if (i != maior) {
                gases[i] *= 100 / total; outros += gases[i];
            }
            // O maior gás é o resíduo: uma mistura quase pura nunca vira 100 + um ulp.
            gases[maior] = Math.clamp(100 - outros, 0, 100);
            oxigenio = gases[0]; nitrogenio = gases[1]; co2 = gases[2]; helio = gases[3];
        }
    }
    public double pressaoParcialOxigenio() { return pressao * oxigenio / 100; }
    public double pressaoParcialCo2() { return pressao * co2 / 100; }

    /** Converte pequenas parcelas de N₂ em CO₂ ou o inverso, sem criar atmosfera. */
    public Atmosfera transferirCarbono(double delta) {
        Validacao.intervalo("Variação de CO₂", delta, -100, 100);
        double troca = Math.clamp(delta, -co2, nitrogenio);
        return new Atmosfera(oxigenio, nitrogenio - troca, co2 + troca, helio, pressao);
    }
}

package com.projekttellus.simulation;

import com.projekttellus.domain.Validacao;

/** Adequação multiplicativa [0,1]. Não é probabilidade nem limite médico. */
public record FatorAmbiental(String nome, double valor, String unidade, double adequacao, String explicacao) {
    public FatorAmbiental {
        nome = Validacao.texto("Fator", nome);
        if (!Double.isFinite(valor)) throw new IllegalArgumentException("Valor do fator deve ser finito.");
        java.util.Objects.requireNonNull(unidade);
        java.util.Objects.requireNonNull(explicacao);
        Validacao.intervalo("Adequação", adequacao, 0, 1);
    }
    public String classificacao() {
        return adequacao >= .8 ? "favorável" : adequacao >= .4 ? "limitante" : "crítico";
    }
}

package com.projekttellus.application;
import java.util.Objects;
/** Fato concluído de uma operação real, pronto para sua projeção didática. */
public record Passo(String titulo, String detalhe, String padrao) {
    public Passo {
        Objects.requireNonNull(titulo); Objects.requireNonNull(detalhe); Objects.requireNonNull(padrao);
    }
}

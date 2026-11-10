package com.projekttellus.simulation;
import com.projekttellus.domain.Validacao;
public record Indicadores(double humana, double biologica) {
    public Indicadores {
        Validacao.intervalo("Habitabilidade humana", humana, 0, 100);
        Validacao.intervalo("Potencial biológico", biologica, 0, 100);
    }
}

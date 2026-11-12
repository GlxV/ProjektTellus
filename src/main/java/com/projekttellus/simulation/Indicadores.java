package com.projekttellus.simulation;
import com.projekttellus.domain.Validacao;
import java.util.List;

public record Indicadores(double humana, double biologica, double sustentabilidade, long capacidadeSuporte,
                          List<FatorAmbiental> fatoresHumanos, List<FatorAmbiental> fatoresBiologicos,
                          List<FatorAmbiental> fatoresCivilizacao) {
    public Indicadores {
        Validacao.intervalo("Habitabilidade humana", humana, 0, 100);
        Validacao.intervalo("Potencial biológico", biologica, 0, 100);
        Validacao.intervalo("Sustentabilidade", sustentabilidade, 0, 100);
        if (capacidadeSuporte < 0 || capacidadeSuporte > 100_000_000_000L)
            throw new IllegalArgumentException("Capacidade fora dos limites do modelo.");
        fatoresHumanos = List.copyOf(fatoresHumanos);
        fatoresBiologicos = List.copyOf(fatoresBiologicos);
        fatoresCivilizacao = List.copyOf(fatoresCivilizacao);
    }
}

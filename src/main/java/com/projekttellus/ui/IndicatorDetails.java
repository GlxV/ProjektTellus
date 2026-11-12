package com.projekttellus.ui;

import com.projekttellus.domain.EstadoPlanetario;
import com.projekttellus.simulation.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/** Explanations consume the exact factors that produced the score; no parallel scoring engine. */
final class IndicatorDetails extends ScrollPane {
    private final VBox content = new VBox(12);
    IndicatorDetails() {
        setFitToWidth(true); setMinSize(0, 0); setHbarPolicy(ScrollBarPolicy.NEVER);
        content.getStyleClass().add("tab-content-pad"); setContent(content);
        clear();
    }
    void clear() { content.getChildren().setAll(label("Crie ou carregue um mundo para examinar os fatores calculados.", "muted")); }
    void show(EstadoPlanetario state, Indicadores i) {
        content.getChildren().setAll(label("Por que este resultado?", "section-title"),
                label("Cada adequação multiplica a pontuação. A perda condicional em pontos percentuais mostra quanto recuperar apenas esse fator ajudaria; as perdas não são somáveis. Limites educacionais, sem validação científica.", "muted"),
                label("Capacidade estimada: " + integer(i.capacidadeSuporte()) + " pessoas\nPopulação: " + integer(state.civilizacao().populacao())
                        + " · Continente: " + f(state.superficieContinental()) + "% do planeta · Área vegetada: " + f(state.superficieVegetada()) + "% do planeta", "notice"));
        section("Habitabilidade humana", i.humana(), i.fatoresHumanos());
        section("Potencial biológico", i.biologica(), i.fatoresBiologicos());
        section("Sustentabilidade da civilização", i.sustentabilidade(), i.fatoresCivilizacao());
        content.getChildren().add(label("Vegetação é a fração vegetada do continente seco disponível. Oceano pode abrigar vida, mas não sustenta ocupação humana convencional neste modelo. Água não distingue salinidade, rios ou aquíferos.", "muted"));
    }
    private void section(String name, double score, List<FatorAmbiental> factors) {
        content.getChildren().add(label(name + " · " + f(score) + "%", "section-title"));
        for (int index = 0; index < factors.size(); index++) {
            var factor = factors.get(index);
            double without = 100;
            for (int j = 0; j < factors.size(); j++) if (index != j) without *= factors.get(j).adequacao();
            content.getChildren().add(label(factor.nome() + ": " + value(factor) + " " + factor.unidade()
                    + " · " + factor.classificacao() + " · adequação " + f(factor.adequacao()*100) + "%"
                    + " · perda condicional " + f(Math.max(0, without-score)) + " pp\n" + factor.explicacao(), ""));
        }
    }
    private static Label label(String text, String style) {
        var l = new Label(text); l.setWrapText(true); l.setMinWidth(0); l.setMaxWidth(Double.MAX_VALUE);
        if (!style.isEmpty()) l.getStyleClass().add(style); return l;
    }
    private static String integer(long n) { return NumberFormat.getIntegerInstance(Locale.of("pt", "BR")).format(n); }
    private static String value(FatorAmbiental factor) {
        var format = NumberFormat.getNumberInstance(Locale.of("pt", "BR"));
        format.setMaximumFractionDigits(factor.unidade().contains("atm p") ? 6 : factor.unidade().equals("pessoas") ? 0 : 2);
        return format.format(factor.valor());
    }
    private static String f(double n) { return String.format(Locale.of("pt", "BR"), "%.2f", n); }
}

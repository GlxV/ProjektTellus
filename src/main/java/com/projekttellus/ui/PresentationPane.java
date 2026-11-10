package com.projekttellus.ui;

import com.projekttellus.application.ResultadoOperacao;
import com.projekttellus.domain.EstadoPlanetario;
import com.projekttellus.simulation.ModeloPlanetario;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

/** Replay is a read-only projection of completed real operations; it never runs the engine again. */
public final class PresentationPane extends ScrollPane implements AutoCloseable {
    private final Label description = label("Execute uma ação para acompanhar seu fluxo real.", "muted");
    private final Label count = label("Nenhuma ação concluída", "eyebrow");
    private final Label title = label("O que acontece em cada etapa?", "presentation-title");
    private final Label detail = label("Crie um mundo, aplique um evento ou restaure um retrato. Os passos aparecem aqui depois da confirmação.", "");
    private final Label pattern = label("", "notice");
    private final Label comparison = label("", "comparison");
    private final Button back = new Button("← Anterior"), next = new Button("Próximo →"), play = new Button("▶ Reproduzir");
    private final Timeline replay = new Timeline();
    private final Consumer<EstadoPlanetario> preview;
    private ResultadoOperacao operation;
    private int index;

    public PresentationPane(Consumer<EstadoPlanetario> preview, Consumer<String> demonstration) {
        this.preview = preview;
        setFitToWidth(true);
        VBox content = new VBox(10); content.setPadding(new Insets(12));
        Label heading = label("Reprodução da última ação", "section-title");
        HBox navigation = new HBox(8, back, next, play, count);
        Button before = new Button("Ver globo antes"), after = new Button("Ver globo depois");
        before.setOnAction(e -> showBefore()); after.setOnAction(e -> showAfter());
        HBox comparisonControls = new HBox(8, before, after);
        HBox demos = new HBox(6);
        for (String name : new String[]{"Builder", "Factory Method", "Observer", "Memento"}) {
            Button b = new Button(name); b.setOnAction(e -> demonstration.accept(name)); demos.getChildren().add(b);
        }
        Label instructions = label("Demonstrar com ações reais (abaixo). Use retratos para preservar e restaurar; a reprodução não modifica seu mundo.", "muted");
        content.getChildren().addAll(heading, description, navigation, title, detail, pattern, comparison, comparisonControls, instructions, demos);
        setContent(content);
        replay.getKeyFrames().add(new KeyFrame(Duration.seconds(2.8), e -> {
            if (operation == null || index >= operation.passos().size() - 1) stop();
            else { index++; render(); }
        })); replay.setCycleCount(Animation.INDEFINITE);
        back.setOnAction(e -> { stop(); if (index > 0) index--; render(); });
        next.setOnAction(e -> { stop(); if (operation != null && index < operation.passos().size() - 1) index++; render(); });
        play.setOnAction(e -> {
            if (replay.getStatus() == Animation.Status.RUNNING) stop();
            else if (operation != null && !operation.passos().isEmpty()) {
                if (index == operation.passos().size() - 1) index = 0;
                render(); play.setText("Ⅱ Pausar"); replay.playFromStart();
            }
        });
        back.setDisable(true); next.setDisable(true); play.setDisable(true);
    }

    public void show(ResultadoOperacao result) {
        stop(); operation = result; index = 0; description.setText(result.descricao());
        comparison.setText(compare(result.antes(), result.depois())); render();
    }
    public void clear() {
        stop(); operation = null; count.setText("Nenhuma ação neste mundo"); title.setText("Execute uma nova ação");
        description.setText("Carregar um mundo não reproduz ações de outra sessão."); detail.setText("Crie condições, aplique um evento ou restaure um retrato para acompanhar os passos reais.");
        comparison.setText(""); pattern.setText(""); back.setDisable(true); next.setDisable(true); play.setDisable(true);
    }
    private void render() {
        if (operation == null || operation.passos().isEmpty()) return;
        var step = operation.passos().get(index);
        count.setText("Etapa " + (index + 1) + " de " + operation.passos().size());
        title.setText(step.titulo()); detail.setText(step.detalhe());
        pattern.setText(switch (step.padrao()) {
            case "Builder" -> "Builder · reúne condições válidas para construir uma Terra independente.";
            case "Factory Method" -> "Factory Method · o criador escolhido prepara o evento com seus próprios efeitos.";
            case "Observer" -> "Observer · os assinantes recebem a mudança e atualizam suas responsabilidades.";
            case "Memento" -> "Memento · um retrato imutável permite recuperar o estado completo deste mundo.";
            default -> "Fluxo real · etapa confirmada pelo serviço compartilhado entre os dois modos.";
        });
        back.setDisable(index == 0); next.setDisable(index == operation.passos().size() - 1); play.setDisable(false);
    }
    public void showBefore() { stop(); if (operation != null) preview.accept(operation.antes()); }
    public void showAfter() { stop(); if (operation != null) preview.accept(operation.depois()); }
    public void stop() { replay.stop(); play.setText("▶ Reproduzir"); }
    @Override public void close() { stop(); }

    private static Label label(String text, String css) { Label l = new Label(text); l.setWrapText(true); if (!css.isEmpty()) l.getStyleClass().add(css); return l; }
    private static String compare(EstadoPlanetario a, EstadoPlanetario b) {
        var first = ModeloPlanetario.calcular(a); var last = ModeloPlanetario.calcular(b);
        return "ANTES  →  DEPOIS\n" +
                "Humanos: " + f(first.humana()) + "% → " + f(last.humana()) + "%   ·   Vida: " + f(first.biologica()) + "% → " + f(last.biologica()) + "%\n" +
                "Temperatura: " + f(a.clima().temperatura()) + " → " + f(b.clima().temperatura()) + " °C   ·   Vegetação: " + f(a.biosfera().vegetacao()) + " → " + f(b.biosfera().vegetacao()) + "%\n" +
                "Água / gelo: " + f(a.hidrosfera().agua()) + " / " + f(a.hidrosfera().gelo()) + " → " + f(b.hidrosfera().agua()) + " / " + f(b.hidrosfera().gelo()) + "%\n" +
                "População: " + NumberFormat.getIntegerInstance(Locale.of("pt", "BR")).format(a.civilizacao().populacao()) + " → " + NumberFormat.getIntegerInstance(Locale.of("pt", "BR")).format(b.civilizacao().populacao()) +
                "   ·   Ano: " + f(a.anos()) + " → " + f(b.anos());
    }
    private static String f(double value) { return String.format(Locale.of("pt", "BR"), "%.1f", value); }
}

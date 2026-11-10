package com.projekttellus.ui;

import com.projekttellus.domain.*;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** Shared validated environmental form for the inspector and the Builder dialog. */
public final class EnvironmentEditor extends VBox {
    private final Map<String, TextField> fields = new LinkedHashMap<>();
    private final BooleanProperty valid = new SimpleBooleanProperty(false);
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);
    private final Label feedback = new Label();
    private final Label gases = new Label();
    private boolean filling;

    public EnvironmentEditor() {
        setSpacing(7);
        field("o2", "Oxigênio · O₂", "%"); field("n2", "Nitrogênio · N₂", "%");
        field("co2", "Dióxido de carbono · CO₂", "%"); field("he", "Hélio · He", "%");
        gases.getStyleClass().add("muted"); getChildren().add(gases);
        Button balance = new Button("Completar 100% com nitrogênio");
        balance.setMaxWidth(Double.MAX_VALUE);
        balance.setOnAction(e -> {
            try {
                double remaining = 100 - value("o2") - value("co2") - value("he");
                if (remaining < 0) throw new IllegalArgumentException("Os demais gases já ultrapassam 100%.");
                fields.get("n2").setText(number(remaining));
            } catch (IllegalArgumentException ex) { feedback.setText(ex.getMessage()); }
        });
        getChildren().add(balance);
        field("pressure", "Pressão", "atm"); field("temperature", "Temperatura", "°C");
        field("aerosol", "Aerossóis", "%"); field("radiation", "Radiação", "%");
        field("vegetation", "Vegetação", "%"); field("water", "Água líquida", "%");
        field("ice", "Gelo", "%"); field("population", "População", "pessoas");
        feedback.setWrapText(true); feedback.getStyleClass().add("error-text");
        getChildren().add(feedback);
        setState(new TerraBuilder().build().estado());
    }

    private void field(String key, String title, String unit) {
        TextField input = new TextField(); input.setPrefWidth(98); input.setMaxWidth(110);
        input.setAccessibleText(title + ", " + unit); input.setTooltip(new Tooltip(title + " (" + unit + ")"));
        fields.put(key, input);
        Label label = new Label(title); label.setLabelFor(input);
        Region space = new Region(); HBox.setHgrow(space, Priority.ALWAYS);
        Label units = new Label(unit); units.getStyleClass().add("muted"); units.setMinWidth(27);
        HBox row = new HBox(6, label, space, input, units); row.setAlignment(Pos.CENTER_LEFT);
        getChildren().add(row);
        input.textProperty().addListener((o, a, b) -> { if (!filling) { dirty.set(true); validate(); } });
    }

    private double value(String key) {
        try {
            double result = Double.parseDouble(fields.get(key).getText().strip().replace(',', '.'));
            if (!Double.isFinite(result)) throw new NumberFormatException();
            return result;
        } catch (NumberFormatException ex) { throw new IllegalArgumentException("Preencha todos os campos com números finitos. Use vírgula ou ponto decimal."); }
    }

    public EstadoPlanetario read(double years) {
        long population;
        try { population = new BigDecimal(fields.get("population").getText().strip().replace(',', '.')).longValueExact(); }
        catch (ArithmeticException | NumberFormatException ex) { throw new IllegalArgumentException("A população deve ser um número inteiro de pessoas."); }
        return new EstadoPlanetario(new Atmosfera(value("o2"), value("n2"), value("co2"), value("he"), value("pressure")),
                new Clima(value("temperature"), value("aerosol"), value("radiation")), new Biosfera(value("vegetation")),
                new Hidrosfera(value("water"), value("ice")), new Civilizacao(population), years);
    }

    private void validate() {
        try { gases.setText("Soma dos gases: " + number(value("o2") + value("n2") + value("co2") + value("he")) + "% / 100%"); }
        catch (IllegalArgumentException ex) { gases.setText("Soma dos gases: confira os quatro valores"); }
        try { read(0); valid.set(true); feedback.setText(""); }
        catch (IllegalArgumentException ex) { valid.set(false); feedback.setText(ex.getMessage()); }
    }

    public void setState(EstadoPlanetario s) {
        filling = true;
        put("o2", s.atmosfera().oxigenio()); put("n2", s.atmosfera().nitrogenio());
        put("co2", s.atmosfera().co2()); put("he", s.atmosfera().helio()); put("pressure", s.atmosfera().pressao());
        put("temperature", s.clima().temperatura()); put("aerosol", s.clima().aerossol()); put("radiation", s.clima().radiacao());
        put("vegetation", s.biosfera().vegetacao()); put("water", s.hidrosfera().agua()); put("ice", s.hidrosfera().gelo());
        fields.get("population").setText(Long.toString(s.civilizacao().populacao()));
        filling = false; dirty.set(false); validate();
    }
    private void put(String key, double value) { fields.get(key).setText(number(value)); }
    private static String number(double value) {
        return BigDecimal.valueOf(value).setScale(8, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
    public ReadOnlyBooleanProperty validProperty() { return valid; }
    public ReadOnlyBooleanProperty dirtyProperty() { return dirty; }
    public boolean isDirty() { return dirty.get(); }
}

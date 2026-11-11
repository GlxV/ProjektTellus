package com.projekttellus.ui;

import com.projekttellus.domain.TerraBuilder;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.Window;

/** The same editor collects actual Builder inputs, never a demonstration-only mock. */
public final class BuilderDialog extends Dialog<TerraBuilder> {
    public BuilderDialog(Window owner) {
        UiDialogs.style(this, owner);
        setTitle("Construir um mundo"); setHeaderText("Monte as condições iniciais da sua Terra");
        TextField name = new TextField("Nova Terra"); name.setAccessibleText("Nome do novo mundo");
        EnvironmentEditor editor = new EnvironmentEditor();
        Label explanation = new Label("Builder · Reúna as condições, confira os limites e construa um mundo independente.");
        explanation.setWrapText(true); explanation.getStyleClass().add("notice");
        VBox content = new VBox(12, explanation, new Label("Nome do mundo"), name, editor); content.setPadding(new Insets(16));
        ScrollPane scroll = new ScrollPane(content); scroll.setFitToWidth(true);
        scroll.setMinHeight(0); scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPrefViewportWidth(Math.min(430, WindowLayout.workArea(owner).getWidth() - 100));
        double availableHeight = WindowLayout.workArea(owner).getHeight();
        if (owner != null) availableHeight = Math.min(availableHeight, owner.getHeight());
        scroll.setPrefViewportHeight(Math.max(180, Math.min(590, availableHeight - 210)));
        getDialogPane().setContent(scroll);
        ButtonType create = new ButtonType("Construir e salvar", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(create, ButtonType.CANCEL);
        getDialogPane().lookupButton(create).disableProperty().bind(editor.validProperty().not()
                .or(name.textProperty().isEmpty()).or(name.textProperty().length().greaterThan(100)));
        setResultConverter(button -> {
            if (button != create) return null;
            var state = editor.read(0);
            return new TerraBuilder().nome(name.getText()).atmosfera(state.atmosfera()).clima(state.clima())
                    .biosfera(state.biosfera()).hidrosfera(state.hidrosfera()).civilizacao(state.civilizacao());
        });
        // Reject whitespace-only names before the dialog closes.
        getDialogPane().lookupButton(create).addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            if (name.getText().isBlank()) { name.setPromptText("Informe um nome"); name.clear(); e.consume(); }
        });
        setResizable(true);
    }
}

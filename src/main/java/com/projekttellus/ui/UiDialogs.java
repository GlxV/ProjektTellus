package com.projekttellus.ui;

import javafx.scene.control.*;
import javafx.stage.Window;
import java.util.Optional;

final class UiDialogs {
    private UiDialogs() { }
    static void style(Dialog<?> dialog, Window owner) {
        if (owner != null) dialog.initOwner(owner);
        dialog.getDialogPane().getStylesheets().add(UiDialogs.class.getResource("/styles/app.css").toExternalForm());
    }
    static boolean confirm(Window owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        style(alert, owner); alert.setTitle(title); alert.setHeaderText(title); alert.setContentText(message);
        ButtonType yes = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yes, no);
        return alert.showAndWait().orElse(no) == yes;
    }
    static Optional<String> name(Window owner, String title, String initial) {
        TextInputDialog dialog = new TextInputDialog(initial); style(dialog, owner);
        dialog.setTitle(title); dialog.setHeaderText(title); dialog.setContentText("Nome (1 a 100 caracteres)");
        dialog.getDialogPane().lookupButton(ButtonType.OK).disableProperty().bind(
                dialog.getEditor().textProperty().isEmpty().or(dialog.getEditor().textProperty().length().greaterThan(100)));
        return dialog.showAndWait().map(String::strip).filter(s -> !s.isBlank());
    }
    static void error(Window owner, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR); style(alert, owner);
        alert.setTitle("Não foi possível concluir"); alert.setHeaderText("Vamos conferir essa ação"); alert.setContentText(message); alert.showAndWait();
    }
}

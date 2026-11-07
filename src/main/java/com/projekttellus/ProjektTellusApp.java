package com.projekttellus;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/** Tela inicial temporaria. Nao contem o simulador. */
public final class ProjektTellusApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        URL fxml = Objects.requireNonNull(
                getClass().getResource("/fxml/main.fxml"),
                "FXML inicial nao encontrado"
        );
        Parent root = FXMLLoader.load(fxml);
        stage.setTitle("ProjektTellus — Simulador Planetario");
        stage.setScene(new Scene(root, 1040, 660));
        stage.setMinWidth(800);
        stage.setMinHeight(520);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

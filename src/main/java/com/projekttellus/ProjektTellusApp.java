package com.projekttellus;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.projekttellus.ui.MainController;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/** Desktop entry point; its controller owns and releases the simulation session. */
public final class ProjektTellusApp extends Application {
    private MainController controller;
    @Override
    public void start(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        URL fxml = Objects.requireNonNull(
                getClass().getResource("/fxml/main.fxml"),
                "FXML inicial nao encontrado"
        );
        FXMLLoader loader = new FXMLLoader(fxml);
        Parent root = loader.load();
        controller = loader.getController();
        stage.setTitle("ProjektTellus — Simulador Planetario");
        stage.setScene(new Scene(root, 1440, 960));
        stage.setMinWidth(1180);
        stage.setMinHeight(780);
        stage.setOnCloseRequest(event -> { if (!controller.canClose()) event.consume(); });
        stage.show();
    }

    @Override public void stop() { if (controller != null) controller.close(); }

    public static void main(String[] args) {
        launch(args);
    }
}

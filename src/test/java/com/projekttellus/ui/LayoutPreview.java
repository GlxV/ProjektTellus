package com.projekttellus.ui;

import com.projekttellus.ProjektTellusApp;
import javafx.application.Platform;
import javafx.stage.Stage;

/** Manual inspection harness. Uses the real application with an explicitly sized outer window. */
public final class LayoutPreview {
    public static void main(String[] args) {
        Platform.startup(() -> {
            try {
                ProjektTellusApp app = new ProjektTellusApp();
                Stage stage = new Stage(); app.start(stage);
                stage.setWidth(Double.parseDouble(args[0]));
                stage.setHeight(Double.parseDouble(args[1]));
                stage.setX(0); stage.setY(0);
                stage.setOnHidden(e -> app.stop());
                var settled = new javafx.animation.PauseTransition(javafx.util.Duration.millis(250));
                settled.setOnFinished(e -> System.out.printf("Outer %.0fx%.0f; scene %.0fx%.0f; output scale %.2fx%.2f; work area %s%n",
                        stage.getWidth(), stage.getHeight(), stage.getScene().getWidth(), stage.getScene().getHeight(),
                        stage.getOutputScaleX(), stage.getOutputScaleY(), WindowLayout.workArea(stage)));
                settled.play();
            } catch (Exception ex) { ex.printStackTrace(); Platform.exit(); }
        });
    }
}

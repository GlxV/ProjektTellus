package com.projekttellus.ui;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

/** Window dimensions are outer, logical pixels, including Windows decorations. */
public final class WindowLayout {
    private WindowLayout() { }

    public static Rectangle2D workArea(Window owner) {
        return owner == null ? Screen.getPrimary().getVisualBounds()
                : Screen.getScreensForRectangle(owner.getX(), owner.getY(), owner.getWidth(), owner.getHeight())
                        .stream().findFirst().orElse(Screen.getPrimary()).getVisualBounds();
    }

    public static void configure(Stage stage, Rectangle2D area) {
        stage.setMinWidth(Math.min(1000, area.getWidth()));
        stage.setMinHeight(Math.min(560, area.getHeight()));
        stage.setWidth(Math.min(1440, area.getWidth()));
        stage.setHeight(Math.min(960, area.getHeight()));
        stage.setX(area.getMinX() + (area.getWidth() - stage.getWidth()) / 2);
        stage.setY(area.getMinY() + (area.getHeight() - stage.getHeight()) / 2);
    }
}

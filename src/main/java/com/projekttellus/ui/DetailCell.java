package com.projekttellus.ui;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import java.util.function.Function;

/** Consistent two-line library/history cells; long descriptions remain readable. */
final class DetailCell<T> extends ListCell<T> {
    private final Function<T, String> titleText, detailText;
    private final Label title = new Label(), detail = new Label();
    private final VBox content = new VBox(4, title, detail);

    DetailCell(ListView<T> list, Function<T, String> titleText, Function<T, String> detailText) {
        this.titleText = titleText; this.detailText = detailText;
        title.setWrapText(true); detail.setWrapText(true); detail.getStyleClass().add("muted");
        content.prefWidthProperty().bind(list.widthProperty().subtract(34));
        content.setMinWidth(0); title.setMaxWidth(Double.MAX_VALUE); detail.setMaxWidth(Double.MAX_VALUE);
        setPrefHeight(USE_COMPUTED_SIZE); setMinHeight(USE_PREF_SIZE);
    }
    @Override protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty); setText(null);
        if (empty || item == null) { setGraphic(null); setAccessibleText(null); }
        else {
            title.setText(titleText.apply(item)); detail.setText(detailText.apply(item));
            setGraphic(content); setAccessibleText(title.getText() + ". " + detail.getText());
        }
    }
}

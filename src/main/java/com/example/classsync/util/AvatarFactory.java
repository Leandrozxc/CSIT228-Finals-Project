package com.example.classsync.util;

import com.example.classsync.model.User;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class AvatarFactory {

    public static StackPane make(User user, int size) {
        StackPane pane = new StackPane();
        pane.setPrefSize(size, size);
        pane.setMinSize(size, size);
        pane.setMaxSize(size, size);
        pane.setAlignment(Pos.CENTER);
        pane.setStyle(
                "-fx-background-color: " + user.getColor() + ";" +
                        "-fx-background-radius: " + size + ";"
        );

        int fontSize = Math.max(8, size / 3);
        Label initials = new Label(user.getInitials());
        initials.setStyle(
                "-fx-font-size: " + fontSize + "px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-text-fill: white;"
        );
        pane.getChildren().add(initials);
        return pane;
    }
}
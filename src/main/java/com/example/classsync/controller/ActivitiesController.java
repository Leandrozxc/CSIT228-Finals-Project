package com.example.classsync.controller;

import com.example.classsync.data.MockData;
import com.example.classsync.model.Group;
import com.example.classsync.model.Notification;
import com.example.classsync.model.User;
import com.example.classsync.session.Session;
import com.example.classsync.util.AvatarFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ActivitiesController {

    @FXML private VBox  notifList;
    @FXML private Label unreadLabel;

    private final MockData data = MockData.get();
    private final User     me   = Session.get().getCurrentUser();

    @FXML
    public void initialize() {
        render();
    }

    private void render() {
        notifList.getChildren().clear();
        List<Notification> notifs = data.getNotificationsForUser(me);
        long unread = notifs.stream().filter(n -> !n.isRead()).count();
        unreadLabel.setText(unread > 0 ? unread + " unread" : "All caught up");

        if (notifs.isEmpty()) {
            Label empty = new Label("No activity yet.");
            empty.setStyle("-fx-text-fill: #8a8a96; -fx-font-size: 13px; -fx-padding: 32;");
            notifList.getChildren().add(empty);
            return;
        }

        for (Notification n : notifs) {
            notifList.getChildren().add(buildRow(n));
        }
    }

    private HBox buildRow(Notification n) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 24, 14, 24));
        row.setStyle(n.isRead()
                ? "-fx-border-color: transparent transparent rgba(255,255,255,0.05) transparent; -fx-border-width: 0 0 1 0; -fx-background-color: transparent;"
                : "-fx-border-color: transparent transparent rgba(255,255,255,0.05) transparent; -fx-border-width: 0 0 1 0; -fx-background-color: rgba(233,69,96,0.04);");
        row.setCursor(javafx.scene.Cursor.HAND);

        // Unread dot
        StackPane dot = new StackPane();
        dot.setPrefSize(8, 8);
        dot.setMinSize(8, 8);
        dot.setStyle(n.isRead()
                ? "-fx-background-color: transparent; -fx-background-radius: 4;"
                : "-fx-background-color: #e94560; -fx-background-radius: 4;");

        // Bell icon
        Label icon = new Label("🔔");
        icon.setStyle("-fx-font-size: 16px;");

        // Text block
        VBox textBlock = new VBox(3);
        HBox.setHgrow(textBlock, Priority.ALWAYS);

        Label msg = new Label(n.getMessage());
        msg.setStyle("-fx-font-size: 13px; -fx-text-fill: " +
                (n.isRead() ? "#8a8a96" : "#f0f0f4") + "; -fx-wrap-text: true;");
        msg.setWrapText(true);

        String timeStr = n.getTimestamp()
                .format(DateTimeFormatter.ofPattern("MMM d · h:mm a"));

        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);
        Label time = new Label(timeStr);
        time.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a8a96;");

        if (n.getGroupId() != null) {
            Group g = data.findGroup(n.getGroupId());
            if (g != null) {
                Label groupBadge = new Label(g.getName());
                groupBadge.setStyle(
                        "-fx-background-color: rgba(233,69,96,0.12); " +
                                "-fx-text-fill: #e94560; -fx-font-size: 10px; " +
                                "-fx-font-weight: 700; -fx-background-radius: 4; -fx-padding: 2 6;");
                meta.getChildren().addAll(time, groupBadge);
            } else {
                meta.getChildren().add(time);
            }
        } else {
            meta.getChildren().add(time);
        }

        textBlock.getChildren().addAll(msg, meta);
        row.getChildren().addAll(dot, icon, textBlock);

        // Click → mark read + navigate to group (future: open group detail)
        row.setOnMouseClicked(e -> {
            n.markRead();
            render();
        });

        return row;
    }

    @FXML
    private void markAllRead() {
        data.getNotificationsForUser(me).forEach(Notification::markRead);
        render();
    }
}
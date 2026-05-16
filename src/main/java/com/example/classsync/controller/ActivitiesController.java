package com.example.classsync.controller;

import com.example.classsync.data.DataService;
import com.example.classsync.model.Group;
import com.example.classsync.model.Notification;
import com.example.classsync.model.User;
import com.example.classsync.session.Session;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ActivitiesController {

    @FXML private VBox  notifList;
    @FXML private Label unreadLabel;

    private final DataService data = DataService.get();
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
            empty.getStyleClass().add("placeholder-label");
            empty.setPadding(new Insets(32));
            notifList.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < notifs.size(); i++) {
            notifList.getChildren().add(buildRow(notifs.get(i)));
            if (i < notifs.size() - 1) {
                Separator sep = new Separator();
                sep.setStyle("-fx-background-color: #2e2e2e; -fx-border-width: 0; -fx-padding: 0;");
                notifList.getChildren().add(sep);
            }
        }
    }

    private HBox buildRow(Notification n) {
        HBox row = new HBox(14);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(14, 18, 14, 18));
        row.setCursor(javafx.scene.Cursor.HAND);
        row.setStyle(n.isRead()
                ? "-fx-background-color: transparent; -fx-background-radius: 8;"
                : "-fx-background-color: rgba(233,69,96,0.05); -fx-background-radius: 8;");

        // Unread dot
        StackPane dot = new StackPane();
        dot.setMinSize(8, 8);
        dot.setPrefSize(8, 8);
        dot.setMaxSize(8, 8);
        dot.setStyle(n.isRead()
                ? "-fx-background-color: transparent; -fx-background-radius: 4;"
                : "-fx-background-color: #e94560; -fx-background-radius: 4;");

        // Bell
        Label icon = new Label("🔔");
        icon.setStyle("-fx-font-size: 15px;");

        // Text block
        VBox textBlock = new VBox(4);
        HBox.setHgrow(textBlock, Priority.ALWAYS);

        Label msg = new Label(n.getMessage());
        msg.setStyle("-fx-font-size: 13px; -fx-text-fill: "
                + (n.isRead() ? "#8a8a8a" : "#e8e8e5") + ";");
        msg.setWrapText(true);

        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);

        String timeStr = n.getTimestamp()
                .format(DateTimeFormatter.ofPattern("MMM d · h:mm a"));
        Label time = new Label(timeStr);
        time.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b6b6b;");
        meta.getChildren().add(time);

        if (n.getGroupId() != null) {
            data.findGroup(n.getGroupId()).ifPresent(g -> {
                Label groupBadge = new Label(g.getName());
                groupBadge.getStyleClass().add("badge-accent");
                meta.getChildren().add(groupBadge);
            });
        }

        // "Tap to view" hint for unread
        if (!n.isRead()) {
            Label hint = new Label("Tap to view");
            hint.setStyle("-fx-font-size: 10px; -fx-text-fill: #4d9ef5;");
            meta.getChildren().add(hint);
        }

        textBlock.getChildren().addAll(msg, meta);
        row.getChildren().addAll(dot, icon, textBlock);

        // Hover effect
        row.setOnMouseEntered(e -> row.setStyle(
                "-fx-background-color: #2a2a2a; -fx-background-radius: 8;"));
        row.setOnMouseExited(e -> row.setStyle(n.isRead()
                ? "-fx-background-color: transparent; -fx-background-radius: 8;"
                : "-fx-background-color: rgba(233,69,96,0.05); -fx-background-radius: 8;"));

        // Click → mark read + show detail dialog
        row.setOnMouseClicked(e -> {
            // mark in DB
            data.markNotificationRead(n.getId());
            // mark in memory so UI updates immediately
            n.markRead();
            showDetailDialog(n);
            render();
        });
        return row;
    }

    private void showDetailDialog(Notification n) {
        VBox root = new VBox(0);
        root.getStyleClass().add("dialog-box");
        root.setPrefWidth(460);

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setStyle("-fx-border-color: #2e2e2e; -fx-border-width: 0 0 1 0;");

        Label bellIcon = new Label("🔔");
        bellIcon.setStyle("-fx-font-size: 16px;");

        Label title = new Label("Notification");
        title.getStyleClass().add("dialog-title");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String timeStr = n.getTimestamp()
                .format(DateTimeFormatter.ofPattern("MMM d · h:mm a"));
        Label time = new Label(timeStr);
        time.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b6b6b;");

        header.getChildren().addAll(bellIcon, title, spacer, time);

        // Body
        VBox body = new VBox(14);
        body.setPadding(new Insets(20, 24, 20, 24));

        Label msgLabel = new Label(n.getMessage());
        msgLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e8e8e5; -fx-wrap-text: true;");
        msgLabel.setWrapText(true);

        body.getChildren().add(msgLabel);

        // Group badge if present
        if (n.getGroupId() != null) {
            data.findGroup(n.getGroupId()).ifPresent(g -> {
                HBox groupRow = new HBox(8);
                groupRow.setAlignment(Pos.CENTER_LEFT);
                Label fromLabel = new Label("Group");
                fromLabel.getStyleClass().add("dialog-field-label");
                Label groupBadge = new Label(g.getName());
                groupBadge.getStyleClass().add("badge-accent");
                groupRow.getChildren().addAll(fromLabel, groupBadge);
                body.getChildren().add(groupRow);
            });
        }

        // Description block — if notification has extra detail
        VBox descBox = new VBox(6);
        descBox.setStyle(
                "-fx-background-color: #242424; -fx-background-radius: 8; " +
                        "-fx-border-color: #2e2e2e; -fx-border-radius: 8; " +
                        "-fx-border-width: 1; -fx-padding: 14;");
        Label descTitle = new Label("DETAILS");
        descTitle.getStyleClass().add("dialog-field-label");
        Label descBody = new Label(
                n.getDescription() != null && !n.getDescription().isBlank()
                        ? n.getDescription()
                        : "No additional details available.");
        descBody.setStyle("-fx-font-size: 12px; -fx-text-fill: #8a8a8a; -fx-wrap-text: true;");
        descBody.setWrapText(true);
        descBox.getChildren().addAll(descTitle, descBody);
        body.getChildren().add(descBox);

        // Footer
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(14, 24, 20, 24));
        footer.setStyle("-fx-border-color: #2e2e2e; -fx-border-width: 1 0 0 0;");
        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("cs-btn-primary");
        footer.getChildren().add(closeBtn);

        root.getChildren().addAll(header, body, footer);

        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);
        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        scene.getStylesheets().add(
                getClass().getResource("/css/app.css").toExternalForm());
        dialog.setScene(scene);
        dialog.centerOnScreen();

        closeBtn.setOnAction(e -> dialog.close());
        dialog.show();
    }

    @FXML
    private void markAllRead() {
        // DB
        data.markAllNotificationsRead(me.getId());
        // in-memory
        data.getNotificationsForUser(me).forEach(Notification::markRead);
        render();
    }
}
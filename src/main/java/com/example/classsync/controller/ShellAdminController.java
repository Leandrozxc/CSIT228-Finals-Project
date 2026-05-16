package com.example.classsync.controller;

import com.example.classsync.model.User;
import com.example.classsync.session.Session;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.List;

public class ShellAdminController {
    @FXML private StackPane contentPane;
    @FXML private Label pageTitle;
    @FXML private Button btnDashboard, btnUsers, btnAI, btnLogs;

    private List<Button> navBtns;

    @FXML
    public void initialize() {
        // Load CSS safely
        var cssResource = getClass().getResource("/css/app.css");
        if (cssResource != null) {
            contentPane.getStylesheets().add(cssResource.toExternalForm());
        } else {
            System.err.println("CSS could not be loaded.");
        }

        navBtns = List.of(btnDashboard, btnUsers, btnAI, btnLogs);
        navDashboard();
    }

    @FXML private void navDashboard() { activate(btnDashboard, "System Dashboard"); load("admin_dashboard.fxml"); }
    @FXML private void navUsers()     { activate(btnUsers,     "User Management");  load("admin_users.fxml"); }
    @FXML private void navAI()        { activate(btnAI,        "AI & System Settings"); load("admin_ai.fxml"); }
    @FXML private void navLogs()       { activate(btnLogs,      "Audit Logs");       load("admin_logs.fxml"); }

    @FXML
    private void handleLogout() {
        Session.get().clear();
        try {
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/com/example/classsync/fxml/login.fxml")), 1100, 700);
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void activate(Button btn, String title) {
        pageTitle.setText(title);
        navBtns.forEach(b -> b.getStyleClass().setAll("nav-btn"));
        btn.getStyleClass().setAll("nav-btn-active");
    }

    private void load(String fxmlFile) {
        try {
            var resource = getClass().getResource(
                    "/com/example/classsync/fxml/" + fxmlFile  // slashes, correct package path
            );

            if (resource == null) {
                System.err.println("Sub-page FXML not found: " + fxmlFile);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Node node = loader.load();
            contentPane.getChildren().setAll(node);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
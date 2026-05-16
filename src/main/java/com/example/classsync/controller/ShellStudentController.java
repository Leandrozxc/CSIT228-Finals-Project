package com.example.classsync.controller;

import com.example.classsync.model.User;
import com.example.classsync.session.Session;
import com.example.classsync.util.AvatarFactory;
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

public class ShellStudentController {

    @FXML private StackPane contentPane;
    @FXML private Label     pageTitle;
    @FXML private Label     topName;
    @FXML private Label     topRole;
    @FXML private StackPane topAvatar;
    @FXML private StackPane railAvatar;

    @FXML private Button btnActivities;
    @FXML private Button btnGroups;
    @FXML private Button btnEvaluate;
    @FXML private Button btnInsights;

    private List<Button> navBtns;

    @FXML
    public void initialize() {
        User me = Session.get().getCurrentUser();
        navBtns = List.of(btnActivities, btnGroups, btnEvaluate, btnInsights);

        if (me != null) {
            topName.setText(me.getName());
            topRole.setText(me.getRole().name());
            topAvatar.getChildren().setAll(AvatarFactory.make(me, 30));
            railAvatar.getChildren().setAll(AvatarFactory.make(me, 30));
        }

        navGroups();
    }

    @FXML private void navActivities() { activate(btnActivities, "Activity");  load("activities.fxml"); }
    @FXML private void navGroups()     { activate(btnGroups,     "Groups");    load("groups.fxml"); }
    @FXML private void navEvaluate()   { activate(btnEvaluate,   "Evaluate");  load("evaluate.fxml"); }
    @FXML private void navInsights()   { activate(btnInsights,   "Insights");  load("insights.fxml"); }

    @FXML
    private void handleLogout() {
        Session.get().clear();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/classsync/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), 1100, 700);
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void activate(Button btn, String title) {
        pageTitle.setText(title);
        navBtns.forEach(b -> b.getStyleClass().setAll("nav-btn"));
        btn.getStyleClass().setAll("nav-btn-active");
    }

    private void load(String fxmlFile) {
        try {
            Node node = FXMLLoader.load(
                    getClass().getResource("/com/example/classsync/fxml/" + fxmlFile));
            FadeTransition ft = new FadeTransition(Duration.millis(150), node);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
            contentPane.getChildren().setAll(node);
        } catch (Exception e) {
            Label ph = new Label(fxmlFile.replace(".fxml", "") + " — coming soon");
            ph.getStyleClass().add("placeholder-label");
            contentPane.getChildren().setAll(ph);
        }
    }
}
package com.example.classsync.controller;

import com.example.classsync.data.MockData;
import com.example.classsync.model.Role;
import com.example.classsync.model.User;
import com.example.classsync.session.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

public class LoginController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String pass  = passwordField.getText();

        if (email.isBlank() || pass.isBlank()) {
            errorLabel.setText("Please enter your email and password.");
            return;
        }

        Optional<User> result = MockData.get().login(email, pass);
        if (result.isEmpty()) {
            errorLabel.setText("Invalid email or password.");
            passwordField.clear();
            return;
        }

        User user = result.get();
        Session.get().setCurrentUser(user);

        try {
            String fxml = user.getRole() == Role.STUDENT
                    ? "shell_student.fxml"
                    : "shell_staff.fxml";

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/classsync/fxml/" + fxml));
            Scene scene = new Scene(loader.load(), 1100, 700);

            scene.getStylesheets().add(
                    getClass().getResource("/css/app.css").toExternalForm()
            );

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Failed to load dashboard.");
        }
    }
}
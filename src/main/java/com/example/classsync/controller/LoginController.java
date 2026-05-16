package com.example.classsync.controller;

import com.example.classsync.data.MockData;
import com.example.classsync.model.Role;
import com.example.classsync.model.User;
import com.example.classsync.session.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginBtn;

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String pass  = passwordField.getText();

        // 1. Basic validation: ensure fields aren't empty
        if (email.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please fill in all fields.");
            return;
        }

        // 2. Search for user in MockData
        Optional<User> userOpt = MockData.get().login(email, pass);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 3. Admin Feature: Reject login if account is deactivated
            if (!user.isActive()) {
                errorLabel.setText("This account has been deactivated by Admin.");
                return;
            }

            // 4. Update Session singleton for current user tracking
            Session.get().setCurrentUser(user);

            // 5. Determine which FXML file to load based on the User's Role
            String fxmlFile;
            if (user.getRole() == Role.ADMIN) {
                fxmlFile = "shell_admin.fxml";
            } else if (user.getRole() == Role.INSTRUCTOR) {
                fxmlFile = "shell_staff.fxml";
            } else {
                // Default role is STUDENT
                fxmlFile = "shell_student.fxml";
            }

            // 6. Transition to the selected shell
            // (Note: we pass the filename only, switchScene handles the path search)
            switchScene(fxmlFile);

        } else {
            // No match found for email + password
            errorLabel.setText("Invalid email or password.");
        }
    }

    private void switchScene(String fxmlFileName) {
        try {
            // Use only the correct, direct path — no fallback guessing
            var resource = getClass().getResource(
                    "/com/example/classsync/fxml/" + fxmlFileName
            );

            if (resource == null) {
                System.err.println("FXML not found: " + fxmlFileName);
                errorLabel.setText("Load Error: FXML not found.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Scene scene = new Scene(loader.load(), 1100, 700);

            // NULL-SAFE CSS loading
            var cssResource = getClass().getResource("/css/app.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            } else {
                System.err.println("Warning: /css/app.css not found on classpath.");
            }

            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (Exception e) {
            System.err.println("FXMLLoader crashed while loading: " + fxmlFileName);
            e.printStackTrace();
            errorLabel.setText("Load Error. Check Console.");
        }
    }
}
package com.example.classsync.controller;

import com.example.classsync.data.MockData;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AdminAIController {
    @FXML private TextField apiKeyField;
    @FXML private TextArea promptArea;

    @FXML
    public void initialize() {
        apiKeyField.setText(MockData.get().getGeminiApiKey());
        promptArea.setText(MockData.get().getAiSystemPrompt());
    }

    @FXML
    private void handleSave() {
        MockData.get().setGeminiApiKey(apiKeyField.getText());
        MockData.get().setAiSystemPrompt(promptArea.getText());
        MockData.get().log("Admin", "SETTINGS_UPDATE", "Updated Gemini API Configuration", false);
    }
}
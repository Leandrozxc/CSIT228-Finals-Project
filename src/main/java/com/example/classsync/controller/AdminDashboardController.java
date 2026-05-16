package com.example.classsync.controller;

import com.example.classsync.data.MockData;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class AdminDashboardController {
    @FXML private ListView<String> recentActivityList;

    @FXML
    public void initialize() {
        var logs = MockData.get().getAuditLogs().stream()
                .map(l -> "[" + l.getFormattedTime() + "] " + l.getAction() + ": " + l.getDetails())
                .toList();
        recentActivityList.setItems(FXCollections.observableArrayList(logs));
    }
}
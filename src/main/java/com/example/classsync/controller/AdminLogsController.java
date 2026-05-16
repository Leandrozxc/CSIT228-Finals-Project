package com.example.classsync.controller;

import com.example.classsync.data.MockData;
import com.example.classsync.model.AuditEntry;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminLogsController {
    @FXML private TableView<AuditEntry> logTable;
    @FXML private TableColumn<AuditEntry, String> colTime, colUser, colAction, colDetails;

    @FXML
    public void initialize() {
        colTime.setCellValueFactory(new PropertyValueFactory<>("formattedTime"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("user"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colDetails.setCellValueFactory(new PropertyValueFactory<>("details"));

        logTable.setItems(FXCollections.observableArrayList(MockData.get().getAuditLogs()));

        // Plagiarism Flagger Visual: Highlighting flagged rows
        logTable.setRowFactory(tv -> new javafx.scene.control.TableRow<>() {
            @Override
            protected void updateItem(AuditEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && item.isFlagged()) {
                    setStyle("-fx-background-color: rgba(255, 77, 77, 0.1); -fx-border-color: #ff4d4d; -fx-border-width: 0 0 0 4;");
                } else {
                    setStyle("");
                }
            }
        });
    }
}
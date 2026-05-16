package com.example.classsync.controller;

import com.example.classsync.data.MockData;
import com.example.classsync.model.Role;
import com.example.classsync.model.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminUsersController {
    @FXML private TextField searchField;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colName, colEmail, colRole, colStatus;
    @FXML private ComboBox<Role> roleCombo;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("active"));

        roleCombo.setItems(FXCollections.observableArrayList(Role.values()));
        refreshTable("");
    }

    @FXML
    private void handleSearch() {
        refreshTable(searchField.getText().toLowerCase());
    }

    private void refreshTable(String query) {
        var results = MockData.get().getUsers().stream()
                .filter(u -> u.getName().toLowerCase().contains(query) || u.getEmail().toLowerCase().contains(query))
                .toList();
        userTable.setItems(FXCollections.observableArrayList(results));
    }

    @FXML
    private void handleChangeRole() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        Role newRole = roleCombo.getValue();
        if (selected != null && newRole != null) {
            MockData.get().updateUserRole(selected.getId(), newRole);
            MockData.get().log("Admin", "ROLE_CHANGE", "Promoted " + selected.getName() + " to " + newRole, false);
            handleSearch();
        }
    }

    @FXML
    private void handleDeactivate() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            MockData.get().deactivateUser(selected.getId());
            MockData.get().log("Admin", "DEACTIVATE", "Deactivated user: " + selected.getEmail(), false);
            handleSearch();
        }
    }
}
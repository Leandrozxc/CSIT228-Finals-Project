module com.example.classsync {
    requires javafx.controls;
    requires javafx.fxml;

    exports com.example.classsync;

    exports com.example.classsync.controller;
    opens com.example.classsync.controller to javafx.fxml;
}
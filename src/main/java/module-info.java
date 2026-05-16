module com.example.classsync {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports com.example.classsync;

    exports com.example.classsync.controller;
    opens com.example.classsync.controller to javafx.fxml;
}
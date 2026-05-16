package com.example.classsync.controller;

import com.example.classsync.data.MockData;
import com.example.classsync.model.*;
import com.example.classsync.session.Session;
import com.example.classsync.util.AvatarFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GroupDetailController {

    @FXML private Label  groupName;
    @FXML private HBox   groupMeta;
    @FXML private VBox   taskList;
    @FXML private VBox   memberList;
    @FXML private Button btnAddTask;

    private final MockData data  = MockData.get();
    private final User     me    = Session.get().getCurrentUser();
    private       Group    group;

    @FXML
    public void initialize() {
        group = Session.get().getSelectedGroup();
        if (group == null) return;

        groupName.setText(group.getName());

        if (!group.getCourse().isBlank()) groupMeta.getChildren().add(badge(group.getCourse(), "badge-muted"));
        if (!group.getSection().isBlank()) groupMeta.getChildren().add(badge(group.getSection(), "badge-muted"));

        boolean isLeader = group.isLeader(me);
        btnAddTask.setVisible(isLeader);
        btnAddTask.setManaged(isLeader);

        renderTasks();
        renderMembers();
    }

    private void renderTasks() {
        taskList.getChildren().clear();
        List<Task> tasks = data.getTasksForGroup(group.getId());

        if (tasks.isEmpty()) {
            Label empty = new Label("No tasks yet.");
            empty.getStyleClass().add("placeholder-label");
            taskList.getChildren().add(empty);
            return;
        }

        for (Task t : tasks) taskList.getChildren().add(buildTaskRow(t));
    }

    private HBox buildTaskRow(Task task) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("task-row");

        Label statusPill = new Label(statusText(task.getStatus()));
        statusPill.getStyleClass().add(statusStyle(task.getStatus()));

        VBox textBlock = new VBox(4);
        HBox.setHgrow(textBlock, Priority.ALWAYS);

        Label title = new Label(task.getTitle());
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #e8e8e5;");
        title.setWrapText(true);

        HBox meta = new HBox(6);
        meta.setAlignment(Pos.CENTER_LEFT);
        if (task.getAssignee() != null) {
            meta.getChildren().add(AvatarFactory.make(task.getAssignee(), 16));
            Label aName = new Label(task.getAssignee().getName());
            aName.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a8a8a;");
            meta.getChildren().add(aName);
        }
        if (task.getDeadline() != null) {
            Label dl = new Label("· " + task.getDeadline()
                    .format(DateTimeFormatter.ofPattern("MMM d")));
            dl.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a8a8a;");
            meta.getChildren().add(dl);
        }
        textBlock.getChildren().addAll(title, meta);
        row.getChildren().addAll(statusPill, textBlock);

        if (task.getAssignee() != null && task.getAssignee().getId().equals(me.getId())) {
            ComboBox<String> statusBox = new ComboBox<>();
            statusBox.getItems().addAll("PENDING", "IN PROGRESS", "COMPLETED");
            statusBox.setValue(statusText(task.getStatus()));
            statusBox.getStyleClass().add("combo-box");
            statusBox.setOnAction(e -> {
                task.setStatus(fromText(statusBox.getValue()));
                renderTasks();
            });
            row.getChildren().add(statusBox);
        }

        return row;
    }

    private void renderMembers() {
        memberList.getChildren().clear();
        for (Group.Member m : group.getMembers()) {
            memberList.getChildren().add(buildMemberRow(m));
        }
    }

    private HBox buildMemberRow(Group.Member m) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("member-row");

        StackPane av = AvatarFactory.make(m.user(), 32);
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(m.user().getName());
        name.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #e8e8e5;");
        Label sec = new Label(m.user().getSection().isBlank() ? "No section" : m.user().getSection());
        sec.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a8a8a;");
        info.getChildren().addAll(name, sec);

        Label roleBadge = new Label(m.isLeader() ? "LEADER" : "MEMBER");
        roleBadge.getStyleClass().add(m.isLeader() ? "badge-accent" : "badge-blue");

        row.getChildren().addAll(av, info, roleBadge);
        return row;
    }

    @FXML
    private void showAddTaskDialog() {
        VBox root = new VBox(0);
        root.getStyleClass().add("dialog-box");
        root.setPrefWidth(440);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setStyle("-fx-border-color: #2e2e2e; -fx-border-width: 0 0 1 0;");
        Label title = new Label("Post Task to \"" + group.getName() + "\"");
        title.getStyleClass().add("dialog-title");
        header.getChildren().add(title);

        // Form body
        VBox body = new VBox(14);
        body.setPadding(new Insets(20, 24, 20, 24));

        // Task title
        Label tLabel = dialogFieldLabel("TASK TITLE");
        TextField tField = new TextField();
        tField.setPromptText("e.g. Design the login screen");
        tField.getStyleClass().add("cs-field");

        // Description
        Label dLabel = dialogFieldLabel("DESCRIPTION");
        TextArea dArea = new TextArea();
        dArea.setPromptText("Describe what needs to be done…");
        dArea.setPrefRowCount(3);
        dArea.setWrapText(true);
        dArea.getStyleClass().add("cs-area");

        // Assign to
        Label aLabel = dialogFieldLabel("ASSIGN TO");
        ComboBox<User> assignBox = new ComboBox<>();
        assignBox.getItems().addAll(group.getUsers());
        assignBox.setPromptText("Select member…");
        assignBox.setMaxWidth(Double.MAX_VALUE);
        assignBox.getStyleClass().add("combo-box");

        // Error
        Label errLabel = new Label("");
        errLabel.setStyle("-fx-text-fill: #e94560; -fx-font-size: 11px;");

        body.getChildren().addAll(tLabel, tField, dLabel, dArea, aLabel, assignBox, errLabel);

        // Footer
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(14, 24, 20, 24));
        footer.setStyle("-fx-border-color: #2e2e2e; -fx-border-width: 1 0 0 0;");
        Button cancel = new Button("Cancel");
        cancel.getStyleClass().add("cs-btn-ghost");
        Button post = new Button("Post Task");
        post.getStyleClass().add("cs-btn-primary");
        footer.getChildren().addAll(cancel, post);

        root.getChildren().addAll(header, body, footer);

        Stage dialog = new Stage(StageStyle.UNDECORATED);
        dialog.initModality(Modality.APPLICATION_MODAL);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                getClass().getResource("/css/app.css").toExternalForm());
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.setScene(scene);
        dialog.centerOnScreen();

        cancel.setOnAction(e -> dialog.close());
        post.setOnAction(e -> {
            String taskTitle = tField.getText().trim();
            if (taskTitle.isBlank()) { errLabel.setText("Title is required."); return; }

            String tid = "t" + (data.getTasks().size() + 1);
            Task newTask = new Task(
                    tid, taskTitle, dArea.getText().trim(),
                    assignBox.getValue(),
                    TaskStatus.PENDING,
                    LocalDate.now().plusDays(7),
                    group.getId()
            );
            data.addTask(newTask);
            dialog.close();
            renderTasks();
        });

        dialog.show();
    }

    private Label dialogFieldLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("dialog-field-label");
        return l;
    }

    @FXML
    private void goBack() {
        try {
            Node groups = FXMLLoader.load(
                    getClass().getResource("/com/example/classsync/fxml/groups.fxml"));
            StackPane contentPane = (StackPane) groupName.getScene().lookup("#contentPane");
            if (contentPane != null) contentPane.getChildren().setAll(groups);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Label badge(String text, String styleClass) {
        Label l = new Label(text);
        l.getStyleClass().add(styleClass);
        return l;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: #8a8a8a;");
        return l;
    }

    private String statusText(TaskStatus s) {
        return switch (s) {
            case PENDING     -> "PENDING";
            case IN_PROGRESS -> "IN PROGRESS";
            case COMPLETED   -> "COMPLETED";
        };
    }

    private String statusStyle(TaskStatus s) {
        return switch (s) {
            case PENDING     -> "pill-pending";
            case IN_PROGRESS -> "pill-progress";
            case COMPLETED   -> "pill-completed";
        };
    }

    private TaskStatus fromText(String t) {
        return switch (t) {
            case "IN PROGRESS" -> TaskStatus.IN_PROGRESS;
            case "COMPLETED"   -> TaskStatus.COMPLETED;
            default            -> TaskStatus.PENDING;
        };
    }
}
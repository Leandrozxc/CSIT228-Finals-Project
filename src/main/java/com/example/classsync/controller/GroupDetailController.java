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

    @FXML private Label groupName;
    @FXML private HBox  groupMeta;
    @FXML private VBox  taskList;
    @FXML private VBox  memberList;
    @FXML private Button btnAddTask;

    private final MockData data  = MockData.get();
    private final User     me    = Session.get().getCurrentUser();
    private       Group    group;

    @FXML
    public void initialize() {
        group = Session.get().getSelectedGroup();
        if (group == null) return;

        groupName.setText(group.getName());

        // Meta badges
        if (!group.getCourse().isBlank()) groupMeta.getChildren().add(badge(group.getCourse(), "badge-muted"));
        if (!group.getSection().isBlank()) groupMeta.getChildren().add(badge(group.getSection(), "badge-muted"));

        // Show "Post Task" button only if the user is leader of this group
        boolean isLeader = group.isLeader(me);
        btnAddTask.setVisible(isLeader);
        btnAddTask.setManaged(isLeader);

        renderTasks();
        renderMembers();
    }

    // ── Render tasks ──────────────────────────────────────────────────────────

    private void renderTasks() {
        taskList.getChildren().clear();
        List<Task> tasks = data.getTasksForGroup(group.getId());

        if (tasks.isEmpty()) {
            Label empty = new Label("No tasks yet.");
            empty.setStyle("-fx-text-fill: #8a8a96; -fx-font-size: 13px;");
            taskList.getChildren().add(empty);
            return;
        }

        for (Task t : tasks) {
            taskList.getChildren().add(buildTaskRow(t));
        }
    }

    private HBox buildTaskRow(Task task) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        row.setStyle("-fx-background-color: #232329; -fx-background-radius: 8; " +
                "-fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 8; -fx-border-width: 1;");

        // Status pill
        Label statusPill = new Label(statusText(task.getStatus()));
        statusPill.getStyleClass().add(statusStyle(task.getStatus()));

        // Title + assignee
        VBox textBlock = new VBox(3);
        HBox.setHgrow(textBlock, Priority.ALWAYS);
        Label title = new Label(task.getTitle());
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #f0f0f4;");
        title.setWrapText(true);

        HBox meta = new HBox(6);
        meta.setAlignment(Pos.CENTER_LEFT);
        if (task.getAssignee() != null) {
            meta.getChildren().add(AvatarFactory.make(task.getAssignee(), 16));
            Label aName = new Label(task.getAssignee().getName());
            aName.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a8a96;");
            meta.getChildren().add(aName);
        }
        if (task.getDeadline() != null) {
            Label dl = new Label("· " + task.getDeadline()
                    .format(DateTimeFormatter.ofPattern("MMM d")));
            dl.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a8a96;");
            meta.getChildren().add(dl);
        }
        textBlock.getChildren().addAll(title, meta);
        row.getChildren().addAll(statusPill, textBlock);

        // If this task belongs to me, allow status update
        if (task.getAssignee() != null && task.getAssignee().getId().equals(me.getId())) {
            ComboBox<String> statusBox = new ComboBox<>();
            statusBox.getItems().addAll("PENDING", "IN PROGRESS", "COMPLETED");
            statusBox.setValue(statusText(task.getStatus()));
            statusBox.setStyle(
                    "-fx-background-color: #2a2a31; -fx-text-fill: #f0f0f4; " +
                            "-fx-font-size: 11px; -fx-background-radius: 6;");
            statusBox.setOnAction(e -> {
                task.setStatus(fromText(statusBox.getValue()));
                renderTasks();
            });
            row.getChildren().add(statusBox);
        }

        return row;
    }

    // ── Render members ────────────────────────────────────────────────────────

    private void renderMembers() {
        memberList.getChildren().clear();
        for (Group.Member m : group.getMembers()) {
            memberList.getChildren().add(buildMemberRow(m));
        }
    }

    private HBox buildMemberRow(Group.Member m) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8));
        row.setStyle("-fx-background-radius: 8;");

        StackPane av = AvatarFactory.make(m.user(), 34);
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(m.user().getName());
        name.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #f0f0f4;");
        Label sec = new Label(m.user().getSection().isBlank() ? "No section" : m.user().getSection());
        sec.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a8a96;");
        info.getChildren().addAll(name, sec);

        Label roleBadge = new Label(m.isLeader() ? "LEADER" : "MEMBER");
        roleBadge.getStyleClass().add(m.isLeader() ? "badge-accent" : "badge-blue");

        row.getChildren().addAll(av, info, roleBadge);
        return row;
    }

    // ── Add Task dialog (leader only) ─────────────────────────────────────────

    @FXML
    private void showAddTaskDialog() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(28));
        root.setStyle("-fx-background-color: #232329; -fx-background-radius: 12;");
        root.setPrefWidth(420);

        Label title = new Label("Post Task to \"" + group.getName() + "\"");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #f0f0f4;");

        // Task title
        Label tLabel = fieldLabel("Task Title");
        TextField tField = styledField("e.g. Design the login screen");

        // Description
        Label dLabel = fieldLabel("Description");
        TextArea dArea = new TextArea();
        dArea.setPromptText("Describe what needs to be done…");
        dArea.setPrefRowCount(3);
        dArea.setWrapText(true);
        dArea.getStyleClass().add("cs-area");

        // Assign to
        Label aLabel = fieldLabel("Assign To");
        ComboBox<User> assignBox = new ComboBox<>();
        assignBox.getItems().addAll(group.getUsers());
        assignBox.setPromptText("Select member…");
        assignBox.setMaxWidth(Double.MAX_VALUE);
        assignBox.setStyle("-fx-background-color: #2a2a31; -fx-text-fill: #f0f0f4; -fx-background-radius: 8;");

        // Error
        Label errLabel = new Label("");
        errLabel.setStyle("-fx-text-fill: #e94560; -fx-font-size: 11px;");

        // Buttons
        HBox btnRow = new HBox(10);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new Button("Cancel");
        cancel.getStyleClass().add("cs-btn-ghost");
        Button post = new Button("Post Task");
        post.getStyleClass().add("cs-btn-primary");
        btnRow.getChildren().addAll(cancel, post);

        root.getChildren().addAll(title, tLabel, tField, dLabel, dArea,
                aLabel, assignBox, errLabel, btnRow);

        Stage dialog = new Stage(StageStyle.UNDECORATED);
        dialog.initModality(Modality.APPLICATION_MODAL);
        Scene scene = new Scene(root);
        try { scene.getStylesheets().add(
                getClass().getResource("/com/example/classsync/css/app.css").toExternalForm());
        } catch (Exception ignored) {}
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

    // ── Back navigation ───────────────────────────────────────────────────────

    @FXML
    private void goBack() {
        try {
            Node groups = FXMLLoader.load(
                    getClass().getResource("/com/example/classsync/fxml/groups.fxml"));
            StackPane contentPane = (StackPane) groupName.getScene().lookup("#contentPane");
            if (contentPane != null) contentPane.getChildren().setAll(groups);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Label badge(String text, String styleClass) {
        Label l = new Label(text);
        l.getStyleClass().add(styleClass);
        return l;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: #8a8a96;");
        return l;
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("cs-field");
        return tf;
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
package com.example.classsync.controller;

import com.example.classsync.data.DataService;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class GroupDetailController {

    @FXML private Label  groupName;
    @FXML private HBox   groupMeta;
    @FXML private VBox   taskList;
    @FXML private VBox   memberList;
    @FXML private Button btnAddTask;

    private final DataService data = DataService.get();
    private final User        me   = Session.get().getCurrentUser();
    private       Group       group;

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

    // ── Task list ─────────────────────────────────────────────────────────────

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
        row.setCursor(javafx.scene.Cursor.HAND);

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

        if (task.getStatus() == TaskStatus.COMPLETED) {
            row.getChildren().add(buildScoreBadge(task));
        }

        row.setOnMouseEntered(e -> row.setStyle(
                "-fx-background-color: #2a2a2a; -fx-background-radius: 8; " +
                        "-fx-border-color: #3a3a3a; -fx-border-radius: 8; " +
                        "-fx-border-width: 1; -fx-padding: 12;"));
        row.setOnMouseExited(e -> row.getStyleClass().setAll("task-row"));
        row.setOnMouseClicked(e -> showTaskPopup(task));

        return row;
    }

    private Label buildScoreBadge(Task task) {
        String scoreText = task.getAiScore() > 0
                ? "★ " + task.getAiScore() + " / 10"
                : "Grading…";
        Label badge = new Label(scoreText);

        if (task.getAiScore() <= 0) {
            badge.setStyle(
                    "-fx-background-color: rgba(107,107,107,0.2);" +
                            "-fx-text-fill: #6b6b6b;" +
                            "-fx-background-radius: 6;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 4 10;");
        } else if (task.getAiScore() >= 8) {
            badge.setStyle(
                    "-fx-background-color: rgba(52,199,89,0.15);" +
                            "-fx-text-fill: #34c759;" +
                            "-fx-background-radius: 6;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 4 10;");
        } else if (task.getAiScore() >= 5) {
            badge.setStyle(
                    "-fx-background-color: rgba(255,159,10,0.15);" +
                            "-fx-text-fill: #ff9f0a;" +
                            "-fx-background-radius: 6;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 4 10;");
        } else {
            badge.setStyle(
                    "-fx-background-color: rgba(233,69,96,0.15);" +
                            "-fx-text-fill: #e94560;" +
                            "-fx-background-radius: 6;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 4 10;");
        }
        return badge;
    }

    // ── Task popup ────────────────────────────────────────────────────────────

    private void showTaskPopup(Task task) {
        boolean isMyTask = task.getAssignee() != null
                && task.getAssignee().getId().equals(me.getId());

        VBox root = new VBox(0);
        root.getStyleClass().add("dialog-box");
        root.setPrefWidth(460);

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setStyle("-fx-border-color: #2e2e2e; -fx-border-width: 0 0 1 0;");

        VBox headerInfo = new VBox(4);
        HBox.setHgrow(headerInfo, Priority.ALWAYS);

        Label titleLabel = new Label(task.getTitle());
        titleLabel.getStyleClass().add("dialog-title");
        titleLabel.setWrapText(true);

        HBox headerMeta = new HBox(8);
        headerMeta.setAlignment(Pos.CENTER_LEFT);

        Label statusPill = new Label(statusText(task.getStatus()));
        statusPill.getStyleClass().add(statusStyle(task.getStatus()));

        if (task.getDeadline() != null) {
            Label dl = new Label("Due " + task.getDeadline()
                    .format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
            dl.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b6b6b;");
            headerMeta.getChildren().addAll(statusPill, dl);
        } else {
            headerMeta.getChildren().add(statusPill);
        }

        headerInfo.getChildren().addAll(titleLabel, headerMeta);

        if (task.getAssignee() != null) {
            StackPane av = AvatarFactory.make(task.getAssignee(), 32);
            header.getChildren().addAll(av, headerInfo);
        } else {
            header.getChildren().add(headerInfo);
        }

        // Body
        VBox body = new VBox(14);
        body.setPadding(new Insets(20, 24, 20, 24));

        Label descTitle = new Label("DESCRIPTION");
        descTitle.getStyleClass().add("dialog-field-label");

        VBox descBox = new VBox(6);
        descBox.setStyle(
                "-fx-background-color: #242424;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: #2e2e2e;" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1;" +
                        "-fx-padding: 14;");

        Label descBody = new Label(
                task.getDescription() != null && !task.getDescription().isBlank()
                        ? task.getDescription()
                        : "No description provided.");
        descBody.setStyle("-fx-font-size: 13px; -fx-text-fill: #8a8a8a; -fx-wrap-text: true;");
        descBody.setWrapText(true);
        descBox.getChildren().add(descBody);
        body.getChildren().addAll(descTitle, descBox);

        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);

        if (isMyTask && task.getStatus() != TaskStatus.COMPLETED) {

            Separator sep = new Separator();
            sep.setStyle("-fx-background-color: #2e2e2e;");
            body.getChildren().add(sep);

            Label fileTitle = new Label("ATTACHMENT (PDF)");
            fileTitle.getStyleClass().add("dialog-field-label");

            HBox fileRow = new HBox(10);
            fileRow.setAlignment(Pos.CENTER_LEFT);

            Label fileNameLabel = new Label("No file selected");
            fileNameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b6b6b;");
            HBox.setHgrow(fileNameLabel, Priority.ALWAYS);

            Button browseBtn = new Button("Browse PDF");
            browseBtn.getStyleClass().add("cs-btn-ghost");
            browseBtn.setStyle("-fx-border-color: #333333; -fx-border-radius: 6; -fx-border-width: 1;");

            final File[] selectedFile = {null};
            browseBtn.setOnAction(e -> {
                FileChooser fc = new FileChooser();
                fc.setTitle("Select PDF");
                fc.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                File f = fc.showOpenDialog(dialog);
                if (f != null) {
                    selectedFile[0] = f;
                    fileNameLabel.setText(f.getName());
                    fileNameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4d9ef5;");
                }
            });

            fileRow.getChildren().addAll(fileNameLabel, browseBtn);

            Label noteTitle = new Label("NOTE (OPTIONAL)");
            noteTitle.getStyleClass().add("dialog-field-label");

            TextArea noteArea = new TextArea();
            noteArea.setPromptText("Add a note about your submission…");
            noteArea.setPrefRowCount(2);
            noteArea.setWrapText(true);
            noteArea.getStyleClass().add("cs-area");

            Label errLabel = new Label("");
            errLabel.setStyle("-fx-text-fill: #e94560; -fx-font-size: 11px;");

            body.getChildren().addAll(fileTitle, fileRow, noteTitle, noteArea, errLabel);

            HBox footer = new HBox(10);
            footer.setAlignment(Pos.CENTER_RIGHT);
            footer.setPadding(new Insets(14, 24, 20, 24));
            footer.setStyle("-fx-border-color: #2e2e2e; -fx-border-width: 1 0 0 0;");

            Button cancelBtn = new Button("Cancel");
            cancelBtn.getStyleClass().add("cs-btn-ghost");

            Button submitBtn = new Button("Submit Task");
            submitBtn.getStyleClass().add("cs-btn-primary");

            footer.getChildren().addAll(cancelBtn, submitBtn);
            root.getChildren().addAll(header, body, footer);

            cancelBtn.setOnAction(e -> dialog.close());
            submitBtn.setOnAction(e -> {
                if (selectedFile[0] == null) {
                    errLabel.setText("Please attach a PDF before submitting.");
                    return;
                }

                data.submitTask(
                        task.getId(),
                        me.getId(),
                        selectedFile[0].getAbsolutePath(),
                        noteArea.getText().trim()
                );


                // Update in-memory object so UI reflects immediately
                task.setStatus(TaskStatus.COMPLETED);
                task.setAiScore(0);
                task.setSubmissionFile(selectedFile[0].getAbsolutePath());
                task.setSubmissionNote(noteArea.getText().trim());

                dialog.close();
                renderTasks();
            });

        } else {
            HBox footer = new HBox();
            footer.setAlignment(Pos.CENTER_RIGHT);
            footer.setPadding(new Insets(14, 24, 20, 24));
            footer.setStyle("-fx-border-color: #2e2e2e; -fx-border-width: 1 0 0 0;");

            Button closeBtn = new Button("Close");
            closeBtn.getStyleClass().add("cs-btn-ghost");
            footer.getChildren().add(closeBtn);

            root.getChildren().addAll(header, body, footer);
            closeBtn.setOnAction(e -> dialog.close());
        }

        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        scene.getStylesheets().add(
                getClass().getResource("/css/app.css").toExternalForm());
        dialog.setScene(scene);
        dialog.centerOnScreen();
        dialog.show();
    }

    // ── Member list ───────────────────────────────────────────────────────────

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
        Label sec = new Label(m.user().getSection().isBlank()
                ? "No section" : m.user().getSection());
        sec.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a8a8a;");
        info.getChildren().addAll(name, sec);

        Label roleBadge = new Label(m.isLeader() ? "LEADER" : "MEMBER");
        roleBadge.getStyleClass().add(m.isLeader() ? "badge-accent" : "badge-blue");

        row.getChildren().addAll(av, info, roleBadge);
        return row;
    }

    // ── Add Task dialog ───────────────────────────────────────────────────────

    @FXML
    private void showAddTaskDialog() {
        VBox root = new VBox(0);
        root.getStyleClass().add("dialog-box");
        root.setPrefWidth(440);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 24, 16, 24));
        header.setStyle("-fx-border-color: #2e2e2e; -fx-border-width: 0 0 1 0;");
        Label title = new Label("Post Task to \"" + group.getName() + "\"");
        title.getStyleClass().add("dialog-title");
        header.getChildren().add(title);

        VBox body = new VBox(14);
        body.setPadding(new Insets(20, 24, 20, 24));

        Label tLabel = dialogFieldLabel("TASK TITLE");
        TextField tField = new TextField();
        tField.setPromptText("e.g. Design the login screen");
        tField.getStyleClass().add("cs-field");

        Label dLabel = dialogFieldLabel("DESCRIPTION");
        TextArea dArea = new TextArea();
        dArea.setPromptText("Describe what needs to be done…");
        dArea.setPrefRowCount(3);
        dArea.setWrapText(true);
        dArea.getStyleClass().add("cs-area");

        Label aLabel = dialogFieldLabel("ASSIGN TO");
        ComboBox<User> assignBox = new ComboBox<>();
        assignBox.getItems().addAll(group.getUsers());
        assignBox.setPromptText("Select member…");
        assignBox.setMaxWidth(Double.MAX_VALUE);
        assignBox.getStyleClass().add("combo-box");

        Label errLabel = new Label("");
        errLabel.setStyle("-fx-text-fill: #e94560; -fx-font-size: 11px;");

        body.getChildren().addAll(tLabel, tField, dLabel, dArea, aLabel, assignBox, errLabel);

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

        Stage dialog = new Stage(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);
        Scene scene = new Scene(root);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        scene.getStylesheets().add(
                getClass().getResource("/css/app.css").toExternalForm());
        dialog.setScene(scene);
        dialog.centerOnScreen();

        cancel.setOnAction(e -> dialog.close());
        post.setOnAction(e -> {
            String taskTitle = tField.getText().trim();
            if (taskTitle.isBlank()) { errLabel.setText("Title is required."); return; }

            Task newTask = new Task(
                    UUID.randomUUID().toString(),
                    taskTitle,
                    dArea.getText().trim(),
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

    // ── Back ──────────────────────────────────────────────────────────────────

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

    private Label dialogFieldLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("dialog-field-label");
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
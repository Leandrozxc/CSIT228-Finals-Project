package com.example.classsync.controller;

import com.example.classsync.data.MockData;
import com.example.classsync.model.Group;
import com.example.classsync.model.Task;
import com.example.classsync.model.TaskStatus;
import com.example.classsync.model.User;
import com.example.classsync.session.Session;
import com.example.classsync.util.AvatarFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;

import java.util.List;

public class GroupsController {

    @FXML private VBox groupList;

    private final MockData data = MockData.get();
    private final User     me   = Session.get().getCurrentUser();

    @FXML
    public void initialize() {
        List<Group> myGroups = data.getGroupsForUser(me);
        groupList.getChildren().clear();

        if (myGroups.isEmpty()) {
            Label empty = new Label("You are not in any groups yet.");
            empty.setStyle("-fx-text-fill: #8a8a96; -fx-font-size: 13px;");
            groupList.getChildren().add(empty);
            return;
        }

        for (Group g : myGroups) {
            groupList.getChildren().add(buildCard(g));
        }
    }

    private VBox buildCard(Group group) {
        VBox card = new VBox(14);
        card.getStyleClass().add("cs-card");
        card.setCursor(javafx.scene.Cursor.HAND);

        // ── Top row: name + role badge ──
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label name = new Label(group.getName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #f0f0f4;");
        HBox.setHgrow(name, Priority.ALWAYS);

        // Show leader/member badge based on THIS group
        boolean isLeaderHere = group.isLeader(me);
        Label roleBadge = new Label(isLeaderHere ? "LEADER" : "MEMBER");
        roleBadge.getStyleClass().add(isLeaderHere ? "badge-accent" : "badge-blue");

        topRow.getChildren().addAll(name, roleBadge);

        // ── Course + section badges ──
        HBox badges = new HBox(6);
        badges.setAlignment(Pos.CENTER_LEFT);
        if (!group.getCourse().isBlank()) {
            Label course = new Label(group.getCourse());
            course.getStyleClass().add("badge-muted");
            badges.getChildren().add(course);
        }
        if (!group.getSection().isBlank()) {
            Label sec = new Label(group.getSection());
            sec.getStyleClass().add("badge-muted");
            badges.getChildren().add(sec);
        }

        // ── Member avatars ──
        HBox avatarRow = new HBox(6);
        avatarRow.setAlignment(Pos.CENTER_LEFT);
        List<User> members = group.getUsers();
        int shown = Math.min(members.size(), 6);
        for (int i = 0; i < shown; i++) {
            avatarRow.getChildren().add(AvatarFactory.make(members.get(i), 28));
        }
        if (members.size() > 6) {
            Label more = new Label("+" + (members.size() - 6));
            more.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a8a96;");
            avatarRow.getChildren().add(more);
        }
        Label memberCount = new Label(members.size() + " members");
        memberCount.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a8a96;");

        // ── Task progress ──
        List<Task> groupTasks = data.getTasksForGroup(group.getId());
        int total     = groupTasks.size();
        int completed = (int) groupTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();

        HBox progressRow = new HBox(10);
        progressRow.setAlignment(Pos.CENTER_LEFT);
        ProgressBar pb = new ProgressBar(total > 0 ? (double) completed / total : 0);
        pb.setPrefWidth(160);
        pb.setStyle("-fx-accent: #e94560; -fx-pref-height: 4; -fx-background-radius: 4;");
        Label progressLabel = new Label(completed + "/" + total + " tasks");
        progressLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a8a96;");
        progressRow.getChildren().addAll(pb, progressLabel);

        card.getChildren().addAll(topRow, badges, avatarRow, memberCount, progressRow);

        // ── Click → open group detail ──
        card.setOnMouseClicked(e -> openGroupDetail(group));

        return card;
    }

    private void openGroupDetail(Group group) {
        try {
            // Store selected group in session for the detail controller to pick up
            Session.get().setSelectedGroup(group);
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/classsync/fxml/group_detail.fxml"));
            Node detail = loader.load();

            // Navigate in the shell's content pane
            StackPane contentPane = (StackPane) groupList.getScene()
                    .lookup("#contentPane");
            if (contentPane != null) {
                contentPane.getChildren().setAll(detail);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
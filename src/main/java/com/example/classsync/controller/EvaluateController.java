package com.example.classsync.controller;

import com.example.classsync.data.MockData;
import com.example.classsync.model.*;
import com.example.classsync.session.Session;
import com.example.classsync.util.AvatarFactory;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluateController {

    @FXML private VBox contentArea;

    private final MockData data = MockData.get();
    private final User     me   = Session.get().getCurrentUser();

    // Rating state for the current eval form
    private final Map<String, int[]> ratings = new HashMap<>();
    // keys: "effort", "reliability", "quality", "overall" → value [0]

    @FXML
    public void initialize() {
        showGroupPicker();
    }

    // ── Phase 1: Group picker ────────────────────────────────────────────────

    private void showGroupPicker() {
        contentArea.getChildren().clear();
        List<Group> myGroups = data.getGroupsForUser(me);

        Label hint = new Label("Select a group to evaluate your peers:");
        hint.setStyle("-fx-font-size: 13px; -fx-text-fill: #8a8a96;");
        contentArea.getChildren().add(hint);

        for (Group g : myGroups) {
            VBox card = new VBox(10);
            card.getStyleClass().add("cs-card");
            card.setCursor(javafx.scene.Cursor.HAND);

            Label name = new Label(g.getName());
            name.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #f0f0f4;");

            // Count how many members still need to be evaluated
            long pending = g.getUsers().stream()
                    .filter(u -> !u.getId().equals(me.getId()))
                    .filter(u -> !data.hasEvaluated(me, u, g.getId()))
                    .count();
            long total = g.getUsers().size() - 1;

            Label status = new Label(pending + " of " + total + " peers not yet evaluated");
            status.setStyle("-fx-font-size: 12px; -fx-text-fill: "
                    + (pending > 0 ? "#facc15" : "#4ade80") + ";");

            card.getChildren().addAll(name, status);
            card.setOnMouseClicked(e -> showMemberList(g));
            contentArea.getChildren().add(card);
        }
    }

    // ── Phase 2: Member list for selected group ──────────────────────────────

    private void showMemberList(Group group) {
        contentArea.getChildren().clear();

        // Back button
        Button back = new Button("← Back to Groups");
        back.getStyleClass().add("cs-btn-ghost");
        back.setOnAction(e -> showGroupPicker());

        Label title = new Label("Evaluating: " + group.getName());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #f0f0f4;");

        contentArea.getChildren().addAll(back, title);

        List<User> peers = group.getUsers().stream()
                .filter(u -> !u.getId().equals(me.getId()))
                .toList();

        if (peers.isEmpty()) {
            Label empty = new Label("No other members to evaluate.");
            empty.setStyle("-fx-text-fill: #8a8a96;");
            contentArea.getChildren().add(empty);
            return;
        }

        for (User peer : peers) {
            boolean alreadyDone = data.hasEvaluated(me, peer, group.getId());
            contentArea.getChildren().add(buildPeerCard(peer, group, alreadyDone));
        }
    }

    // ── Peer eval card ───────────────────────────────────────────────────────

    private VBox buildPeerCard(User peer, Group group, boolean alreadyDone) {
        VBox card = new VBox(14);
        card.getStyleClass().add("cs-card");

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().add(AvatarFactory.make(peer, 38));
        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(peer.getName());
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #f0f0f4;");
        Label sec = new Label(peer.getSection().isBlank() ? "Student" : peer.getSection());
        sec.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a8a96;");
        info.getChildren().addAll(name, sec);
        header.getChildren().add(info);

        if (alreadyDone) {
            Label done = new Label("✓ Evaluated");
            done.getStyleClass().add("badge-green");
            header.getChildren().add(done);
            card.getChildren().add(header);
            card.setStyle(card.getStyle() + "-fx-opacity: 0.6;");
            return card;
        }

        card.getChildren().add(header);

        // Rating rows
        int[] effort      = {0};
        int[] reliability = {0};
        int[] quality     = {0};
        int[] overall     = {0};

        card.getChildren().addAll(
                buildStarRow("Effort",      effort),
                buildStarRow("Reliability", reliability),
                buildStarRow("Quality",     quality),
                buildStarRow("Overall",     overall)
        );

        // Notes
        Label notesLabel = new Label("Notes / Comment");
        notesLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: #8a8a96;");
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Optional — share specific feedback…");
        notesArea.setPrefRowCount(2);
        notesArea.setWrapText(true);
        notesArea.getStyleClass().add("cs-area");

        // Submit
        Button submit = new Button("Submit Evaluation");
        submit.getStyleClass().add("cs-btn-primary");
        Label errLabel = new Label("");
        errLabel.setStyle("-fx-text-fill: #e94560; -fx-font-size: 11px;");

        submit.setOnAction(e -> {
            if (effort[0] == 0 || reliability[0] == 0 || quality[0] == 0 || overall[0] == 0) {
                errLabel.setText("Please rate all four categories.");
                return;
            }
            String eid = "e" + (data.getEvaluations().size() + 1);
            Evaluation ev = new Evaluation(
                    eid, me, peer, group.getId(),
                    effort[0], reliability[0], quality[0], overall[0],
                    notesArea.getText().trim()
            );
            data.addEvaluation(ev);

            // Re-render this group's member list
            showMemberList(group);
        });

        card.getChildren().addAll(notesLabel, notesArea, errLabel, submit);
        return card;
    }

    private HBox buildStarRow(String label, int[] valueHolder) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #f0f0f4; -fx-font-weight: 600;");
        lbl.setPrefWidth(90);

        HBox stars = new HBox(4);
        stars.setAlignment(Pos.CENTER_LEFT);
        Button[] starBtns = new Button[5];

        for (int i = 1; i <= 5; i++) {
            final int val = i;
            Button star = new Button("☆");
            star.getStyleClass().add("star-btn");
            star.setStyle("-fx-font-size: 20px; -fx-text-fill: #8a8a96; " +
                    "-fx-background-color: transparent; -fx-border-color: transparent; -fx-cursor: hand;");
            star.setOnAction(e -> {
                valueHolder[0] = val;
                for (int j = 0; j < 5; j++) {
                    String color = j < val ? "#facc15" : "#8a8a96";
                    String symbol = j < val ? "★" : "☆";
                    starBtns[j].setText(symbol);
                    starBtns[j].setStyle("-fx-font-size: 20px; -fx-text-fill: " + color +
                            "; -fx-background-color: transparent; -fx-border-color: transparent; -fx-cursor: hand;");
                }
            });
            starBtns[i - 1] = star;
            stars.getChildren().add(star);
        }

        row.getChildren().addAll(lbl, stars);
        return row;
    }
}
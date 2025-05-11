package org.main.quiz;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class LeaderboardDialog {

    @SuppressWarnings("unchecked")
    public static void show(Stage parent) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parent);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Quiz Master Leaderboard");
        dialog.setMinWidth(600);
        dialog.setMinHeight(400);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(15));
        mainLayout.getStyleClass().add("leaderboard-dialog");
        
        Label titleLabel = new Label("Quiz Master Leaderboard");
        titleLabel.getStyleClass().add("leaderboard-title");
        
        TableView<LeaderboardEntry> tableView = new TableView<>();
        tableView.getStyleClass().add("leaderboard-table");

        TableColumn<LeaderboardEntry, Number> rankColumn = new TableColumn<>("Rank");
        rankColumn.setCellValueFactory(p -> new SimpleIntegerProperty(
            tableView.getItems().indexOf(p.getValue()) + 1));
        rankColumn.setStyle("-fx-alignment: CENTER;");
        rankColumn.setMinWidth(60);

        TableColumn<LeaderboardEntry, String> nameColumn = new TableColumn<>("Player");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("playerName"));
        nameColumn.setMinWidth(150);

        TableColumn<LeaderboardEntry, String> scoreColumn = new TableColumn<>("Score");
        scoreColumn.setCellValueFactory(p -> new SimpleStringProperty(
            p.getValue().getScore() + "/" + p.getValue().getTotalQuestions()));
        scoreColumn.setStyle("-fx-alignment: CENTER;");
        scoreColumn.setMinWidth(80);

        TableColumn<LeaderboardEntry, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryColumn.setMinWidth(120);

        TableColumn<LeaderboardEntry, String> timeColumn = new TableColumn<>("Time Taken");
        timeColumn.setCellValueFactory(p -> new SimpleStringProperty(
            LeaderboardEntry.formatTime(p.getValue().getTimeSpentSeconds())));
        timeColumn.setStyle("-fx-alignment: CENTER;");
        timeColumn.setMinWidth(90);

        TableColumn<LeaderboardEntry, String> dateColumn = new TableColumn<>("Date");
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        dateColumn.setCellValueFactory(p -> new SimpleStringProperty(
            dateFormat.format(p.getValue().getTimestamp())));
        dateColumn.setMinWidth(140);

        tableView.getColumns().addAll(rankColumn, nameColumn, scoreColumn, categoryColumn, timeColumn, dateColumn);

        Label loadingLabel = new Label("Loading leaderboard data...");
        loadingLabel.getStyleClass().add("loading-label");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(40, 40);

        VBox loadingBox = new VBox(10, progressIndicator, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        
        mainLayout.setCenter(loadingBox);

        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("close-leaderboard-button");
        closeButton.setOnAction(e -> dialog.close());
        closeButton.setPrefWidth(100);

        Button refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add("refresh-leaderboard-button");
        refreshButton.setPrefWidth(100);
        refreshButton.setOnAction(e -> {
            tableView.setItems(null);
            mainLayout.setCenter(loadingBox);
            loadLeaderboardData(tableView, mainLayout, loadingBox);
        });

        HBox buttonBox = new HBox(15, refreshButton, closeButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        
        VBox contentBox = new VBox(15, titleLabel, tableView, buttonBox);
        contentBox.setPadding(new Insets(10));
        
        mainLayout.setTop(titleLabel);
        
        Scene scene = new Scene(mainLayout);
        String cssPath = LeaderboardDialog.class.getResource("/org/main/quiz/styles.css").toExternalForm();
        scene.getStylesheets().add(cssPath);
        
        dialog.setScene(scene);
        
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                dialog.close();
            }
        });
        
        dialog.show();
        loadLeaderboardData(tableView, mainLayout, loadingBox);
    }
    
    private static void loadLeaderboardData(TableView<LeaderboardEntry> tableView, 
                                           BorderPane mainLayout,
                                           VBox loadingBox) {
        new Thread(() -> {
            try {
                List<LeaderboardEntry> entries = Firebase.getLeaderboardEntries();
                
                Platform.runLater(() -> {
                    if (entries.isEmpty()) {
                        Label noDataLabel = new Label("No leaderboard entries found. Be the first to complete a quiz!");
                        noDataLabel.getStyleClass().add("no-data-label");
                        noDataLabel.setWrapText(true);
                        
                        VBox noDataBox = new VBox(15, noDataLabel);
                        noDataBox.setAlignment(Pos.CENTER);
                        
                        mainLayout.setCenter(noDataBox);
                    } else {
                        tableView.getItems().clear();
                        tableView.getItems().addAll(entries);
                        mainLayout.setCenter(tableView);
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    Label errorLabel = new Label("Failed to load leaderboard data: " + e.getMessage());
                    errorLabel.getStyleClass().add("error-label");
                    errorLabel.setWrapText(true);
                    
                    Button retryButton = new Button("Retry");
                    retryButton.getStyleClass().add("retry-button");
                    retryButton.setOnAction(event -> loadLeaderboardData(tableView, mainLayout, loadingBox));
                    
                    VBox errorBox = new VBox(15, errorLabel, retryButton);
                    errorBox.setAlignment(Pos.CENTER);
                    
                    mainLayout.setCenter(errorBox);
                });
                e.printStackTrace();
            }
        }).start();
    }
}

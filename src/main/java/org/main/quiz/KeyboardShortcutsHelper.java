package org.main.quiz;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class KeyboardShortcutsHelper {

    public static void showKeyboardShortcutsDialog(Stage parentStage) {
        showKeyboardShortcutsDialog(parentStage, null);
    }

    public static void showKeyboardShortcutsDialog(Stage parentStage, Runnable onCloseCallback) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("Keyboard Shortcuts");
        dialog.setResizable(false);
        
        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label("Quiz Master Keyboard Shortcuts");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        GridPane shortcutsGrid = new GridPane();
        shortcutsGrid.setHgap(15);
        shortcutsGrid.setVgap(8);
        shortcutsGrid.setPadding(new Insets(10));
        
        // Start Screen shortcuts
        addCategoryHeader(shortcutsGrid, "Start Screen", 0);
        addShortcut(shortcutsGrid, "S", "Start Quiz", 1);
        addShortcut(shortcutsGrid, "I", "Show Instructions", 2);
        
        // Quiz Screen shortcuts
        addCategoryHeader(shortcutsGrid, "Quiz Screen", 3);
        addShortcut(shortcutsGrid, "1-4", "Select answer option", 4);
        addShortcut(shortcutsGrid, "Enter / Right Arrow", "Next question", 5);
        addShortcut(shortcutsGrid, "Backspace / Left Arrow", "Previous question", 6);
        addShortcut(shortcutsGrid, "F", "50-50 Lifeline", 7);
        addShortcut(shortcutsGrid, "A", "Ask Computer Lifeline", 8);
        addShortcut(shortcutsGrid, "S", "Submit Quiz", 9);
        addShortcut(shortcutsGrid, "P", "Pause game", 10);
        
        // Results Screen shortcuts
        addCategoryHeader(shortcutsGrid, "Results Screen", 11);
        addShortcut(shortcutsGrid, "N", "New Quiz", 12);
        
        // Global shortcuts
        addCategoryHeader(shortcutsGrid, "Global Shortcuts", 13);
        addShortcut(shortcutsGrid, "Esc", "Close dialog / Pause / Resume", 14);
        addShortcut(shortcutsGrid, "Ctrl+E", "Exit game", 15);
        addShortcut(shortcutsGrid, "H", "Show this help", 16);
        
        ScrollPane scrollPane = new ScrollPane(shortcutsGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> {
            dialog.close();
            if (onCloseCallback != null) {
                onCloseCallback.run();
            }
        });
        closeButton.setPrefWidth(100);
        
        mainContainer.getChildren().addAll(titleLabel, scrollPane, closeButton);
        
        Scene scene = new Scene(mainContainer);
        
        // Add event handler for Escape key to close the dialog
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                dialog.close();
                if (onCloseCallback != null) {
                    onCloseCallback.run();
                }
                event.consume();
            }
        });
        
        dialog.setOnCloseRequest(event -> {
            if (onCloseCallback != null) {
                onCloseCallback.run();
            }
        });
        
        dialog.setScene(scene);
        dialog.show();
    }
    
    private static void addCategoryHeader(GridPane grid, String category, int row) {
        Label headerLabel = new Label(category);
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        grid.add(headerLabel, 0, row, 2, 1);
    }
    
    private static void addShortcut(GridPane grid, String key, String description, int row) {
        HBox keyBox = new HBox(5);
        keyBox.setAlignment(Pos.CENTER_LEFT);
        
        Label keyLabel = new Label(key);
        keyLabel.getStyleClass().add("keyboard-key");
        keyLabel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; " +
                "-fx-border-width: 1px; -fx-border-radius: 3px; -fx-padding: 2px 6px; " +
                "-fx-font-family: 'Courier New', monospace; -fx-font-weight: bold;");
        
        keyBox.getChildren().add(keyLabel);
        
        Label descLabel = new Label(description);
        
        grid.add(keyBox, 0, row);
        grid.add(descLabel, 1, row);
    }
}

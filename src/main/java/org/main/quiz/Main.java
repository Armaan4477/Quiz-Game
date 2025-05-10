package org.main.quiz;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            System.out.println("Starting Quiz Master application...");
            URL fxmlUrl = getClass().getResource("/org/main/quiz/quiz.fxml");
            if (fxmlUrl == null) {
                System.err.println("Cannot find FXML resource. Check your build path and file names.");
                throw new IOException("FXML resource not found");
            }
            
            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root, 800, 600);
            
            URL cssUrl = getClass().getResource("/org/main/quiz/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("CSS stylesheet loaded successfully");
            } else {
                System.err.println("Warning: CSS stylesheet not found");
            }
            
            // Configure the primary stage
            primaryStage.setTitle("Quiz Master");
            primaryStage.setScene(scene);
            
            // Force the stage to be in front on macOS
            primaryStage.setAlwaysOnTop(true);
            primaryStage.show();
            
            // Then set it back to normal behavior after it's shown
            Platform.runLater(() -> {
                primaryStage.setAlwaysOnTop(false);
            });
            
            System.out.println("Application window should now be visible");
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

package org.main.quiz;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminLoginDialog {

    public interface AuthenticationCallback {
        void onSuccess();
        void onFailure(String message);
    }

    public static void show(Stage parentStage, AuthenticationCallback callback) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(parentStage);
        dialog.setTitle("Admin Login");
        dialog.setResizable(false);
        
        VBox mainContainer = new VBox(15);
        mainContainer.getStyleClass().add("admin-login-container");
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setMaxWidth(400);
        
        Label titleLabel = new Label("Admin Login");
        titleLabel.getStyleClass().add("admin-login-title");
        
        GridPane loginForm = new GridPane();
        loginForm.setHgap(10);
        loginForm.setVgap(15);
        loginForm.setPadding(new Insets(10, 0, 20, 0));
        
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter admin username");
        
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter admin password");
        
        loginForm.add(usernameLabel, 0, 0);
        loginForm.add(usernameField, 1, 0);
        loginForm.add(passwordLabel, 0, 1);
        loginForm.add(passwordField, 1, 1);
        
        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("login-error");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        
        Button loginButton = new Button("Login");
        loginButton.setPrefWidth(100);
        loginButton.getStyleClass().add("add-button");
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(100);
        cancelButton.getStyleClass().add("delete-button");
        cancelButton.setOnAction(e -> dialog.close());
        
        HBox buttonBox = new HBox(15, loginButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);
        
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            
            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Username and password are required");
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
                return;
            }
            
            try {
                boolean isAuthenticated = Firebase.authenticateAdmin(username, password);
                
                if (isAuthenticated) {
                    dialog.close();
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    errorLabel.setText("Invalid username or password");
                    errorLabel.setVisible(true);
                    errorLabel.setManaged(true);
                }
            } catch (IOException ex) {
                errorLabel.setText("Authentication error: " + ex.getMessage());
                errorLabel.setVisible(true);
                errorLabel.setManaged(true);
            }
        });
        
        mainContainer.getChildren().addAll(titleLabel, loginForm, errorLabel, buttonBox);
        
        Scene scene = new Scene(mainContainer);
        String cssPath = AdminLoginDialog.class.getResource("/org/main/quiz/styles.css").toExternalForm();
        scene.getStylesheets().add(cssPath);
        
        dialog.setScene(scene);
        dialog.show();
    }
}

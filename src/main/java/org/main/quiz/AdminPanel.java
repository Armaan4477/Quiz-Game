package org.main.quiz;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminPanel {

    private TableView<Question> questionTable;
    private ObservableList<Question> questions = FXCollections.observableArrayList();

    private TextField idField;
    private TextField textField;
    private TextField categoryField;
    private ComboBox<Integer> difficultyField;
    private List<TextField> optionFields;
    private ToggleGroup correctOptionGroup;
    private List<RadioButton> correctOptionButtons;

    public void show(Stage parentStage) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(parentStage);
        stage.setTitle("Admin Panel - Quiz Master");
        stage.setMinWidth(800);
        stage.setMinHeight(600);

        BorderPane mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("admin-panel");
        mainLayout.setPadding(new Insets(15));

        Label titleLabel = new Label("Quiz Master Admin Panel");
        titleLabel.getStyleClass().add("admin-panel-title");
        mainLayout.setTop(titleLabel);

        VBox contentBox = new VBox(15);
        
        setupQuestionsTable();
        VBox tableContainer = new VBox(5);
        Label questionsTitle = new Label("Questions");
        questionsTitle.getStyleClass().add("admin-section-title");
        tableContainer.getChildren().addAll(questionsTitle, questionTable);
        VBox.setVgrow(questionTable, Priority.ALWAYS);
        
        VBox formContainer = createQuestionForm();
        
        contentBox.getChildren().addAll(tableContainer, formContainer);
        mainLayout.setCenter(contentBox);
        
        loadQuestions();
        
        Scene scene = new Scene(mainLayout);
        String cssPath = AdminPanel.class.getResource("/org/main/quiz/styles.css").toExternalForm();
        scene.getStylesheets().add(cssPath);
        
        stage.setScene(scene);
        stage.show();
    }

    @SuppressWarnings("unchecked")
    private void setupQuestionsTable() {
        questionTable = new TableView<>();
        questionTable.getStyleClass().add("questions-table");
        
        TableColumn<Question, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        
        TableColumn<Question, String> textCol = new TableColumn<>("Question");
        textCol.setCellValueFactory(new PropertyValueFactory<>("text"));
        textCol.setPrefWidth(300);
        
        TableColumn<Question, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(100);
        
        TableColumn<Question, Integer> difficultyCol = new TableColumn<>("Difficulty");
        difficultyCol.setCellValueFactory(new PropertyValueFactory<>("difficultyLevel"));
        difficultyCol.setPrefWidth(80);
        
        TableColumn<Question, String> correctAnswerCol = new TableColumn<>("Correct Answer");
        correctAnswerCol.setCellValueFactory(cellData -> {
            Question question = cellData.getValue();
            return new SimpleStringProperty(question.getCorrectAnswer());
        });
        correctAnswerCol.setPrefWidth(150);
        
        questionTable.getColumns().addAll(idCol, textCol, categoryCol, difficultyCol, correctAnswerCol);
        questionTable.setItems(questions);
        
        questionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });
        
        questionTable.setPrefHeight(300);
    }

    private VBox createQuestionForm() {
        VBox container = new VBox(15);
        container.getStyleClass().add("form-container");
        
        Label formTitle = new Label("Add/Edit Question");
        formTitle.getStyleClass().add("admin-section-title");
        
        idField = new TextField();
        idField.setVisible(false);
        idField.setManaged(false);
        
        VBox textContainer = new VBox(5);
        textContainer.getStyleClass().add("form-field");
        Label textLabel = new Label("Question Text:");
        textLabel.getStyleClass().add("form-field-label");
        textField = new TextField();
        textField.setPromptText("Enter the question text");
        textContainer.getChildren().addAll(textLabel, textField);
        
        VBox categoryContainer = new VBox(5);
        categoryContainer.getStyleClass().add("form-field");
        Label categoryLabel = new Label("Category:");
        categoryLabel.getStyleClass().add("form-field-label");
        categoryField = new TextField();
        categoryField.setPromptText("Enter category");
        categoryContainer.getChildren().addAll(categoryLabel, categoryField);
        
        VBox difficultyContainer = new VBox(5);
        difficultyContainer.getStyleClass().add("form-field");
        Label difficultyLabel = new Label("Difficulty Level:");
        difficultyLabel.getStyleClass().add("form-field-label");
        difficultyField = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        difficultyField.setValue(1);
        difficultyContainer.getChildren().addAll(difficultyLabel, difficultyField);
        
        VBox optionsContainer = new VBox(10);
        optionsContainer.getStyleClass().add("options-container");
        Label optionsLabel = new Label("Options (select the correct one):");
        optionsLabel.getStyleClass().add("form-field-label");
        
        optionFields = new ArrayList<>(4);
        correctOptionGroup = new ToggleGroup();
        correctOptionButtons = new ArrayList<>(4);
        
        VBox optionEntriesContainer = new VBox(8);
        
        for (int i = 0; i < 4; i++) {
            HBox optionRow = new HBox(10);
            optionRow.getStyleClass().add("option-row");
            
            RadioButton radioButton = new RadioButton();
            radioButton.setToggleGroup(correctOptionGroup);
            radioButton.setUserData(i);
            
            TextField optionField = new TextField();
            optionField.setPromptText("Option " + (i + 1));
            optionField.setPrefWidth(400);
            
            optionFields.add(optionField);
            correctOptionButtons.add(radioButton);
            
            optionRow.getChildren().addAll(radioButton, optionField);
            optionEntriesContainer.getChildren().add(optionRow);
        }
        
        optionsContainer.getChildren().addAll(optionsLabel, optionEntriesContainer);
        
        HBox buttonContainer = new HBox(10);
        buttonContainer.getStyleClass().add("admin-button-container");
        buttonContainer.setAlignment(Pos.CENTER);
        
        Button addButton = new Button("Add");
        addButton.getStyleClass().addAll("add-button");
        addButton.setOnAction(e -> handleAddQuestion());
        
        Button updateButton = new Button("Update");
        updateButton.getStyleClass().addAll("update-button");
        updateButton.setOnAction(e -> handleUpdateQuestion());
        
        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().addAll("delete-button");
        deleteButton.setOnAction(e -> handleDeleteQuestion());
        
        Button clearButton = new Button("Clear");
        clearButton.getStyleClass().addAll("clear-button");
        clearButton.setOnAction(e -> clearForm());
        
        buttonContainer.getChildren().addAll(addButton, updateButton, deleteButton, clearButton);
        
        container.getChildren().addAll(formTitle, idField, textContainer, categoryContainer, 
                                      difficultyContainer, optionsContainer, buttonContainer);
        
        return container;
    }

    private void loadQuestions() {
        try {
            List<Question> loadedQuestions = Firebase.getAllQuestions();
            questions.clear();
            questions.addAll(loadedQuestions);
        } catch (IOException e) {
            showAlert("Error", "Failed to load questions: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void populateForm(Question question) {
        idField.setText(question.getId());
        textField.setText(question.getText());
        categoryField.setText(question.getCategory());
        difficultyField.setValue(question.getDifficultyLevel());
        
        List<String> options = question.getOptions();
        int correctIndex = question.getCorrectOptionIndex();
        
        for (int i = 0; i < optionFields.size(); i++) {
            if (i < options.size()) {
                optionFields.get(i).setText(options.get(i));
            } else {
                optionFields.get(i).setText("");
            }
            
            if (i == correctIndex) {
                correctOptionButtons.get(i).setSelected(true);
            }
        }
    }

    private void clearForm() {
        idField.setText("");
        textField.setText("");
        categoryField.setText("");
        difficultyField.setValue(1);
        
        for (TextField field : optionFields) {
            field.setText("");
        }
        
        if (correctOptionGroup.getSelectedToggle() != null) {
            correctOptionGroup.getSelectedToggle().setSelected(false);
        }
        
        questionTable.getSelectionModel().clearSelection();
    }

    private void handleAddQuestion() {
        if (!validateForm()) {
            showAlert("Validation Error", "Please fill all fields and select a correct answer.", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            List<Question> latestQuestions = Firebase.getAllQuestions();
            Question newQuestion = createQuestionFromForm();
            String generatedId = generateSequentialQuestionId(latestQuestions);
            newQuestion.setId(generatedId);
            String response = Firebase.pushQuestion(newQuestion);
            showAlert("Success", "Question added successfully!", Alert.AlertType.INFORMATION);
            clearForm();
            loadQuestions();
        } catch (IOException e) {
            showAlert("Error", "Failed to add question: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Generates a sequential question ID in the format "question{number}"
     * @param questionList The list of questions to check for the highest number
     * @return The next available sequential question ID
     */
    private String generateSequentialQuestionId(List<Question> questionList) {
        int maxQuestionNumber = getHighestQuestionNumber(questionList);
        return String.format("question%d", maxQuestionNumber + 1);
    }

    /**
     * Finds the highest question number from existing question IDs
     * @param questionList The list of questions to check
     * @return The highest question number found, or 0 if none found
     */
    private int getHighestQuestionNumber(List<Question> questionList) {
        int maxNumber = 0;
        for (Question question : questionList) {
            String id = question.getId();
            if (id != null && id.startsWith("question")) {
                try {
                    String numberPart = id.substring("question".length());
                    int questionNumber = Integer.parseInt(numberPart);
                    if (questionNumber > maxNumber) {
                        maxNumber = questionNumber;
                    }
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                }
            }
        }
        return maxNumber;
    }

    private void handleUpdateQuestion() {
        if (idField.getText().isEmpty()) {
            showAlert("Selection Required", "Please select a question to update.", Alert.AlertType.WARNING);
            return;
        }
        
        if (!validateForm()) {
            showAlert("Validation Error", "Please fill all fields and select a correct answer.", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            Question updatedQuestion = createQuestionFromForm();
            Firebase.updateQuestion(updatedQuestion);
            showAlert("Success", "Question updated successfully!", Alert.AlertType.INFORMATION);
            clearForm();
            loadQuestions();
        } catch (IOException e) {
            showAlert("Error", "Failed to update question: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleDeleteQuestion() {
        if (idField.getText().isEmpty()) {
            showAlert("Selection Required", "Please select a question to delete.", Alert.AlertType.WARNING);
            return;
        }
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Question");
        confirmDialog.setContentText("Are you sure you want to delete this question?");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Firebase.deleteQuestion(idField.getText());
                showAlert("Success", "Question deleted successfully!", Alert.AlertType.INFORMATION);
                clearForm();
                loadQuestions();
            } catch (IOException e) {
                showAlert("Error", "Failed to delete question: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private boolean validateForm() {
        if (textField.getText().trim().isEmpty() || 
            categoryField.getText().trim().isEmpty() || 
            difficultyField.getValue() == null) {
            return false;
        }
        
        if (correctOptionGroup.getSelectedToggle() == null) {
            return false;
        }
        
        for (TextField field : optionFields) {
            if (field.getText().trim().isEmpty()) {
                return false;
            }
        }
        
        return true;
    }

    private Question createQuestionFromForm() {
        String id = idField.getText().trim();
        String text = textField.getText().trim();
        String category = categoryField.getText().trim();
        int difficulty = difficultyField.getValue();
        
        List<String> options = new ArrayList<>();
        for (TextField field : optionFields) {
            options.add(field.getText().trim());
        }
        
        int correctOptionIndex = (int) correctOptionGroup.getSelectedToggle().getUserData();
        
        return new Question(id, text, options, correctOptionIndex, category, difficulty);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

package org.main.quiz;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.*;
import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.application.Platform;

public class Controller {
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private Button startButton;
    @FXML private Button submitButton;
    @FXML private Button newQuizButton;
    
    @FXML private Label scoreLabel;
    @FXML private Label timeLabel;
    @FXML private Label questionNumberLabel;
    @FXML private Label questionTextLabel;
    @FXML private Label feedbackLabel;
    @FXML private Label finalScoreLabel;
    @FXML private Label timeSpentLabel;
    
    @FXML private VBox welcomeScreen;
    @FXML private VBox questionScreen;
    @FXML private VBox resultsScreen;
    @FXML private VBox optionsBox;
    @FXML private StackPane contentPane;
    @FXML private ImageView logoImageView;
    
    @FXML private ToggleGroup optionsGroup;
    @FXML private RadioButton option1;
    @FXML private RadioButton option2;
    @FXML private RadioButton option3;
    @FXML private RadioButton option4;

    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int secondsElapsed = 0;
    private Timeline timer;
    private ObservableList<String> categories = FXCollections.observableArrayList();
    private List<RadioButton> optionButtons;

    @FXML
    public void initialize() {
        try {
            System.out.println("Initializing Controller...");
            
            // Initialize quiz UI components
            if (option1 != null && option2 != null && option3 != null && option4 != null) {
                optionButtons = Arrays.asList(option1, option2, option3, option4);
                
                // Ensure the toggle group is properly set
                if (optionsGroup == null) {
                    optionsGroup = new ToggleGroup();
                }
                
                // Explicitly assign toggle group to each radio button
                for (RadioButton button : optionButtons) {
                    button.setToggleGroup(optionsGroup);
                }
            } else {
                System.err.println("Warning: One or more option buttons are null");
            }
            
            // Setup category combo box
            if (categoryComboBox != null) {
                try {
                    System.out.println("Testing Firebase connection...");
                    boolean connected = Firebase.testConnection();
                    System.out.println("Firebase connection test: " + (connected ? "Success" : "Failed"));
                    
                    System.out.println("Loading categories...");
                    loadCategories();
                    System.out.println("Categories loaded successfully");
                } catch (Exception e) {
                    System.err.println("Failed to load categories: " + e.getMessage());
                    e.printStackTrace();
                    
                    // Provide default category for offline testing
                    categories = FXCollections.observableArrayList("General", "Science", "History");
                    categoryComboBox.setItems(categories);
                    categoryComboBox.getSelectionModel().selectFirst();
                }
            } else {
                System.err.println("Warning: Category ComboBox is null");
            }
            
            // Setup timer
            timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                secondsElapsed++;
                updateTimeLabel();
            }));
            timer.setCycleCount(Timeline.INDEFINITE);
            
            // Initially show welcome screen
            if (welcomeScreen != null) welcomeScreen.setVisible(true);
            if (questionScreen != null) questionScreen.setVisible(false);
            if (resultsScreen != null) resultsScreen.setVisible(false);
            
            System.out.println("Controller initialization complete");
        } catch (Exception e) {
            System.err.println("Error during controller initialization: " + e.getMessage());
            e.printStackTrace();
            showAlert("Initialization Error", "There was a problem initializing the application: " + e.getMessage());
        }
    }

    private void loadCategories() throws IOException {
        if (categoryComboBox == null) {
            return;
        }
        
        // Get all questions to extract categories
        List<Question> allQuestions = Firebase.getAllQuestions();
        Set<String> uniqueCategories = new HashSet<>();
        
        for (Question q : allQuestions) {
            uniqueCategories.add(q.getCategory());
        }
        
        categories.clear();
        categories.addAll(uniqueCategories);
        if (categories.isEmpty()) {
            categories.add("General");
        }
        
        categoryComboBox.setItems(categories);
        categoryComboBox.getSelectionModel().selectFirst();
    }
    
    @FXML
    private void handleStartQuiz() {
        if (categoryComboBox == null) {
            showAlert("Error", "Category selection not available");
            return;
        }
        
        String selectedCategory = categoryComboBox.getValue();
        
        try {
            // Load questions for selected category
            if (selectedCategory != null && !selectedCategory.isEmpty()) {
                questions = Firebase.getQuestionsByCategory(selectedCategory);
            } else {
                questions = Firebase.getAllQuestions();
            }
            
            if (questions.isEmpty()) {
                showAlert("No Questions", "No questions available for this category.");
                return;
            }
            
            // Reset quiz state
            currentQuestionIndex = 0;
            score = 0;
            secondsElapsed = 0;
            
            // Update UI
            if (scoreLabel != null) {
                scoreLabel.setText("Score: 0/" + questions.size());
            }
            updateTimeLabel();
            
            // Start timer
            timer.playFromStart();
            
            // Show first question
            showCurrentQuestion();
            
            // Switch to question screen
            if (welcomeScreen != null) welcomeScreen.setVisible(false);
            if (questionScreen != null) questionScreen.setVisible(true);
            if (resultsScreen != null) resultsScreen.setVisible(false);
            
        } catch (Exception e) {
            // Enhanced error handling with detailed message
            e.printStackTrace();
            showAlert("Error", "Failed to load questions: " + e.getClass().getName() + 
                     "\nDetails: " + e.getMessage());
        }
    }
    
    private void showCurrentQuestion() {
        if (currentQuestionIndex < 0 || currentQuestionIndex >= questions.size() || 
            questionNumberLabel == null || questionTextLabel == null || 
            optionButtons == null || optionButtons.isEmpty()) {
            return;
        }
        
        // Reset UI state - clear selection without triggering events
        if (optionsGroup != null) {
            optionsGroup.selectToggle(null);
        }
        
        if (feedbackLabel != null) {
            feedbackLabel.setText("");
            feedbackLabel.getStyleClass().removeAll("correct", "incorrect");
        }
        
        // Get current question
        Question question = questions.get(currentQuestionIndex);
        
        // Update UI
        questionNumberLabel.setText("Question " + (currentQuestionIndex + 1) + "/" + questions.size());
        questionTextLabel.setText(question.getText());
        
        // Set options
        List<String> options = question.getOptions();
        for (int i = 0; i < optionButtons.size(); i++) {
            RadioButton button = optionButtons.get(i);
            if (i < options.size()) {
                button.setText(options.get(i));
                button.setVisible(true);
                button.setUserData(Integer.valueOf(i)); // Store option index as userData
                button.setSelected(false); // Ensure it's not selected by default
            } else {
                button.setVisible(false);
            }
        }
    }
    
    @FXML
    private void handleSubmitAnswer() {
        if (optionsGroup == null || feedbackLabel == null) {
            showAlert("Error", "Option group not initialized properly");
            return;
        }
        
        Toggle selectedToggle = optionsGroup.getSelectedToggle();
        
        if (selectedToggle == null) {
            showAlert("No Selection", "Please select an answer before submitting.");
            return;
        }
        
        // Get the selected option index from the userData
        Object userData = selectedToggle.getUserData();
        if (userData == null) {
            showAlert("Error", "Selected option data not found");
            return;
        }
        
        int selectedIndex = (int) userData;
        Question currentQuestion = questions.get(currentQuestionIndex);
        boolean isCorrect = currentQuestion.isCorrectAnswer(selectedIndex);
        
        // Update feedback and score
        if (isCorrect) {
            score++;
            feedbackLabel.setText("Correct!");
            feedbackLabel.getStyleClass().removeAll("incorrect");
            feedbackLabel.getStyleClass().add("correct");
        } else {
            feedbackLabel.setText("Incorrect! The correct answer is: " + 
                                currentQuestion.getCorrectAnswer());
            feedbackLabel.getStyleClass().removeAll("correct");
            feedbackLabel.getStyleClass().add("incorrect");
        }
        
        // Update score
        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + score + "/" + questions.size());
        }
        
        // Disable option selection after answering
        for (RadioButton button : optionButtons) {
            button.setDisable(true);
        }
        
        // Enable options again and move to next question after a delay
        Timer delayTimer = new Timer();
        delayTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    // Re-enable options for the next question
                    for (RadioButton button : optionButtons) {
                        button.setDisable(false);
                    }
                    
                    currentQuestionIndex++;
                    if (currentQuestionIndex < questions.size()) {
                        showCurrentQuestion();
                    } else {
                        endQuiz();
                    }
                });
            }
        }, 2000); // 2 second delay
    }
    
    private void endQuiz() {
        // Stop timer
        timer.stop();
        
        // Update results screen
        if (finalScoreLabel != null) {
            finalScoreLabel.setText("Your score: " + score + "/" + questions.size());
        }
        
        if (timeSpentLabel != null) {
            timeSpentLabel.setText("Time spent: " + formatTime(secondsElapsed));
        }
        
        // Switch to results screen
        if (welcomeScreen != null) welcomeScreen.setVisible(false);
        if (questionScreen != null) questionScreen.setVisible(false);
        if (resultsScreen != null) resultsScreen.setVisible(true);
    }
    
    @FXML
    private void handleNewQuiz() {
        // Reset and go back to welcome screen
        if (welcomeScreen != null) welcomeScreen.setVisible(true);
        if (questionScreen != null) questionScreen.setVisible(false);
        if (resultsScreen != null) resultsScreen.setVisible(false);
    }
    
    private void updateTimeLabel() {
        if (timeLabel != null) {
            timeLabel.setText("Time: " + formatTime(secondsElapsed));
        }
    }
    
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

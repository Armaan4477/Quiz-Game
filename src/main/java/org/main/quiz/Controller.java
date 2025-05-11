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
import javafx.scene.paint.Color;

public class Controller {
    @FXML private TextField playerNameField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private Button startButton;
    @FXML private Button submitButton;
    @FXML private Button newQuizButton;
    @FXML private Button nextButton;  // Add reference to the next button
    
    // Add lifeline buttons and previous button
    @FXML private Button fiftyFiftyButton;
    @FXML private Button askComputerButton;
    @FXML private Button previousButton; // Add reference to the previous button
    
    @FXML private Label playerDisplayLabel;
    @FXML private Label playerResultLabel;
    @FXML private Label scoreLabel;
    @FXML private Label timeLabel;
    @FXML private Label questionNumberLabel;
    @FXML private Label questionTextLabel;
    @FXML private Label feedbackLabel;
    @FXML private Label finalScoreLabel;
    @FXML private Label timeSpentLabel;
    
    @FXML private VBox startScreen;  // Renamed from welcomeScreen to startScreen
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
    private int secondsElapsed = 0;  // Keep this for total quiz time tracking
    private Timeline timer;  // Overall timer
    private ObservableList<String> categories = FXCollections.observableArrayList();
    private List<RadioButton> optionButtons;
    private String playerName;
    
    // Simplified timer fields
    private static final int QUESTION_TIME_LIMIT = 20; // 20 seconds per question
    private Timeline questionTimer; // Timer for the current question
    private int currentQuestionTimeRemaining; // Time remaining for current question
    
    // New fields for lifelines
    private boolean fiftyFiftyUsed = false;
    private boolean askComputerUsed = false;
    
    // New field to track user answers
    private String[] userAnswers;    // Array to store user answers for each question
    private boolean[] questionAnswered;  // Track which questions have been answered
    private boolean[] questionLocked;    // Track which questions are locked due to timeout

    // New fields for per-question state
    private int[] questionTimeRemaining; // Time left for each question
    private List<Integer>[] fiftyFiftyRemovedOptions; // Removed option indices for each question

    @FXML
    public void initialize() {
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
        }
        
        // Setup category combo box
        if (categoryComboBox != null) {
            try {
                loadCategories();
            } catch (IOException e) {
                showAlert("Error", "Failed to load categories: " + e.getMessage());
            }
        }
        
        // Setup silent timer for tracking total quiz time
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsElapsed++;
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        
        // Setup per-question timer
        questionTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            currentQuestionTimeRemaining--;
            updateQuestionTimerDisplay();

            // Save the remaining time for the current question
            if (questionTimeRemaining != null && currentQuestionIndex >= 0 && currentQuestionIndex < questionTimeRemaining.length) {
                questionTimeRemaining[currentQuestionIndex] = currentQuestionTimeRemaining;
            }

            // Check for time running out
            if (currentQuestionTimeRemaining <= 0) {
                handleQuestionTimeout();
            }
        }));
        questionTimer.setCycleCount(Timeline.INDEFINITE);
        
        // Initially show start screen
        if (startScreen != null) startScreen.setVisible(true);
        if (questionScreen != null) questionScreen.setVisible(false);
        if (resultsScreen != null) resultsScreen.setVisible(false);

        // Ensure startScreen is brought to front and visible
        if (contentPane != null && startScreen != null) {
            if (!contentPane.getChildren().contains(startScreen)) {
                contentPane.getChildren().add(startScreen);
            }
            startScreen.toFront();
            startScreen.setVisible(true);
        }
        
        // Initially disable previous button since we start at first question
        if (previousButton != null) {
            previousButton.setDisable(true);
        }
    }

    // Update the question timer display with visual feedback
    private void updateQuestionTimerDisplay() {
        if (timeLabel == null) return;
        // Prevent negative display
        int displayTime = Math.max(0, currentQuestionTimeRemaining);
        timeLabel.setText("Time: " + displayTime);

        // Visual feedback when time is running low
        if (displayTime <= 5) {
            if (!timeLabel.getStyleClass().contains("warning")) {
                timeLabel.getStyleClass().add("warning");
            }
        } else {
            timeLabel.getStyleClass().removeAll("warning");
        }
    }

    // Handle question timeout
    private void handleQuestionTimeout() {
        // Stop the timer
        questionTimer.stop();

        // Prevent timer from going negative
        currentQuestionTimeRemaining = 0;
        updateQuestionTimerDisplay();

        // Save that this question timed out
        if (questionTimeRemaining != null && currentQuestionIndex >= 0 && currentQuestionIndex < questionTimeRemaining.length) {
            questionTimeRemaining[currentQuestionIndex] = 0;
        }

        // Mark the question as locked
        if (questionLocked != null && currentQuestionIndex >= 0 && currentQuestionIndex < questionLocked.length) {
            questionLocked[currentQuestionIndex] = true;
        }

        // Disable all radio buttons for this question
        if (optionButtons != null) {
            for (RadioButton btn : optionButtons) {
                btn.setDisable(true);
            }
        }

        // Show timeout alert
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Time's Up");
            alert.setHeaderText(null);
            alert.setContentText("You ran out of time for this question! You cannot change your answer for this question anymore.");
            alert.showAndWait();
        });
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
        // Validate player name
        if (playerNameField == null || playerNameField.getText().trim().isEmpty()) {
            showAlert("Missing Information", "Please enter your name before starting the quiz.");
            return;
        }
        
        // Store player name
        playerName = playerNameField.getText().trim();
        
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
            
            // Initialize arrays to track user answers and question completion
            userAnswers = new String[questions.size()];
            questionAnswered = new boolean[questions.size()];
            questionLocked = new boolean[questions.size()]; // Initialize the lock tracking array

            // Initialize per-question time and 50-50 removed options
            questionTimeRemaining = new int[questions.size()];
            fiftyFiftyRemovedOptions = new List[questions.size()];
            for (int i = 0; i < questions.size(); i++) {
                questionTimeRemaining[i] = QUESTION_TIME_LIMIT;
                fiftyFiftyRemovedOptions[i] = null;
                questionLocked[i] = false; // Initialize all questions as unlocked
            }
            
            // Reset lifeline states
            fiftyFiftyUsed = false;
            askComputerUsed = false;
            
            // Disable previous button at the start of quiz
            if (previousButton != null) {
                previousButton.setDisable(true);
            }
            
            // Re-enable lifeline buttons
            if (fiftyFiftyButton != null) fiftyFiftyButton.setDisable(false);
            if (askComputerButton != null) askComputerButton.setDisable(false);
            
            // Reset current question timer
            currentQuestionTimeRemaining = QUESTION_TIME_LIMIT;
            
            // Update UI
            if (scoreLabel != null) {
                scoreLabel.setText("Score: 0/" + questions.size());
            }
            
            // Update player display name in game screen
            if (playerDisplayLabel != null) {
                playerDisplayLabel.setText("Player: " + playerName);
            }
            
            // Start overall timer
            timer.playFromStart();
            
            // Show first question
            showCurrentQuestion();
            
            // Switch to question screen
            if (startScreen != null) startScreen.setVisible(false);
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
        
        // Important: Always stop the timer before changing questions
        questionTimer.stop();
        
        // Enable/disable navigation buttons based on current question index
        if (previousButton != null) {
            previousButton.setDisable(currentQuestionIndex == 0);
        }
        if (nextButton != null) {
            nextButton.setDisable(currentQuestionIndex == questions.size() - 1);
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
        
        // Check if this question is locked due to previous timeout
        boolean isLocked = false;
        if (questionLocked != null && currentQuestionIndex >= 0 && currentQuestionIndex < questionLocked.length) {
            isLocked = questionLocked[currentQuestionIndex];
        }
        
        // Restore per-question timer - use the saved time remaining for this question
        if (questionTimeRemaining != null && currentQuestionIndex >= 0 && 
            currentQuestionIndex < questionTimeRemaining.length) {
            currentQuestionTimeRemaining = questionTimeRemaining[currentQuestionIndex];
            
            // If question is locked, ensure timer shows 0
            if (isLocked) {
                currentQuestionTimeRemaining = 0;
            }
        } else {
            // Fallback to default time if array not initialized
            currentQuestionTimeRemaining = isLocked ? 0 : QUESTION_TIME_LIMIT;
        }
        updateQuestionTimerDisplay();
        
        // Set options
        List<String> options = question.getOptions();
        for (int i = 0; i < optionButtons.size(); i++) {
            RadioButton button = optionButtons.get(i);
            if (i < options.size()) {
                button.setText(options.get(i));
                button.setVisible(true);
                button.setUserData(Integer.valueOf(i)); // Store option index as userData
                button.setSelected(false); // Ensure it's not selected by default
                
                // Enable/disable based on timer for this question or if it's locked
                button.setDisable(currentQuestionTimeRemaining == 0 || isLocked);
            } else {
                button.setVisible(false);
            }
        }
        
        // If this question was previously answered, restore the user's answer
        if (userAnswers[currentQuestionIndex] != null) {
            for (int i = 0; i < options.size(); i++) {
                if (options.get(i).equals(userAnswers[currentQuestionIndex])) {
                    optionsGroup.selectToggle(optionButtons.get(i));
                    break;
                }
            }
        }
        
        // Restore 50-50 state if it was used on this question
        if (fiftyFiftyRemovedOptions != null && 
            currentQuestionIndex < fiftyFiftyRemovedOptions.length && 
            fiftyFiftyRemovedOptions[currentQuestionIndex] != null) {
            
            List<Integer> removedIndices = fiftyFiftyRemovedOptions[currentQuestionIndex];
            // Apply the removed options
            for (Integer index : removedIndices) {
                if (index >= 0 && index < optionButtons.size()) {
                    optionButtons.get(index).setVisible(false);
                }
            }
        }
        
        // Start the question timer only if time remains and question isn't locked
        if (currentQuestionTimeRemaining > 0 && !isLocked) {
            questionTimer.playFromStart();
        }
    }
    
    // Replace handleSubmitAnswer with handleNextQuestion
    @FXML
    private void handleNextQuestion() {
        // First save current question state before moving
        saveCurrentQuestionState();
        
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            showCurrentQuestion();
        }
    }
    
    // Add new method for submitting the entire quiz
    @FXML
    private void handleSubmitQuiz() {
        // Save the current selection before submitting
        saveCurrentSelection();
        
        // Prompt for confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Submit Quiz");
        alert.setHeaderText("Are you sure you want to submit your quiz?");
        alert.setContentText("You won't be able to change your answers after submission.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Grade the quiz
            gradeQuiz();
            
            // End the quiz
            endQuiz();
        }
    }
    
    // New method to grade the quiz and calculate final score
    private void gradeQuiz() {
        score = 0;
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            String userAnswer = userAnswers[i];
            
            if (userAnswer != null && 
                userAnswer.equals(question.getOptions().get(question.getCorrectOptionIndex()))) {
                score++;
            }
        }
        
        // Update score label
        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + score + "/" + questions.size());
        }
    }
    
    // Update navigation to previous question
    @FXML
    private void handlePreviousQuestion() {
        // First save current question state before moving
        saveCurrentQuestionState();
        
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            showCurrentQuestion();
        }
    }
    
    // Save current selection and state for the current question
    private void saveCurrentSelection() {
        if (optionsGroup != null && questions != null && currentQuestionIndex >= 0 
            && currentQuestionIndex < questions.size()) {
            
            Toggle selectedToggle = optionsGroup.getSelectedToggle();
            if (selectedToggle != null) {
                Object userData = selectedToggle.getUserData();
                if (userData != null) {
                    int selectedIndex = (int) userData;
                    if (selectedIndex >= 0 && selectedIndex < questions.get(currentQuestionIndex).getOptions().size()) {
                        userAnswers[currentQuestionIndex] = questions.get(currentQuestionIndex)
                                                           .getOptions().get(selectedIndex);
                    }
                }
            }
        }
    }

    private void saveCurrentQuestionState() {
        // Save current selection first
        saveCurrentSelection();
        
        // Save time remaining for this question
        if (questionTimeRemaining != null && currentQuestionIndex >= 0 
            && currentQuestionIndex < questionTimeRemaining.length) {
            questionTimeRemaining[currentQuestionIndex] = currentQuestionTimeRemaining;
        }
    }
    
    private void endQuiz() {
        // Stop all timers
        timer.stop();
        questionTimer.stop();
        
        // Update results screen with player name
        if (playerResultLabel != null) {
            playerResultLabel.setText("Player: " + playerName);
        }
        
        // Update results score
        if (finalScoreLabel != null) {
            finalScoreLabel.setText("Your score: " + score + "/" + questions.size());
        }
        
        if (timeSpentLabel != null) {
            timeSpentLabel.setText("Time spent: " + formatTime(secondsElapsed));
        }
        
        // Switch to results screen
        if (startScreen != null) startScreen.setVisible(false);
        if (questionScreen != null) questionScreen.setVisible(false);
        if (resultsScreen != null) resultsScreen.setVisible(true);
    }
    
    @FXML
    private void handleNewQuiz() {
        // Reset player name field for a new quiz
        if (playerNameField != null) {
            playerNameField.clear();
        }
        
        // Reset lifeline states
        fiftyFiftyUsed = false;
        askComputerUsed = false;
        
        // Reset and go back to start screen
        if (startScreen != null) startScreen.setVisible(true);
        if (questionScreen != null) questionScreen.setVisible(false);
        if (resultsScreen != null) resultsScreen.setVisible(false);

        // Ensure startScreen is brought to front and visible
        if (contentPane != null && startScreen != null) {
            if (!contentPane.getChildren().contains(startScreen)) {
                contentPane.getChildren().add(startScreen);
            }
            startScreen.toFront();
            startScreen.setVisible(true);
        }
    }
    
    // Add 50-50 lifeline handler
    @FXML
    private void handleFiftyFifty() {
        if (fiftyFiftyUsed || currentQuestionIndex >= questions.size()) {
            return;
        }
        
        // Get current question and correct answer
        Question currentQuestion = questions.get(currentQuestionIndex);
        int correctOptionIndex = currentQuestion.getCorrectOptionIndex();
        
        // Count to track how many incorrect options we've disabled
        List<Integer> incorrectIndices = new ArrayList<>();
        
        // Find all incorrect options
        for (int i = 0; i < optionButtons.size(); i++) {
            if (i != correctOptionIndex) {
                incorrectIndices.add(i);
            }
        }
        
        // Shuffle the incorrect options to randomize which ones are disabled
        Collections.shuffle(incorrectIndices);
        
        // Remove 2 incorrect options
        List<Integer> removedIndices = new ArrayList<>();
        for (int i = 0; i < Math.min(2, incorrectIndices.size()); i++) {
            int indexToHide = incorrectIndices.get(i);
            optionButtons.get(indexToHide).setVisible(false);
            removedIndices.add(indexToHide);
        }

        // Save removed indices for this question - they will remain hidden
        fiftyFiftyRemovedOptions[currentQuestionIndex] = removedIndices;
        
        // Mark the lifeline as used and disable the button
        fiftyFiftyUsed = true;
        fiftyFiftyButton.setDisable(true);
    }
    
    // Add Ask Computer lifeline handler
    @FXML
    private void handleAskComputer() {
        if (askComputerUsed || currentQuestionIndex >= questions.size()) {
            return;
        }
        
        // Get current question
        Question currentQuestion = questions.get(currentQuestionIndex);
        int correctOptionIndex = currentQuestion.getCorrectOptionIndex();
        
        // Generate random number to simulate computer's accuracy (80% chance to be correct)
        Random random = new Random();
        int responsePercentage = random.nextInt(100);
        
        if (responsePercentage < 80) {
            // Computer gives the correct answer
            optionsGroup.selectToggle(optionButtons.get(correctOptionIndex));
        } else {
            // Computer gives a random wrong answer
            List<Integer> incorrectIndices = new ArrayList<>();
            for (int i = 0; i < optionButtons.size(); i++) {
                if (i != correctOptionIndex) {
                    incorrectIndices.add(i);
                }
            }
            
            if (!incorrectIndices.isEmpty()) {
                int randomIncorrectIndex = incorrectIndices.get(random.nextInt(incorrectIndices.size()));
                optionsGroup.selectToggle(optionButtons.get(randomIncorrectIndex));
            }
        }
        
        // Mark the lifeline as used and disable the button
        askComputerUsed = true;
        askComputerButton.setDisable(true);
    }
    
    // We no longer need updateTimeLabel() as we only show the countdown timer
    // Keeping formatTime for the results screen
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

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
    @FXML private TextField playerNameField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private Button startButton;
    @FXML private Button submitButton;
    @FXML private Button newQuizButton;
    @FXML private Button nextButton;
    
    @FXML private Button fiftyFiftyButton;
    @FXML private Button askComputerButton;
    @FXML private Button previousButton;
    
    @FXML private Label playerDisplayLabel;
    @FXML private Label playerResultLabel;
    @FXML private Label scoreLabel;
    @FXML private Label timeLabel;
    @FXML private Label questionNumberLabel;
    @FXML private Label questionTextLabel;
    @FXML private Label feedbackLabel;
    @FXML private Label finalScoreLabel;
    @FXML private Label timeSpentLabel;
    
    @FXML private VBox startScreen;
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
    private String playerName;
    
    private static final int QUESTION_TIME_LIMIT = 20;
    private Timeline questionTimer;
    private int currentQuestionTimeRemaining;
    
    private boolean fiftyFiftyUsed = false;
    private boolean askComputerUsed = false;
    
    private String[] userAnswers;
    private boolean[] questionLocked;

    private int[] questionTimeRemaining;
    private List<Integer>[] fiftyFiftyRemovedOptions;

    @FXML
    public void initialize() {
        if (option1 != null && option2 != null && option3 != null && option4 != null) {
            optionButtons = Arrays.asList(option1, option2, option3, option4);
            
            if (optionsGroup == null) {
                optionsGroup = new ToggleGroup();
            }
            
            for (RadioButton button : optionButtons) {
                button.setToggleGroup(optionsGroup);
            }
        }
        
        if (categoryComboBox != null) {
            try {
                loadCategories();
            } catch (IOException e) {
                showAlert("Error", "Failed to load categories: " + e.getMessage());
            }
        }
        
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsElapsed++;
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        
        questionTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            currentQuestionTimeRemaining--;
            updateQuestionTimerDisplay();

            if (questionTimeRemaining != null && currentQuestionIndex >= 0 && currentQuestionIndex < questionTimeRemaining.length) {
                questionTimeRemaining[currentQuestionIndex] = currentQuestionTimeRemaining;
            }

            if (currentQuestionTimeRemaining <= 0) {
                handleQuestionTimeout();
            }
        }));
        questionTimer.setCycleCount(Timeline.INDEFINITE);

        if (startScreen != null) startScreen.setVisible(true);
        if (questionScreen != null) questionScreen.setVisible(false);
        if (resultsScreen != null) resultsScreen.setVisible(false);

        if (contentPane != null && startScreen != null) {
            if (!contentPane.getChildren().contains(startScreen)) {
                contentPane.getChildren().add(startScreen);
            }
            startScreen.toFront();
            startScreen.setVisible(true);
        }
        
        if (previousButton != null) {
            previousButton.setDisable(true);
        }
    }

    private void updateQuestionTimerDisplay() {
        if (timeLabel == null) return;
        int displayTime = Math.max(0, currentQuestionTimeRemaining);
        timeLabel.setText("Time: " + displayTime);

        if (displayTime <= 5) {
            if (!timeLabel.getStyleClass().contains("warning")) {
                timeLabel.getStyleClass().add("warning");
            }
        } else {
            timeLabel.getStyleClass().removeAll("warning");
        }
    }

    private void handleQuestionTimeout() {
        questionTimer.stop();

        currentQuestionTimeRemaining = 0;
        updateQuestionTimerDisplay();

        if (questionTimeRemaining != null && currentQuestionIndex >= 0 && currentQuestionIndex < questionTimeRemaining.length) {
            questionTimeRemaining[currentQuestionIndex] = 0;
        }

        if (questionLocked != null && currentQuestionIndex >= 0 && currentQuestionIndex < questionLocked.length) {
            questionLocked[currentQuestionIndex] = true;
        }

        if (optionButtons != null) {
            for (RadioButton btn : optionButtons) {
                btn.setDisable(true);
            }
        }

        if (fiftyFiftyButton != null) fiftyFiftyButton.setDisable(true);
        if (askComputerButton != null) askComputerButton.setDisable(true);


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
        if (playerNameField == null || playerNameField.getText().trim().isEmpty()) {
            showAlert("Missing Information", "Please enter your name before starting the quiz.");
            return;
        }
        
        playerName = playerNameField.getText().trim();
        
        if (categoryComboBox == null) {
            showAlert("Error", "Category selection not available");
            return;
        }
        
        String selectedCategory = categoryComboBox.getValue();
        
        try {
            if (selectedCategory != null && !selectedCategory.isEmpty()) {
                questions = Firebase.getQuestionsByCategory(selectedCategory);
            } else {
                questions = Firebase.getAllQuestions();
            }
            
            if (questions.isEmpty()) {
                showAlert("No Questions", "No questions available for this category.");
                return;
            }
            
            currentQuestionIndex = 0;
            score = 0;
            secondsElapsed = 0;
            
            userAnswers = new String[questions.size()];
            questionLocked = new boolean[questions.size()];

            questionTimeRemaining = new int[questions.size()];
            @SuppressWarnings("unchecked")
            List<Integer>[] tempArray = new ArrayList[questions.size()];
            fiftyFiftyRemovedOptions = tempArray;
            
            for (int i = 0; i < questions.size(); i++) {
                questionTimeRemaining[i] = QUESTION_TIME_LIMIT;
                fiftyFiftyRemovedOptions[i] = null;
                questionLocked[i] = false;
            }
            
            fiftyFiftyUsed = false;
            askComputerUsed = false;
            
            if (previousButton != null) {
                previousButton.setDisable(true);
            }
            
            if (fiftyFiftyButton != null) fiftyFiftyButton.setDisable(false);
            if (askComputerButton != null) askComputerButton.setDisable(false);
            
            currentQuestionTimeRemaining = QUESTION_TIME_LIMIT;
            
            if (scoreLabel != null) {
                scoreLabel.setText("Score: 0/" + questions.size());
            }
            
            if (playerDisplayLabel != null) {
                playerDisplayLabel.setText("Player: " + playerName);
            }
            
            timer.playFromStart();
            
            showCurrentQuestion();

            if (startScreen != null) startScreen.setVisible(false);
            if (questionScreen != null) questionScreen.setVisible(true);
            if (resultsScreen != null) resultsScreen.setVisible(false);
            
        } catch (Exception e) {
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
        
        questionTimer.stop();
        
        if (previousButton != null) {
            previousButton.setDisable(currentQuestionIndex == 0);
        }
        if (nextButton != null) {
            nextButton.setDisable(currentQuestionIndex == questions.size() - 1);
        }
        
        if (optionsGroup != null) {
            optionsGroup.selectToggle(null);
        }
        
        if (feedbackLabel != null) {
            feedbackLabel.setText("");
            feedbackLabel.getStyleClass().removeAll("correct", "incorrect");
        }

        Question question = questions.get(currentQuestionIndex);
        
        questionNumberLabel.setText("Question " + (currentQuestionIndex + 1) + "/" + questions.size());
        questionTextLabel.setText(question.getText());

        boolean isLocked = false;
        if (questionLocked != null && currentQuestionIndex >= 0 && currentQuestionIndex < questionLocked.length) {
            isLocked = questionLocked[currentQuestionIndex];
        }

        if (questionTimeRemaining != null && currentQuestionIndex >= 0 && 
            currentQuestionIndex < questionTimeRemaining.length) {
            currentQuestionTimeRemaining = questionTimeRemaining[currentQuestionIndex];

            if (isLocked) {
                currentQuestionTimeRemaining = 0;
            }
        } else {
            currentQuestionTimeRemaining = isLocked ? 0 : QUESTION_TIME_LIMIT;
        }
        updateQuestionTimerDisplay();
        
        List<String> options = question.getOptions();
        for (int i = 0; i < optionButtons.size(); i++) {
            RadioButton button = optionButtons.get(i);
            if (i < options.size()) {
                button.setText(options.get(i));
                button.setVisible(true);
                button.setUserData(Integer.valueOf(i));
                button.setSelected(false);
                
                button.setDisable(currentQuestionTimeRemaining == 0 || isLocked);
            } else {
                button.setVisible(false);
            }
        }
        
        if (userAnswers[currentQuestionIndex] != null) {
            for (int i = 0; i < options.size(); i++) {
                if (options.get(i).equals(userAnswers[currentQuestionIndex])) {
                    optionsGroup.selectToggle(optionButtons.get(i));
                    break;
                }
            }
        }
        
        if (fiftyFiftyRemovedOptions != null && 
            currentQuestionIndex < fiftyFiftyRemovedOptions.length && 
            fiftyFiftyRemovedOptions[currentQuestionIndex] != null) {
            
            List<Integer> removedIndices = fiftyFiftyRemovedOptions[currentQuestionIndex];
            for (Integer index : removedIndices) {
                if (index >= 0 && index < optionButtons.size()) {
                    optionButtons.get(index).setVisible(false);
                }
            }
        }
        
        if (currentQuestionTimeRemaining > 0 && !isLocked) {
            questionTimer.playFromStart();
        }
        
        if (fiftyFiftyButton != null) {
            fiftyFiftyButton.setDisable(fiftyFiftyUsed || isLocked || currentQuestionTimeRemaining == 0);
        }
        if (askComputerButton != null) {
            askComputerButton.setDisable(askComputerUsed || isLocked || currentQuestionTimeRemaining == 0);
        }
    }

    @FXML
    private void handleNextQuestion() {
        saveCurrentQuestionState();
        
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
            showCurrentQuestion();
        }
    }
    
    @FXML
    private void handleSubmitQuiz() {
        saveCurrentSelection();
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Submit Quiz");
        alert.setHeaderText("Are you sure you want to submit your quiz?");
        alert.setContentText("You won't be able to change your answers after submission.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            gradeQuiz();
            endQuiz();
        }
    }

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
        
        if (scoreLabel != null) {
            scoreLabel.setText("Score: " + score + "/" + questions.size());
        }
    }
    
    @FXML
    private void handlePreviousQuestion() {
        saveCurrentQuestionState();
        
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            showCurrentQuestion();
        }
    }
    
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
        saveCurrentSelection();
        
        if (questionTimeRemaining != null && currentQuestionIndex >= 0 
            && currentQuestionIndex < questionTimeRemaining.length) {
            questionTimeRemaining[currentQuestionIndex] = currentQuestionTimeRemaining;
        }
    }
    
    private void endQuiz() {
        timer.stop();
        questionTimer.stop();
        
        if (playerResultLabel != null) {
            playerResultLabel.setText("Player: " + playerName);
        }

        if (finalScoreLabel != null) {
            finalScoreLabel.setText("Your score: " + score + "/" + questions.size());
        }
        
        if (timeSpentLabel != null) {
            timeSpentLabel.setText("Time spent: " + formatTime(secondsElapsed));
        }
        
        if (startScreen != null) startScreen.setVisible(false);
        if (questionScreen != null) questionScreen.setVisible(false);
        if (resultsScreen != null) resultsScreen.setVisible(true);
    }
    
    @FXML
    private void handleNewQuiz() {
        if (playerNameField != null) {
            playerNameField.clear();
        }
        
        fiftyFiftyUsed = false;
        askComputerUsed = false;
        
        if (startScreen != null) startScreen.setVisible(true);
        if (questionScreen != null) questionScreen.setVisible(false);
        if (resultsScreen != null) resultsScreen.setVisible(false);

        if (contentPane != null && startScreen != null) {
            if (!contentPane.getChildren().contains(startScreen)) {
                contentPane.getChildren().add(startScreen);
            }
            startScreen.toFront();
            startScreen.setVisible(true);
        }
    }
    
    @FXML
    private void handleFiftyFifty() {
        if (fiftyFiftyUsed || currentQuestionIndex >= questions.size()) {
            return;
        }
        
        if (questionLocked != null && currentQuestionIndex < questionLocked.length && questionLocked[currentQuestionIndex]) {
            return;
        }
        
        if (currentQuestionTimeRemaining <= 0) {
            return;
        }
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        int correctOptionIndex = currentQuestion.getCorrectOptionIndex();

        List<Integer> incorrectIndices = new ArrayList<>();

        for (int i = 0; i < optionButtons.size(); i++) {
            if (i != correctOptionIndex) {
                incorrectIndices.add(i);
            }
        }
        
        Collections.shuffle(incorrectIndices);
        
        List<Integer> removedIndices = new ArrayList<>();
        for (int i = 0; i < Math.min(2, incorrectIndices.size()); i++) {
            int indexToHide = incorrectIndices.get(i);
            optionButtons.get(indexToHide).setVisible(false);
            removedIndices.add(indexToHide);
        }

        fiftyFiftyRemovedOptions[currentQuestionIndex] = removedIndices;
        
        fiftyFiftyUsed = true;
        fiftyFiftyButton.setDisable(true);
    }
    
    @FXML
    private void handleAskComputer() {
        if (askComputerUsed || currentQuestionIndex >= questions.size()) {
            return;
        }
        
        if (questionLocked != null && currentQuestionIndex < questionLocked.length && questionLocked[currentQuestionIndex]) {
            return;
        }
        
        if (currentQuestionTimeRemaining <= 0) {
            return;
        }
        
        Question currentQuestion = questions.get(currentQuestionIndex);
        int correctOptionIndex = currentQuestion.getCorrectOptionIndex();
        
        List<Integer> visibleOptionIndices = new ArrayList<>();
        for (int i = 0; i < optionButtons.size(); i++) {
            if (optionButtons.get(i).isVisible()) {
                visibleOptionIndices.add(i);
            }
        }
        
        if (visibleOptionIndices.isEmpty()) {
            return;
        }

        boolean correctOptionVisible = visibleOptionIndices.contains(correctOptionIndex);
        
        Random random = new Random();
        int responsePercentage = random.nextInt(100);
        
        if (responsePercentage < 80 && correctOptionVisible) {
            optionsGroup.selectToggle(optionButtons.get(correctOptionIndex));
        } else {
            List<Integer> visibleIncorrectIndices = new ArrayList<>();
            for (Integer i : visibleOptionIndices) {
                if (i != correctOptionIndex) {
                    visibleIncorrectIndices.add(i);
                }
            }
            
            if (!visibleIncorrectIndices.isEmpty()) {
                int randomIncorrectIndex = visibleIncorrectIndices.get(random.nextInt(visibleIncorrectIndices.size()));
                optionsGroup.selectToggle(optionButtons.get(randomIncorrectIndex));
            } else if (correctOptionVisible) {
                optionsGroup.selectToggle(optionButtons.get(correctOptionIndex));
            }
        }
        
        askComputerUsed = true;
        askComputerButton.setDisable(true);
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

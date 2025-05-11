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
import javafx.stage.Stage;

public class Controller {
    @FXML private TextField playerNameField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<Integer> questionCountComboBox;
    @FXML private Button startButton;
    @FXML private Button submitButton;
    @FXML private Button newQuizButton;
    @FXML private Button nextButton;
    @FXML private Button instructionsButton;
    @FXML private Button closeInstructionsButton;
    
    @FXML private Button fiftyFiftyButton;
    @FXML private Button askComputerButton;
    @FXML private Button previousButton;
    
    @FXML private Button pauseButton;
    @FXML private Button resumeButton;
    @FXML private Button newGameButton;
    @FXML private Button exitButton;
    @FXML private StackPane pauseOverlay;
    @FXML private VBox pauseMenu;
    @FXML private StackPane instructionsOverlay;
    @FXML private VBox instructionsModal;
    
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

    @FXML private VBox questionDetailsContainer;
    @FXML private ScrollPane questionDetailsScrollPane;

    private List<Question> questions;
    private List<Question> allAvailableQuestions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int secondsElapsed = 0;
    private Timeline timer; 
    private ObservableList<String> categories = FXCollections.observableArrayList();
    private ObservableList<Integer> questionCounts = FXCollections.observableArrayList(5, 10, 15, 20);
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
    private int[] randomizedCorrectIndices;

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
        
        if (questionCountComboBox != null) {
            questionCountComboBox.setItems(questionCounts);
            questionCountComboBox.getSelectionModel().select(1); // Default to 10 questions
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
        if (pauseOverlay != null) pauseOverlay.setVisible(false);
        if (instructionsOverlay != null) instructionsOverlay.setVisible(false);

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
        
        if (pauseButton != null) {
            pauseButton.setVisible(false);
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
        
        allAvailableQuestions = Firebase.getAllQuestions();
        Set<String> uniqueCategories = new HashSet<>();
        
        for (Question q : allAvailableQuestions) {
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
            List<Question> categoryQuestions;
            if (selectedCategory != null && !selectedCategory.isEmpty()) {
                categoryQuestions = Firebase.getQuestionsByCategory(selectedCategory);
            } else {
                categoryQuestions = allAvailableQuestions;
            }
            
            if (categoryQuestions.isEmpty()) {
                showAlert("No Questions", "No questions available for this category.");
                return;
            }
            
            Integer requestedQuestionCount = questionCountComboBox.getValue();
            if (requestedQuestionCount == null) {
                requestedQuestionCount = 10;
            }
            
            int availableQuestions = categoryQuestions.size();
            int finalQuestionCount = Math.min(requestedQuestionCount, availableQuestions);
            
            if (finalQuestionCount < requestedQuestionCount) {
                showAlert("Limited Questions", 
                    "Only " + finalQuestionCount + " questions are available in this category.");
            }
            
            Collections.shuffle(categoryQuestions);
            questions = categoryQuestions.subList(0, finalQuestionCount);

            currentQuestionIndex = 0;
            score = 0;
            secondsElapsed = 0;
            
            userAnswers = new String[questions.size()];
            questionLocked = new boolean[questions.size()];
            randomizedCorrectIndices = new int[questions.size()];

            questionTimeRemaining = new int[questions.size()];
            @SuppressWarnings("unchecked")
            List<Integer>[] tempArray = new ArrayList[questions.size()];
            fiftyFiftyRemovedOptions = tempArray;
            
            for (int i = 0; i < questions.size(); i++) {
                questionTimeRemaining[i] = QUESTION_TIME_LIMIT;
                fiftyFiftyRemovedOptions[i] = null;
                questionLocked[i] = false;
                randomizedCorrectIndices[i] = questions.get(i).getCorrectOptionIndex();
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
            
            if (pauseButton != null) {
                pauseButton.setVisible(true);
            }
            
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
        
        List<String> displayOptions;
        
        if (isLocked || userAnswers[currentQuestionIndex] != null) {
            displayOptions = question.getOptions();
        } else {
            Question.OptionRandomizationResult randomizationResult = question.getRandomizedOptions();
            displayOptions = randomizationResult.getRandomizedOptions();
            randomizedCorrectIndices[currentQuestionIndex] = randomizationResult.getNewCorrectIndex();
        }
        
        for (int i = 0; i < optionButtons.size(); i++) {
            RadioButton button = optionButtons.get(i);
            if (i < displayOptions.size()) {
                button.setText(displayOptions.get(i));
                button.setVisible(true);
                button.setUserData(Integer.valueOf(i));
                button.setSelected(false);
                
                button.setDisable(currentQuestionTimeRemaining == 0 || isLocked);
            } else {
                button.setVisible(false);
            }
        }
        
        if (userAnswers[currentQuestionIndex] != null) {
            for (int i = 0; i < displayOptions.size(); i++) {
                if (displayOptions.get(i).equals(userAnswers[currentQuestionIndex])) {
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
                userAnswer.equals(question.getCorrectAnswer())) {
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
                RadioButton selectedButton = (RadioButton) selectedToggle;
                userAnswers[currentQuestionIndex] = selectedButton.getText();
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
        
        if (pauseButton != null) {
            pauseButton.setVisible(false);
        }
        
        if (playerResultLabel != null) {
            playerResultLabel.setText("Player: " + playerName);
        }

        if (finalScoreLabel != null) {
            finalScoreLabel.setText("Your score: " + score + "/" + questions.size());
        }
        
        if (timeSpentLabel != null) {
            timeSpentLabel.setText("Time spent: " + formatTime(secondsElapsed));
        }
        
        // Populate question details
        populateQuestionDetails();
        
        if (startScreen != null) startScreen.setVisible(false);
        if (questionScreen != null) questionScreen.setVisible(false);
        if (resultsScreen != null) resultsScreen.setVisible(true);
    }
    

    private void populateQuestionDetails() {
        if (questionDetailsContainer == null || questions == null || userAnswers == null) {
            return;
        }
        
        questionDetailsContainer.getChildren().clear();
        
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            String userAnswer = userAnswers[i];
            String correctAnswer = question.getCorrectAnswer();
            boolean isCorrect = (userAnswer != null && userAnswer.equals(correctAnswer));
            boolean isUnanswered = (userAnswer == null);
            
            VBox questionDetail = new VBox(5);
            questionDetail.getStyleClass().add("question-detail-item");
            
            Label questionText = new Label("Q" + (i + 1) + ": " + question.getText());
            questionText.getStyleClass().add("question-detail-text");
            questionText.setWrapText(true);
            
            Label userAnswerLabel = new Label("Your answer: " + (userAnswer != null ? userAnswer : "Unanswered"));
            userAnswerLabel.getStyleClass().add("user-answer");
            
            Label correctAnswerLabel = new Label("Correct answer: " + correctAnswer);
            correctAnswerLabel.getStyleClass().add("correct-answer");

            Label statusLabel = new Label();
            if (isUnanswered) {
                statusLabel.setText("Status: Not answered");
                statusLabel.getStyleClass().add("answer-status-unanswered");
            } else if (isCorrect) {
                statusLabel.setText("Status: Correct");
                statusLabel.getStyleClass().add("answer-status-correct");
            } else {
                statusLabel.setText("Status: Incorrect");
                statusLabel.getStyleClass().add("answer-status-incorrect");
            }
            
            questionDetail.getChildren().addAll(questionText, userAnswerLabel, correctAnswerLabel, statusLabel);
            
            questionDetailsContainer.getChildren().add(questionDetail);
        }
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
        
        if (pauseButton != null) {
            pauseButton.setVisible(false);
        }
        
        if (categoryComboBox != null) {
            try {
                loadCategories();
            } catch (IOException e) {
                showAlert("Error", "Failed to reload categories: " + e.getMessage());
            }
        }
        
        if (questionCountComboBox != null) {
            questionCountComboBox.getSelectionModel().select(1);
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
        
        // Use the randomized correct index for the current question
        int correctOptionIndex = randomizedCorrectIndices[currentQuestionIndex];

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
        
        // Use the randomized correct index for the current question
        int correctOptionIndex = randomizedCorrectIndices[currentQuestionIndex];
        
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
    
    @FXML
    private void handlePauseGame() {
        if (timer != null) {
            timer.pause();
        }
        if (questionTimer != null) {
            questionTimer.pause();
        }
        
        if (optionsBox != null) {
            optionsBox.setVisible(false);
        }
        if (questionTextLabel != null) {
            questionTextLabel.setVisible(false);
        }
        if (questionNumberLabel != null) {
            questionNumberLabel.setVisible(false);
        }
        if (timeLabel != null) {
            timeLabel.setVisible(false);
        }
        if (feedbackLabel != null) {
            feedbackLabel.setVisible(false);
        }
        
        if (previousButton != null) previousButton.setVisible(false);
        if (nextButton != null) nextButton.setVisible(false);
        if (submitButton != null) submitButton.setVisible(false);
        if (fiftyFiftyButton != null) fiftyFiftyButton.setVisible(false);
        if (askComputerButton != null) askComputerButton.setVisible(false);

        if (pauseOverlay != null) {
            pauseOverlay.setVisible(true);
            pauseOverlay.toFront();
        }
    }
    
    @FXML
    private void handleResumeGame() {
        if (pauseOverlay != null) {
            pauseOverlay.setVisible(false);
        }

        if (optionsBox != null) {
            optionsBox.setVisible(true);
        }
        if (questionTextLabel != null) {
            questionTextLabel.setVisible(true);
        }
        if (questionNumberLabel != null) {
            questionNumberLabel.setVisible(true);
        }
        if (timeLabel != null) {
            timeLabel.setVisible(true);
        }
        if (feedbackLabel != null) {
            feedbackLabel.setVisible(true);
        }
        
        if (previousButton != null) {
            previousButton.setVisible(true);
            previousButton.setDisable(currentQuestionIndex == 0);
        }
        if (nextButton != null) {
            nextButton.setVisible(true);
            nextButton.setDisable(currentQuestionIndex == questions.size() - 1);
        }
        if (submitButton != null) submitButton.setVisible(true);
        
        if (fiftyFiftyButton != null) {
            fiftyFiftyButton.setVisible(true);
            fiftyFiftyButton.setDisable(fiftyFiftyUsed || 
                questionLocked[currentQuestionIndex] || 
                currentQuestionTimeRemaining == 0);
        }
        if (askComputerButton != null) {
            askComputerButton.setVisible(true);
            askComputerButton.setDisable(askComputerUsed || 
                questionLocked[currentQuestionIndex] || 
                currentQuestionTimeRemaining == 0);
        }
        
        if (timer != null) {
            timer.play();
        }
        if (questionTimer != null && currentQuestionTimeRemaining > 0 && 
            !questionLocked[currentQuestionIndex]) {
            questionTimer.play();
        }
    }
    
    @FXML
    private void handleNewGameFromPause() {
        if (timer != null) {
            timer.stop();
        }
        if (questionTimer != null) {
            questionTimer.stop();
        }
        
        if (pauseOverlay != null) {
            pauseOverlay.setVisible(false);
        }
        
        if (pauseButton != null) {
            pauseButton.setVisible(false);
        }
        
        if (optionsBox != null) {
            optionsBox.setVisible(true);
        }
        if (questionTextLabel != null) {
            questionTextLabel.setVisible(true);
        }
        if (questionNumberLabel != null) {
            questionNumberLabel.setVisible(true);
        }
        if (timeLabel != null) {
            timeLabel.setVisible(true);
        }
        if (feedbackLabel != null) {
            feedbackLabel.setVisible(true);
        }
        
        if (previousButton != null) previousButton.setVisible(true);
        if (nextButton != null) nextButton.setVisible(true);
        if (submitButton != null) submitButton.setVisible(true);
        if (fiftyFiftyButton != null) fiftyFiftyButton.setVisible(true);
        if (askComputerButton != null) askComputerButton.setVisible(true);
        
        handleNewQuiz();
        
        Platform.runLater(() -> {
            if (contentPane != null && startScreen != null) {
                if (!contentPane.getChildren().contains(startScreen)) {
                    contentPane.getChildren().add(startScreen);
                }
                startScreen.toFront();
                startScreen.setVisible(true);
            }
        });
    }
    
    @FXML
    private void handleExitGame() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Game");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Any unsaved progress will be lost.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Stage stage = (Stage) exitButton.getScene().getWindow();
            stage.close();
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
    
    @FXML
    private void handleShowInstructions() {
        if (instructionsOverlay != null) {
            instructionsOverlay.setVisible(true);
            instructionsOverlay.toFront();
        }
    }
    
    @FXML
    private void handleCloseInstructions() {
        if (instructionsOverlay != null) {
            instructionsOverlay.setVisible(false);
        }
    }
}

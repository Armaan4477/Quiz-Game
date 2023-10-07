import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;

public class QuizGameGUI extends JFrame {

    private JLabel questionLabel;
    private JRadioButton[] options;
    private ButtonGroup optionGroup;
    private JButton nextButton;
    private JButton backButton;
    private JButton pauseButton;
    private JButton fiftyFiftyButton;
    private JButton askFriendButton;
    private boolean paused = false;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private JTextArea summaryTextArea;
    private List<Question> allQuestions;
    private List<Question> selectedQuestions;
    private String[] userAnswers;
    private JFrame startupFrame;
    private JComboBox<Integer> questionCountComboBox;
    private JPopupMenu pauseMenu;
    private Timer questionTimer;
    private int timerSeconds = 20; // Set the timer duration in seconds
    private boolean timeUp = false;
    private Set<Integer> timedOutQuestions;
    private StringBuilder summary;
    private int[] timeRemaining;
    private JLabel timerLabel;
    private JFrame summaryFrame;


    public QuizGameGUI() {
        // Create the startup frame
        createStartupFrame();

        // Initialize the JTextArea for the summary
        summaryTextArea = new JTextArea(20, 80);
        summaryTextArea.setEditable(false);

        // Initialize questions and userAnswers
        initializeQuestions();

        // Set up the main quiz frame
        setUpQuizFrame();

        // Show the startup frame
        startupFrame.setVisible(true);

        timedOutQuestions = new HashSet<>();

        timeRemaining = new int[allQuestions.size()];
Arrays.fill(timeRemaining, timerSeconds);

questionTimer = new Timer(1000, new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        // Decrement the time remaining for the current question
        timeRemaining[currentQuestionIndex]--;

        // Update the timer label
        if (timeRemaining[currentQuestionIndex] <= 6) {
            // Blink the timer label red at even seconds
            if (timeRemaining[currentQuestionIndex] % 2 == 0) {
                timerLabel.setForeground(Color.RED);
            } else {
                timerLabel.setForeground(Color.BLACK);
            }
        } else {
            // Reset the timer label color to black
            timerLabel.setForeground(Color.BLACK);
        }
        timerLabel.setText("Timer: " + timeRemaining[currentQuestionIndex] + " seconds");

        if (timeRemaining[currentQuestionIndex] <= 0) {
            questionTimer.stop();
            handleTimeout();
        }
    }
});
    }   

    private void createStartupFrame() {
        // Create the startup frame
        startupFrame = new JFrame("Quiz Startup");
        startupFrame.setSize(300, 150);
        startupFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        startupFrame.setLocationRelativeTo(null);

        JPanel startupPanel = new JPanel();
        startupPanel.setLayout(new FlowLayout());

        JLabel label = new JLabel("Select the number of questions:");
        startupPanel.add(label);

        Integer[] options = {5, 10, 15, 20};
        questionCountComboBox = new JComboBox<>(options);
        startupPanel.add(questionCountComboBox);

        JButton startButton = new JButton("Start Quiz");
        startupPanel.add(startButton);

        JButton instructionsButton = new JButton("Instructions");  // New button for instructions
        startupPanel.add(instructionsButton);

        startupFrame.add(startupPanel);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the selected number of questions
                int selectedQuestionCount = (int) questionCountComboBox.getSelectedItem();

                // Select random questions based on the user's choice
                selectRandomQuestions(selectedQuestionCount);

                // Load the first question
                loadQuestion(currentQuestionIndex);

                // Hide the startup frame and show the quiz frame
                startupFrame.setVisible(false);
                setVisible(true);
            }
        });

        instructionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open a new frame for instructions
                showInstructionsFrame();
            }
        });
    }

    private void setUpQuizFrame() {
        setTitle("Quiz Game");
        setSize(1100, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        questionLabel = new JLabel();
        panel.add(questionLabel, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridLayout(4, 1));

        options = new JRadioButton[4];
        optionGroup = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            optionsPanel.add(options[i]);
            optionGroup.add(options[i]);
        }
        panel.add(optionsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

         timerLabel = new JLabel("Timer: " + timerSeconds + " seconds");
         timerLabel.setHorizontalAlignment(JLabel.CENTER);

          // Add the timer label to the panel
          JPanel timerPanel = new JPanel(new BorderLayout());
          timerPanel.add(timerLabel, BorderLayout.CENTER);
           add(timerPanel, BorderLayout.SOUTH);


        backButton = new JButton("Back");
        buttonPanel.add(backButton);

        nextButton = new JButton("Next");
        buttonPanel.add(nextButton);

        pauseButton = new JButton("Pause");
        buttonPanel.add(pauseButton);

        fiftyFiftyButton = new JButton("50-50");
        buttonPanel.add(fiftyFiftyButton);

        askFriendButton = new JButton("Ask the Computer");
        buttonPanel.add(askFriendButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkAnswer();
                currentQuestionIndex++;

                  optionGroup.clearSelection();

                if (currentQuestionIndex < selectedQuestions.size()) {
                    loadQuestion(currentQuestionIndex);
                } else {
                    showResult();
                }
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // If the timer has expired, mark the current question as timed out
                if (timerSeconds <= 0 && !timedOutQuestions.contains(currentQuestionIndex)) {
                    timedOutQuestions.add(currentQuestionIndex);
                }
        
                if (currentQuestionIndex > 0) {
                    currentQuestionIndex--;
                    loadQuestion(currentQuestionIndex);
                }
        
                // Enable the back button for all questions before the current one
                for (int i = 0; i < currentQuestionIndex; i++) {
                    if (!timedOutQuestions.contains(i)) {
                        backButton.setEnabled(true);
                        break;
                    }
                }
            }
        });
        

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPauseMenu();
            }
        });

        fiftyFiftyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                useFiftyFiftyLifeline();
            }
        });

        askFriendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                useAskFriendLifeline();
            }
        });

        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_SPACE) {
                    showPauseMenu();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });


    }

    private void initializeQuestions() {
        allQuestions = new ArrayList<>();

        allQuestions.add(new Question("What does JVM stand for?", "Java Virtual Machine", "Just Very Much", "Jungle Virtual Mouse", "Java Virtual Method", "Java Virtual Machine"));
        allQuestions.add(new Question("What is a variable in Java?", "A reserved keyword", "A data type", "A storage location", "An operator", "A storage location"));
        allQuestions.add(new Question("Which data type is used for whole numbers in Java?", "float", "double", "int", "String", "int"));
        allQuestions.add(new Question("How do you declare a constant variable in Java?", "Using the 'var' keyword", "Using the 'let' keyword", "Using the 'final' keyword", "Using the 'const' keyword", "Using the 'final' keyword"));
        allQuestions.add(new Question("What is the main purpose of the 'public static void main(String[] args)' method?", "To declare variables", "To print output", "To initialize objects", "To start the program", "To start the program"));
        allQuestions.add(new Question("Which Java keyword is used to create a new instance of a class?", "new", "class", "instance", "this", "new"));
        allQuestions.add(new Question("What is the output of 'System.out.println(5 + 3 * 2)'?", "11", "16", "56", "26", "11"));
        allQuestions.add(new Question("Which operator is used for equality comparison in Java?", "==", "=", "!=", "===","=="));
        allQuestions.add(new Question("What is the keyword used to create a new class in Java?", "new", "class", "instance", "this", "class"));
        allQuestions.add(new Question("Which loop is used for iterating over elements of an array or collection in Java?", "for loop", "while loop", "if-else loop", "do-while loop", "for loop"));
        allQuestions.add(new Question("What is the correct syntax for a single-line comment in Java?", "// This is a comment", "/* This is a comment */", "# This is a comment", "<!-- This is a comment -->", "// This is a comment"));
        allQuestions.add(new Question("Which access modifier makes a class or method accessible only within the same package?", "public", "protected", "private", "default", "default"));
        allQuestions.add(new Question("What is the purpose of the 'this' keyword in Java?", "To create a new instance of a class", "To call a method of the superclass", "To refer to the current instance of a class", "To declare a constant", "To refer to the current instance of a class"));
        allQuestions.add(new Question("Which Java data type is used to store text?", "int", "char", "String", "float", "String"));
        allQuestions.add(new Question("What is the result of '10 % 3' in Java?", "1", "2", "3", "0", "1"));
        allQuestions.add(new Question("Which statement is used to exit a loop prematurely in Java?", "break", "continue", "return", "exit", "break"));
        allQuestions.add(new Question("What is the term for a function defined within a class in Java?", "Procedure", "Function", "Method", "Routine", "Method"));
        allQuestions.add(new Question("What is the correct syntax to create a new object of a class in Java?", "new Object();", "create Object();", "Object.create();", "Object.new();", "new Object();"));
        allQuestions.add(new Question("What is the default value of a boolean variable in Java?", "0", "1", "false", "true", "false"));
        allQuestions.add(new Question("Which Java keyword is used to declare a constant variable?", "constant", "const", "final", "static", "final"));
        allQuestions.add(new Question("What is the term for a class that cannot be instantiated and may have abstract methods?", "Interface", "Abstract class", "Concrete class", "Final class", "Abstract class"));
        allQuestions.add(new Question("Which keyword is used to implement multiple inheritance in Java?", "inherit", "extends", "implements", "multiextends", "extends"));
        allQuestions.add(new Question("What is the output of 'System.out.println(\"Hello\" + \"World\");'?", "Hello World", "Hello\nWorld", "Hello + World", "HelloWorld", "HelloWorld"));
        allQuestions.add(new Question("In Java, a switch statement can be used with which data types?", "int", "float", "String", "All of the above", "int"));
        allQuestions.add(new Question("What is the purpose of the 'default' case in a switch statement?", "To specify the default value", "To define the default behavior when no case matches", "To indicate an error", "To break out of the switch statement", "To define the default behavior when no case matches"));
        allQuestions.add(new Question("Which operator is used for logical AND in Java?", "&", "&&", "||", "!", "&&"));
        allQuestions.add(new Question("What is the term for a class that inherits properties and behaviors from another class in Java?", "Derived class", "Superclass", "Parent class", "Child class", "Child class"));
        allQuestions.add(new Question("Which exception is thrown when an array index is out of bounds?", "IndexOutOfRangeException", "ArrayIndexException", "OutOfBoundsException", "ArrayIndexOutOfBoundsException", "ArrayIndexOutOfBoundsException"));
        allQuestions.add(new Question("What is the purpose of the 'finally' block in a try-catch-finally statement?", "To specify the catch block", "To handle exceptions", "To ensure code is executed regardless of exceptions", "To skip the try block", "To ensure code is executed regardless of exceptions"));
        allQuestions.add(new Question("What is the difference between '=='' and '.equals()' when comparing strings in Java?", "'==' compares object references, '.equals()' compares string contents", "'==' compares string contents, '.equals()' compares object references", "There is no difference", "Both are used to compare object references", "'==' compares object references, '.equals()' compares string contents"));
        allQuestions.add(new Question("Which Java keyword is used to explicitly call a superclass constructor?", "superclass", "base", "super", "parent", "super"));
        allQuestions.add(new Question("In Java, which keyword is used to create an array?", "new", "array", "create", "make", "new"));
        allQuestions.add(new Question("What is the result of '5 / 2' in Java?", "2.5", "2", "2.0", "2.25", "2"));
        allQuestions.add(new Question("What is the purpose of the 'volatile' keyword in Java?", "To make a variable thread-safe", "To declare a constant", "To define a final variable", "To prevent variable modification", "To make a variable thread-safe"));
        allQuestions.add(new Question("Which Java data type is used to represent a single 16-bit Unicode character?", "char", "byte", "int", "short", "char"));
        allQuestions.add(new Question("What is the term for a method that has the same name as the class and is used to initialize objects?", "Constructor", "Initializer", "Destructor", "Accessor", "Constructor"));
        allQuestions.add(new Question("What is the Java keyword used to create a subclass that inherits from a superclass?", "inherits", "extends", "implements", "inheritsfrom", "extends"));
        allQuestions.add(new Question("What is the Java keyword used to refer to the current instance of a class within that class's methods?", "this", "self", "current", "instance", "this"));
        allQuestions.add(new Question("Which access modifier allows a class or method to be accessible only within the same package or by subclasses?", "private", "public", "protected", "default", "protected"));
        allQuestions.add(new Question("What is the result of '5 + 5.0' in Java?", "10.0", "10", "5.0", "5", "10.0"));
        allQuestions.add(new Question("Which Java data type is used to store characters in Unicode format?", "char", "byte", "int", "string", "char"));
        allQuestions.add(new Question("What is the purpose of the 'super' keyword in Java?", "To call a superclass method", "To call a static method", "To create a new instance of a class", "To declare a constant", "To call a superclass method"));
        allQuestions.add(new Question("In Java, what is the term for hiding a class's implementation details and exposing only necessary functionalities?", "Abstraction", "Encapsulation", "Inheritance", "Polymorphism", "Abstraction"));
        allQuestions.add(new Question("Which loop in Java is used for iterating a block of code repeatedly while a condition is true?", "for loop", "while loop", "do-while loop", "if-else loop", "while loop"));
        allQuestions.add(new Question("What is the purpose of the 'break' statement in a loop?", "To exit the loop prematurely", "To continue to the next iteration of the loop", "To skip the loop entirely", "To create a nested loop", "To exit the loop prematurely"));
        allQuestions.add(new Question("What is the term for defining more than one method with the same name in a class, but with different parameters?", "Method overloading", "Method overriding", "Method hiding", "Method chaining", "Method overloading"));
        allQuestions.add(new Question("In Java, what keyword is used to declare a method that does not return a value?", "void", "null", "return", "empty", "void"));
        allQuestions.add(new Question("What is the output of 'System.out.println(10 > 5 && 5 < 3);' in Java?", "true", "false", "compile error", "runtime error", "false"));
        allQuestions.add(new Question("Which exception is thrown when an arithmetic operation results in a value that is too large or too small to be represented in the data type?", "ArithmeticException", "OverflowException", "NumberFormatException", "InvalidValueException", "ArithmeticException"));
        allQuestions.add(new Question("What is the term for a method that is defined in a subclass and provides a specific implementation for a method declared in its superclass?", "Overloading", "Overriding", "Hiding", "Polymorphism", "Overriding"));
        allQuestions.add(new Question("What is the result of '5 / 0' in Java?", "5", "0", "Infinity", "Runtime error", "Runtime error"));
        allQuestions.add(new Question("In Java, which operator is used for bitwise AND?", "&", "&&", "|", "!", "&"));
        allQuestions.add(new Question("What is the purpose of the 'try' and 'catch' blocks in exception handling?", "To handle exceptions", "To throw exceptions", "To declare variables", "To define methods", "To handle exceptions"));
        allQuestions.add(new Question("What is the term for the process of converting an object into a stream of bytes for storage or transmission?", "Serialization", "Deserialization", "Encoding", "Decoding", "Serialization"));
        allQuestions.add(new Question("In Java, which keyword is used to create an interface?", "interface", "create", "new", "implements", "interface"));  
    }

    private void selectRandomQuestions(int count) {
        if (count >= allQuestions.size()) {
            selectedQuestions = allQuestions;
        } else {
            List<Question> shuffledQuestions = new ArrayList<>(allQuestions);
            Collections.shuffle(shuffledQuestions, new Random());
            selectedQuestions = shuffledQuestions.subList(0, count);
        }

        // Initialize userAnswers based on the selected question count
        userAnswers = new String[selectedQuestions.size()];
    }

    private void loadQuestion(int index) {
        // Reset timer and start for the new question
        timerSeconds = timeRemaining[index]; // Set timer to the time remaining for the question
        questionTimer.restart();
    
        // Reset timeUp
        timeUp = false;
    
        Question currentQuestion = selectedQuestions.get(index);
        questionLabel.setText("Question " + (index + 1) + ": " + currentQuestion.getQuestion()); // Include question number
    
        String[] answerChoices = currentQuestion.getAnswerChoices();
        for (int i = 0; i < 4; i++) {
            options[i].setText(answerChoices[i]);
            options[i].setEnabled(timeRemaining[index] > 0); // Set enabled or disabled based on time remaining
            options[i].setSelected(false);
        }
    
        if (userAnswers[index] != null) {
            for (int i = 0; i < 4; i++) {
                if (options[i].getText().equals(userAnswers[index])) {
                    options[i].setSelected(true);
                    break;
                }
            }
        }
    }
    

    private void checkAnswer() {
        for (int i = 0; i < 4; i++) {
            if (options[i].isSelected()) {
                userAnswers[currentQuestionIndex] = options[i].getText();
                if (userAnswers[currentQuestionIndex].equals(selectedQuestions.get(currentQuestionIndex).getCorrectAnswer())) {
                    score++;
                }
                break;
            }
        }

        String selectedAnswer = null;
        for (int i = 0; i < 4; i++) {
            if (options[i].isSelected()) {
                selectedAnswer = options[i].getText();
                userAnswers[currentQuestionIndex] = selectedAnswer; // Store the user's answer
                break;
            }
        }
    }

    private void showResult() {
        questionTimer.stop();
        // Display the summary screen
        StringBuilder summary = new StringBuilder();
        summary.append("Quiz Complete!\nYour Score: ").append(score).append(" out of ").append(selectedQuestions.size()).append("\n\n");
    
        // Iterate through selected questions and display information
        for (int i = 0; i < selectedQuestions.size(); i++) {
            Question question = selectedQuestions.get(i);
            summary.append("Question ").append(i + 1).append(": ").append(question.getQuestion()).append("\n");
            summary.append("Correct Answer: ").append(question.getCorrectAnswer()).append("\n");
            summary.append("Your Answer: ").append(userAnswers[i]).append("\n");
    
            // Check if the user's answer is correct
            boolean answeredCorrectly = userAnswers[i] != null && userAnswers[i].equals(question.getCorrectAnswer());
    
            // Indicate if the user answered correctly or not
            if (answeredCorrectly) {
                summary.append("Result: Correct\n");
            } else {
                summary.append("Result: Incorrect\n");
            }
    
            // Include the remaining time for each question
            summary.append("Time Remaining: ").append(timeRemaining[i]).append(" seconds\n\n");
        }
    
        // Set the summary text to the JTextArea
        summaryTextArea.setText(summary.toString());
    
        // Remove the Next button and adjust the window size
        nextButton.setVisible(false);
        backButton.setVisible(false);  // Hide the "Back" button as well
        pauseButton.setVisible(false);
        fiftyFiftyButton.setVisible(false);
        askFriendButton.setVisible(false);
    
        // Add an "Exit" button
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0); // Exit the entire program
            }
        });
        // Add a "New Game" button
        JButton newgameButton = new JButton("New Game");
        newgameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startNewGame(); // Start a new game
                
            }
            
        });
    
        // Create a new panel for the summary and exit button
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BorderLayout());
        summaryPanel.add(new JScrollPane(summaryTextArea), BorderLayout.CENTER);
    
        // Create a new panel for the exit button
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(exitButton);
        buttonPanel.add(newgameButton);
    
        // Add the panels to the summary frame
            summaryFrame = new JFrame("Quiz Summary");
         summaryFrame.setSize(900, 600);
         summaryFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         summaryFrame.setLayout(new BorderLayout());
        summaryFrame.add(summaryPanel, BorderLayout.CENTER);
         summaryFrame.add(buttonPanel, BorderLayout.SOUTH);
         summaryFrame.setLocationRelativeTo(null);
         summaryFrame.setVisible(true);
         summaryTextArea.setCaretPosition(0);
          // Close the quiz game window
          dispose();  
    }
    
    
    private void showPauseMenu() {
        if (!paused) {
            questionTimer.stop();
            paused = true;
            createPauseMenu();
            pauseMenu.show(pauseButton, 0, pauseButton.getHeight());
            nextButton.setVisible(false);
            backButton.setVisible(false);
            fiftyFiftyButton.setVisible(false);
            askFriendButton.setVisible(false);
            //hide question
            questionLabel.setVisible(false);
            //hide radio buttons
            for (int i = 0; i < options.length; i++) {
                options[i].setVisible(false);
            }

        } else {
            paused = false;
            pauseMenu.setVisible(false);
            nextButton.setEnabled(true);
            backButton.setEnabled(true);
            
        }
    }

    private void createPauseMenu() {
        if (pauseMenu == null) {
            pauseMenu = new JPopupMenu();

            JMenuItem resumeItem = new JMenuItem("Resume");
            resumeItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    paused = false;
                    pauseMenu.setVisible(false);
                    nextButton.setVisible(true);
                    backButton.setVisible(true);
                    fiftyFiftyButton.setVisible(true);
                    askFriendButton.setVisible(true);
                    questionTimer.start();
                    //show question
                    questionLabel.setVisible(true);
                    //show radio buttons
                    for (int i = 0; i < options.length; i++) {
                        options[i].setVisible(true);
                    }
                }
            });

            JMenuItem newGameItem = new JMenuItem("New Game");
            newGameItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    paused = false;
                    pauseMenu.setVisible(false);
                    nextButton.setEnabled(true);
                    backButton.setEnabled(true);
                    restartGame();
                }
            });

            JMenuItem creditsItem = new JMenuItem("Credits");
            creditsItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showCredits();
                }
            });

            JMenuItem exitItem = new JMenuItem("Exit");
            exitItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });

            pauseMenu.add(resumeItem);
            pauseMenu.add(newGameItem);
            pauseMenu.add(creditsItem);
            pauseMenu.add(exitItem);
        }
    }
    
    private void restartGame() {

        questionTimer.stop();

        // Reset the game state (e.g., score, currentQuestionIndex)
        score = 0;
        currentQuestionIndex = 0;
    
        // Show the startup frame to allow the user to choose new options
        startupFrame.setVisible(true);
    
        // Hide the current quiz frame
        setVisible(false);

        timedOutQuestions.clear();
        Arrays.fill(timeRemaining, timerSeconds);
        Arrays.fill(userAnswers, null);
       
        paused = false;
        pauseMenu.setVisible(false);
        nextButton.setVisible(true);
        backButton.setVisible(true);
        fiftyFiftyButton.setVisible(true);
        askFriendButton.setVisible(true);
        //enable fifty-fifty and ask friend buttons
        fiftyFiftyButton.setEnabled(true);
        askFriendButton.setEnabled(true);
        //show the question
        questionLabel.setVisible(true);
        //show the radio buttons
        for (int i = 0; i < options.length; i++) {
            options[i].setVisible(true);
        }
    }
    
    private void showCredits() { 
        JOptionPane.showMessageDialog(this, "Credits: \n Armaan Nakhuda B-02 \n  Sushant Navle B-05 \n Nishal Poojary B-17 \n \n");
    }

    private void useFiftyFiftyLifeline() {
        Question currentQuestion = selectedQuestions.get(currentQuestionIndex);
       String correctAnswer = currentQuestion.getCorrectAnswer();

         // Disable two incorrect options
            int disabledCount = 0;
            for (int i = 0; i < options.length; i++) {
                if (!options[i].getText().equals(correctAnswer)) {
                    options[i].setEnabled(false);
                    disabledCount++;
                    if (disabledCount == 2) {
                        break;
                    }
                }
            }
    
        // Disable the 50-50 lifeline button after using it
        //Disable for testing by putting // in front of the line
        fiftyFiftyButton.setEnabled(false);
    }

    private void useAskFriendLifeline() {
        // Implement logic for Ask a Friend lifeline
        Question currentQuestion = selectedQuestions.get(currentQuestionIndex);

        // Generate a random number to simulate friend's response
        int responsePercentage = new Random().nextInt(101);

        // Friend has an 80% chance of giving the correct answer
        if (responsePercentage <= 80) {
            // Select the correct answer
            String correctAnswer = currentQuestion.getCorrectAnswer();
            for (int i = 0; i < options.length; i++) {
                if (options[i].getText().equals(correctAnswer)) {
                    options[i].setSelected(true);
                    break;
                }
            }
        } else {
            // Friend gives a random wrong answer
            int correctIndex = findCorrectAnswerIndex(currentQuestion.getAnswerChoices());
            int wrongIndex = generateRandomWrongIndex(currentQuestion.getAnswerChoices().length, correctIndex);
            options[wrongIndex].setSelected(true);
        }

        // Disable the Ask a Friend lifeline button after using it
        //Disable for testing by putting // in front of the line
        askFriendButton.setEnabled(false);
    }

    private int generateRandomWrongIndex(int totalOptions, int correctIndex) {
        int wrongIndex = new Random().nextInt(totalOptions);
        while (wrongIndex == correctIndex) {
            wrongIndex = new Random().nextInt(totalOptions);
        }
        //keep it in bounds of 0-3
        if (wrongIndex < 0) {
            wrongIndex = 0;
        } else if (wrongIndex > 3) {
            wrongIndex = 3;
        }
       
        // Return the wrong index
        return wrongIndex;
    }

    private int findCorrectAnswerIndex(String[] answerChoices) {
        for (int i = 0; i < answerChoices.length; i++) {
            if (answerChoices[i].equals(selectedQuestions.get(currentQuestionIndex).getCorrectAnswer())) {
                return i;
            }
        }
        return -1; // Not found
    }

    private void handleTimeout() {
        // If the timer runs out, show a message to the user
        int choice = JOptionPane.showOptionDialog(
                this,
                "Time's up! Click OK to move to the next question.",
                "Timeout",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                null,
                null);
    
                // Update and disable radio buttons (whether the user clicks OK or Cancel)
                disableRadioButtonsForTimedOutQuestion(currentQuestionIndex);
    
        // If the user clicks OK, move to the next question
        if (choice == JOptionPane.OK_OPTION) {
            currentQuestionIndex++;
            if (currentQuestionIndex < selectedQuestions.size()) {
                loadQuestion(currentQuestionIndex);
                //load the previously selected answer by the user
                for (int i = 0; i < 4; i++) {
                    if (options[i].getText().equals(userAnswers[currentQuestionIndex])) {
                        options[i].setSelected(true);
                        break;
                    }
                }
            } else {
                showResult();
            }
        }
        // If the user clicks Cancel, do nothing (stay on the current question)
    }
    
    // Add a new method to disable radio buttons for the timed out question
    private void disableRadioButtonsForTimedOutQuestion(int questionIndex) {
        for (int i = 0; i < options.length; i++) {
            options[i].setEnabled(false);
        }
    }

    private void showInstructionsFrame(){
        JFrame instructionsFrame = new JFrame("Instructions");
        instructionsFrame.setSize(800, 400);
        instructionsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        instructionsFrame.setLocationRelativeTo(null);

        JTextArea instructionsTextArea = new JTextArea();
        instructionsTextArea.setEditable(false);
        instructionsTextArea.setText("Instructions: \n\n" +
                "1. Select the number of questions you want to answer from the drop-down menu.\n" +
                "2. Click the 'Start Quiz' button to begin the quiz.\n" +
                "3. Click the 'Next' button to move to the next question.\n" +
                "4. Click the 'Back' button to move to the previous question.\n" +
                "5. Click the 'Pause' button to pause the quiz and access the pause menu.\n" +
                "6. Click the '50-50' button to use the 50-50 lifeline.\n" +
                "7. Click the 'Ask the Computer' button to use the Ask a Friend lifeline.\n" +
                "8. You have 20 seconds to answer each question.\n" +
                "9. The timer will start as soon as the question is loaded.\n" +
                "10. Once the timer is complete the answer buttons will be disabled after which it wont be possible to answer the question/change your answer.\n" +
                "11. The timer will stop when you click the 'Next' button or when you run out of time.\n" +
                "12. Click the 'Exit' button to exit the quiz.\n\n" +
                "Note: You can also use the spacebar to pause the quiz.\n\n" +
                "Good luck!");

                JButton closeButton = new JButton("Close");
                closeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        instructionsFrame.dispose(); // Close the instructions frame
                    }
                });
            
                JPanel buttonPanel = new JPanel();
                buttonPanel.add(closeButton);
            
                instructionsFrame.add(new JScrollPane(instructionsTextArea), BorderLayout.CENTER);
                instructionsFrame.add(buttonPanel, BorderLayout.SOUTH);
                instructionsFrame.setVisible(true);
    }

    private void startNewGame() {
        questionTimer.stop();

        // Reset the game state (e.g., score, currentQuestionIndex)
        score = 0;
        currentQuestionIndex = 0;
    
        // Show the startup frame to allow the user to choose new options
        startupFrame.setVisible(true);
    
        // Hide the current quiz frame
        setVisible(false);

        timedOutQuestions.clear();
        Arrays.fill(timeRemaining, timerSeconds);
        Arrays.fill(userAnswers, null);
       
        paused = false;
        pauseButton.setVisible(true);
        nextButton.setVisible(true);
        backButton.setVisible(true);
        fiftyFiftyButton.setVisible(true);
        askFriendButton.setVisible(true);
        fiftyFiftyButton.setEnabled(true);
        askFriendButton.setEnabled(true);
        //show the question
        questionLabel.setVisible(true);
        //show the radio buttons
        for (int i = 0; i < options.length; i++) {
            options[i].setVisible(true);
        }

        //close the summary frame
                summaryFrame.setVisible(false);
       
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new QuizGameGUI();
            }
        });
    }

    
}

class Question {
    private String question;
    private String[] answerChoices;
    private String correctAnswer;

    public Question(String question, String... answerChoices) {
        this.question = question;
        this.answerChoices = answerChoices;
        this.correctAnswer = answerChoices[answerChoices.length - 1];
    }

    public String getQuestion() {
        return question;
    }

    public String[] getAnswerChoices() {
        return answerChoices;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}
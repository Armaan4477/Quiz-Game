import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
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

    private int timerSeconds = 15; // Set the timer duration in seconds

    private boolean timeUp = false;

    private Set<Integer> timedOutQuestions;

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

        questionTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timerSeconds--;
                if (timerSeconds <= 0) {
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
    }

    private void setUpQuizFrame() {
        // Set up the main quiz frame
        setTitle("Quiz Game");
        setSize(400, 300);
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

        JPanel buttonPanel = new JPanel(); // Create a new panel for the buttons
        buttonPanel.setLayout(new FlowLayout()); // Use FlowLayout for button arrangement

        backButton = new JButton("Back");
        buttonPanel.add(backButton); // Add the "Back" button to the button panel

        nextButton = new JButton("Next");
        buttonPanel.add(nextButton); // Add the "Next" button to the button panel

        // Add a pause button
        pauseButton = new JButton("Pause");
        buttonPanel.add(pauseButton);

        // Add lifeline buttons
        fiftyFiftyButton = new JButton("50-50");
        buttonPanel.add(fiftyFiftyButton);

        askFriendButton = new JButton("Ask a Friend");
        buttonPanel.add(askFriendButton);

        panel.add(buttonPanel, BorderLayout.SOUTH); // Add the button panel to the main panel

        add(panel);

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkAnswer();
                currentQuestionIndex++;
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
                if (currentQuestionIndex > 0 && timerSeconds > 0) {
                    currentQuestionIndex--;
                    loadQuestion(currentQuestionIndex);
                }
            }
        });

        // Add action listener for the pause button
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPauseMenu();
            }
        });

        // Add action listener for the 50-50 lifeline button
        fiftyFiftyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                useFiftyFiftyLifeline();
            }
        });

        // Add action listener for the Ask a Friend lifeline button
        askFriendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                useAskFriendLifeline();
            }
        });

        // Add a key listener for pausing with the spacebar
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
        allQuestions.add(new Question("What is the output of 'System.out.println(5 + 3 * 2)'?", "10", "16", "56", "26", "16"));
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
        allQuestions.add(new Question("Which keyword is used to implement multiple inheritance in Java?", "inherit", "extends", "implements", "multiextends", "implements"));
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
        allQuestions.add(new Question("What is the result of '5 / 0' in Java?", "5", "0", "Infinity", "Compile error", "Infinity"));
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
        timerSeconds = 15;
        questionTimer.restart();

        // Reset timeUp
        timeUp = false;

        Question currentQuestion = selectedQuestions.get(index);
        questionLabel.setText(currentQuestion.getQuestion());

        String[] answerChoices = currentQuestion.getAnswerChoices();
        for (int i = 0; i < 4; i++) {
            options[i].setText(answerChoices[i]);
            options[i].setEnabled(true);
            options[i].setSelected(false);
        }

        // Enable or disable the back button based on timeUp and whether the question has timed out
        backButton.setEnabled(!timeUp && !timedOutQuestions.contains(index));
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
    }

    private void showResult() {
        // Stop the timer
        questionTimer.stop();

        // Display the result
        StringBuilder result = new StringBuilder("Quiz Completed!\n\n");
        result.append("Your Score: ").append(score).append("/").append(selectedQuestions.size()).append("\n\n");
        for (int i = 0; i < selectedQuestions.size(); i++) {
            result.append("Q").append(i + 1).append(": ").append(selectedQuestions.get(i).getQuestion()).append("\n");
            result.append("Your Answer: ").append(userAnswers[i]).append("\n");
            result.append("Correct Answer: ").append(selectedQuestions.get(i).getCorrectAnswer()).append("\n\n");
        }

        summaryTextArea.setText(result.toString());
        JOptionPane.showMessageDialog(this, new JScrollPane(summaryTextArea), "Quiz Result", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    private void showPauseMenu() {
        // If the game is paused, resume it
        if (paused) {
            paused = false;
            questionTimer.start();
            pauseMenu.setVisible(false);
        } else {
            // If the game is running, pause it
            paused = true;

            // Pause the timer
            questionTimer.stop();

            // Show the pause menu
            int x = getLocation().x + getWidth() / 2 - 75;
            int y = getLocation().y + getHeight() / 2 - 50;
            pauseMenu.show(this, x, y);
        }
    }

    private void useFiftyFiftyLifeline() {
        // Implement logic to remove two wrong options
        Question currentQuestion = selectedQuestions.get(currentQuestionIndex);
        String[] answerChoices = currentQuestion.getAnswerChoices();
        int correctIndex = findCorrectAnswerIndex(answerChoices);

        // Generate two random indices for wrong answers
        int wrongIndex1 = generateRandomWrongIndex(answerChoices.length, correctIndex);
        int wrongIndex2 = generateRandomWrongIndex(answerChoices.length, correctIndex, wrongIndex1);

        // Disable the two wrong options
        options[wrongIndex1].setEnabled(false);
        options[wrongIndex2].setEnabled(false);

        // Disable the 50-50 lifeline button after using it
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
        askFriendButton.setEnabled(false);
    }

    private int findCorrectAnswerIndex(String[] answerChoices) {
        for (int i = 0; i < answerChoices.length; i++) {
            if (answerChoices[i].equals(selectedQuestions.get(currentQuestionIndex).getCorrectAnswer())) {
                return i;
            }
        }
        return -1; // Not found (shouldn't happen in a well-formed question)
    }

    private int generateRandomWrongIndex(int totalOptions, int... excludeIndices) {
        List<Integer> availableIndices = new ArrayList<>();
        for (int i = 0; i < totalOptions; i++) {
            availableIndices.add(i);
        }

        // Remove excluded indices
        for (int excludeIndex : excludeIndices) {
            availableIndices.remove(Integer.valueOf(excludeIndex));
        }

        // Randomly select an index from the remaining options
        return availableIndices.get(new Random().nextInt(availableIndices.size()));
    }

    private void handleTimeout() {
        // If the timer runs out, treat it as if the user didn't answer
        JOptionPane.showMessageDialog(this, "Time's up! Moving to the next question.", "Timeout", JOptionPane.INFORMATION_MESSAGE);

        // Mark the current question as timed out
        timedOutQuestions.add(currentQuestionIndex);

        currentQuestionIndex++;
        if (currentQuestionIndex < selectedQuestions.size()) {
            loadQuestion(currentQuestionIndex);
        } else {
            showResult();
        }

        // Set timeUp to true
        timeUp = true;

        // Disable the back button
        backButton.setEnabled(false);
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

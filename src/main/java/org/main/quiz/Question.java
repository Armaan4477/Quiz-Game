package org.main.quiz;

import java.util.List;

public class Question {
    private String id;
    private String text;
    private List<String> options;
    private int correctOptionIndex;
    private String category;
    private int difficultyLevel;

    public Question(String id, String text, List<String> options, int correctOptionIndex, String category, int difficultyLevel) {
        this.id = id;
        this.text = text;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
        this.category = category;
        this.difficultyLevel = difficultyLevel;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    
    public int getCorrectOptionIndex() { return correctOptionIndex; }
    public void setCorrectOptionIndex(int correctOptionIndex) { this.correctOptionIndex = correctOptionIndex; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public int getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(int difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    
    public String getCorrectAnswer() {
        if (options != null && correctOptionIndex >= 0 && correctOptionIndex < options.size()) {
            return options.get(correctOptionIndex);
        }
        return null;
    }
    
    public boolean isCorrectAnswer(int selectedIndex) {
        return selectedIndex == correctOptionIndex;
    }
}

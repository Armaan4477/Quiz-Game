package org.main.quiz;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

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
    
    /**
     * Returns a randomized copy of the options list, and updates the correctOptionIndex accordingly.
     * @return A Pair containing the randomized options list and the new correct option index
     */
    public OptionRandomizationResult getRandomizedOptions() {
        List<String> randomizedOptions = new ArrayList<>(options);
        String correctAnswer = options.get(correctOptionIndex);
        
        Collections.shuffle(randomizedOptions);
        
        int newCorrectIndex = randomizedOptions.indexOf(correctAnswer);
        
        return new OptionRandomizationResult(randomizedOptions, newCorrectIndex);
    }

    public static class OptionRandomizationResult {
        private final List<String> randomizedOptions;
        private final int newCorrectIndex;
        
        public OptionRandomizationResult(List<String> randomizedOptions, int newCorrectIndex) {
            this.randomizedOptions = randomizedOptions;
            this.newCorrectIndex = newCorrectIndex;
        }
        
        public List<String> getRandomizedOptions() {
            return randomizedOptions;
        }
        
        public int getNewCorrectIndex() {
            return newCorrectIndex;
        }
    }
}

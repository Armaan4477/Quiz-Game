package org.main.quiz;

import java.util.Date;

public class LeaderboardEntry {
    private String id;
    private String playerName;
    private int score;
    private int totalQuestions;
    private int timeSpentSeconds;
    private String category;
    private Date timestamp;

    public LeaderboardEntry() {
    }

    public LeaderboardEntry(String playerName, int score, int totalQuestions, 
                            int timeSpentSeconds, String category) {
        this.playerName = playerName;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.timeSpentSeconds = timeSpentSeconds;
        this.category = category;
        this.timestamp = new Date();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(int timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return playerName + " - Score: " + score + "/" + totalQuestions + 
               " - Time: " + formatTime(timeSpentSeconds);
    }

    public static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }
}

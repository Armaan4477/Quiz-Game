package org.main.quiz;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class Firebase {
    private static String DATABASE_URL;
    private static String AUTH_PARAM;
    private static final String QUESTIONS_NODE = "questions";
    private static final String LEADERBOARD_NODE = "leaderboard";
    private static final String ADMIN_NODE = "admin";
    
    static {
        try (InputStream input = Firebase.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            {
                prop.load(input);
                DATABASE_URL = prop.getProperty("firebase.database.url");
                String authKey = prop.getProperty("firebase.auth.key");
                AUTH_PARAM = "?auth=" + authKey;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to load Firebase configuration", ex);    
        }
    }

    public static String pushQuestion(Question question) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("text", question.getText());
        
        JSONArray optionsArray = new JSONArray();
        for (String option : question.getOptions()) {
            optionsArray.put(option);
        }
        
        jsonObject.put("options", optionsArray);
        jsonObject.put("correctOptionIndex", question.getCorrectOptionIndex());
        jsonObject.put("category", question.getCategory());
        jsonObject.put("difficultyLevel", question.getDifficultyLevel());
        
        String jsonPayload = jsonObject.toString();

        String urlString = DATABASE_URL + QUESTIONS_NODE + "/" + question.getId() + ".json" + AUTH_PARAM;
        URL url = URI.create(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        InputStream is = (responseCode >= 200 && responseCode < 300) 
                         ? connection.getInputStream() 
                         : connection.getErrorStream();
                         
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line.trim());
            }
        }

        if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
            throw new IOException("Firebase request failed with response code: " + responseCode + 
                                "\nResponse: " + response);
        }

        return response.toString();
    }

    public static List<Question> getAllQuestions() throws IOException {
        String urlString = DATABASE_URL + QUESTIONS_NODE + ".json" + AUTH_PARAM;
        URL url = URI.create(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        InputStream is = (responseCode >= 200 && responseCode < 300) 
                         ? connection.getInputStream() 
                         : connection.getErrorStream();

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to get questions. Response code: " + responseCode);
        }

        List<Question> questions = new ArrayList<>();
        JSONObject jsonResponse = new JSONObject(response.toString());
        
        if (!jsonResponse.isEmpty()) {
            for (String key : jsonResponse.keySet()) {
                JSONObject questionJson = jsonResponse.getJSONObject(key);
                
                List<String> options = new ArrayList<>();
                JSONArray optionsArray = questionJson.getJSONArray("options");
                for (int i = 0; i < optionsArray.length(); i++) {
                    options.add(optionsArray.getString(i));
                }
                
                Question question = new Question(
                    key,
                    questionJson.getString("text"),
                    options,
                    questionJson.getInt("correctOptionIndex"),
                    questionJson.optString("category", "General"),
                    questionJson.optInt("difficultyLevel", 1)
                );
                questions.add(question);
            }
        }

        return questions;
    }
    
    public static List<Question> getQuestionsByCategory(String category) throws IOException {

        String encodedCategory = URLEncoder.encode("\"" + category + "\"", StandardCharsets.UTF_8);
        String encodedOrderBy = URLEncoder.encode("\"category\"", StandardCharsets.UTF_8);
        
        String urlString = DATABASE_URL + QUESTIONS_NODE + ".json" + AUTH_PARAM + 
                           "&orderBy=" + encodedOrderBy + "&equalTo=" + encodedCategory;
                           
        URL url = URI.create(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        InputStream is = (responseCode >= 200 && responseCode < 300) 
                          ? connection.getInputStream() 
                          : connection.getErrorStream();

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to get questions. Response code: " + responseCode);
        }

        List<Question> questions = new ArrayList<>();
        JSONObject jsonResponse = new JSONObject(response.toString());
        
        if (!jsonResponse.isEmpty()) {
            for (String key : jsonResponse.keySet()) {
                JSONObject questionJson = jsonResponse.getJSONObject(key);
                
                List<String> options = new ArrayList<>();
                JSONArray optionsArray = questionJson.getJSONArray("options");
                for (int i = 0; i < optionsArray.length(); i++) {
                    options.add(optionsArray.getString(i));
                }
                
                Question question = new Question(
                    key,
                    questionJson.getString("text"),
                    options,
                    questionJson.getInt("correctOptionIndex"),
                    questionJson.optString("category", "General"),
                    questionJson.optInt("difficultyLevel", 1)
                );
                questions.add(question);
            }
        }

        return questions;
    }

    public static String addLeaderboardEntry(LeaderboardEntry entry) throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("playerName", entry.getPlayerName());
        jsonObject.put("score", entry.getScore());
        jsonObject.put("totalQuestions", entry.getTotalQuestions());
        jsonObject.put("timeSpentSeconds", entry.getTimeSpentSeconds());
        jsonObject.put("category", entry.getCategory());
        jsonObject.put("timestamp", entry.getTimestamp().getTime());
        
        String jsonPayload = jsonObject.toString();

        String urlString = DATABASE_URL + LEADERBOARD_NODE + ".json" + AUTH_PARAM;
        URL url = URI.create(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        InputStream is = (responseCode >= 200 && responseCode < 300) 
                         ? connection.getInputStream() 
                         : connection.getErrorStream();
                         
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line.trim());
            }
        }

        if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
            throw new IOException("Firebase request failed with response code: " + responseCode + 
                                "\nResponse: " + response);
        }

        return response.toString();
    }

    public static List<LeaderboardEntry> getLeaderboardEntries() throws IOException {
        String urlString = DATABASE_URL + LEADERBOARD_NODE + ".json" + AUTH_PARAM + "&orderBy=%22$key%22&limitToLast=50";
        
        URL url = URI.create(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        InputStream is = (responseCode >= 200 && responseCode < 300) 
                         ? connection.getInputStream() 
                         : connection.getErrorStream();

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to get leaderboard. Response code: " + responseCode);
        }

        List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();
        String responseString = response.toString();
        
        if (responseString == null || responseString.equals("null") || responseString.trim().isEmpty()) {
            return leaderboardEntries;
        }
        
        JSONObject jsonResponse = new JSONObject(responseString);
        
        if (!jsonResponse.isEmpty()) {
            for (String key : jsonResponse.keySet()) {
                JSONObject entryJson = jsonResponse.getJSONObject(key);
                
                LeaderboardEntry entry = new LeaderboardEntry();
                entry.setId(key);
                entry.setPlayerName(entryJson.getString("playerName"));
                entry.setScore(entryJson.getInt("score"));
                entry.setTotalQuestions(entryJson.getInt("totalQuestions"));
                entry.setTimeSpentSeconds(entryJson.getInt("timeSpentSeconds"));
                entry.setCategory(entryJson.optString("category", "General"));
                entry.setTimestamp(new Date(entryJson.getLong("timestamp")));
                
                leaderboardEntries.add(entry);
            }
        }

        Collections.sort(leaderboardEntries, (a, b) -> {
            int scoreCompare = Integer.compare(b.getScore(), a.getScore());
            if (scoreCompare != 0) {
                return scoreCompare;
            }
            return Integer.compare(a.getTimeSpentSeconds(), b.getTimeSpentSeconds());
        });

        return leaderboardEntries;
    }

    public static boolean authenticateAdmin(String username, String password) throws IOException {
        String urlString = DATABASE_URL + ADMIN_NODE + ".json" + AUTH_PARAM;
        URL url = URI.create(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to authenticate. Response code: " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }

        JSONObject adminData = new JSONObject(response.toString());
        
        // Check if the provided credentials match
        if (adminData.has(username)) {
            JSONObject userData = adminData.getJSONObject(username);
            if (userData.has("password") && userData.getString("password").equals(password)) {
                return true;
            }
        }
        
        return false;
    }

    public static void updateQuestion(Question question) throws IOException {
        if (question.getId() == null || question.getId().isEmpty()) {
            throw new IOException("Question ID cannot be empty for update operation");
        }
        
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("text", question.getText());
        
        JSONArray optionsArray = new JSONArray();
        for (String option : question.getOptions()) {
            optionsArray.put(option);
        }
        
        jsonObject.put("options", optionsArray);
        jsonObject.put("correctOptionIndex", question.getCorrectOptionIndex());
        jsonObject.put("category", question.getCategory());
        jsonObject.put("difficultyLevel", question.getDifficultyLevel());
        
        String jsonPayload = jsonObject.toString();

        String urlString = DATABASE_URL + QUESTIONS_NODE + "/" + question.getId() + ".json" + AUTH_PARAM;
        URL url = URI.create(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setDoOutput(true);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            InputStream errorStream = connection.getErrorStream();
            StringBuilder errorResponse = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) {
                    errorResponse.append(line.trim());
                }
            }
            throw new IOException("Firebase request failed with response code: " + responseCode + 
                               "\nResponse: " + errorResponse);
        }
    }

    public static void deleteQuestion(String questionId) throws IOException {
        String urlString = DATABASE_URL + QUESTIONS_NODE + "/" + questionId + ".json" + AUTH_PARAM;
        URL url = URI.create(urlString).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("DELETE");
        
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            InputStream errorStream = connection.getErrorStream();
            StringBuilder errorResponse = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) {
                    errorResponse.append(line.trim());
                }
            }
            throw new IOException("Firebase delete request failed with response code: " + responseCode + 
                               "\nResponse: " + errorResponse);
        }
    }
}

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

        String urlString = DATABASE_URL + QUESTIONS_NODE + ".json" + AUTH_PARAM;
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
}

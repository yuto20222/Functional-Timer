package persistence;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import model.PomodoroSession;
import model.Statistics;
import model.Task;
import org.json.*;

// Represents a reader that reads workroom from JSON data stored in file
public class JsonReader {
    private String source;

    // EFFECTS: constructs reader to read from source file
    public JsonReader(String source) {
        this.source = source;
    }

    // EFFECTS: reads data from file and returns a PomodoroSession object;
    // throws IOException if an error occurs reading data from file
    public PomodoroSession readPomodoroSession() throws IOException {
        String jsonData = readFile(source);
        JSONObject jsonObject = new JSONObject(jsonData);
        return parsePomodoroSession(jsonObject);
    }

    // EFFECTS: reads data from file and returns a Statistics object;
    // throws IOException if an error occurs reading data from file
    public Statistics readStatistics() throws IOException {
        String jsonData = readFile(source);
        JSONObject jsonObject = new JSONObject(jsonData);
        return parseStatistics(jsonObject);
    }

    // EFFECTS: reads source file as string and returns it as a List<Task>
    public List<Task> readTasks() throws IOException {
        String jsonData = readFile(source);
        JSONArray jsonArray = new JSONArray(jsonData);
        List<Task> taskList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Task task = parseTask(jsonObject);
            taskList.add(task);
        }
        return taskList;
    }

    // EFFECTS: parses task from JSON object and returns it
    private Task parseTask(JSONObject jsonObject) {
        String name = jsonObject.optString("taskName", "");
        return new Task(name);
    }

    // EFFECTS: reads source file as string and returns it
    private String readFile(String source) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();

        try (Stream<String> stream = Files.lines(Paths.get(source), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s));
        }

        return contentBuilder.toString();
    }

    // EFFECTS: parses workroom from JSON object and returns it
    private PomodoroSession parsePomodoroSession(JSONObject jsonObject) {
        int setWorkDuration = jsonObject.optInt("setWorkDuration", 25);  // default is 25 if not present
        int setShortBreakDuration = jsonObject.optInt("setShortBreakDuration", 5);  // default is 5 if not present
        int setLongBreakDuration = jsonObject.optInt("setLongBreakDuration", 10);  // default is 10 if not present

        JSONObject statJson = jsonObject.optJSONObject("statistics");
        Statistics stat = parseStatistics(statJson);

        return new PomodoroSession(setWorkDuration, setShortBreakDuration, setLongBreakDuration, stat);
    }

    // EFFECTS: parses Statistics from JSON object and returns it
    private Statistics parseStatistics(JSONObject jsonObject) {
        Statistics stat = new Statistics();
        addCompletedTasks(stat, jsonObject);
        return stat;
    }

    // MODIFIES: stat
    // EFFECTS: parses CompletedTasks from JSON object and adds them to Statistics
    private void addCompletedTasks(Statistics stat, JSONObject jsonObject) {
        JSONArray jsonArray = jsonObject.optJSONArray("tasks");  // Change "completedTasks" to "tasks"
        if (jsonArray != null) {
            for (Object json : jsonArray) {
                JSONObject nextThingy = (JSONObject) json;
                addCompletedTask(stat, nextThingy);
            }
        } else {
            System.err.println("Warning: tasks key not found in the JSON object.");
        }
    }

    // MODIFIES: stat
    // EFFECTS: parses thingy from JSON object and adds it to Statistics
    private void addCompletedTask(Statistics stat, JSONObject jsonObject) {
        String name = jsonObject.getString("name");
        Task task = new Task(name);
        stat.addCompletedTaskList(task);
    }


}


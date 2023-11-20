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

//    // EFFECTS: reads data from file and returns a Statistics object;
//    // throws IOException if an error occurs reading data from file
//    public Statistics readStatistics() throws IOException {
//        String jsonData = readFile(source);
//        JSONObject jsonObject = new JSONObject(jsonData);
//        return parseStatistics(jsonObject);
//    }

    // EFFECTS: reads source file as string and returns it as a List<Task>
    public List<Task> readTasks() throws IOException {
        String jsonData = readFile(source);
        JSONObject fullJson = new JSONObject(jsonData);
        JSONArray jsonArray = fullJson.getJSONArray("tasks");
        List<Task> taskList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Task task = parseTask(jsonObject);
            taskList.add(task);
        }
        return taskList;
    }

    // EFFECTS: parses task from JSON object and returns it
    private Task parseTask(JSONObject taskJson) {
        String taskName = taskJson.getString("taskName");
        boolean isCompleted = taskJson.getBoolean("isCompleted");

        Task task = new Task(taskName);
        if (isCompleted) {
            task.markIfCompleted(); // This method is used instead of directly setting isCompleted to true
        }
        return task;
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
        JSONObject sessionData = jsonObject.getJSONObject("pomodoroSession");

        int setWorkDuration = sessionData.getInt("workDuration");
        int setShortBreakDuration = sessionData.getInt("shortBreakDuration");
        int setLongBreakDuration = sessionData.getInt("longBreakDuration");

        boolean isRunning = sessionData.optBoolean("isRunning", false);
        boolean isOnBreak = sessionData.optBoolean("isOnBreak", false);
        int currentDuration = sessionData.optInt("currentDuration", setWorkDuration * 60);

        JSONObject statisticsData = sessionData.getJSONObject("statistics");
        Statistics stat = parseStatistics(statisticsData);

        PomodoroSession pomodoroSession
                = new PomodoroSession(setWorkDuration, setShortBreakDuration, setLongBreakDuration, stat);

        pomodoroSession.setRunning(isRunning);
        pomodoroSession.setOnBreak(isOnBreak);
        pomodoroSession.setCurrentDuration(currentDuration);

        if (pomodoroSession.isRunning()) {
            pomodoroSession.startTimer();
        }

        return pomodoroSession;
    }

//    private void addStatistics(PomodoroSession ps, JSONObject jsonObject) {
//        JSONArray jsonArray = jsonObject.getJSONArray("statistics");
//        for (Object json : jsonArray) {
//            JSONObject nextThingy = (JSONObject) json;
//            addStatistic(ps, nextThingy);
//        }
//    }

//    private void addStatistic(PomodoroSession ps, JSONObject jsonObject) {
//        int totalWork = jsonObject.getInt("totalWorkTime");
//        int completedSessions = jsonObject.getInt("completedSessions");
//        Task completedTask = jsonObject.getJSONArray("tasks");
//    }

    // EFFECTS: parses Statistics from JSON object and returns it
    private Statistics parseStatistics(JSONObject jsonObject) {
        int totalWorkTime = jsonObject.optInt("totalWorkTime", 0); // default
        int completedSessions = jsonObject.optInt("completedSessions", 0); // default

        Statistics statistics = new Statistics();
        for (int i = 0; i < completedSessions; i++) {
            statistics.addCompletedSession();
        }
        statistics.addTotalWorkTime(totalWorkTime);

        // Parse completed tasks if they exist
        if (jsonObject.has("tasks")) {
            JSONArray tasksArray = jsonObject.getJSONArray("tasks");
            for (int i = 0; i < tasksArray.length(); i++) {
                JSONObject taskJson = tasksArray.getJSONObject(i);
                Task task = parseTask(taskJson); // when isCompleted is true
                statistics.addCompletedTaskList(task);
            }
        }

        return statistics;
    }

//    // MODIFIES: stat
//    // EFFECTS: parses CompletedTasks from JSON object and adds them to Statistics
//    public void addCompletedTasks(Statistics stat, JSONObject jsonObject) {
//        JSONArray jsonArray = jsonObject.optJSONArray("tasks");  // Change "completedTasks" to "tasks"
//        if (jsonArray != null) {
//            for (Object json : jsonArray) {
//                JSONObject nextThingy = (JSONObject) json;
//                addCompletedTask(stat, nextThingy);
//            }
//        }
////        else {
////            System.err.println("Warning: tasks key not found in the JSON object.");
////        }
//    }
//
//    // MODIFIES: stat
//    // EFFECTS: parses thingy from JSON object and adds it to Statistics
//    private void addCompletedTask(Statistics stat, JSONObject jsonObject) {
//        String name = jsonObject.getString("name");
//        Task task = new Task(name);
//        stat.addCompletedTaskList(task);
//    }

}


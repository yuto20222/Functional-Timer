package persistence;

import model.PomodoroSession;
import model.Task;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.List;

// Represents a writer that writes JSON representation of workroom to file
public class JsonWriter {
    private static final int TAB = 4;
    private PrintWriter writer;
    private String destination;

    // EFFECTS: constructs writer to write to destination file
    public JsonWriter(String destination) {
        this.destination = destination;
    }

    // MODIFIES: this
    // EFFECTS: opens writer; throws FileNotFoundException if destination file cannot
    // be opened for writing
    public void open() throws FileNotFoundException {
        writer = new PrintWriter(new File(destination));
    }

    // MODIFIES: this
    // EFFECTS: writes JSON representation of all data to file
    public void write(PomodoroSession ps, List<Task> taskList) {
        JSONObject json = new JSONObject();
        json.put("pomodoroSession", ps.toJson());
//        json.put("statistics", st.toJson());

        JSONArray taskArray = new JSONArray();
        for (Task task : taskList) {
            taskArray.put(task.toJson());
        }
        json.put("tasks", taskArray);

        saveToFile(json.toString(TAB));
    }

    // MODIFIES: this
    // EFFECTS: closes writer
    public void close() {
        writer.close();
    }

    // MODIFIES: this
    // EFFECTS: writes string to file
    private void saveToFile(String json) {
        writer.print(json);
    }
}

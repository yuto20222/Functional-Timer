package persistence;

import model.PomodoroSession;
import model.Statistics;
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
    // EFFECTS: writes JSON representation of workroom to file
    public void write(PomodoroSession ps) {
        JSONObject json = ps.toJson();
        saveToFile(json.toString(TAB));
    }

    // MODIFIES: this
    // EFFECTS: writes JSON representation of workroom to file
    public void write(Statistics st) {
        JSONObject json = st.toJson();
        saveToFile(json.toString(TAB));
    }

    // MODIFIES: this
    // EFFECTS: writes JSON representation of workroom to file
    public void write(List<Task> taskList) throws FileNotFoundException {
        JSONArray jsonArray = new JSONArray();
        for (Task task : taskList) {
            jsonArray.put(task.toJson());
        }
        saveToFile(jsonArray.toString());
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

package model;

import org.json.JSONObject;
import persistence.Writable;


// Represents a task with a specific name.
// Keeps track of the task's completion status.
public class Task implements Writable {
    private final String taskName;
    private boolean isCompleted;

    /*
     * MODIFIES: this
     * EFFECTS: Initializes a new Task with the given task name and sets its completion status to false.
     */
    public Task(String taskName) {
        this.taskName = taskName;
        this.isCompleted = false;
    }

    /*
     * MODIFIES: this
     * EFFECTS: sets its completion status to true.
     */
    public void markIfCompleted() {
        this.isCompleted = true;
    }

    /*
     * EFFECTS: check its completion status
     */
    public boolean isCompleted() {
        return isCompleted;
    }

    public String getTaskName() {
        return taskName;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("taskName", taskName);
        json.put("isCompleted", isCompleted);
        return json;
    }
}

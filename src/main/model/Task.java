package model;

import org.json.JSONObject;
import persistence.Writable;

import model.Event;
import model.EventLog;

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
        EventLog.getInstance().logEvent(new Event("Uncompleted Task is added: " + this.taskName));
    }

    /*
     * MODIFIES: this
     * EFFECTS: sets its completion status to true.
     */
    public void markIfCompleted() {
        this.isCompleted = true;
        // Recorded in event log
        EventLog.getInstance().logEvent(new Event("Uncompleted Task is completed: " + this.taskName));
    }

    /*
     * EFFECTS: check its completion status
     */
    public boolean isCompleted() {
        return isCompleted;
    }

    /*
     * EFFECTS: Returns the task's name.
     */
    public String getTaskName() {
        return taskName;
    }

    /*
     * EFFECTS: Returns this task as a JSON object.
     */
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("taskName", taskName);
        json.put("isCompleted", isCompleted);
        return json;
    }
}

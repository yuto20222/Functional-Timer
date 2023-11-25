package model;

import model.Event;
import model.EventLog;

import org.json.JSONArray;
import org.json.JSONObject;
import persistence.Writable;

import java.util.ArrayList;
import java.util.List;

// Represents the statistics related to work sessions.
// Keeps track of the number of completed sessions and the total work time.
public class Statistics implements Writable {
    private int completedSessions;
    private int totalWorkTime;
    private List<Task> completedTaskList;

    /*
     * MODIFIES: this
     * EFFECTS: Initializes a new Statistics object with zero completed sessions and total work time.
     */
    public Statistics() {
        this.completedSessions = 0;
        this.totalWorkTime = 0;
        completedTaskList = new ArrayList<>();
    }

    /*
     * MODIFIES: this
     * EFFECTS: Increments the number of completed sessions by one
     */
    public void addCompletedSession() {
        this.completedSessions += 1;
    }

    /*
     * REQUIRES: workDuration is a positive integer
     * MODIFIES: this
     * EFFECTS:  increments the number of totalWorkTime by completed time
     */
    public void addTotalWorkTime(int time) {
        this.totalWorkTime += time;
    }

    /*
     * MODIFIES: this
     * EFFECTS: Adds the given task to the list of completed tasks.
     */
    public void addCompletedTaskList(Task task) {
        this.completedTaskList.add(task);

        // for event log
        EventLog.getInstance().logEvent(new Event("Completed Task added to the stat: " + task.getTaskName()));
    }

    /*
     * EFFECTS: Returns the number of completed sessions.
     */
    public int getCompletedSessions() {
        return completedSessions;
    }

    /*
     * EFFECTS: Returns the total work time.
     */
    public int getTotalWorkTime() {
        return totalWorkTime;
    }

    // EFFECTS: Return the size of completedTaskList
    public int getCompletedTaskSize() {
        return completedTaskList.size();
    }

    public List<Task> getCompletedTaskList() {
        return completedTaskList;
    }

    /*
     * EFFECTS: Returns this statistics as a JSON object.
     */
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("completedSessions", completedSessions);
        json.put("totalWorkTime", totalWorkTime);
        JSONArray tasksArray = new JSONArray();
        for (Task task : completedTaskList) {
            tasksArray.put(task.toJson());
        }
        json.put("tasks", tasksArray);
        return json;
    }
}

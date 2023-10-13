package model;

// Represents the statistics related to work sessions.
// Keeps track of the number of completed sessions and the total work time.
public class Statistics {
    private int completedSessions;
    private int totalWorkTime;

    /*
     * MODIFIES: this
     * EFFECTS: Initializes a new Statistics object with zero completed sessions and total work time.
     */
    public Statistics() {
        this.completedSessions = 0;
        this.totalWorkTime = 0;
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


    public int getCompletedSessions() {
        return completedSessions;
    }

    public int getTotalWorkTime() {
        return totalWorkTime;
    }
}

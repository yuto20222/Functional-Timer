package model;

import model.Event;
import model.EventLog;
import org.json.JSONObject;
import persistence.Writable;

import java.util.Timer;
import java.util.TimerTask;

// Represents a Pomodoro work session with configurable work and break durations.
// Allows starting, stopping, and resetting of the timer and keeps track of session statistics.
public class PomodoroSession implements Writable {
    // delete or rename this class!
    private final int workDuration;
    private final int shortBreakDuration;
    private final int longBreakDuration;

    private int currentDuration;
    private boolean isRunning;
    private boolean isOnBreak;
    private Statistics stat;
    private Timer timer;

    /*
     * REQUIRES: every duration is a positive integer
     * MODIFIES: this
     * EFFECTS: Initializes a new Pomodoro session with the given work duration.
     *          If workDuration is negative, sets it to the default value.
     *          Each boolean value is set "false"
     *          set "Timer" class as real timer, or countdown clock
     */
    public PomodoroSession(int setWorkDuration, int setShortBreakDuration, int setLongBreakDuration, Statistics stat) {
        if (setWorkDuration < 0) {
            setWorkDuration = 25; //default
        }
        if (setShortBreakDuration < 0) {
            setShortBreakDuration = 5; //default
        }
        if (setLongBreakDuration < 0) {
            setLongBreakDuration = 10; //default
        }
        this.workDuration = setWorkDuration * 60; //from min to seconds
        this.shortBreakDuration = setShortBreakDuration * 60;
        this.longBreakDuration = setLongBreakDuration * 60;
        this.stat = stat;
        this.isRunning = false;
        this.isOnBreak = false;
        this.currentDuration = workDuration;
        this.timer = new Timer();
    }

    /*
     * MODIFIES: this
     * EFFECTS: everything will begin from this method
     *          it will call startTimer()
     */
    public void startWork() {
        isRunning = true;
        isOnBreak = false;
        currentDuration = workDuration;
        startTimer();
    }

    /*
     * MODIFIES: this, Time
     * EFFECTS: it can start every session with another methods
     *          this is the core timer
     *          when currentDuration is over, it will move to endWork method to take a break
     */
    // https://www.delftstack.com/ja/howto/java/countdown-timer-java/
    public void startTimer() {
        if (!isRunning) {
            isRunning = true;  // for stop
        }
        if (timer != null) {
            timer.cancel();
            timer = new Timer();
        }  // test for when timer is null
        assert timer != null; //I don't know why it is necessary
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                currentDuration = currentDuration - 1; //every second
                if (currentDuration <= 0) {  //test for when timer is running out
                    timer.cancel();
                    if (!isOnBreak) {
//                        isOnBreak = false; // for break
                        endWork();
                    } else {
//                        JOptionPane.showMessageDialog(null, "Break is done",
//                        "let's study", JOptionPane.INFORMATION_MESSAGE);
                        startWork(); //?????
                    }
                }
            }
        }, 0, 1000);
    }

    /*
     * MODIFIES: this, Statistics
     * EFFECTS: it can start breakTime
     *          it will call stat's method to record the number of finished study session and its length
     *          if user already finish studySession three times, take longBreak
     *          Otherwise, shortBreak
     */
    public void endWork() {
        isRunning = false; //stop workSession
        timer.cancel();

        stat.addCompletedSession();
        stat.addTotalWorkTime(workDuration);
        if (stat.getCompletedSessions() % 3 == 0) {
            startLongBreak();
        } else {
            startShortBreak();
        }
    }

    /*
     * MODIFIES: this
     * EFFECTS: it can start shortBreakSession with startTimer method
     */
    public void startShortBreak() {
        isRunning = true;
        isOnBreak = true;
        currentDuration = shortBreakDuration;
//        JOptionPane.showMessageDialog(null, "short break begins", "let's take a break",
//        JOptionPane.INFORMATION_MESSAGE);
        startTimer();
    }

    /*
     * MODIFIES: this
     * EFFECTS: it can start longBreakSession with startTimer method
     */
    public void startLongBreak() {
        isRunning = true;
        isOnBreak = true;
        currentDuration = longBreakDuration;
//        JOptionPane.showMessageDialog(null, "long break begins", "let's take a break",
//        JOptionPane.INFORMATION_MESSAGE);
        startTimer();
    }

    /*
     * MODIFIES: this
     * EFFECTS: Stops the current timer.
     */
    public void stop() {
        isRunning = false;
        timer.cancel();
    }

    /*
     * MODIFIES: this
     * EFFECTS: Resets and stops the timer.
     */
    public void resetTimer() {
        timer.cancel();
        isRunning = false;
        isOnBreak = false;
//        stat = new Statistics();
        timer = new Timer();
    }

    /*
     * EFFECTS: Returns the work duration of the session.
     */
    public int getWorkDuration() {
        return workDuration;
    }

    /*
     * EFFECTS: Returns the short break duration of the session.
     */
    public int getShortBreakDuration() {
        return shortBreakDuration;
    }

    /*
     * EFFECTS: Returns the long break duration of the session.
     */
    public int getLongBreakDuration() {
        return longBreakDuration;
    }

    /*
     * EFFECTS: Returns the current duration left on the timer.
     */
    public int getCurrentDuration() {
        return currentDuration;
    }

    /*
     * EFFECTS: Returns true if the session is currently running, false otherwise.
     */
    public boolean isRunning() {
        return isRunning;
    }

    /*
     * EFFECTS: Returns true if the session is currently on a break, false otherwise.
     */
    public boolean isOnBreak() {
        return isOnBreak;
    }

    public Statistics getStatistics() {
        return this.stat;
    }

    /*
     * MODIFIES: this
     * EFFECTS: Sets the timer for the session.
     */
    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    // MODIFIES: this
    // EFFECTS: sets the current duration
    public void setCurrentDuration(int currentDuration) {
        this.currentDuration = currentDuration;
    }

    // MODIFIES: this
    // EFFECTS: sets the running state
    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    // MODIFIES: this
    // EFFECTS: sets the on break state
    public void setOnBreak(boolean isOnBreak) {
        this.isOnBreak = isOnBreak;
    }

    /*
     * EFFECTS: Returns this Pomodoro session as a JSON object.
     */
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("workDuration", this.workDuration / 60);
        json.put("shortBreakDuration", this.shortBreakDuration / 60);
        json.put("longBreakDuration", this.longBreakDuration / 60);
        json.put("currentDuration", this.currentDuration);
        json.put("isRunning", this.isRunning);
        json.put("isOnBreak", this.isOnBreak);
        json.put("statistics", this.stat.toJson()); // Assuming Statistics class also implements Writable
        return json;
    }
}

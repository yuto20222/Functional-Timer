package model;

import java.util.Timer;
import java.util.TimerTask;

// Represents a Pomodoro work session with configurable work and break durations.
// Allows starting, stopping, and resetting of the timer and keeps track of session statistics.
public class PomodoroSession {
    // delete or rename this class!
    private int workDuration;
    private int shortBreakDuration;
    private int longBreakDuration;

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
        this.workDuration = setWorkDuration * 60; //from min to s
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
    private void startTimer() {
        if (timer != null) {
            timer.cancel();
            timer = new Timer();
        }
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                currentDuration = currentDuration - 1;
                if (currentDuration < 0) {
                    timer.cancel();
                    if (isOnBreak) {
                        isOnBreak = false;
                    } else {
                        endWork();
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
        startTimer();
    }

    public void stop() {
        isRunning = false;
        timer.cancel();
    }

    public void resetTimer() {
        timer.cancel();
        currentDuration = workDuration;
        isRunning = false;
        isOnBreak = false;
        timer = new Timer();
    }

    public int getWorkDuration() {
        return workDuration;
    }

    public int getShortBreakDuration() {
        return shortBreakDuration;
    }

    public int getLongBreakDuration() {
        return longBreakDuration;
    }

    public int getCurrentDuration() {
        return currentDuration;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isOnBreak() {
        return isOnBreak;
    }
}

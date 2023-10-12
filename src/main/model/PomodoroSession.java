package model;

public class PomodoroSession {
    // delete or rename this class!
    private int workDuration;
    private int shortBreakDuration;
    private int longBreakDuration;
    private Statistics stat;

    /*
     * REQUIRES: every duration is a positive integer
     * MODIFIES: this
     * EFFECTS: Initializes a new Pomodoro session with the given work duration.
     *          If workDuration is negative, sets it to the default value.
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
        this.workDuration = setWorkDuration;
        this.shortBreakDuration = setShortBreakDuration;
        this.longBreakDuration = setLongBreakDuration;
        this.stat = stat;
    }

    /*
     * REQUIRES: workDuration is over
     * MODIFIES: stat
     * EFFECTS: Occurs addCompletedSession method and addTotalWorkTime method with value of workDuration
     *          if user finishes work three times, user can take a long break
     *          Otherwise, user take a short break
     */
    public int workEnd() {
        stat.addCompletedSession();
        stat.addTotalWorkTime(workDuration);
        if (stat.getCompletedSessions() % 3 == 0) {
            return getLongBreakDuration();
        } else {
            return getShortBreakDuration();
        }
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

}

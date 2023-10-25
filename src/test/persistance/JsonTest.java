package persistance;

import model.PomodoroSession;
import model.Statistics;
import model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonTest {

    protected void checkPomodoroSession(int workDuration, int shortBreak, int longBreak, PomodoroSession session) {
        assertEquals(workDuration, session.getWorkDuration());
        assertEquals(shortBreak, session.getShortBreakDuration());
        assertEquals(longBreak, session.getLongBreakDuration());
    }

    protected void checkStatistics(int completedSessions, int totalWorkTime, Statistics statistics) {
        assertEquals(completedSessions, statistics.getCompletedSessions());
        assertEquals(totalWorkTime, statistics.getTotalWorkTime());
    }

    protected void checkTask(String taskName, boolean isCompleted, Task task) {
        assertEquals(taskName, task.getTaskName());
        assertEquals(isCompleted, task.isCompleted());
    }
}
package model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class PomodoroSessionTest {
    private PomodoroSession testPomodoro;
    private Statistics testStat;

    @BeforeEach
    void runBefore() {
        testStat = new Statistics();
        testPomodoro = new PomodoroSession(20, 4, 8, testStat);
    }
    @Test
    void testConstructor() {
        assertEquals(20, testPomodoro.getWorkDuration());
        assertEquals(4, testPomodoro.getShortBreakDuration());
        assertEquals(8, testPomodoro.getLongBreakDuration());
    }

    @Test
    void testConstructorNegativeNum() {
        testPomodoro = new PomodoroSession(-1,-1, -1, testStat);
        assertEquals(25, testPomodoro.getWorkDuration());
        assertEquals(5, testPomodoro.getShortBreakDuration());
        assertEquals(10, testPomodoro.getLongBreakDuration());
    }

    @Test
    void testWorkEnd() {
        assertEquals(4, testPomodoro.workEnd());
        assertEquals(4, testPomodoro.workEnd());
        assertEquals(8, testPomodoro.workEnd());
    }
}

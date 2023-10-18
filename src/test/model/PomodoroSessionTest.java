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
        assertEquals(1200, testPomodoro.getWorkDuration());
        assertEquals(240, testPomodoro.getShortBreakDuration());
        assertEquals(480, testPomodoro.getLongBreakDuration());
    }

    @Test
    void testConstructorNegativeNum() {
        testPomodoro = new PomodoroSession(-1,-1, -1, testStat);
        assertEquals(1500, testPomodoro.getWorkDuration());
        assertEquals(300, testPomodoro.getShortBreakDuration());
        assertEquals(600, testPomodoro.getLongBreakDuration());
    }

    @Test
    public void testStartWork() {
        testPomodoro.startWork();
        assertTrue(testPomodoro.isRunning());
        assertFalse(testPomodoro.isOnBreak());
    }

    //https://www.digitalocean.com/community/tutorials/thread-sleep-java
    //I saw this website to write below code.
    @Test
    public void testStartTimerDecreasesCurrentDuration() throws InterruptedException {
        testPomodoro.startWork();
        int initialDuration = testPomodoro.getCurrentDuration();

        Thread.sleep(2000); // wait for 2 seconds

        assertTrue(testPomodoro.getCurrentDuration() < initialDuration);

    }

    @Test
    public void testStartTimerNotNull() {
        testPomodoro.setTimer(null);
    }


    @Test
    public void testStartTimer() {
        testPomodoro.startWork();
        assertEquals(1200, testPomodoro.getCurrentDuration());
        assertFalse(testPomodoro.isOnBreak());
    }

    @Test
    public void testStartShortBreak() {
        testPomodoro.startShortBreak();
        assertTrue(testPomodoro.isRunning());
        assertTrue(testPomodoro.isOnBreak());
    }
    @Test
    public void testStartLongBreak() {
        testPomodoro.startLongBreak();
        assertTrue(testPomodoro.isRunning());
        assertTrue(testPomodoro.isOnBreak());
    }

    @Test
    public void testStop() {
        testPomodoro.stop();
        assertFalse(testPomodoro.isRunning());
    }

    @Test
    public void testResetTimer() {
        testPomodoro.resetTimer();
        assertEquals(1200, testPomodoro.getWorkDuration());
        assertFalse(testPomodoro.isRunning());
        assertFalse(testPomodoro.isOnBreak());
    }

    @Test
    public void testEndWorkAfterOneSession() {
        testPomodoro.startWork();
        testPomodoro.endWork();
        assertTrue(testPomodoro.isOnBreak());
        assertEquals(testPomodoro.getShortBreakDuration(), testPomodoro.getCurrentDuration());
        assertEquals(1, testStat.getCompletedSessions());
    }

    // make sure if stat can have how many times this ends sessions correctly
    @Test
    public void testEndWorkAfterThreeSessions() {
        for (int i = 0; i < 3; i++) {
            testPomodoro.startWork();
            testPomodoro.endWork();
        }
        assertTrue(testPomodoro.isOnBreak());
        assertEquals(testPomodoro.getLongBreakDuration(), testPomodoro.getCurrentDuration());
        assertEquals(3, testStat.getCompletedSessions());
    }
}

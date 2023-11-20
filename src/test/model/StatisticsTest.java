package model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class StatisticsTest {
    private Statistics testStat;
    private Task task1;
    private Task task2;

    @BeforeEach
    void runBefore() {
        testStat = new Statistics();
        task1 = new Task("Task 1");
        task2 = new Task("Task 2");
    }

    @Test
    void testConstructor() {
        assertEquals(0, testStat.getCompletedSessions());
        assertEquals(0, testStat.getTotalWorkTime());
    }

    @Test
    void testAddCompletedSession() {
        testStat.addCompletedSession();
        assertEquals(1, testStat.getCompletedSessions());
    }

    @Test
    void testAddMultipleCompletedSession() {
        testStat.addCompletedSession();
        testStat.addCompletedSession();
        assertEquals(2, testStat.getCompletedSessions());
    }

    @Test
    void testAddTotalWorkTime() {
        testStat.addTotalWorkTime(20);
        assertEquals(20, testStat.getTotalWorkTime());
    }

    @Test
    void testAddMultipleTotalWorkTime() {
        testStat.addTotalWorkTime(20);
        assertEquals(20, testStat.getTotalWorkTime());
        testStat.addTotalWorkTime(0);
        assertEquals(20, testStat.getTotalWorkTime());
    }

    @Test
    void testAddCompletedTaskList() {
        testStat.addCompletedTaskList(task1);
        assertEquals(1, testStat.getCompletedTaskSize());

        testStat.addCompletedTaskList(task2);
        assertEquals(2, testStat.getCompletedTaskSize());
    }

    @Test
    void testGetCompletedTaskList() {
        testStat.addCompletedTaskList(task1);
        testStat.addCompletedTaskList(task2);

        List<Task> tasks = testStat.getCompletedTaskList();
        assertNotNull(tasks);
        assertEquals(2, tasks.size());
        assertTrue(tasks.contains(task1));
        assertTrue(tasks.contains(task2));
    }

}

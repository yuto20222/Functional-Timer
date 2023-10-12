package model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class StatisticsTest {
    private Statistics testStat;

    @BeforeEach
    void runBefore() {
        testStat = new Statistics();
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
}

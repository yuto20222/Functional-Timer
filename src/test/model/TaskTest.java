package model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class TaskTest {
    private Task testTask;

    @BeforeEach
    void runBefore() {
        testTask = new Task("Task1");
    }
    @Test
    void testConstructor() {
        assertEquals("Task1", testTask.getTaskName());
        assertFalse(testTask.isCompleted());
    }
    @Test
    void testMarkIfCompleted() {
        testTask.markIfCompleted();
        assertTrue(testTask.isCompleted());
    }
}

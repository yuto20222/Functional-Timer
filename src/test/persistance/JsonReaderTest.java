package persistance;


import model.PomodoroSession;
import model.Statistics;
import model.Task;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import persistence.JsonReader;
import persistence.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonReaderTest extends JsonTest {
    @Test
    void testReadPomodoroSession() {
        try {
            PomodoroSession ps = new PomodoroSession(25, 5, 10, new Statistics());
            List<Task> tasks = new ArrayList<>();
            tasks.add(new Task("Task 1"));
            tasks.add(new Task("Task 2"));

            JsonWriter writer = new JsonWriter("./data/testReadPomodoroSession.json");
            writer.open();
            writer.write(ps, tasks);  // Assuming JsonWriter has a method to write both session and tasks together.
            writer.close();

            JsonReader reader = new JsonReader("./data/testReadPomodoroSession.json");
            ps = reader.readPomodoroSession();
            // Here you should also validate the read PomodoroSession object if required.

        } catch (IOException e) {
            fail("IOException should not have been thrown");
        }
    }

    @Test
    void testReadTasks() {
        try {
            PomodoroSession ps = new PomodoroSession(25, 5, 10, new Statistics());
            List<Task> taskList = new ArrayList<>();
            taskList.add(new Task("Task 1"));
            taskList.add(new Task("Task 2"));

            JsonWriter writer = new JsonWriter("./data/testReadTasks.json");
            writer.open();
            writer.write(ps ,taskList);  // Assuming JsonWriter has a method writeTasks to write list of tasks.
            writer.close();

            JsonReader reader = new JsonReader("./data/testReadTasks.json");
            List<Task> readTaskList = reader.readTasks();
            assertEquals(2, readTaskList.size());
            checkTask("Task 1", false, readTaskList.get(0));
            checkTask("Task 2", false, readTaskList.get(1));

        } catch (IOException e) {
            fail("IOException should not have been thrown");
        }
    }

    @Test
    void testReadTasksJSONException() {
        JsonReader badReader = new JsonReader("./data/badTestFile.json");
        try {
            List<Task> tasks = badReader.readTasks();
            fail("JSONException should have been thrown");
        } catch (IOException | JSONException e) {
            // pass
        }
    }

    @Test
    void testReadStatistics() {
        try {
            // Set up a PomodoroSession with Statistics and write it to a file
            Statistics stats = new Statistics();
            stats.addCompletedSession();
            stats.addTotalWorkTime(120);
            stats.addCompletedTaskList(new Task("Completed Task 1"));

            PomodoroSession ps = new PomodoroSession(25, 5, 15, stats);
            JsonWriter writer = new JsonWriter("./data/testReadStatistics.json");
            writer.open();
            writer.write(ps, new ArrayList<>());
            writer.close();

            // Read the file and assert the statistics
            JsonReader reader = new JsonReader("./data/testReadStatistics.json");
            PomodoroSession readSession = reader.readPomodoroSession();
            Statistics readStats = readSession.getStatistics();

            assertEquals(1, readStats.getCompletedSessions());
            assertEquals(120, readStats.getTotalWorkTime());
            assertEquals(1, readStats.getCompletedTaskList().size());
            checkTask("Completed Task 1", false, readStats.getCompletedTaskList().get(0));

        } catch (IOException e) {
            fail("IOException should not have been thrown");
        }
    }

    @Test
    void testIOExceptionHandling() {
        JsonReader reader = new JsonReader("nonexistentfile.json");
        assertThrows(IOException.class, reader::readPomodoroSession);
        assertThrows(IOException.class, reader::readTasks);
    }

    @Test
    void testParseTask() {
        try {
            // Set up a task list and write it to a file
            List<Task> tasks = new ArrayList<>();
            tasks.add(new Task("Test Task"));
            tasks.get(0).markIfCompleted(); // Marking the task as completed

            JsonWriter writer = new JsonWriter("./data/testParseTask.json");
            writer.open();
            writer.write(new PomodoroSession(25, 5, 15, new Statistics()), tasks);
            writer.close();

            // Reading the task back
            JsonReader reader = new JsonReader("./data/testParseTask.json");
            List<Task> readTasks = reader.readTasks();

            // Check if the task read is as expected
            assertEquals(1, readTasks.size());
            assertTrue(readTasks.get(0).isCompleted());
            assertEquals("Test Task", readTasks.get(0).getTaskName());

        } catch (IOException e) {
            fail("IOException should not have been thrown");
        }
    }

    @Test
    void testParseStatistics() {
        try {
            // Set up a PomodoroSession with specific Statistics and write it to a file
            Statistics stats = new Statistics();
            stats.addCompletedSession();
            stats.addTotalWorkTime(150);

            PomodoroSession session = new PomodoroSession(25, 5, 15, stats);
            JsonWriter writer = new JsonWriter("./data/testParseStatistics.json");
            writer.open();
            writer.write(session, new ArrayList<>());
            writer.close();

            // Reading the session back
            JsonReader reader = new JsonReader("./data/testParseStatistics.json");
            PomodoroSession readSession = reader.readPomodoroSession();
            Statistics readStats = readSession.getStatistics();

            // Check if the statistics read are as expected
            assertEquals(1, readStats.getCompletedSessions());
            assertEquals(150, readStats.getTotalWorkTime());

        } catch (IOException e) {
            fail("IOException should not have been thrown");
        }
    }

    @Test
    void testParseStatisticsWithTasks() {
        try {
            // Set up Statistics with tasks and write it to a file
            Statistics stats = new Statistics();
            stats.addCompletedSession();
            stats.addTotalWorkTime(150);
            Task completedTask = new Task("Completed Task");
            Task completedTask2 = new Task("Completed Task");
            completedTask.markIfCompleted();
            completedTask2.markIfCompleted();
            stats.addCompletedTaskList(completedTask);
            stats.addCompletedTaskList(completedTask2);

            PomodoroSession session = new PomodoroSession(25, 5, 15, stats);
            JsonWriter writer = new JsonWriter("./data/testParseStatisticsWithTasks.json");
            writer.open();
            writer.write(session, new ArrayList<>());
            writer.close();

            // Reading the session back
            JsonReader reader = new JsonReader("./data/testParseStatisticsWithTasks.json");
            PomodoroSession readSession = reader.readPomodoroSession();
            Statistics readStats = readSession.getStatistics();

            // Check if the statistics and tasks read are as expected
            assertEquals(1, readStats.getCompletedSessions());
            assertEquals(150, readStats.getTotalWorkTime());
            assertEquals(2, readStats.getCompletedTaskList().size());
            assertTrue(readStats.getCompletedTaskList().get(0).isCompleted());
            assertEquals("Completed Task", readStats.getCompletedTaskList().get(0).getTaskName());

        } catch (IOException e) {
            fail("IOException should not have been thrown");
        }
    }

    @Test
    void testParseStatisticsWithoutTasks() {
        try {
            // Set up Statistics without tasks and write it to a file
            Statistics stats = new Statistics();
            stats.addCompletedSession();
            stats.addTotalWorkTime(150);

            PomodoroSession session = new PomodoroSession(25, 5, 15, stats);
            JsonWriter writer = new JsonWriter("./data/testParseStatisticsWithoutTasks.json");
            writer.open();
            writer.write(session, new ArrayList<>());
            writer.close();

            // Reading the session back
            JsonReader reader = new JsonReader("./data/testParseStatisticsWithoutTasks.json");
            PomodoroSession readSession = reader.readPomodoroSession();
            Statistics readStats = readSession.getStatistics();

            // Check if the statistics read are as expected, and no tasks are included
            assertEquals(1, readStats.getCompletedSessions());
            assertEquals(150, readStats.getTotalWorkTime());
            assertEquals(0, readStats.getCompletedTaskList().size());

        } catch (IOException e) {
            fail("IOException should not have been thrown");
        }
    }



}
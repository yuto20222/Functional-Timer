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

//    @Test
//    void testAddCompletedTasks() {
//        JsonReader jsonReader = new JsonReader("dummyPath");
//        JSONObject testJson = new JSONObject();
//        JSONArray tasksArray = new JSONArray();
//
//        // Add two tasks
//        JSONObject task1 = new JSONObject();
//        task1.put("name", "Test Task 1");
//        tasksArray.put(task1);
//
//        JSONObject task2 = new JSONObject();
//        task2.put("name", "Test Task 2");
//        tasksArray.put(task2);
//
//        testJson.put("tasks", tasksArray);
//
//        Statistics stats = new Statistics();
//        jsonReader.addCompletedTasks(stats, testJson);
//
//        // Assuming Statistics class has a method to get the size of completed tasks.
//        assertEquals(2, stats.getCompletedTaskSize());
//    }

}
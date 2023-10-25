package persistance;


import model.PomodoroSession;
import model.Statistics;
import model.Task;
import org.json.JSONException;
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

//    @Test
//    void testReadStatistics() {
//        try {
//            Statistics st = new Statistics();
//            st.addCompletedSession();
//            st.addTotalWorkTime(1500);  // for example, 25 minutes in seconds
//
//            JsonWriter writer = new JsonWriter("./data/testReadStatistics.json");
//            writer.open();
//            writer.write(st);
//            writer.close();
//
//            JsonReader reader = new JsonReader("./data/testReadStatistics.json");
//            st = reader.readStatistics();
//            // Assuming there are no completed sessions and total work time is 0 in the test file
//            checkStatistics(0, 0, st);
//        } catch (IOException e) {
//            fail("IOException should not have been thrown");
//        }
//    }


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
}
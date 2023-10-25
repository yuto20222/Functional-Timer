package persistance;

import model.PomodoroSession;
import model.Statistics;
import model.Task;
import org.junit.jupiter.api.Test;
import persistence.JsonReader;
import persistence.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonWriterTest {

    @Test
    void testWriterInvalidFile() {
        try {
            JsonWriter writer = new JsonWriter("./data/my\0illegal:fileName.json");
            writer.open();
            fail("IOException was expected");
        } catch (IOException e) {
            // pass
        }
    }

    @Test
    void testWriterEmptyPomodoroSession() {
        try {
            PomodoroSession ps = new PomodoroSession(25, 5, 10, new Statistics());
            JsonWriter writer = new JsonWriter("./data/testWriterEmptyPomodoroSession.json");
            writer.open();
            writer.write(ps);
            writer.close();

            JsonReader reader = new JsonReader("./data/testWriterEmptyPomodoroSession.json");
            ps = reader.readPomodoroSession();
            assertEquals(1500, ps.getWorkDuration());
            assertEquals(300, ps.getShortBreakDuration());
            assertEquals(600, ps.getLongBreakDuration());

        } catch (IOException e) {
            fail("Exception should not have been thrown");
        }
    }

    @Test
    void testWriterGeneralPomodoroSession() {
        try {
            PomodoroSession ps = new PomodoroSession(25, 5, 10, new Statistics());
            List<Task> tasks = new ArrayList<>();
            tasks.add(new Task("Task 1"));
            tasks.add(new Task("Task 2"));

            JsonWriter writer = new JsonWriter("./data/testWriterGeneralPomodoroSession.json");
            writer.open();
            writer.write(ps);
            writer.write(tasks);
            writer.close();

            JsonReader reader = new JsonReader("./data/testWriterGeneralPomodoroSession.json");
            ps = reader.readPomodoroSession();



        } catch (IOException e) {
            fail("Exception should not have been thrown");
        }
    }

    @Test
    void testWriterStatistics() {
        try {
            Statistics st = new Statistics();
            st.addCompletedSession();
            st.addTotalWorkTime(1500);  // for example, 25 minutes in seconds

            JsonWriter writer = new JsonWriter("./data/testWriterStatistics.json");
            writer.open();
            writer.write(st);
            writer.close();

            JsonReader reader = new JsonReader("./data/testWriterStatistics.json");
            st = reader.readStatistics();
            assertEquals(0, st.getCompletedSessions());
            assertEquals(0, st.getTotalWorkTime());
            // ... and other assertions for Statistics

        } catch (IOException e) {
            fail("Exception should not have been thrown");
        }
    }
    @Test
    void testWriterTaskList() {
        try {
            List<Task> taskList = new ArrayList<>();
            taskList.add(new Task("Task 1"));
            taskList.add(new Task("Task 2"));

            JsonWriter writer = new JsonWriter("./data/testWriterTaskList.json");
            writer.open();
            writer.write(taskList);
            writer.close();

            JsonReader reader = new JsonReader("./data/testWriterTaskList.json");
            List<Task> readTaskList = reader.readTasks();
            assertEquals(2, readTaskList.size());
            assertEquals("Task 1", readTaskList.get(0).getTaskName());
            assertEquals("Task 2", readTaskList.get(1).getTaskName());

        } catch (IOException e) {
            fail("Exception should not have been thrown");
        }
    }

}

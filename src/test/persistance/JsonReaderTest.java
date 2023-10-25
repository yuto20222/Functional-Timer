//package persistance;
//
//
//import model.PomodoroSession;
//import model.Statistics;
//import model.Task;
//import org.json.JSONException;
//import org.junit.jupiter.api.Test;
//import persistence.JsonReader;
//import java.io.IOException;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class JsonReaderTest extends JsonTest {
//
//    @Test
//    void testReadPomodoroSession() {
//        JsonReader reader = new JsonReader("./data/testReadPomodoroSession.json");
//        try {
//            PomodoroSession session = reader.readPomodoroSession(25, 5, 10, new Statistics());
//            checkPomodoroSession(25, 5, 10, session);
//        } catch (IOException e) {
//            fail("IOException should not have been thrown");
//        }
//    }
//
//    @Test
//    void testReadStatistics() {
//        JsonReader reader = new JsonReader("./data/testReadStatistics.json");
//        try {
//            Statistics stats = reader.readStatistics();
//            // Assuming there are no completed sessions and total work time is 0 in the test file
//            checkStatistics(0, 0, stats);
//        } catch (IOException e) {
//            fail("IOException should not have been thrown");
//        }
//    }
//
//    @Test
//    void testReadTasks() {
//        JsonReader reader = new JsonReader("./data/testReadTasks.json");
//        try {
//            List<Task> tasks = reader.readTasks();
//            assertEquals(2, tasks.size());
//            checkTask("Task 1", false, tasks.get(0));
//            checkTask("Task 2", false, tasks.get(1));
//        } catch (IOException e) {
//            fail("IOException should not have been thrown");
//        }
//    }
//
//    @Test
//    void testReadTasksJSONException() {
//        JsonReader badReader = new JsonReader("./data/badTestFile.json");
//        try {
//            List<Task> tasks = badReader.readTasks();
//            fail("JSONException should have been thrown");
//        } catch (IOException | JSONException e) {
//            // pass
//        }
//    }
//}
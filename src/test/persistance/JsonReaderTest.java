package persistance;


import model.Task;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import persistence.JsonReader;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonReaderTest extends JsonTest {

//    private JsonReader reader;

    @Test
    void testReadTasksJSONException() {
        JsonReader badReader = new JsonReader("./data/badTestFile.json");  // Assume this file has malformed JSON
        try {
            List<Task> tasks = badReader.readTasks();
            fail("JSONException should have been thrown");
        } catch (IOException | JSONException e) {
            // pass
        }
    }
}
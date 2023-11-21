package persistence;

import org.json.JSONObject;

/**
 * Represents a contract for objects that can be written to JSON.
 * Classes implementing this interface should provide their own implementation
 * of the toJson method to convert their state to a JSONObject.
 */
public interface Writable {
    JSONObject toJson();
}

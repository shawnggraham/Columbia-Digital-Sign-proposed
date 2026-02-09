import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

public class JSONReader {

    //JsonReader (file you want to get, information you want from file)

    //prevent outside code from breaking method
    private final JsonElement element;

    public JSONReader(JsonElement element)  {

        this.element = element;
    }

     /*
    =====================
    Chainable methods
    =====================
     */

    //getting a string value
    public JSONReader getName(String name) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            //return the field name
            return new JSONReader(object.get(name));
        }

        return null;
    }

    //finding a number value for groups
    public JSONReader getIndex(Integer index) {
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();

            //return field name
            return new JSONReader(array.get(index));
        }

        return null;
    }

    //finding the file method
    public static JSONReader read(String fileName) throws FileNotFoundException {

        return new JSONReader(reader(fileName));
    }

    //to string method
    public String asString() {

        return element.getAsString();
    }

    //to int method
    public Integer asInt() {

        return element.getAsInt();
    }

    //getting the Json element as is
    public JsonElement raw() {

        return element;
    }

     /*
    =====================
    getAsInteger methods
    =====================
     */

    //single value outside of groups
    public Integer getAsInteger(String filename, String getIntegerValue) throws FileNotFoundException {
        JsonObject getFile = reader(filename);

        //check if it's a valid int
        if (!getFile.has(getIntegerValue)) { return null; }

        //return the result
        return getFile.get(getIntegerValue).getAsInt();
    }

    //single value inside of groups
    public Integer getAsInteger(String filename, String groupName, Integer ID, String getIntegerValue) throws FileNotFoundException {
        //gets the file
        JsonObject getFile = reader(filename);
        //gets the first array field
        JsonArray getArrayField = getFile.getAsJsonArray(groupName);
        //grabs the id of the student
        JsonObject getID = getArrayField.get(ID).getAsJsonObject();

        //check if it's a valid int
        if (!getID.has(getIntegerValue)) { return null; }

        //return the result
        return getID.get(getIntegerValue).getAsInt();
    }

    /*
    =====================
    getAsString methods
    =====================
     */

    //single value outside of groups
    public String getAsString(String filename, String getStringValue) throws FileNotFoundException {
        JsonObject getFile = reader(filename);

        //check if it's a valid String
        if (!getFile.has(getStringValue)) { return null; }

        //return the result
        return getFile.get(getStringValue).getAsString();
    }

    //single value inside of groups
    public String getAsString(String filename, String groupName, Integer ID, String getStringValue) throws FileNotFoundException {
        //gets the file
        JsonObject getFile = reader(filename);
        //gets the first array field
        JsonArray getArrayField = getFile.getAsJsonArray(groupName);
        //grabs the id of the student
        JsonObject getID = getArrayField.get(ID).getAsJsonObject();

        //check if it's a valid int
        if (!getID.has(getStringValue)) { return null; }

        //return the result
        return getID.get(getStringValue).getAsString();
    }

    /*
    =====================
    Support methods
    =====================
     */

    //a support class to read file information
    private static JsonObject reader(String filename) throws FileNotFoundException {
        //reads the JSON file
        Reader reader = new BufferedReader(new FileReader(filename));

        //returns JSON Object
        return new Gson().fromJson(reader, JsonObject.class);
    }
}

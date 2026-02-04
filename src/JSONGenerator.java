import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.FormattingStyle;
import com.google.gson.GsonBuilder;

public class JSONGenerator {

    //the builder is what gets the formatting
    Gson gson = new GsonBuilder()
            .setFormattingStyle(FormattingStyle.PRETTY) //makes good formatting
            .create(); //creates GSON instance


   JSONGenerator (String filename) {
        //get the JSON writer going
        try (BufferedWriter bWriter = new BufferedWriter(new FileWriter(filename))) {

            String line = gson.toJson(data());
            //makes it write in the data it has
            bWriter.write(line);

            bWriter.newLine();

        } catch (IOException e) {
            System.out.print("issue saving file");
            e.printStackTrace();
        }
    }

      /*
    =================================
    Retriever and Sender Class.
    =================================
     */

    //map that will store all the students
    Map<String, studentDetails> students = new LinkedHashMap<>();

   public Map<String, studentDetails> data() {
       //temporary variables to fill class
       String tempName = "name";
       String tempDate = "date";
       int tempArrivalTime = 60;
       int tempSlidesWatched = 1;
       int ID = 3; //change # for student amount

       //loop to add students to hashmap
       for (int i = 1; i <= ID; i++) {
           //.put map to put new students into the map
            students.put("Student " + i, new studentDetails(tempName, tempDate, tempArrivalTime, tempSlidesWatched));
       }

       //fill and return the data
       return students;
   }
}

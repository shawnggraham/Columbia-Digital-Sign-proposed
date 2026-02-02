import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class JSONGenerator {

    Gson gson = new Gson();

   JSONGenerator (String filename, studentDetails data) {
        //get the JSON writer going
        try (BufferedWriter bWriter = new BufferedWriter(new FileWriter(filename, true))) {

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

   public studentDetails data() {

       // Creating a student object
       Student student = new Student("Default",0,0,0);

       // Use student object data to fill the studentDetails object
       return new studentDetails(
               student.name,
               student.ID,
               student.arrivalTime,
               student.SLIDES_WATCHED_TOTAL

       );
   }
}

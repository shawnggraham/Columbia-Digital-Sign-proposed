import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class JSONGenerator {

    Gson gson = new Gson();

   JSONGenerator (String filename) {
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
       //temporary variables to fill class
       String tempName = "name";
       String tempDate = "date";
       int tempArrivalTime = 60;
       int tempSlidesWatched = 1;

       //fill and return the data
       return new studentDetails(
               tempName,
               tempDate,
               tempArrivalTime,
               tempSlidesWatched
       );
   }
}

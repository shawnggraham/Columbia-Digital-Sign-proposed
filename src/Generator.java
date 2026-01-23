import com.google.gson.Gson;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
//the Generator part of the JSON creator
//works with the Data class
public class Generator {

    //get the library going for GSON
    Gson gson = new Gson();

    Generator (String filename) {
        //run Data class to get the data needed
        Data data = new Data();

        //get the JSON writer going
        try (BufferedWriter bWriter = new BufferedWriter(new FileWriter(filename))) {

            String line = gson.toJson(data);
            //makes it write in the data it has
            bWriter.write(line);
            bWriter.newLine();

        } catch (IOException e) {
            System.out.print("issue saving file");
            e.printStackTrace();
        }
    }






}
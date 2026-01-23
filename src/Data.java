import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Data {

    Map<String, String> data = new HashMap<>();

    Data () {

        Boolean loop = true;
        //listener for user input (can be adjusted to literally anything)
        Scanner scanner = new Scanner(System.in);

        //Instructions
        System.out.println("Type enter to continue or type 'default' to get default file");
        String Default = scanner.nextLine();
        if (Default.equals("default")) {

            //this down below is where you can edit the default
            //use 'this.data.put(String, String)'
            //the current data is just a template
            this.data.put("student1", "1");
            this.data.put("student2", "2");
            this.data.put("student3", "3");

            loop = false;
        }

        //using while loop to have it constantly wait for user input
        //it will wait until user has decided that it has all the data it will need
        //then break the loop.

        while (loop) {

            //category string
            System.out.println("Input a Category or type 'done' to finalize");
            String category = scanner.nextLine();
            //removes extra spaces
            category = category.trim();

            //closes scanner if input is 'done'
            if (category.equals("done")) { break; }

            //for loading default config


            //value string
            System.out.println("Input a value");
            String value = scanner.nextLine();
            value = value.trim();

            //puts all the data into the Map
            this.data.put(category, value);
        }


    }
}

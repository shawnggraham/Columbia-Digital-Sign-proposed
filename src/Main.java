
import javax.swing.SwingUtilities;
import java.io.IOException;
public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        /*
         * --------------------------------------------
         * DATA / GENERATION SETUP
         * --------------------------------------------
         * If the Generator is required to prepare or
         * validate JSON files, it can still live here.
         */

        // new Generator("data.JSON");

       /*
         * --------------------------------------------
         * FUTURE EXTENSIONS (DO NOT DELETE we will name them appropriately)
         * --------------------------------------------
         */

        // SlideList slideList = SlideList.generateSlideList(20);
        // StudentList studentList = StudentList.generateStudentList(10);


        new JSONGenerator("data1.JSON");

        SlideList list = new SlideList();
        list.insert(new Slide("Slide1", 1, 10));
        list.insert(new Slide("Slide2", 2, 10));
        list.insert(new Slide("Slide3", 3, 10));

        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.next());
        }

        StudentList studentList = new StudentList();
        studentList.insert(new Student("Bob", 1, 0, 0));
        studentList.insert(new Student("Alice", 2, 0, 0));
        studentList.insert(new Student("Carol", 3, 0, 0));

        for (int i = 0; i < studentList.size(); i++) {
            System.out.println(studentList.next());
        }
        /*
         * --------------------------------------------
         * UI ENTRY POINT
         * --------------------------------------------
         * This simply says:
         * "When the app starts, show the UI."
         */
        SwingUtilities.invokeLater(() -> {
            new ColumbiaSignUI();
        });
    }

}

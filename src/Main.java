import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) throws InterruptedException {

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
         * UI ENTRY POINT
         * --------------------------------------------
         * This simply says:
         * "When the app starts, show the UI."
         */

        SwingUtilities.invokeLater(() -> {
            new ColumbiaSignUI();
        });

        /*
         * --------------------------------------------
         * FUTURE EXTENSIONS (DO NOT DELETE we will name them appropriately)
         * --------------------------------------------
         */

        // SlideList slideList = SlideList.generateSlideList(20);
        // StudentList studentList = StudentList.generateStudentList(10);
    }
}

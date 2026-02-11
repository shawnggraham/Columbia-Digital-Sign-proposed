
import javax.swing.SwingUtilities;
import java.io.IOException;
public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {

         /* --------------------------------------------
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

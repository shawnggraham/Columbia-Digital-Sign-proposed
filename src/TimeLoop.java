import java.io.IOException;

public class TimeLoop {
    public static void main(String[] args) throws InterruptedException, IOException {

        String GREEN = "\u001B[32m";
        String RESET = "\u001B[0m";

        int secondsPerDay = 86400;
        int secondsPerHour = 3600;

        int STEP = 0; // tracks seconds elapsed
        int DAY = 1;


        //SlideList slides = SlideList.generateSlideList(20);
        //StudentList student = StudentList.generateStudentList(5);

        while (STEP < secondsPerDay) {
            Thread.sleep(1);
            STEP ++;

            int hour24 = (STEP / secondsPerHour) % 24;
            int minute = (STEP / 60) % 60;
            int minutesElapsed = STEP / 60;
            int second = STEP % 60;
            int hours =  STEP / secondsPerHour;

            String AMPM = (hour24 >= 12) ? "pm" : "am";
            int hour12 = hour24 % 12;
            if (hour12 == 0) hour12 = 12;

            System.out.print("\r Day " + DAY);
            System.out.print("  [ Standard Time: " + GREEN + String.format("%02d:%02d:%02d", hour12, minute, second) + RESET + AMPM + " ]");
            System.out.print("  [ Daily Time Elapsed: "
                    + GREEN + hours + RESET + "h" + " "
                    + GREEN + minutesElapsed + RESET + "m" + " "
                    + GREEN + STEP + RESET + "s ]" );


            if (STEP == secondsPerDay) {
                DAY++;
                STEP = 0;

                if (DAY == 5){
                    System.exit(0);
                }
            }
        }
    }
}

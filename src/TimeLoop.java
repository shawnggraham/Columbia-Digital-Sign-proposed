public class TimeLoop {

    public static void main(String[] args) throws InterruptedException {

        String GREEN = "\u001B[32m";
        String RESET = "\u001B[0m";

        int secondsPerDay = 86400;
        int hoursInSeconds = 3600;
        int DAY = 1;
        int STEP = 0;
        double hours;

        while (STEP < secondsPerDay) {
            STEP++;
            hours = (double) STEP / hoursInSeconds;

            System.out.print("\r Day " + DAY);
            System.out.print("  [ Time in seconds: " + GREEN + STEP +  "s"+ RESET + " ]" );
            System.out.printf("  [ Time in hours: "+ GREEN + String.format("%.2f",hours) +  "h"+ RESET + " ]" );

            Thread.sleep(1);
            if (STEP == secondsPerDay) {
                DAY++;
                STEP = 0;

                Thread.sleep(1000);
                if (DAY == 4) System.exit(0);
            }
        }
    }
}

public class TimeLoop {

    public static void main(String[] args) throws InterruptedException {

        String GREEN = "\u001B[32m";
        String RESET = "\u001B[0m";

        int secondsPerDay = 86400;
        int secondsPerHour = 3600;
        int secondsPerMinute = 60;

        int STEP = 0;
        int DAY = 1;
        double hours;
        int minutes;
        String AMPM;

        while (STEP < secondsPerDay) {
            STEP++;
            hours = (double) STEP / secondsPerHour;
            minutes = (int) (hours % 1 * 60);

            if (hours >= 12) AMPM = "pm"; else AMPM = "am";

            System.out.print("\r Day " + DAY);
            System.out.print("  [ Time in seconds: " + GREEN + STEP + RESET + "s" + " ]" );
            System.out.printf("  [ Time in hours: "+ GREEN + String.format("%.2f",hours) + RESET + "h" +  " ]" );
            System.out.print("   [ Time: " + GREEN + String.format("%02d:%02d", (int)hours, minutes % 60) + RESET + AMPM + " ]");

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

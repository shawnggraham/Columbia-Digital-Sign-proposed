public class TimeLoop {

    public static void main(String[] args) throws InterruptedException {

        String GREEN = "\u001B[32m";
        String RESET = "\u001B[0m";

        int secondsPerDay = 86400;
        int secondsPerHour = 3600;

        int STEP = 0; // tracks seconds elapsed
        int DAY = 1;



        while (STEP < secondsPerDay) {
            STEP++;

            int hour24 = (STEP / secondsPerHour) % 24;
            int minute = (STEP / 60) % 60;
            int minutes = STEP / 60;
            int second = STEP % 60;
            int hours =  STEP / secondsPerHour;


            String AMPM = (hour24 >= 12) ? "pm" : "am";
            int hour12 = hour24 % 12;
            if (hour12 == 0) hour12 = 12;

            System.out.print("\r Day " + DAY);
            System.out.print("  [ Standard Time: " + GREEN + String.format("%02d:%02d:%02d", hour12, minute, second) + RESET + AMPM + " ]");
            System.out.print("  [ Time in seconds: " + GREEN + STEP + RESET + "s ]" );
            System.out.print("  [ Time in minutes: " + GREEN + minutes + RESET + "m" + " ]" );
            System.out.print("  [ Time in hours: "+ GREEN + hours + RESET + "h" +  " ]" );

            Thread.sleep(1);
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

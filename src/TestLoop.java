public class TestLoop {

    public static void main(String[] args) throws InterruptedException {

        int secondsPerDay = 86400;
        int DAY = 0;
        int STEP = 0;

        while (STEP < secondsPerDay) {
            STEP++;
            System.out.println("Time in seconds: " + STEP + "s");
            Thread.sleep(1);
            if (STEP == secondsPerDay) {
                DAY++;
                STEP = 0;
                System.out.println("Day " + DAY);
                Thread.sleep(1000);
            }
        }
    }
}

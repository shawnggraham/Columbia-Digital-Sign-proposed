import java.io.IOException;
import java.util.Random;

public class TimeLoop {
    public static void main(String[] args) throws InterruptedException, IOException {

        Random random = new Random();
        String GREEN = "\u001B[32m";
        String ORANGE = "\u001B[33m";
        String RESET = "\u001B[0m";

        int secondsPerDay = 86400;
        int secondsPerHour = 3600;

        int STEP = 0; // tracks seconds elapsed
        int DAY = 1;
        int ID = 0;

        int studentViewTimer = random.nextInt(40, 60);

        boolean studentActive = false;

        StudentList students = StudentList.generateStudentList(4);

        // Use this method to generate a new slide list with randomness
        //SlideList slides = SlideList.generateSlideList(10);

        // Or manually create slide objects and add to a SlideList
        SlideList slides = new SlideList();

        while (slides.size() < 10) {
            slides.insert(new Slide("Slide",  + ID, random.nextInt(7, 15)));
            ID++;
        }

        int slideDuration = slides.getSlideDuration();

        while (STEP < secondsPerDay && slideDuration > 0) {
            //Thread.sleep(1);

            int hour24 = (STEP / secondsPerHour) % 24;
            int minute = (STEP / 60) % 60;
            int minutesElapsed = STEP / 60;
            int second = STEP % 60;
            int hours = STEP / secondsPerHour;

            String AMPM = (hour24 >= 12) ? "pm" : "am";
            int hour12 = hour24 % 12;
            if (hour12 == 0) hour12 = 12;

            System.out.print("\r Day " + DAY);
            System.out.print("  [ Standard Time: " + GREEN + String.format("%02d:%02d:%02d", hour12, minute, second) + RESET + AMPM + " ]");
//            System.out.print("  [ Daily Time Elapsed: "
//                    + GREEN + hours + RESET + "h" + " "
//                    + GREEN + minutesElapsed + RESET + "m" + " "
//                    + GREEN + STEP + RESET + "s ]");

            // Adding slides to the loop
            slides.currentSlide();
            slideDuration--;
            STEP++;

            System.out.print("  ||  " + slides.currentSlide() + " [Slide timer: " + GREEN + (slideDuration + 1) + RESET + "] || ");



            int arrivalSecond = students.currentStudent().arrivalTime * 60 * 60;

            if (STEP == arrivalSecond) {
                studentActive = true;
                //Thread.sleep(1000);
            }

            if (slideDuration == 0) {
                System.out.print("\r || " + slides.next());
                if (studentActive){
                    students.setSlidesWatched(1);
                }
                slideDuration = slides.getSlideDuration();
            }

            if (studentActive) {
                students.setDurationWatched(1);
                studentViewTimer--;
                System.out.print(ORANGE + "Student " + students.getNameAndID() + RESET + " [" + ORANGE + "Student Watch timer: " + GREEN + studentViewTimer + RESET +"]");
                //Thread.sleep(100);

                if (studentViewTimer == 0) {
                    studentViewTimer = random.nextInt(40,60);
                    studentActive = false;
                    students.next();
                }
            }

            if (studentActive) {
                System.out.print(ORANGE + "[Slides Watched: " + GREEN + students.getSlidesWatched() + RESET + "]");
                //Thread.sleep(1000);
            }

            if (STEP == secondsPerDay) {
                DAY++;
                STEP = 0;

                if (DAY == 7) {
                    break;
                }
            }
        }

        int IDX = 0;
        System.out.println("\n\n");
        while (IDX < students.size()) {

            System.out.println("\n" + students.next());
            IDX++;
        }
    }
}

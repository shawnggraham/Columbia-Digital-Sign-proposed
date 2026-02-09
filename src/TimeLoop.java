import java.io.IOException;
import java.sql.Time;
import java.time.Clock;
import java.time.LocalTime;
import java.util.Random;
import java.util.Timer;

public class TimeLoop {

    public static void main(String[] args) throws InterruptedException, IOException {


        String GREEN = "\u001B[32m";
        String ORANGE = "\u001B[33m";
        String RESET = "\u001B[0m";

        Random random = new Random();

        // Time tracking constants
        final int secondsPerDay = 86400;
        final int secondsPerHour = 3600;

        // Time tracking variables
        int STEP = 0; // seconds
        int DAY = 1; // days

        // Student and Slide tracking variables
        int ID = 1; // use for Student or Slide ID's
        int arrivalTime = 8;
        int slidesWatchedTotal = 0;
        int studentViewTimer = random.nextInt(40, 60);
        boolean studentActive = false;

        /// Use this method to generate a new student list with randomness
        //StudentList students = StudentList.generateStudentList(4);

        /// Or manually create student objects and add to a StudentList
        StudentList students = new StudentList();
        /// Generate a list of student objects with random names using the getRandomName() method.
        while (students.size() < 8) {;
            students.insert(new Student(StudentList.getRandomName(), ID, slidesWatchedTotal, arrivalTime));
            ID++; // increment student ID
            arrivalTime++; // increment arrival time
        }

        // Print the list of students (optional)
//        for (int i = 0; i < students.size(); i++) {
//            System.out.println(students.next());
//        }

        /// Use this method to generate a new slide list with randomness
        //SlideList slides = SlideList.generateSlideList(10);

        /// Or manually create slide objects and add to a SlideList
        SlideList slides = new SlideList();

        /// Generate a list of slide objects with random names using the getRandomName() method.
        while (slides.size() < 10) {
            slides.insert(new Slide("Slide", +ID, random.nextInt(7, 15)));
            ID++;
        }

        int slideDuration = slides.getSlideDuration();

        /// Main Simulation Loop below

        System.out.println("--------------------------------------------------------------------------------------------------------------------------");
        Time time = Time.valueOf(LocalTime.now());
        System.out.println("Loop Starting @" + time + "\n");

        while (STEP < secondsPerDay && slideDuration > 0) {

            /// USE THIS THREAD SLEEP FOR BETTER VIEW IN CONSOLE
            //Thread.sleep(1);

            // Time tracking
            int hour24 = (STEP / secondsPerHour) % 24;
            int minute = (STEP / 60) % 60;
            int minutesElapsed = STEP / 60;
            int second = STEP % 60;
            int hours = STEP / secondsPerHour;

            String AMPM = (hour24 >= 12) ? "pm" : "am";
            int hour12 = hour24 % 12;
            if (hour12 == 0) hour12 = 12;

            // Printing time
            System.out.print("\r Day " + DAY);
            System.out.print("  [ Standard Time: " + GREEN + String.format("%02d:%02d:%02d", hour12, minute, second) + RESET + AMPM + " ]");
//            System.out.print("  [ Daily Time Elapsed: "
//                    + GREEN + hours + RESET + "h" + " "
//                    + GREEN + minutesElapsed + RESET + "m" + " "
//                    + GREEN + STEP + RESET + "s ]");

            // Adding slides to the loop
            slides.currentSlide();
            slideDuration--; // decrement slide duration each tick
            STEP++;

            System.out.print("  ||  " + slides.currentSlide() + " [Slide timer: " + GREEN + (slideDuration + 1) + RESET + "] || ");

            // Student arrival in seconds
            int arrivalSecond = students.currentStudent().arrivalTime * 60 * 60;

            int randomArrivalSecond = random.nextInt(arrivalSecond - 160,arrivalSecond + 160);

            // Flip student active status upon arrival
            if (STEP == randomArrivalSecond) {
                studentActive = true;
                //Thread.sleep(1000);
            }

            // Move to next slide if timer is 0
            if (slideDuration == 0) {
                System.out.print("\r || " + slides.next());
                // If a student is active, increment their slide watched count
                if (studentActive) {
                    students.setSlidesWatched(1);
                }
                // Reset slide duration
                slideDuration = slides.getSlideDuration();
            }

            // Student view timer

            if (studentActive) {
                /// USE THIS THREAD SLEEP TO SLOW RENDERING WHEN A STUDENT ARRIVES
                Thread.sleep(1);
                students.setDurationWatched(1);
                studentViewTimer--;
                System.out.println(ORANGE + "Student " + students.getNameAndID() + RESET + " [" + ORANGE + "Student Watch timer: " + GREEN + studentViewTimer + RESET + "]");
                System.out.print(ORANGE + "[Slides Watched: " + GREEN + students.getSlidesWatched() + RESET + "]");

                if (studentViewTimer == 0) {
                    studentViewTimer = random.nextInt(40, 60);
                    studentActive = false;
                    students.next();
                }
            }

            // Day tracking and reset / completion conditions
            if (STEP == secondsPerDay) {
                DAY++;
                STEP = 0;
                if (DAY == 10) {
                    break;
                }
            }
        }

        System.out.println("\n\nLoop Finished @" + time);
        System.out.println("--------------------------------------------------------------------------------------------------------------------------");
        // Printing final student data
        int IDX = 0;
        System.out.println("\nFinal Student Data:");


        while (IDX < students.size()) {
            System.out.println("\n" + ORANGE + students.next() + RESET);
            IDX++;
        }
        System.out.println("--------------------------------------------------------------------------------------------------------------------------");
    }
}

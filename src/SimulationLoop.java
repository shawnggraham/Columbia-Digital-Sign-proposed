import java.util.Random;

public class SimulationLoop {
    public static void main(String[] args) throws InterruptedException {

        final int DAYS = 4;

        final int MAX_VIEW_SECONDS = 60; // driveway time cap

        Random rng = new Random();

        SlideList slideList = SlideList.generateSlideList(20);
        StudentList students = StudentList.generateStudentList(10);

        int cycleSeconds = slideList.cycleDuration();
        if (cycleSeconds <= 0) throw new IllegalStateException("Slide cycle duration must be > 0");


        for (int day = 0; day < DAYS; day++) {
            for (int i = 0; i < students.size(); i++) {
                Student student = students.next();

                // How long they are in view today (0..60). Change distribution if desired.
                int viewSeconds = rng.nextInt(MAX_VIEW_SECONDS + 1);
                if (viewSeconds == 0) {
                    continue;
                }

                // Random starting phase within the repeating slide cycle
                int offset = rng.nextInt(cycleSeconds);

                // Use an independent cursor so we can "seek" without affecting others
                SlideList cursor = slideList.copy();

                // Seek to the slide active at the offset (and how far into it we are)
                Slide current = cursor.next();
                while (offset >= current.duration) {
                    offset -= current.duration;
                    current = cursor.next();
                }

                // simulate watching for viewSeconds starting offset seconds into "current"
                int remainingInSlide = current.duration - offset;

                while (viewSeconds > 0) {
                    int watchedNow = Math.min(viewSeconds, remainingInSlide);

                    student.durationWatched += watchedNow;

                    // Count the slide if they watched any part of it
                    if (watchedNow > 0) {
                        student.SLIDES_WATCHED_TOTAL++;
                        student.slideWatched = true;
                    }

                    viewSeconds -= watchedNow;

                    System.out.println("Student " + student.name + " watched " + watchedNow + " seconds of slide " + current.name);

                    // Move to next slide if still time left
                    current = cursor.next();
                    remainingInSlide = current.duration;
                }
            }
        }

        // Print final totals
        for (int i = 0; i < students.size(); i++) {
            System.out.println(students.next());
        }
    }
}
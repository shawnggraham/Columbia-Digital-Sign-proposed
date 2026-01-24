import java.util.Random;

public class SimulationLoop {
    public static void main(String[] args) throws InterruptedException {

        final int secondsPerDay = 86400;
        final int days = 4;
        // endTime = 4 days in seconds
        final int endTime = secondsPerDay * days;
        int tick = 0;

        Random random = new Random();

        //slideTotalDuration = list.getSlideListDuration();
        SlideList list = SlideList.generateSlideList();

        // 4 day loop of slides
        while (tick < endTime) {
            Slide slide = list.next();
            System.out.println("\nSlide " + slide.name + " is playing for " + slide.duration + " seconds.");
            tick += slide.duration;
            System.out.println("Time elapsed : " + tick + "s");
        }


        /*StudentList studentList = StudentList.generateStudentList();
        Student S = studentList.next();

         Simulation loop below
        for (int i = 0; i < simTime; i++) {

            S.durationWatched = random.nextInt(20);

            if (S.name == 1) {
                System.out.println("Day # " + DAY);
                DAY++;
            }
            //System.out.println(list.next());
            if (S.durationWatched > 0) {
                S.SLIDES_WATCHED_TOTAL++;
                S.slideWatched = true;
            }
            System.out.println(S);
        }
        for (int k = 0; k < simTime; k++) {
            System.out.println(list.next());
        }*/
    }
}

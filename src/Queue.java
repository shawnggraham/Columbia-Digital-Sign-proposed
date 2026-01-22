import java.util.Random;

public class Queue {
    public int secondsWatched;
    public int SLIDES_WATCHED_TOTAL;
    boolean slideWatched;
    public int name;
    Random random = new Random();

    public Queue(int name) {
        SLIDES_WATCHED_TOTAL = 0;
        secondsWatched = 0;
        slideWatched = false;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Student ID #" + this.name + "[Slides watched: " + SLIDES_WATCHED_TOTAL + ", Duration watched: " + secondsWatched + "s]";
    }

    public int getData() {
        System.out.println(this.name);
        return this.name;
    }
}

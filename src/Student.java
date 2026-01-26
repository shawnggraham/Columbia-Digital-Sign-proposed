import java.util.Random;

public class Student {
    public int durationWatched;
    public int SLIDES_WATCHED_TOTAL;
    public boolean slideWatched;
    public int name;
    Random random = new Random();

    public Student(int name, int durationWatched, boolean slideWatched) {
        SLIDES_WATCHED_TOTAL = 0;
        this.durationWatched = durationWatched;
        this.slideWatched = slideWatched;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Student ID #" + this.name + " [Slides watched: " + SLIDES_WATCHED_TOTAL + ", Duration watched: " + durationWatched + "s]";
    }

    public int getData() {
        System.out.println(this.name);
        return this.name;
    }
}

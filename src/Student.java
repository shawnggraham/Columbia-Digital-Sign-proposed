import java.util.Random;

public class Student {
    public int durationWatched;
    public int SLIDES_WATCHED_TOTAL;
    public boolean slideWatched;
    public String name;
    public int ID;
    Random random = new Random();

    public Student(String name, int ID, int durationWatched) {
        SLIDES_WATCHED_TOTAL = 0;
        this.durationWatched = durationWatched;
        this.name = name;
        this.ID = ID;
    }

    @Override
    public String toString() {
        return "Student " + this.name + " ID # " + ID + " [Slides watched: " + SLIDES_WATCHED_TOTAL + ", Duration watched: " + durationWatched + "s]";
    }

    public int getData() {
        System.out.println(this.name);
        //return System.out.println(this.name);
        return this.ID;
    }
}

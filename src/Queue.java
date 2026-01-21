import java.util.Random;

public class Queue {
    public int secondsWatched;
    public int slidesWatched;
    public int name;
    Random random = new Random();

    public Queue(int name) {
        secondsWatched = 0;
        slidesWatched = 0;
        this.name = name;
    }

    @Override
    public String toString() {
        return " [Slides watched: " + slidesWatched + ", Duration watched: " + secondsWatched + "s]";
    }
}

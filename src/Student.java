public class Student {
    public int durationWatched;
    public int slidesWatchedTotal;
    public String name;
    public int ID;
    public int arrivalTime;

    public Student(String name, int ID, int slidesWatchedTotal, int arrivalTime) {
        this.slidesWatchedTotal = slidesWatchedTotal;
        this.name = name;
        this.ID = ID;
        this.arrivalTime = arrivalTime;
        this.durationWatched = 0;
    }

    @Override
    public String toString() {
        return "Student " + this.name + " ID # " + ID +
                "\n Arrives at school at " + arrivalTime +
                "\n [Slides watched: " + slidesWatchedTotal + ", Duration watched: " + durationWatched + "s]";
    }

    public int getData() {
        System.out.println(this.name + " ID # " + this.ID + "Arrives at school @" + arrivalTime);
        //return System.out.println(this.name);
        return this.ID;
    }
}

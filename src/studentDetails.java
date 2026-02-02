   /* *
    * temporary data object that contains student details to save to a JSON
    * <p>
    * used for input detection, then discarded when saved to JSON
    */

public class studentDetails {

    /*
    =========================================================
    Down below is going to be Gavin's test on Object to JSON.
    =========================================================
    */

    //going to start by listing out potential data we're going to track.
        private String name;
        private int ID;
        private Integer arrivalTime;
        private Integer slidesWatched;

    public studentDetails(String name, int ID, Integer arrivalTime, Integer slidesWatched) {
        super();
        this.name = name;
        this.ID = ID;
        this.arrivalTime = arrivalTime;
        this.slidesWatched = slidesWatched;
    }
    /*
    =========================
    Getters and Setters
    =========================
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public Integer getArrivalTime(Integer arrivalTime) {
        return arrivalTime;
    }

    public void setArrivalTime() {
        this.arrivalTime = arrivalTime;
    }

    public Integer getSlidesWatched(Integer slidesWatched) {
        return slidesWatched;
    }

    public void setSlidesWatched() {
        this.slidesWatched = slidesWatched;
    }
}

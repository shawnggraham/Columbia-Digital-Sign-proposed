public class slideDetails {

    //going to start by listing out potential data we're going to track.
    private String name;
    private String date;
    private Integer arrivalTime;
    private Integer slidesWatched;

    public slideDetails(String name, String date, Integer arrivalTime, Integer slidesWatched) {
        super();
        this.name = name;
        this.date = date;
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
    public String getDate() {
        return date;
    }
    public Integer getArrivalTime(Integer arrivalTime) {
        return arrivalTime;
    }
    public Integer getSlidesWatched(Integer slidesWatched) {
        return slidesWatched;
    }

    public void setName(String name) {
        this.name = name;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setArrivalTime() {
        this.arrivalTime = arrivalTime;
    }
    public void setSlidesWatched() {
        this.slidesWatched = slidesWatched;
    }
}

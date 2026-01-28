/** Slide class
 *
 * Has its own duration (int) and name (int)
 * */

public class Slide {
     public int duration;
     public int ID;
     public String name;

     Slide(String name, int ID, int duration) {
         this.duration = duration;
         this.name = name;
         this.ID = ID;
     }

     public String toString() {
         return "Slide " + this.name + " # " + ID + " [Duration: " + this.duration + "s]";
     }
}

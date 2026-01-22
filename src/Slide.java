/** Slide class
 *
 * Has its own duration (int) and name (int)
 * */

public class Slide {
     public int duration;
     public int name;

     Slide(int name, int duration) {
         this.duration = duration;
         this.name = name;
     }

     public String toString() {
         return "Slide #" + this.name + " Duration: " + this.duration + "s";
     }
}

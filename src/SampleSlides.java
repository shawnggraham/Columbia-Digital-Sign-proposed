
//            This is just a sample class and method to illustrate how we can pull the
//             variables sent from the addButton click event on the form and man handle them
//             this just outputs to the console we can delete this file later

import java.util.List;

public class SampleSlides {

    public static void handleSlides(List<ColumbiaSignUI.SlideDef> slides) {
        System.out.println("=== SampleSlides.handleSlides() ===");
        System.out.println("slideCount = " + (slides == null ? 0 : slides.size()));

        if (slides != null) {
            for (ColumbiaSignUI.SlideDef s : slides) {
                System.out.println(
                        "slideId=" + s.getSlideId() +
                                ", order=" + s.getSlideOrder() +
                                ", name=\"" + s.getSlideName() + "\"" +
                                ", durationSec=" + s.getDurationSeconds() +
                                ", imagePath=" + (s.getImagePath() == null ? "null" : "\"" + s.getImagePath() + "\"")
                );
            }
        }

        System.out.println("===================================");
    }
}

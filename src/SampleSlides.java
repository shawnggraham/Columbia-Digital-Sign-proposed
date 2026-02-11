// FILE: src/SampleSlides.java
// Purpose: Takes the slide list from ColumbiaSignUI and writes slidesData.json (pretty-printed) with Gson.
// Also prints to console so you can sanity-check what just got saved.

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SampleSlides {

    /* =========================================================
       Gson + output filename

       One Gson instance (pretty printed) and one default output file.
       Keep it dead simple.
       ========================================================= */
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final String DEFAULT_OUTPUT_FILE = "slidesData.json";

    /* =========================================================
       Main entrypoint (called by the UI)

       UI hands us the slide list.
       We do 2 things:
         1) Print it (quick "does this look right?" check)
         2) Write slidesData.json
       ========================================================= */
    public static void handleSlides(List<ColumbiaSignUI.SlideDef> slides) {

        // Don't blow up on nulls
        if (slides == null) slides = new ArrayList<>();

        // Quick console dump for verification
        printSlidesToConsole(slides);

        // Write JSON file (one file, whole list)
        writeSlidesToJsonFile(slides, DEFAULT_OUTPUT_FILE);
    }

    /* =========================================================
       Console output

       This is the "what did we just save?" printout.
       Helps catch bad order, wrong durations, missing image paths, etc.
       ========================================================= */
    private static void printSlidesToConsole(List<ColumbiaSignUI.SlideDef> slides) {

        System.out.println("=== SampleSlides.handleSlides() ===");
        System.out.println("slideCount = " + slides.size());

        for (ColumbiaSignUI.SlideDef s : slides) {
            if (s == null) continue;

            System.out.println(
                    "slideId=" + s.getSlideId() +
                            ", order=" + s.getSlideOrder() +
                            ", name=\"" + s.getSlideName() + "\"" +
                            ", durationSec=" + s.getDurationSeconds() +
                            ", imagePath=" + (s.getImagePath() == null ? "null" : "\"" + s.getImagePath() + "\"")
            );
        }

        System.out.println("===================================");
    }

    /* =========================================================
       JSON write

       Output format:
       {
         "generatedAt": "...",
         "slides": [ ... ]
       }

       We wrap the list so the file has a clean top-level structure.
       ========================================================= */
    private static void writeSlidesToJsonFile(List<ColumbiaSignUI.SlideDef> slides, String filename) {

        SlidesFile payload = new SlidesFile(slides);

        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(payload, writer);
            System.out.println("Wrote " + filename);
        } catch (IOException e) {
            System.err.println("Failed to write " + filename + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* =========================================================
       JSON wrapper

       This is just the "shape" of slidesData.json:
       timestamp + slide list.
       ========================================================= */
    private static class SlidesFile {
        String generatedAt;
        List<ColumbiaSignUI.SlideDef> slides;

        SlidesFile(List<ColumbiaSignUI.SlideDef> slides) {
            this.generatedAt = LocalDateTime.now().toString();
            this.slides = (slides == null) ? new ArrayList<>() : slides;
        }
    }
}

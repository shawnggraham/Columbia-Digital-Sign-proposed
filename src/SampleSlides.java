// FILE: src/SampleSlides.java
// Purpose: Accept slides from ColumbiaSignUI and write them to a JSON file using GSON.
// Minimal change: keep console output + add JSON output (slidesData.json).

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SampleSlides {

    /* =========================================================
       BLOCK 1 — GSON + Output Filename
       ========================================================= */
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final String DEFAULT_OUTPUT_FILE = "slidesData.json";

    /* =========================================================
       BLOCK 2 — Public entrypoint called by UI
       ========================================================= */
    public static void handleSlides(List<ColumbiaSignUI.SlideDef> slides) {

        // Safety: avoid nulls
        if (slides == null) slides = new ArrayList<>();

        // Keep console output (for quick verification)
        printSlidesToConsole(slides);

        // Write JSON file (minimal: one file, all slides)
        writeSlidesToJsonFile(slides, DEFAULT_OUTPUT_FILE);
    }

    /* =========================================================
       BLOCK 3 — Console output (matches your current behavior)
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
       BLOCK 4 — JSON write (object graph as-is)
       Output format:
       {
         "generatedAt": "...",
         "slides": [
           { slideId, slideOrder, slideName, durationSeconds, imagePath },
           ...
         ]
       }
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
       BLOCK 5 — Simple wrapper for clean JSON structure
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

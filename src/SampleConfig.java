// FILE: src/SampleConfig.java
// Purpose: Takes config values from the UI and writes configData.json (pretty-printed) with Gson.

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class SampleConfig {

    /* =========================================================
       Gson + output file

       Keep this simple: one Gson instance, pretty printed,
       and one default filename for config output.
       ========================================================= */
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final String DEFAULT_OUTPUT_FILE = "configData.json";

    /* =========================================================
       Main entrypoint

       This is what the UI calls when you hit "Save Config JSON".
       We do 3 things:
         1) Print the values (so you can sanity-check them)
         2) Wrap them into a payload object
         3) Write configData.json
       ========================================================= */
    public static void handleConfig(
            String simulationStartTime,
            int weeksToSimulate,
            int schoolDaysPerWeek,
            int dailyStartOffsetSec,
            double visibleMeanSec,
            double visibleStdDevSec,
            int defaultSlideSec
    ) {

        printConfigToConsole(
                simulationStartTime,
                weeksToSimulate,
                schoolDaysPerWeek,
                dailyStartOffsetSec,
                visibleMeanSec,
                visibleStdDevSec,
                defaultSlideSec
        );

        ConfigFile payload = new ConfigFile(
                simulationStartTime,
                weeksToSimulate,
                schoolDaysPerWeek,
                dailyStartOffsetSec,
                visibleMeanSec,
                visibleStdDevSec,
                defaultSlideSec
        );

        writeConfigToJsonFile(payload, DEFAULT_OUTPUT_FILE);
    }

    /* =========================================================
       Console output

       This is just a loud "here's what we're about to write"
       so you can catch bad values before you go chasing bugs.
       ========================================================= */
    private static void printConfigToConsole(
            String simulationStartTime,
            int weeksToSimulate,
            int schoolDaysPerWeek,
            int dailyStartOffsetSec,
            double visibleMeanSec,
            double visibleStdDevSec,
            int defaultSlideSec
    ) {

        System.out.println("=== SampleConfig.handleConfig() ===");
        System.out.println("simulationStartTime = " + simulationStartTime);
        System.out.println("weeksToSimulate     = " + weeksToSimulate);
        System.out.println("schoolDaysPerWeek   = " + schoolDaysPerWeek);
        System.out.println("dailyStartOffsetSec = " + dailyStartOffsetSec);
        System.out.println("visibleMeanSec      = " + visibleMeanSec);
        System.out.println("visibleStdDevSec    = " + visibleStdDevSec);
        System.out.println("defaultSlideSec     = " + defaultSlideSec);
        System.out.println("===================================");
    }

    /* =========================================================
       JSON write

       Writes a fresh configData.json each time (overwrite).
       If this fails, we scream about it in stderr.
       ========================================================= */
    private static void writeConfigToJsonFile(ConfigFile payload, String filename) {

        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(payload, writer);
            System.out.println("Wrote " + filename);
        } catch (IOException e) {
            System.err.println("Failed to write " + filename + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* =========================================================
       JSON payload object

       This class is basically the "shape" of configData.json.
       generatedAt is just a timestamp so you can tell when the
       file was last written.
       ========================================================= */
    private static class ConfigFile {

        String generatedAt;

        String simulationStartTime;
        int weeksToSimulate;
        int schoolDaysPerWeek;
        int dailyStartOffsetSec;
        double visibleMeanSec;
        double visibleStdDevSec;
        int defaultSlideSec;

        ConfigFile(
                String simulationStartTime,
                int weeksToSimulate,
                int schoolDaysPerWeek,
                int dailyStartOffsetSec,
                double visibleMeanSec,
                double visibleStdDevSec,
                int defaultSlideSec
        ) {
            this.generatedAt = LocalDateTime.now().toString();
            this.simulationStartTime = simulationStartTime;
            this.weeksToSimulate = weeksToSimulate;
            this.schoolDaysPerWeek = schoolDaysPerWeek;
            this.dailyStartOffsetSec = dailyStartOffsetSec;
            this.visibleMeanSec = visibleMeanSec;
            this.visibleStdDevSec = visibleStdDevSec;
            this.defaultSlideSec = defaultSlideSec;
        }
    }
}

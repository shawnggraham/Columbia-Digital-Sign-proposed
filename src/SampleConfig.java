// FILE: src/SampleConfig.java
// Purpose: Accept simulation config values and write them to configData.json using GSON.

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class SampleConfig {

    /* =========================================================
       GSON + OUTPUT FILE
       ========================================================= */
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final String DEFAULT_OUTPUT_FILE = "configData.json";

    /* =========================================================
       UPDATED PUBLIC ENTRYPOINT (NOW INCLUDES START TIME)
       ========================================================= */
    public static void handleConfig(
            String simulationStartTime,   // NEW
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
       CONSOLE OUTPUT
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
       JSON WRITE
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
       CONFIG JSON WRAPPER
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

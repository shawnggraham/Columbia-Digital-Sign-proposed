import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * SampleProcessor
 *
 * Discrete-event simulation for digital sign visibility.
 *
 * - Loads JSON config, slides, students
 * - Randomizes arrival times and visibility duration
 * - Calculates which slide(s) a student sees based on start time
 * - Models slides as a circular schedule
 * - Outputs only meaningful "hit" events
 *
 * No per-second ticking. No UI logic. Console output only.
 */
public class SampleProcessor {

    /* ===============================
       JSON FILES
       =============================== */
    private static final String CONFIG_JSON_FILE   = "configData.json";
    private static final String SLIDES_JSON_FILE   = "slidesData.json";
    private static final String STUDENTS_JSON_FILE = "studentData.json";

    /* ===============================
       TIME FORMATS
       =============================== */
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    /* ===============================
       JSON / RANDOM
       =============================== */
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private final Random rng = new Random();

    /* ===============================
       JSON MODELS
       =============================== */
    public static class ConfigFile {
        String simulationStartTime;
        int weeksToSimulate;
        int schoolDaysPerWeek;
        int dailyStartOffsetSec;
        double visibleMeanSec;
        double visibleStdDevSec;
        int defaultSlideSec;
    }

    private static class SlidesFile {
        List<ColumbiaSignUI.SlideDef> slides;
    }

    private static class StudentFile {
        List<ColumbiaSignUI.StudentDef> students;
    }

    /* ===============================
       ARRIVAL EVENT MODEL
       =============================== */
    public static class StudentArrival {
        int studentId;
        String studentName;
        String day;
        LocalTime arrivalTime;

        StudentArrival(int id, String name, String day, LocalTime time) {
            this.studentId = id;
            this.studentName = name;
            this.day = day;
            this.arrivalTime = time;
        }
    }

    /* ===============================
       ENTRY POINT
       =============================== */
    public void run() {

        System.out.println("=== SAMPLE PROCESSOR START ===");

        ConfigFile config = loadConfig();
        List<ColumbiaSignUI.SlideDef> slides = loadSlides();
        List<StudentArrival> arrivals = loadStudents();

        if (config == null || slides == null || arrivals == null) {
            System.err.println("Simulation aborted due to load failure.");
            return;
        }

        runSimulation(config, slides, arrivals);

        System.out.println("=== SAMPLE PROCESSOR END ===");
    }

    /* ===============================
       LOAD CONFIG
       =============================== */
    private ConfigFile loadConfig() {
        try (FileReader r = new FileReader(CONFIG_JSON_FILE)) {
            ConfigFile cfg = gson.fromJson(r, ConfigFile.class);

            System.out.println("=== CONFIG LOADED ===");
            System.out.println("startTime        = " + cfg.simulationStartTime);
            System.out.println("visibleMeanSec   = " + cfg.visibleMeanSec);
            System.out.println("visibleStdDevSec = " + cfg.visibleStdDevSec);
            System.out.println();

            return cfg;
        } catch (Exception ex) {
            System.err.println("Failed to load configData.json");
            return null;
        }
    }

    /* ===============================
       LOAD SLIDES (CIRCULAR ORDER)
       =============================== */
    private List<ColumbiaSignUI.SlideDef> loadSlides() {
        try (FileReader r = new FileReader(SLIDES_JSON_FILE)) {
            SlidesFile data = gson.fromJson(r, SlidesFile.class);

            data.slides.sort(
                    Comparator.comparingInt(ColumbiaSignUI.SlideDef::getSlideOrder)
            );

            System.out.println("=== SLIDES LOADED ===");
            for (var s : data.slides) {
                System.out.println(
                        "[" + s.getSlideOrder() + "] " +
                                s.getSlideName() +
                                " (" + s.getDurationSeconds() + "s)"
                );
            }
            System.out.println();

            return data.slides;
        } catch (Exception ex) {
            System.err.println("Failed to load slidesData.json");
            return null;
        }
    }

    /* ===============================
       LOAD STUDENTS + RANDOMIZE ARRIVALS
       =============================== */
    private List<StudentArrival> loadStudents() {
        try (FileReader r = new FileReader(STUDENTS_JSON_FILE)) {
            StudentFile data = gson.fromJson(r, StudentFile.class);
            List<StudentArrival> out = new ArrayList<>();

            for (var s : data.students) {
                for (var a : s.getArrivals()) {

                    LocalTime base =
                            LocalTime.parse(a.getTime(), TIME_FMT);

                    // ±5 minutes arrival jitter
                    LocalTime randomized =
                            base.plusMinutes(rng.nextInt(11) - 5);

                    out.add(new StudentArrival(
                            s.getStudentId(),
                            s.getStudentName(),
                            a.getDay(),
                            randomized
                    ));
                }
            }

            // Queue order by time
            out.sort(
                    Comparator
                            .comparing((StudentArrival a) -> a.day)
                            .thenComparing(a -> a.arrivalTime)
            );

            System.out.println("=== STUDENT ARRIVALS LOADED ===");
            for (var a : out) {
                System.out.println(
                        a.day + " " +
                                a.arrivalTime.format(TIME_FMT) +
                                " — " + a.studentName
                );
            }
            System.out.println();

            return out;
        } catch (Exception ex) {
            System.err.println("Failed to load studentData.json");
            return null;
        }
    }

    /* ===============================
       CORE SLIDE VISIBILITY CALCULATION
       =============================== */
    private Map<Integer, Integer> computeSlidesSeen(
            LocalTime simStart,
            List<ColumbiaSignUI.SlideDef> slides,
            LocalTime arrivalTime,
            int visibilitySeconds
    ) {
        Map<Integer, Integer> seen = new HashMap<>();

        // Total cycle duration
        int cycleSeconds = 0;
        for (var s : slides) {
            cycleSeconds += s.getDurationSeconds();
        }

        int elapsedSeconds =
                (int) Duration.between(simStart, arrivalTime).getSeconds();

        int cycleOffset = Math.floorMod(elapsedSeconds, cycleSeconds);

        int slideIndex = 0;
        int offsetIntoSlide = 0;
        int acc = 0;

        for (int i = 0; i < slides.size(); i++) {
            int dur = slides.get(i).getDurationSeconds();
            if (cycleOffset < acc + dur) {
                slideIndex = i;
                offsetIntoSlide = cycleOffset - acc;
                break;
            }
            acc += dur;
        }

        int remaining = visibilitySeconds;

        while (remaining > 0) {
            var slide = slides.get(slideIndex);

            int secondsLeft =
                    slide.getDurationSeconds() - offsetIntoSlide;

            int seenSeconds =
                    Math.min(secondsLeft, remaining);

            seen.merge(slide.getSlideId(), seenSeconds, Integer::sum);

            remaining -= seenSeconds;
            offsetIntoSlide = 0;
            slideIndex = (slideIndex + 1) % slides.size();
        }

        return seen;
    }

    /* ===============================
       SIMULATION (EVENT-DRIVEN)
       =============================== */
    private void runSimulation(
            ConfigFile config,
            List<ColumbiaSignUI.SlideDef> slides,
            List<StudentArrival> arrivals
    ) {

        LocalTime simStart =
                LocalTime.parse(config.simulationStartTime, TIME_FMT);

        int mean = (int) config.visibleMeanSec;
        int std  = (int) config.visibleStdDevSec;

        System.out.println("=== SIMULATION START ===");

        for (StudentArrival a : arrivals) {

            int visibilitySeconds =
                    Math.max(1,
                            (int) Math.round(
                                    mean + rng.nextGaussian() * std
                            ));

            Map<Integer, Integer> seen =
                    computeSlidesSeen(
                            simStart,
                            slides,
                            a.arrivalTime,
                            visibilitySeconds
                    );

            for (var e : seen.entrySet()) {

                var slide = slides.stream()
                        .filter(s -> s.getSlideId() == e.getKey())
                        .findFirst()
                        .orElse(null);

                if (slide != null) {
                    System.out.println(
                            "[" + a.arrivalTime.format(TIME_FMT) + "] HIT — " +
                                    a.studentName +
                                    " saw \"" + slide.getSlideName() +
                                    "\" for " + e.getValue() + "s"
                    );
                }
            }
        }

        System.out.println("=== SIMULATION END ===");
    }
}

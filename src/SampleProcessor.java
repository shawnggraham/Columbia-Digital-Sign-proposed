import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SampleProcessor
 *
 * Clean Simulation Engine
 * - Loads JSON state
 * - Runs simulation
 * - Builds playback events
 * - Builds completion report
 * - Returns formatted report string for UI
 */
public class SampleProcessor {

    /* ===============================
       JSON FILES
       =============================== */
    private static final String CONFIG_JSON_FILE   = "configData.json";
    private static final String SLIDES_JSON_FILE   = "slidesData.json";
    private static final String STUDENTS_JSON_FILE = "studentData.json";

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final Map<String, Integer> DAY_INDEX = Map.of(
            "Monday", 0,
            "Tuesday", 1,
            "Wednesday", 2,
            "Thursday", 3,
            "Friday", 4
    );

    private final Gson gson = new GsonBuilder().create();
    private final Random rng = new Random();

    /* ===============================
       JSON MODELS
       =============================== */

    private static class ConfigFile {
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
       RUNTIME MODELS
       =============================== */
    public SimulationResult runFullSimulation() {

        ConfigFile config = loadConfig();
        List<ColumbiaSignUI.SlideDef> slides = loadSlides();
        List<StudentArrival> arrivals = loadStudents();

        if (config == null || slides == null || arrivals == null) {
            return new SimulationResult(
                    Collections.emptyList(),
                    Collections.emptyList()
            );
        }

        List<PlaybackEvent> events =
                generatePlaybackEvents(config, slides, arrivals);

        List<SlideCompletionRecord> report =
                generateCompletionReport(events, slides);

        return new SimulationResult(events, report);
    }

    public static class PlaybackEvent {
        public int weekNumber;
        public String studentName;
        public String day;
        public LocalTime arrivalTime;
        public int slideId;
        public String slideName;
        public int secondsToDisplay;

        public PlaybackEvent(int weekNumber,
                             String studentName,
                             String day,
                             LocalTime arrivalTime,
                             int slideId,
                             String slideName,
                             int secondsToDisplay) {

            this.weekNumber = weekNumber;
            this.studentName = studentName;
            this.day = day;
            this.arrivalTime = arrivalTime;
            this.slideId = slideId;
            this.slideName = slideName;
            this.secondsToDisplay = secondsToDisplay;
        }
    }

    public static class SlideCompletionRecord {
        public int weekNumber;
        public String studentName;
        public String slideName;
        public boolean fullySeen;

        public SlideCompletionRecord(int weekNumber,
                                     String studentName,
                                     String slideName,
                                     boolean fullySeen) {

            this.weekNumber = weekNumber;
            this.studentName = studentName;
            this.slideName = slideName;
            this.fullySeen = fullySeen;
        }
    }
    public static class SimulationResult {
        public List<PlaybackEvent> playbackEvents;
        public List<SlideCompletionRecord> completionReport;

        public SimulationResult(List<PlaybackEvent> playbackEvents,
                                List<SlideCompletionRecord> completionReport) {
            this.playbackEvents = playbackEvents;
            this.completionReport = completionReport;
        }
    }

    /* ===============================
       PUBLIC ENTRY FOR UI
       =============================== */

    public String runAndReturnReport() {

        ConfigFile config = loadConfig();
        List<ColumbiaSignUI.SlideDef> slides = loadSlides();
        List<StudentArrival> arrivals = loadStudents();

        if (config == null || slides == null || arrivals == null)
            return "Simulation failed: missing JSON data.";

        List<PlaybackEvent> events =
                generatePlaybackEvents(config, slides, arrivals);

        List<SlideCompletionRecord> report =
                generateCompletionReport(events, slides);

        return formatReport(events, report);
    }

    /* ===============================
       PLAYBACK GENERATION
       =============================== */

    private List<PlaybackEvent> generatePlaybackEvents(
            ConfigFile config,
            List<ColumbiaSignUI.SlideDef> slides,
            List<StudentArrival> arrivals) {

        List<PlaybackEvent> events = new ArrayList<>();

        LocalTime simStart =
                LocalTime.parse(config.simulationStartTime, TIME_FMT);

        int mean = (int) config.visibleMeanSec;
        int std  = (int) config.visibleStdDevSec;

        for (int week = 1; week <= config.weeksToSimulate; week++) {

            for (StudentArrival a : arrivals) {

                int visibilitySeconds =
                        Math.max(1,
                                (int) Math.round(
                                        mean + rng.nextGaussian() * std
                                ));

                events.addAll(
                        computePlaybackForStudent(
                                simStart,
                                slides,
                                config.schoolDaysPerWeek,
                                a,
                                visibilitySeconds,
                                week
                        )
                );
            }
        }

        return events;
    }

    /* ===============================
       COMPLETION REPORT
       =============================== */

    private List<SlideCompletionRecord> generateCompletionReport(
            List<PlaybackEvent> events,
            List<ColumbiaSignUI.SlideDef> slides) {

        List<SlideCompletionRecord> report = new ArrayList<>();

        Map<String, Integer> accumulation = new HashMap<>();

        Map<String, Integer> slideDurations =
                slides.stream()
                        .collect(Collectors.toMap(
                                ColumbiaSignUI.SlideDef::getSlideName,
                                ColumbiaSignUI.SlideDef::getDurationSeconds
                        ));

        for (PlaybackEvent e : events) {

            String key = e.weekNumber + "|" +
                    e.studentName + "|" +
                    e.slideName;

            accumulation.merge(key, e.secondsToDisplay, Integer::sum);
        }

        for (String key : accumulation.keySet()) {

            String[] parts = key.split("\\|");

            int week = Integer.parseInt(parts[0]);
            String student = parts[1];
            String slide = parts[2];

            int secondsSeen = accumulation.get(key);
            int required = slideDurations.getOrDefault(slide, Integer.MAX_VALUE);

            report.add(new SlideCompletionRecord(
                    week,
                    student,
                    slide,
                    secondsSeen >= required
            ));
        }

        return report;
    }

    /* ===============================
       REPORT FORMATTER
       =============================== */

    private String formatReport(List<PlaybackEvent> events,
                                List<SlideCompletionRecord> report) {

        StringBuilder sb = new StringBuilder();

        sb.append("=== PLAYBACK EVENTS ===\n\n");

        for (PlaybackEvent e : events) {

            sb.append(
                    "Week " + e.weekNumber + " " +
                            e.day + " " +
                            e.arrivalTime.format(TIME_FMT) +
                            " — " +
                            e.studentName +
                            " saw \"" +
                            e.slideName +
                            "\" for " +
                            e.secondsToDisplay +
                            "s\n"
            );
        }

        sb.append("\n=== COMPLETION REPORT ===\n\n");

        for (SlideCompletionRecord r : report) {

            sb.append(
                    "Week " + r.weekNumber +
                            " — " + r.studentName +
                            " — " + r.slideName +
                            " — " +
                            (r.fullySeen ? "FULL" : "PARTIAL") +
                            "\n"
            );
        }

        return sb.toString();
    }

    /* ===============================
       STUDENT ARRIVAL MODEL
       =============================== */

    private static class StudentArrival {
        String studentName;
        String day;
        LocalTime arrivalTime;

        StudentArrival(String name, String day, LocalTime time) {
            this.studentName = name;
            this.day = day;
            this.arrivalTime = time;
        }
    }

    /* ===============================
       JSON LOADERS
       =============================== */

    private List<StudentArrival> loadStudents() {

        try (FileReader r = new FileReader(STUDENTS_JSON_FILE)) {

            StudentFile data = gson.fromJson(r, StudentFile.class);
            List<StudentArrival> out = new ArrayList<>();

            for (var s : data.students) {
                for (var a : s.getArrivals()) {

                    LocalTime base =
                            LocalTime.parse(a.getTime(), TIME_FMT);

                    LocalTime randomized =
                            base.plusMinutes(rng.nextInt(11) - 5);

                    out.add(new StudentArrival(
                            s.getStudentName(),
                            a.getDay(),
                            randomized
                    ));
                }
            }

            out.sort(
                    Comparator
                            .comparing((StudentArrival a) ->
                                    DAY_INDEX.getOrDefault(a.day, 99))
                            .thenComparing(a -> a.arrivalTime)
            );

            return out;

        } catch (Exception ex) {
            return null;
        }
    }

    private List<ColumbiaSignUI.SlideDef> loadSlides() {

        try (FileReader r = new FileReader(SLIDES_JSON_FILE)) {

            SlidesFile data = gson.fromJson(r, SlidesFile.class);

            data.slides.sort(
                    Comparator.comparingInt(
                            ColumbiaSignUI.SlideDef::getSlideOrder
                    )
            );

            return data.slides;

        } catch (Exception ex) {
            return null;
        }
    }

    private ConfigFile loadConfig() {

        try (FileReader r = new FileReader(CONFIG_JSON_FILE)) {
            return gson.fromJson(r, ConfigFile.class);
        } catch (Exception ex) {
            return null;
        }
    }

    /* ===============================
       CORE PLAYBACK COMPUTATION
       =============================== */

    private List<PlaybackEvent> computePlaybackForStudent(
            LocalTime simStart,
            List<ColumbiaSignUI.SlideDef> slides,
            int schoolDaysPerWeek,
            StudentArrival a,
            int visibilitySeconds,
            int weekNumber) {

        List<PlaybackEvent> out = new ArrayList<>();

        int cycleSeconds = slides.stream()
                .mapToInt(ColumbiaSignUI.SlideDef::getDurationSeconds)
                .sum();

        int dayOffset = DAY_INDEX.getOrDefault(a.day, 0);
        int secondsInDay = 86400;
        int secondsInWeek = secondsInDay * schoolDaysPerWeek;

        int secondsFromStartOfDay =
                (int) Duration.between(simStart, a.arrivalTime).getSeconds();

        int elapsedSeconds =
                (weekNumber - 1) * secondsInWeek +
                        dayOffset * secondsInDay +
                        secondsFromStartOfDay;

        int cycleOffset =
                Math.floorMod(elapsedSeconds, cycleSeconds);

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

            int showSeconds =
                    Math.min(secondsLeft, remaining);

            out.add(new PlaybackEvent(
                    weekNumber,
                    a.studentName,
                    a.day,
                    a.arrivalTime,
                    slide.getSlideId(),
                    slide.getSlideName(),
                    showSeconds
            ));

            remaining -= showSeconds;
            offsetIntoSlide = 0;
            slideIndex = (slideIndex + 1) % slides.size();
        }

        return out;
    }
}

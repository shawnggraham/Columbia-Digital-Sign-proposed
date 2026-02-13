import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class SampleProcessor {

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

    /* ============================================================
       CIRCULAR LINKED LIST NODE
    ============================================================ */

    private static class SlideNode {
        ColumbiaSignUI.SlideDef slide;
        SlideNode next;

        SlideNode(ColumbiaSignUI.SlideDef slide) {
            this.slide = slide;
        }
    }

    /* ============================================================
       ARRIVAL QUEUE MODEL
    ============================================================ */

    private static class ArrivalEvent {
        String studentName;
        int weekNumber;
        String day;
        LocalTime arrivalTime;
        long absoluteSeconds;
        int remainingVisibilitySeconds;

        ArrivalEvent(String studentName,
                     int weekNumber,
                     String day,
                     LocalTime arrivalTime,
                     long absoluteSeconds,
                     int visibilitySeconds) {

            this.studentName = studentName;
            this.weekNumber = weekNumber;
            this.day = day;
            this.arrivalTime = arrivalTime;
            this.absoluteSeconds = absoluteSeconds;
            this.remainingVisibilitySeconds = visibilitySeconds;
        }
    }

    /* ============================================================
       RESULT MODELS
    ============================================================ */

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

    /* ============================================================
       JSON STRUCTURES
    ============================================================ */

    private static class ConfigFile {
        String simulationStartTime;
        int arrivalRandomMinutes;
        int weeksToSimulate;
        int schoolDaysPerWeek;
        double visibleMeanSec;
        double visibleStdDevSec;
    }

    private static class SlidesFile {
        List<ColumbiaSignUI.SlideDef> slides;
    }

    private static class StudentFile {
        List<ColumbiaSignUI.StudentDef> students;
    }

    /* ============================================================
       ENTRY POINTS
    ============================================================ */

    public SimulationResult runFullSimulation() {

        ConfigFile config = loadConfig();
        List<ColumbiaSignUI.SlideDef> slides = loadSlides();
        Queue<ArrivalEvent> queue = buildArrivalQueue(config);

        if (config == null || slides == null || queue == null || slides.isEmpty()) {
            return new SimulationResult(
                    Collections.emptyList(),
                    Collections.emptyList()
            );
        }

        SlideNode head = buildCircularSlideList(slides);

        return runTimelineSimulation(config, head, queue, slides);
    }

    public String runAndReturnReport() {
        SimulationResult result = runFullSimulation();

        StringBuilder sb = new StringBuilder();
        sb.append("=== PLAYBACK EVENTS ===\n\n");

        for (PlaybackEvent e : result.playbackEvents) {
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

        for (SlideCompletionRecord r : result.completionReport) {
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

    /* ============================================================
       TIME FORMATTER FOR CLOCK LOGGING
    ============================================================ */

    private String formatSimulationTime(long totalSeconds, int schoolDaysPerWeek) {

        int secondsPerDay = 86400;
        int secondsPerWeek = secondsPerDay * schoolDaysPerWeek;

        int week = (int)(totalSeconds / secondsPerWeek) + 1;
        long remainderWeek = totalSeconds % secondsPerWeek;

        int dayIndex = (int)(remainderWeek / secondsPerDay);
        long remainderDay = remainderWeek % secondsPerDay;

        int hours = (int)(remainderDay / 3600);
        int minutes = (int)((remainderDay % 3600) / 60);
        int seconds = (int)(remainderDay % 60);

        return "Week " + week +
                " DayIndex " + dayIndex +
                String.format(" %02d:%02d:%02d", hours, minutes, seconds);
    }

    /* ============================================================
       BUILD CIRCULAR LIST
    ============================================================ */

    private SlideNode buildCircularSlideList(List<ColumbiaSignUI.SlideDef> slides) {

        slides.sort(Comparator.comparingInt(ColumbiaSignUI.SlideDef::getSlideOrder));

        SlideNode head = new SlideNode(slides.get(0));
        SlideNode current = head;

        for (int i = 1; i < slides.size(); i++) {
            current.next = new SlideNode(slides.get(i));
            current = current.next;
        }

        current.next = head;

        return head;
    }

    /* ============================================================
       BUILD ARRIVAL QUEUE
    ============================================================ */

    private Queue<ArrivalEvent> buildArrivalQueue(ConfigFile config) {

        try (FileReader r = new FileReader(STUDENTS_JSON_FILE)) {

            StudentFile data = gson.fromJson(r, StudentFile.class);
            if (data == null || data.students == null) return null;

            List<ArrivalEvent> temp = new ArrayList<>();

            LocalTime simStart = LocalTime.parse(config.simulationStartTime, TIME_FMT);
            int variance = Math.max(0, config.arrivalRandomMinutes);

            int secondsPerDay = 86400;
            int secondsPerWeek = secondsPerDay * config.schoolDaysPerWeek;

            for (int week = 1; week <= config.weeksToSimulate; week++) {

                for (var s : data.students) {

                    for (var a : s.getArrivals()) {

                        LocalTime base = LocalTime.parse(a.getTime(), TIME_FMT);
                        int offset = rng.nextInt(variance * 2 + 1) - variance;
                        LocalTime randomized = base.plusMinutes(offset);

                        if (randomized.isBefore(simStart)) {
                            randomized = simStart;
                        }

                        int dayOffset = DAY_INDEX.getOrDefault(a.getDay(), 0);

                        long secondsFromStartOfDay =
                                Duration.between(simStart, randomized).getSeconds();

                        long absoluteSeconds =
                                (long)(week - 1) * secondsPerWeek +
                                        (long)dayOffset * secondsPerDay +
                                        secondsFromStartOfDay;

                        int visibilitySeconds =
                                Math.max(1,
                                        (int) Math.round(
                                                config.visibleMeanSec +
                                                        rng.nextGaussian() * config.visibleStdDevSec
                                        ));

                        temp.add(new ArrivalEvent(
                                s.getStudentName(),
                                week,
                                a.getDay(),
                                randomized,
                                absoluteSeconds,
                                visibilitySeconds
                        ));
                    }
                }
            }

            temp.sort(Comparator.comparingLong(a -> a.absoluteSeconds));
            return new LinkedList<>(temp);

        } catch (Exception ex) {
            return null;
        }
    }

    /* ============================================================
       CORE LOOP WITH CLOCK LOGGING
    ============================================================ */

    private SimulationResult runTimelineSimulation(
            ConfigFile config,
            SlideNode head,
            Queue<ArrivalEvent> queue,
            List<ColumbiaSignUI.SlideDef> slides) {

        List<PlaybackEvent> playback = new ArrayList<>();
        List<SlideCompletionRecord> completion = new ArrayList<>();
        Map<String, Integer> accumulation = new HashMap<>();

        SlideNode current = head;
        long currentTime = 0;

        int secondsPerDay = 86400;
        long simulationEnd =
                (long) config.weeksToSimulate *
                        config.schoolDaysPerWeek *
                        secondsPerDay;

        List<ArrivalEvent> active = new ArrayList<>();

        while (currentTime < simulationEnd) {

            System.out.println(
                    "[CLOCK] " +
                            formatSimulationTime(currentTime, config.schoolDaysPerWeek) +
                            " — Showing slide: " +
                            current.slide.getSlideName()
            );

            while (!queue.isEmpty() &&
                    queue.peek().absoluteSeconds <= currentTime) {

                ArrivalEvent arriving = queue.poll();

                System.out.println(
                        "[ARRIVAL HIT] " +
                                formatSimulationTime(currentTime, config.schoolDaysPerWeek) +
                                " — " +
                                arriving.studentName +
                                " entered system"
                );

                active.add(arriving);
            }

            int slideDuration = current.slide.getDurationSeconds();

            Iterator<ArrivalEvent> it = active.iterator();

            while (it.hasNext()) {

                ArrivalEvent viewer = it.next();

                int showSeconds =
                        Math.min(slideDuration,
                                viewer.remainingVisibilitySeconds);

                System.out.println(
                        "[SLIDE HIT] " +
                                formatSimulationTime(currentTime, config.schoolDaysPerWeek) +
                                " — " +
                                viewer.studentName +
                                " seeing \"" +
                                current.slide.getSlideName() +
                                "\" for " +
                                showSeconds + "s"
                );

                playback.add(new PlaybackEvent(
                        viewer.weekNumber,
                        viewer.studentName,
                        viewer.day,
                        viewer.arrivalTime,
                        current.slide.getSlideId(),
                        current.slide.getSlideName(),
                        showSeconds
                ));

                String key =
                        viewer.weekNumber + "|" +
                                viewer.studentName + "|" +
                                current.slide.getSlideName();

                accumulation.merge(key, showSeconds, Integer::sum);

                viewer.remainingVisibilitySeconds -= showSeconds;

                if (viewer.remainingVisibilitySeconds <= 0) {
                    it.remove();
                }
            }

            currentTime += slideDuration;
            current = current.next;
        }

        Map<String, Integer> slideDurations =
                slides.stream().collect(Collectors.toMap(
                        ColumbiaSignUI.SlideDef::getSlideName,
                        ColumbiaSignUI.SlideDef::getDurationSeconds
                ));

        List<String> sortedKeys = new ArrayList<>(accumulation.keySet());
        sortedKeys.sort(Comparator.naturalOrder());

        for (String key : sortedKeys) {

            String[] parts = key.split("\\|");

            int week = Integer.parseInt(parts[0]);
            String student = parts[1];
            String slide = parts[2];

            int secondsSeen = accumulation.get(key);
            int required = slideDurations.getOrDefault(slide, Integer.MAX_VALUE);

            completion.add(new SlideCompletionRecord(
                    week,
                    student,
                    slide,
                    secondsSeen >= required
            ));
        }

        return new SimulationResult(playback, completion);
    }

    /* ============================================================
       JSON LOADERS
    ============================================================ */

    private List<ColumbiaSignUI.SlideDef> loadSlides() {
        try (FileReader r = new FileReader(SLIDES_JSON_FILE)) {
            SlidesFile data = gson.fromJson(r, SlidesFile.class);
            return data == null ? null : data.slides;
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
}

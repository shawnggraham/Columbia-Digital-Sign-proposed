// FILE: /home/shawng/IdeaProjects/Columbia-Digital-Sign/src/ColumbiaSignUI.java

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
=============================================================
Columbia Sign UI (Swing) — UI-only, JSON-save hooks

Key behavior:
- Students have multiple arrivals (day + time) in-memory
- Selecting a student shows only their arrivals
- "Write Student Information to JSON" sends ALL students + arrivals
  to StudentSample.handleStudents(...) (writes studentData.json)
- "Save Slides JSON" sends slides to SampleSlides.handleSlides(...) (writes slidesData.json)
- "Save Config JSON" sends config to SampleConfig.handleConfig(...) (writes configData.json)
- On startup, UI auto-loads:
    - studentData.json
    - slidesData.json
    - configData.json

NEW:
- Simulation Start Time dropdown in Config panel (15-minute intervals, HH:mm)
- Saved/loaded via configData.json
=============================================================
*/

public class ColumbiaSignUI extends JFrame {

    /* ===============================
       BLOCK 0 — Auto-load filenames + Gson
       =============================== */
    private static final String STUDENTS_JSON_FILE = "studentData.json";
    private static final String SLIDES_JSON_FILE   = "slidesData.json";
    private static final String CONFIG_JSON_FILE   = "configData.json";
    private static final String IMAGE_FOLDER = "slide-images";

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /* ===============================
       BLOCK 0.1 — Wrapper types for auto-load
       (Matches the writers: { generatedAt, students/slides/config })
       =============================== */
    private static class StudentFile {
        String generatedAt;
        List<StudentDef> students;
    }

    private static class SlidesFile {
        String generatedAt;
        List<SlideDef> slides;
    }

    private static class ConfigFile {
        String generatedAt;

        // NEW
        String simulationStartTime;

        int weeksToSimulate;
        int schoolDaysPerWeek;
        int dailyStartOffsetSec;
        double visibleMeanSec;
        double visibleStdDevSec;
        int defaultSlideSec;
    }

    /* ===============================
       Data Contracts
       =============================== */

    public static class SlideDef {
        private int slideId;
        private int slideOrder;
        private String slideName;
        private int durationSeconds;
        private String imagePath;
        private int rotationDegrees;

        // Needed for Gson
        public SlideDef() { }

        public SlideDef(int slideId, int slideOrder, String slideName,
                        int durationSeconds, String imagePath) {

            this.slideId = slideId;
            this.slideOrder = slideOrder;
            this.slideName = slideName;
            this.durationSeconds = durationSeconds;
            this.imagePath = imagePath;
            this.rotationDegrees = 0;
        }
        public int getRotationDegrees() {
            return rotationDegrees;
        }

        public void setRotationDegrees(int rotationDegrees) {
            this.rotationDegrees = rotationDegrees;
        }

        public int getSlideId() { return slideId; }
        public int getSlideOrder() { return slideOrder; }
        public String getSlideName() { return slideName; }
        public int getDurationSeconds() { return durationSeconds; }
        public String getImagePath() { return imagePath; }

        public void setSlideOrder(int slideOrder) { this.slideOrder = slideOrder; }
        public void setSlideName(String slideName) { this.slideName = slideName; }
        public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }
        public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    }

    /* ===============================
       STUDENTS + ARRIVALS MODELS
       =============================== */

    public static class ArrivalDef {
        private String day;   // Monday..Friday
        private String time;  // "HH:mm"

        // Needed for Gson
        public ArrivalDef() { }

        public ArrivalDef(String day, String time) {
            this.day = day;
            this.time = time;
        }

        public String getDay() { return day; }
        public String getTime() { return time; }

        public void setDay(String day) { this.day = day; }
        public void setTime(String time) { this.time = time; }

        @Override
        public String toString() {
            return day + " @ " + time;
        }
    }

    public static class StudentDef {
        private int studentId;
        private String studentName;

        // IMPORTANT: not final so Gson can restore it reliably
        private List<ArrivalDef> arrivals = new ArrayList<>();

        // Needed for Gson
        public StudentDef() { }

        public StudentDef(int studentId, String studentName) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.arrivals = new ArrayList<>();
        }

        public int getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }

        public List<ArrivalDef> getArrivals() {
            if (arrivals == null) arrivals = new ArrayList<>();
            return arrivals;
        }

        public void setStudentName(String studentName) { this.studentName = studentName; }

        public void addArrival(ArrivalDef a) { getArrivals().add(a); }

        public void removeArrivalAt(int idx) {
            List<ArrivalDef> list = getArrivals();
            if (idx < 0 || idx >= list.size()) return;
            list.remove(idx);
        }

        public boolean hasArrival(String day, String time) {
            for (ArrivalDef a : getArrivals()) {
                if (a == null) continue;
                if (safeTrim(a.getDay()).equals(day) && safeTrim(a.getTime()).equals(time)) return true;
            }
            return false;
        }

        @Override
        public String toString() {
            int n = getArrivals().size();
            return "ID: " + studentId + " | " + studentName + "  (" + n + " arrival" + (n == 1 ? "" : "s") + ")";
        }
    }

    public static class SignConfig {
        public final int weeksToSimulate;
        public final int schoolDaysPerWeek;
        public final double visibleMeanSec;
        public final double visibleStdDevSec;

        public SignConfig(int weeksToSimulate,
                          int schoolDaysPerWeek,
                          double visibleMeanSec,
                          double visibleStdDevSec) {
            this.weeksToSimulate = weeksToSimulate;
            this.schoolDaysPerWeek = schoolDaysPerWeek;
            this.visibleMeanSec = visibleMeanSec;
            this.visibleStdDevSec = visibleStdDevSec;
        }
    }

    /* ===============================
       JSON Save Hooks (no IO here)
       =============================== */
    public interface ConfigSaveHandler { void onSaveConfig(SignConfig config); }
    public interface StudentsSaveHandler { void onSaveStudents(List<StudentDef> students); }
    public interface SlidesSaveHandler { void onSaveSlides(List<SlideDef> slides); }

    private ConfigSaveHandler configSaveHandler = null;
    private StudentsSaveHandler studentsSaveHandler = null;
    private SlidesSaveHandler slidesSaveHandler = null;

    public void setConfigSaveHandler(ConfigSaveHandler h) { this.configSaveHandler = h; }
    public void setStudentsSaveHandler(StudentsSaveHandler h) { this.studentsSaveHandler = h; }
    public void setSlidesSaveHandler(SlidesSaveHandler h) { this.slidesSaveHandler = h; }

    /* ===============================
       UI: Left Tabs
       =============================== */
    private JTabbedPane leftTabs;

    // Slides tab
    private JButton btnBrowseSlideImage;
    private JTextField txtSlideImagePath;
    private JTextField txtSlideName;
    private JSpinner spnSlideDuration;
    private JButton btnAddSlide;

    private JTable tblSlides;
    private SlideTableModel slideTableModel;
    private JButton btnMoveSlideUp;
    private JButton btnMoveSlideDown;
    private JButton btnDeleteSlide;
    private JButton btnSaveSlidesJson;

    // Students tab
    private JTextField txtStudentName;
    private JButton btnAddStudent;
    private JButton btnUpdateStudent;
    private JButton btnClearStudentFields;
    private JButton btnDeleteStudent;

    private JList<StudentDef> studentList;
    private DefaultListModel<StudentDef> studentModel;

    private JComboBox<String> cboArrivalDay;
    private JComboBox<String> cboArrivalTime;
    private JButton btnAddArrival;
    private JButton btnRemoveArrival;

    private JList<ArrivalDef> arrivalsList;
    private DefaultListModel<ArrivalDef> arrivalsModel;

    private JButton btnWriteStudentsJson;

    /* ===============================
       UI: Config (Right-Top)
       =============================== */
    private JComboBox<String> cboSimulationStartTime; // NEW
    private JSpinner spnWeeks;
    private JSpinner spnSchoolDaysPerWeek;
    private JSpinner spnVisibleMean;
    private JSpinner spnVisibleStd;
    private JButton btnSaveConfigJson;

    /* ===============================
       UI: Preview
       =============================== */
    private JLabel lblPreview;
    private JButton btnRotatePreview;
    private int previewRotationDegrees = 0;
    private Image originalPreviewImage = null;

    /* ===============================
       UI: Simulation controls (in-memory)
       =============================== */
    private JButton btnRunSimulation;
    private JButton btnStopRealtime;
    private JRadioButton rbFast;
    private JRadioButton rbRealtime;
    private JComboBox<String> cboPlaybackSpeed;
    private JLabel lblStatus;
    private JTextArea txtResults;
    /* ===============================
       REAL-TIME PLAYBACK STATE
       =============================== */
    private javax.swing.Timer playbackTimer;
    private List<SampleProcessor.PlaybackEvent> playbackEvents;
    private SampleProcessor.SimulationResult currentResult;
    private int playbackEventIndex = 0;
    private double playbackSpeedMultiplier = 1.0;
    /* ===============================
       IDs
       =============================== */
    private int nextSlideId = 1;
    private int nextStudentId = 1;

    public ColumbiaSignUI() {
        setTitle("columbia-dash-sign");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1220, 780));

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(root);

        JPanel leftPanel = buildLeftTabbedPanel();
        JPanel rightPanel = buildRightPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setResizeWeight(0.48);
        split.setDividerLocation(600);

        root.add(split, BorderLayout.CENTER);
        root.add(buildSimulationPanel(), BorderLayout.SOUTH);

        wireEvents();

        setLocationRelativeTo(null);
        setVisible(true);

        // Auto-load JSON after UI is live and models exist
        SwingUtilities.invokeLater(() -> {
            txtStudentName.requestFocusInWindow();
            autoLoadJsonOnStartup();
        });
    }

    /* ===============================
       Slides table model
       =============================== */
    private static class SlideTableModel extends AbstractTableModel {
        private final String[] cols = {"Order", "Slide (ID)", "Duration (sec)", "Image"};
        private final List<SlideDef> slides = new ArrayList<>();

        public List<SlideDef> getSlides() { return slides; }

        public void addSlide(SlideDef s) {
            slides.add(s);
            renumberOrders();
            fireTableDataChanged();
        }

        public SlideDef getAt(int row) {
            if (row < 0 || row >= slides.size()) return null;
            return slides.get(row);
        }

        public void removeAt(int row) {
            if (row < 0 || row >= slides.size()) return;
            slides.remove(row);
            renumberOrders();
            fireTableDataChanged();
        }

        public void move(int row, int delta) {
            int newRow = row + delta;
            if (row < 0 || row >= slides.size()) return;
            if (newRow < 0 || newRow >= slides.size()) return;

            SlideDef tmp = slides.get(row);
            slides.set(row, slides.get(newRow));
            slides.set(newRow, tmp);

            renumberOrders();
            fireTableDataChanged();
        }

        public void renumberOrders() {
            for (int i = 0; i < slides.size(); i++) slides.get(i).setSlideOrder(i + 1);
        }

        @Override public int getRowCount() { return slides.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int column) { return cols[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            SlideDef s = slides.get(rowIndex);
            switch (columnIndex) {
                case 0: return s.getSlideOrder();
                case 1: return s.getSlideName() + " (ID: " + s.getSlideId() + ")";
                case 2: return s.getDurationSeconds();
                case 3: return (s.getImagePath() != null && !s.getImagePath().trim().isEmpty()) ? "Yes" : "";
                default: return "";
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
    }

    /* ===============================
       LEFT: Tabs
       =============================== */
    private JPanel buildLeftTabbedPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        leftTabs = new JTabbedPane();
        leftTabs.addTab("Slides", buildSlidesPanel());
        leftTabs.addTab("Students", buildStudentsPanel());
        panel.add(leftTabs, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSlidesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Slides"));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        // Row 0
        btnBrowseSlideImage = new JButton("Browse Image...");
        txtSlideImagePath = new JTextField();
        txtSlideImagePath.setEditable(false);

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        panel.add(btnBrowseSlideImage, gc);

        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1; gc.gridwidth = 3; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtSlideImagePath, gc);
        gc.gridwidth = 1;

        // Row 1
        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Slide Name:"), gc);

        txtSlideName = new JTextField();
        gc.gridx = 1; gc.gridy = 1; gc.weightx = 1; gc.gridwidth = 3; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtSlideName, gc);
        gc.gridwidth = 1;

        // Row 2
        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Duration (sec):"), gc);

        spnSlideDuration = new JSpinner(new SpinnerNumberModel(20, 1, 3600, 1));
        gc.gridx = 1; gc.gridy = 2; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(spnSlideDuration, gc);

        btnAddSlide = new JButton("Add Slide");
        gc.gridx = 2; gc.gridy = 2; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        panel.add(btnAddSlide, gc);

        // Row 3
        slideTableModel = new SlideTableModel();
        tblSlides = new JTable(slideTableModel);
        tblSlides.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblSlides.getColumnModel().getColumn(0).setPreferredWidth(55);
        tblSlides.getColumnModel().getColumn(0).setMaxWidth(70);

        JScrollPane scroll = new JScrollPane(tblSlides);
        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 4;
        gc.weightx = 1; gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        panel.add(scroll, gc);
        gc.gridwidth = 1;

        // Row 4
        JPanel rowButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnMoveSlideUp = new JButton("Move Up");
        btnMoveSlideDown = new JButton("Move Down");
        btnDeleteSlide = new JButton("Delete Selected");
        btnSaveSlidesJson = new JButton("Save Slides JSON");

        rowButtons.add(btnMoveSlideUp);
        rowButtons.add(btnMoveSlideDown);
        rowButtons.add(btnDeleteSlide);
        rowButtons.add(btnSaveSlidesJson);

        gc.gridx = 0; gc.gridy = 4; gc.gridwidth = 4;
        gc.weightx = 1; gc.weighty = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(rowButtons, gc);

        return panel;
    }

    private JPanel buildStudentsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Students"));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Name row
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Student Name:"), gc);

        txtStudentName = new JTextField();
        txtStudentName.setColumns(24);
        txtStudentName.setEditable(true);
        txtStudentName.setEnabled(true);
        txtStudentName.setFocusable(true);
        txtStudentName.setMinimumSize(new Dimension(260, 28));
        txtStudentName.setPreferredSize(new Dimension(360, 28));

        gc.gridx = 1; gc.gridy = row; gc.weightx = 1; gc.gridwidth = 3; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtStudentName, gc);
        gc.gridwidth = 1;
        row++;

        // Buttons row
        JPanel studentBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnAddStudent = new JButton("Add Student");
        btnUpdateStudent = new JButton("Update Student");
        btnUpdateStudent.setEnabled(false);
        btnClearStudentFields = new JButton("Clear");
        btnDeleteStudent = new JButton("Delete Selected");

        studentBtns.add(btnAddStudent);
        studentBtns.add(btnUpdateStudent);
        studentBtns.add(btnClearStudentFields);
        studentBtns.add(btnDeleteStudent);

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4;
        gc.weightx = 1; gc.weighty = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(studentBtns, gc);
        gc.gridwidth = 1;
        row++;

        // Student list (shorter)
        studentModel = new DefaultListModel<>();
        studentList = new JList<>(studentModel);
        studentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane stScroll = new JScrollPane(studentList);
        stScroll.setPreferredSize(new Dimension(10, 170));

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4;
        gc.weightx = 1; gc.weighty = 0.35;
        gc.fill = GridBagConstraints.BOTH;
        panel.add(stScroll, gc);
        gc.gridwidth = 1;
        row++;

        // Arrivals section
        JPanel arrivalsPanel = new JPanel(new GridBagLayout());
        arrivalsPanel.setBorder(new TitledBorder("Arrivals (for selected student)"));

        GridBagConstraints ac = new GridBagConstraints();
        ac.insets = new Insets(6, 6, 6, 6);
        ac.fill = GridBagConstraints.HORIZONTAL;

        int arow = 0;

        ac.gridx = 0; ac.gridy = arow; ac.weightx = 0;
        arrivalsPanel.add(new JLabel("Arrival Day:"), ac);

        cboArrivalDay = new JComboBox<>(new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"});
        ac.gridx = 1; ac.gridy = arow; ac.weightx = 0.5;
        arrivalsPanel.add(cboArrivalDay, ac);

        ac.gridx = 2; ac.gridy = arow; ac.weightx = 0;
        arrivalsPanel.add(new JLabel("Arrival Time:"), ac);

        cboArrivalTime = new JComboBox<>(buildQuarterHourTimes());
        cboArrivalTime.setSelectedItem("08:00");
        ac.gridx = 3; ac.gridy = arow; ac.weightx = 0.5;
        arrivalsPanel.add(cboArrivalTime, ac);

        arow++;

        JPanel arrivalBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnAddArrival = new JButton("Add Arrival");
        btnRemoveArrival = new JButton("Remove Selected Arrival");
        btnAddArrival.setEnabled(false);
        btnRemoveArrival.setEnabled(false);

        arrivalBtns.add(btnAddArrival);
        arrivalBtns.add(btnRemoveArrival);

        ac.gridx = 0; ac.gridy = arow; ac.gridwidth = 4; ac.weightx = 1;
        arrivalsPanel.add(arrivalBtns, ac);
        ac.gridwidth = 1;
        arow++;

        arrivalsModel = new DefaultListModel<>();
        arrivalsList = new JList<>(arrivalsModel);
        arrivalsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane arScroll = new JScrollPane(arrivalsList);
        arScroll.setPreferredSize(new Dimension(10, 170));

        ac.gridx = 0; ac.gridy = arow; ac.gridwidth = 4;
        ac.weightx = 1; ac.weighty = 1;
        ac.fill = GridBagConstraints.BOTH;
        arrivalsPanel.add(arScroll, ac);
        arow++;

        btnWriteStudentsJson = new JButton("Write Student Information to JSON");
        ac.gridx = 0; ac.gridy = arow; ac.gridwidth = 4;
        ac.weightx = 1; ac.weighty = 0;
        ac.fill = GridBagConstraints.HORIZONTAL;
        arrivalsPanel.add(btnWriteStudentsJson, ac);

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4;
        gc.weightx = 1; gc.weighty = 0.65;
        gc.fill = GridBagConstraints.BOTH;
        panel.add(arrivalsPanel, gc);

        return panel;
    }

    private String[] buildQuarterHourTimes() {
        List<String> times = new ArrayList<>(96);
        for (int hour = 0; hour < 24; hour++) {
            for (int min = 0; min < 60; min += 15) {
                times.add(String.format("%02d:%02d", hour, min));
            }
        }
        return times.toArray(new String[0]);
    }

    /* ===============================
       RIGHT: Config + Preview + Results
       =============================== */
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(buildConfigPanel(), BorderLayout.NORTH);
        panel.add(buildPreviewPanel(), BorderLayout.CENTER);
        panel.add(buildResultsPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildConfigPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Simulation Parameters (Config JSON)"));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // NEW: Simulation Start Time (top)
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        panel.add(new JLabel("Simulation Start Time:"), gc);

        cboSimulationStartTime = new JComboBox<>(buildQuarterHourTimes());
        cboSimulationStartTime.setSelectedItem("06:00");

        gc.gridx = 1; gc.gridy = row; gc.weightx = 1;
        panel.add(cboSimulationStartTime, gc);

        gc.gridx = 2; gc.gridy = row; gc.weightx = 0;
        panel.add(new JLabel(""), gc);

        gc.gridx = 3; gc.gridy = row; gc.weightx = 1;
        panel.add(new JLabel(""), gc);

        row++;

        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        panel.add(new JLabel("Weeks to Simulate:"), gc);

        spnWeeks = new JSpinner(new SpinnerNumberModel(1, 1, 52, 1));
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1;
        panel.add(spnWeeks, gc);

        gc.gridx = 2; gc.gridy = row; gc.weightx = 0;
        panel.add(new JLabel("School Days/Week:"), gc);

        spnSchoolDaysPerWeek = new JSpinner(new SpinnerNumberModel(5, 1, 7, 1));
        gc.gridx = 3; gc.gridy = row; gc.weightx = 1;
        panel.add(spnSchoolDaysPerWeek, gc);

        row++;

        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        panel.add(new JLabel("Visible Mean (sec):"), gc);

        spnVisibleMean = new JSpinner(new SpinnerNumberModel(60.0, 1.0, 600.0, 1.0));
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1;
        panel.add(spnVisibleMean, gc);

        gc.gridx = 2; gc.gridy = row; gc.weightx = 0;
        panel.add(new JLabel("Visible StdDev (sec):"), gc);

        spnVisibleStd = new JSpinner(new SpinnerNumberModel(5.0, 0.0, 120.0, 0.5));
        gc.gridx = 3; gc.gridy = row; gc.weightx = 1;
        panel.add(spnVisibleStd, gc);

        row++;

        btnSaveConfigJson = new JButton("Save Config JSON");
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4; gc.weightx = 1;
        panel.add(btnSaveConfigJson, gc);

        return panel;
    }

    private JPanel buildPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new TitledBorder("Slide Preview"));

        lblPreview = new JLabel("No slide selected", SwingConstants.CENTER);
        lblPreview.setOpaque(true);
        lblPreview.setBackground(Color.WHITE);
        lblPreview.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        btnRotatePreview = new JButton("Rotate 90°");
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(btnRotatePreview);

        panel.add(top, BorderLayout.NORTH);
        panel.add(lblPreview, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(new TitledBorder("Results (in-memory)"));

        txtResults = new JTextArea(7, 30);
        txtResults.setEditable(false);
        txtResults.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        panel.add(new JScrollPane(txtResults), BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildSimulationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Simulation Controls (in-memory)"));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        btnRunSimulation = new JButton("Run");
        btnStopRealtime = new JButton("Stop");
        btnStopRealtime.setEnabled(false);

        rbFast = new JRadioButton("Fast (Analytical)", true);
        rbRealtime = new JRadioButton("Real-Time (Visual)");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbFast);
        bg.add(rbRealtime);

        cboPlaybackSpeed = new JComboBox<>(new String[]{"1x", "5x", "10x", "20x"});
        cboPlaybackSpeed.setSelectedItem("10x");

        lblStatus = new JLabel("Ready");

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        panel.add(btnRunSimulation, gc);

        gc.gridx = 1; gc.gridy = 0; gc.weightx = 0;
        panel.add(btnStopRealtime, gc);

        gc.gridx = 2; gc.gridy = 0; gc.weightx = 0;
        panel.add(rbFast, gc);

        gc.gridx = 3; gc.gridy = 0; gc.weightx = 0;
        panel.add(rbRealtime, gc);

        gc.gridx = 4; gc.gridy = 0; gc.weightx = 0;
        panel.add(new JLabel("Playback Speed:"), gc);

        gc.gridx = 5; gc.gridy = 0; gc.weightx = 0;
        panel.add(cboPlaybackSpeed, gc);

        gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 6; gc.weightx = 1;
        panel.add(lblStatus, gc);

        return panel;
    }

    /* ===============================
       Wiring
       =============================== */
    private void wireEvents() {

        // Slides
        btnBrowseSlideImage.addActionListener(e -> {

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select a slide image");
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Images (*.jpg, *.jpeg, *.png)", "jpg", "jpeg", "png"));

            int result = chooser.showOpenDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) return;

            File selected = chooser.getSelectedFile();
            if (selected == null || !selected.exists()) return;

            try {
                File imageDir = new File(IMAGE_FOLDER);
                if (!imageDir.exists()) imageDir.mkdirs();

                File dest = new File(imageDir, selected.getName());

                java.nio.file.Files.copy(
                        selected.toPath(),
                        dest.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );

                // Store RELATIVE path, not absolute
                txtSlideImagePath.setText(dest.getPath());

                showImagePreview(dest);

                lblStatus.setText("Image copied to " + IMAGE_FOLDER + "/");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to copy image.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });


        btnAddSlide.addActionListener(e -> {
            String name = safeTrim(txtSlideName.getText());
            int dur = (Integer) spnSlideDuration.getValue();
            String imgPath = (txtSlideImagePath.getText() == null || txtSlideImagePath.getText().trim().isEmpty())
                    ? null : txtSlideImagePath.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a slide name.", "Missing Slide Name", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int order = slideTableModel.getSlides().size() + 1;
            SlideDef s = new SlideDef(nextSlideId++, order, name, dur, imgPath);
            slideTableModel.addSlide(s);

            txtSlideName.setText("");
            int last = slideTableModel.getRowCount() - 1;
            if (last >= 0) tblSlides.getSelectionModel().setSelectionInterval(last, last);

            lblStatus.setText("Added slide.");
        });

        btnMoveSlideUp.addActionListener(e -> {
            int row = tblSlides.getSelectedRow();
            if (row < 0) return;
            slideTableModel.move(row, -1);
            int newRow = Math.max(0, row - 1);
            tblSlides.getSelectionModel().setSelectionInterval(newRow, newRow);
        });

        btnMoveSlideDown.addActionListener(e -> {
            int row = tblSlides.getSelectedRow();
            if (row < 0) return;
            slideTableModel.move(row, +1);
            int newRow = Math.min(slideTableModel.getRowCount() - 1, row + 1);
            tblSlides.getSelectionModel().setSelectionInterval(newRow, newRow);
        });

        btnDeleteSlide.addActionListener(e -> {
            int row = tblSlides.getSelectedRow();
            if (row < 0) return;
            slideTableModel.removeAt(row);
            lblStatus.setText("Deleted slide.");
        });

        tblSlides.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = tblSlides.getSelectedRow();
            SlideDef s = slideTableModel.getAt(row);
            if (s == null) return;
            previewRotationDegrees = s.getRotationDegrees();

            if (s.getImagePath() != null) {
                File f = new File(s.getImagePath());
                if (f.exists()) showImagePreview(f);
                else {
                    originalPreviewImage = null;
                    previewRotationDegrees = 0;
                    lblPreview.setIcon(null);
                    lblPreview.setText("Image not found");
                }
            } else {
                originalPreviewImage = null;
                previewRotationDegrees = 0;
                lblPreview.setIcon(null);
                lblPreview.setText(s.getSlideName() + " (no image)");
            }
        });

        btnRotatePreview.addActionListener(e -> {

            if (originalPreviewImage == null) return;

            previewRotationDegrees = (previewRotationDegrees + 90) % 360;

            int row = tblSlides.getSelectedRow();
            if (row >= 0) {
                SlideDef s = slideTableModel.getAt(row);
                if (s != null) {
                    s.setRotationDegrees(previewRotationDegrees);
                }
            }

            renderPreviewScaledAndRotated();
        });


        lblPreview.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                if (originalPreviewImage != null) renderPreviewScaledAndRotated();
            }
        });

        // Students selection
        studentList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            StudentDef s = studentList.getSelectedValue();
            onStudentSelectionChanged(s);
        });

        arrivalsList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            btnRemoveArrival.setEnabled(arrivalsList.getSelectedIndex() >= 0 && studentList.getSelectedValue() != null);
        });

        // Student buttons
        btnAddStudent.addActionListener(e -> {
            String name = safeTrim(txtStudentName.getText());
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a student name.", "Missing Student Name", JOptionPane.WARNING_MESSAGE);
                return;
            }

            StudentDef def = new StudentDef(nextStudentId++, name);
            studentModel.addElement(def);

            int idx = studentModel.size() - 1;
            studentList.setSelectedIndex(idx);

            lblStatus.setText("Added student.");
            txtStudentName.requestFocusInWindow();
        });

        btnUpdateStudent.addActionListener(e -> {
            int idx = studentList.getSelectedIndex();
            if (idx < 0) return;

            String name = safeTrim(txtStudentName.getText());
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a student name.", "Missing Student Name", JOptionPane.WARNING_MESSAGE);
                return;
            }

            StudentDef s = studentModel.get(idx);
            s.setStudentName(name);

            studentList.repaint();
            lblStatus.setText("Updated student.");
        });

        btnDeleteStudent.addActionListener(e -> {
            int idx = studentList.getSelectedIndex();
            if (idx < 0) return;

            studentModel.remove(idx);
            lblStatus.setText("Deleted student.");

            arrivalsModel.clear();
            btnAddArrival.setEnabled(false);
            btnRemoveArrival.setEnabled(false);
            btnUpdateStudent.setEnabled(false);
            clearStudentFields();
        });

        btnClearStudentFields.addActionListener(e -> clearStudentFields());

        // Arrival buttons
        btnAddArrival.addActionListener(e -> {
            StudentDef s = studentList.getSelectedValue();
            if (s == null) {
                JOptionPane.showMessageDialog(this, "Select a student first.", "Arrivals", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String day = (String) cboArrivalDay.getSelectedItem();
            String time = (String) cboArrivalTime.getSelectedItem();

            if (day == null || day.trim().isEmpty() || time == null || time.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Select a day and time.", "Arrivals", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (s.hasArrival(day, time)) {
                JOptionPane.showMessageDialog(this, "That arrival already exists for this student.", "Arrivals", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            s.addArrival(new ArrivalDef(day, time));
            loadArrivalsForStudent(s);
            studentList.repaint();
            lblStatus.setText("Added arrival for student ID " + s.getStudentId() + ".");
        });

        btnRemoveArrival.addActionListener(e -> {
            StudentDef s = studentList.getSelectedValue();
            if (s == null) return;

            int aidx = arrivalsList.getSelectedIndex();
            if (aidx < 0) return;

            s.removeArrivalAt(aidx);
            loadArrivalsForStudent(s);
            studentList.repaint();
            lblStatus.setText("Removed arrival for student ID " + s.getStudentId() + ".");
        });

        // WRITE STUDENTS JSON (console + JSON writer)
        btnWriteStudentsJson.addActionListener(e -> {
            List<StudentDef> students = snapshotStudentsFromUI();
            if (students.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No students to write.", "Students", JOptionPane.WARNING_MESSAGE);
                return;
            }

            StudentSample.handleStudents(students);

            if (studentsSaveHandler != null) {
                studentsSaveHandler.onSaveStudents(students);
            }

            lblStatus.setText("Wrote studentData.json");
            txtResults.setText(
                    "STUDENTS JSON WRITE\n" +
                            "Wrote: " + STUDENTS_JSON_FILE + "\n\n" +
                            "Data:\n" +
                            "  - students[]\n" +
                            "    - studentId\n" +
                            "    - studentName\n" +
                            "    - arrivals[] { day, time }\n"
            );
        });

        // Config save — WRITE configData.json (includes simulationStartTime)
        btnSaveConfigJson.addActionListener(e -> {

            String simulationStartTime = (String) cboSimulationStartTime.getSelectedItem();
            if (simulationStartTime == null || simulationStartTime.trim().isEmpty()) {
                simulationStartTime = "06:00";
            }

            int weeksToSimulate = (Integer) spnWeeks.getValue();
            int schoolDaysPerWeek = (Integer) spnSchoolDaysPerWeek.getValue();
            double visibleMeanSec = (Double) spnVisibleMean.getValue();
            double visibleStdDevSec = (Double) spnVisibleStd.getValue();

            // Your UI doesn't have controls for these yet; keep defaults consistent
            int dailyStartOffsetSec = 60;
            int defaultSlideSec = 20;

            // Write configData.json (console + file)
            // IMPORTANT: SampleConfig.handleConfig signature must be:
            // handleConfig(String, int, int, int, double, double, int)
            SampleConfig.handleConfig(
                    simulationStartTime,
                    weeksToSimulate,
                    schoolDaysPerWeek,
                    dailyStartOffsetSec,
                    visibleMeanSec,
                    visibleStdDevSec,
                    defaultSlideSec
            );

            // Optional handler (legacy)
            SignConfig cfg = new SignConfig(weeksToSimulate, schoolDaysPerWeek, visibleMeanSec, visibleStdDevSec);
            if (configSaveHandler != null) {
                configSaveHandler.onSaveConfig(cfg);
            }

            lblStatus.setText("Wrote configData.json");
            txtResults.setText(
                    "CONFIG JSON SAVE\n" +
                            "Wrote: " + CONFIG_JSON_FILE + "\n\n" +
                            "simulationStartTime = " + simulationStartTime + "\n" +
                            "weeksToSimulate     = " + weeksToSimulate + "\n" +
                            "schoolDaysPerWeek   = " + schoolDaysPerWeek + "\n" +
                            "dailyStartOffsetSec = " + dailyStartOffsetSec + "\n" +
                            "visibleMeanSec      = " + visibleMeanSec + "\n" +
                            "visibleStdDevSec    = " + visibleStdDevSec + "\n" +
                            "defaultSlideSec     = " + defaultSlideSec + "\n"
            );
        });

        // Slides save — console + JSON writer
        btnSaveSlidesJson.addActionListener(e -> {
            List<SlideDef> slides = snapshotSlidesFromUI();
            if (slides.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No slides to save.", "Slides", JOptionPane.WARNING_MESSAGE);
                return;
            }

            slideTableModel.renumberOrders();
            slideTableModel.fireTableDataChanged();

            SampleSlides.handleSlides(slides);

            if (slidesSaveHandler != null) {
                slidesSaveHandler.onSaveSlides(slides);
            }

            lblStatus.setText("Wrote slidesData.json");
            txtResults.setText(
                    "SLIDES JSON SAVE\n" +
                            "Wrote: " + SLIDES_JSON_FILE + "\n\n" +
                            "Data:\n" +
                            "  - slides[]\n" +
                            "    - slideId\n" +
                            "    - slideOrder\n" +
                            "    - slideName\n" +
                            "    - durationSeconds\n" +
                            "    - imagePath\n"
            );
        });

        // Simulation controls (UI-only)
        btnRunSimulation.addActionListener(e -> {

            SampleProcessor processor = new SampleProcessor();

            if (rbFast.isSelected()) {

                String report = processor.runAndReturnReport();
                txtResults.setText(report);
                lblStatus.setText("Simulation complete.");
            }
            else {

                // Keep your real-time branch exactly as it was
                currentResult = processor.runFullSimulation();
                playbackEvents = currentResult.playbackEvents;
                playbackEventIndex = 0;

                btnRunSimulation.setEnabled(false);
                btnStopRealtime.setEnabled(true);

                startRealtimePlayback();
            }
        });


        btnStopRealtime.addActionListener(e -> stopRealtime());

        ItemListener speedEnable = e -> cboPlaybackSpeed.setEnabled(rbRealtime.isSelected());
        rbFast.addItemListener(speedEnable);
        rbRealtime.addItemListener(speedEnable);
        cboPlaybackSpeed.setEnabled(false);

        SwingUtilities.invokeLater(() -> txtStudentName.requestFocusInWindow());
    }

    private void onStudentSelectionChanged(StudentDef s) {
        boolean hasStudent = (s != null);

        btnUpdateStudent.setEnabled(hasStudent);
        btnAddArrival.setEnabled(hasStudent);
        btnRemoveArrival.setEnabled(false);

        if (!hasStudent) {
            arrivalsModel.clear();
            clearStudentFields();
            return;
        }

        txtStudentName.setText(s.getStudentName());
        loadArrivalsForStudent(s);
    }

    private void loadArrivalsForStudent(StudentDef s) {
        arrivalsModel.clear();
        for (ArrivalDef a : s.getArrivals()) arrivalsModel.addElement(a);
        btnRemoveArrival.setEnabled(arrivalsList.getSelectedIndex() >= 0);
    }

    private static String safeTrim(String s) {
        return (s == null) ? "" : s.trim();
    }

    /* ===============================
       Snapshot + config reads
       =============================== */
    private SignConfig readConfigFromUI() {
        int weeks = (Integer) spnWeeks.getValue();
        int days = (Integer) spnSchoolDaysPerWeek.getValue();
        double mean = (Double) spnVisibleMean.getValue();
        double std = (Double) spnVisibleStd.getValue();
        return new SignConfig(weeks, days, mean, std);
    }

    private List<StudentDef> snapshotStudentsFromUI() {
        List<StudentDef> out = new ArrayList<>();
        for (int i = 0; i < studentModel.size(); i++) out.add(studentModel.get(i));
        return out;
    }

    private List<SlideDef> snapshotSlidesFromUI() {
        return new ArrayList<>(slideTableModel.getSlides());
    }

    private void clearStudentFields() {
        txtStudentName.setText("");
        cboArrivalDay.setSelectedItem("Monday");
        cboArrivalTime.setSelectedItem("08:00");
        arrivalsList.clearSelection();
        txtStudentName.requestFocusInWindow();
    }

    /* ===============================
       BLOCK A — Auto-load JSON on startup
       =============================== */
    private void autoLoadJsonOnStartup() {
        boolean loadedAnything = false;

        File studentsFile = new File(STUDENTS_JSON_FILE);
        if (studentsFile.exists() && studentsFile.isFile()) {
            if (loadStudentsFromJson(studentsFile)) loadedAnything = true;
        }

        File slidesFile = new File(SLIDES_JSON_FILE);
        if (slidesFile.exists() && slidesFile.isFile()) {
            if (loadSlidesFromJson(slidesFile)) loadedAnything = true;
        }

        File configFile = new File(CONFIG_JSON_FILE);
        if (configFile.exists() && configFile.isFile()) {
            if (loadConfigFromJson(configFile)) loadedAnything = true;
        }

        if (loadedAnything) {
            lblStatus.setText("Loaded JSON on startup");
        } else {
            lblStatus.setText("No JSON files found; starting fresh");
        }
    }

    /* ===============================
       BLOCK B — Load Students JSON
       =============================== */
    private boolean loadStudentsFromJson(File f) {
        try (FileReader r = new FileReader(f)) {
            StudentFile data = gson.fromJson(r, StudentFile.class);
            List<StudentDef> students = (data == null ? null : data.students);

            studentModel.clear();
            arrivalsModel.clear();

            int maxId = 0;

            if (students != null) {
                for (StudentDef s : students) {
                    if (s == null) continue;
                    studentModel.addElement(s);
                    if (s.getStudentId() > maxId) maxId = s.getStudentId();
                }
            }

            nextStudentId = maxId + 1;
            studentList.clearSelection();
            clearStudentFields();

            return true;
        } catch (IOException ex) {
            System.err.println("Failed to read " + f.getName() + ": " + ex.getMessage());
            return false;
        } catch (Exception ex) {
            System.err.println("Invalid students JSON " + f.getName() + ": " + ex.getMessage());
            return false;
        }
    }

    /* ===============================
       BLOCK C — Load Slides JSON
       =============================== */
    private boolean loadSlidesFromJson(File f) {
        try (FileReader r = new FileReader(f)) {
            SlidesFile data = gson.fromJson(r, SlidesFile.class);
            List<SlideDef> slides = (data == null ? null : data.slides);

            slideTableModel.getSlides().clear();

            int maxId = 0;

            if (slides != null) {
                slideTableModel.getSlides().addAll(slides);
                for (SlideDef s : slides) {
                    if (s == null) continue;
                    if (s.getSlideId() > maxId) maxId = s.getSlideId();
                }
            }

            slideTableModel.renumberOrders();
            slideTableModel.fireTableDataChanged();

            nextSlideId = maxId + 1;

            return true;
        } catch (IOException ex) {
            System.err.println("Failed to read " + f.getName() + ": " + ex.getMessage());
            return false;
        } catch (Exception ex) {
            System.err.println("Invalid slides JSON " + f.getName() + ": " + ex.getMessage());
            return false;
        }
    }

    /* ===============================
       BLOCK D — Load Config JSON
       Applies only the controls that exist in this UI.
       =============================== */
    private boolean loadConfigFromJson(File f) {
        try (FileReader r = new FileReader(f)) {
            ConfigFile cfg = gson.fromJson(r, ConfigFile.class);
            if (cfg == null) return false;

            // NEW
            String t = (cfg.simulationStartTime == null || cfg.simulationStartTime.trim().isEmpty())
                    ? "06:00"
                    : cfg.simulationStartTime.trim();
            cboSimulationStartTime.setSelectedItem(t);

            // Apply to UI spinners that exist:
            spnWeeks.setValue(cfg.weeksToSimulate);
            spnSchoolDaysPerWeek.setValue(cfg.schoolDaysPerWeek);
            spnVisibleMean.setValue(cfg.visibleMeanSec);
            spnVisibleStd.setValue(cfg.visibleStdDevSec);

            // dailyStartOffsetSec + defaultSlideSec exist in file but no UI controls yet

            return true;
        } catch (IOException ex) {
            System.err.println("Failed to read " + f.getName() + ": " + ex.getMessage());
            return false;
        } catch (Exception ex) {
            System.err.println("Invalid config JSON " + f.getName() + ": " + ex.getMessage());
            return false;
        }
    }

    /* ===============================
       Preview helpers
       =============================== */
    private void showImagePreview(File imgFile) {
        ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
        int w = icon.getIconWidth();
        int h = icon.getIconHeight();

        if (w <= 0 || h <= 0) {
            lblPreview.setIcon(null);
            lblPreview.setText("Preview unavailable");
            originalPreviewImage = null;
            return;
        }

        originalPreviewImage = icon.getImage();
//        previewRotationDegrees = 0;
        renderPreviewScaledAndRotated();
    }

    private void renderPreviewScaledAndRotated() {
        if (originalPreviewImage == null) return;

        int panelW = Math.max(200, lblPreview.getWidth() - 30);
        int panelH = Math.max(200, lblPreview.getHeight() - 30);

        Image rotated = rotateImage(originalPreviewImage, previewRotationDegrees);

        int imgW = rotated.getWidth(null);
        int imgH = rotated.getHeight(null);
        if (imgW <= 0 || imgH <= 0) {
            lblPreview.setIcon(null);
            lblPreview.setText("Preview unavailable");
            return;
        }

        double scale = Math.min((double) panelW / imgW, (double) panelH / imgH);
        scale = Math.min(scale, 1.0);

        int newW = (int) (imgW * scale);
        int newH = (int) (imgH * scale);

        Image scaled = rotated.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);

        lblPreview.setText("");
        lblPreview.setIcon(new ImageIcon(scaled));
    }

    private Image rotateImage(Image src, int degrees) {
        if (degrees % 360 == 0) return src;

        int w = src.getWidth(null);
        int h = src.getHeight(null);
        if (w <= 0 || h <= 0) return src;

        int newW = (degrees == 90 || degrees == 270) ? h : w;
        int newH = (degrees == 90 || degrees == 270) ? w : h;

        java.awt.image.BufferedImage out = new java.awt.image.BufferedImage(
                newW, newH, java.awt.image.BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2.translate(newW / 2.0, newH / 2.0);
        g2.rotate(Math.toRadians(degrees));
        g2.translate(-w / 2.0, -h / 2.0);

        g2.drawImage(src, 0, 0, null);
        g2.dispose();

        return out;
    }
/* ===============================
   REAL-TIME PLAYBACK ENGINE
   =============================== */

    private void startRealtimePlayback() {

        // Recalculate playback speed every time playback starts
        String speed = (String) cboPlaybackSpeed.getSelectedItem();

        if (speed != null && speed.endsWith("x")) {
            playbackSpeedMultiplier =
                    Double.parseDouble(speed.replace("x", ""));
        } else {
            playbackSpeedMultiplier = 1.0;
        }

        playNextEvent();
    }
    private void playNextEvent() {

        if (playbackEventIndex >= playbackEvents.size()) {

            stopRealtime();

            txtResults.append("\n=== SUMMARY BY STUDENT (FULL ONLY) ===\n");

            SampleProcessor processor = new SampleProcessor();
            var report = currentResult.completionReport;

            java.util.Map<String, java.util.List<String>> summary =
                    new java.util.LinkedHashMap<>();

            for (var r : report) {

                if (!r.fullySeen) continue;

                String key = "Week " + r.weekNumber + " — " + r.studentName;

                summary.computeIfAbsent(key,
                                k -> new java.util.ArrayList<>())
                        .add(r.slideName);
            }

            for (String key : summary.keySet()) {

                var slides = summary.get(key);

                txtResults.append(
                        key + " fully saw: " +
                                String.join(", ", slides) + "\n"
                );
            }

            lblStatus.setText("Playback complete.");
            return;
        }




        SampleProcessor.PlaybackEvent event =
                playbackEvents.get(playbackEventIndex);

        lblStatus.setText(
                "Week " + event.weekNumber + " " +
                        event.day + " " +
                        event.arrivalTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) +
                        " — " +
                        event.studentName +
                        " seeing \"" +
                        event.slideName +
                        "\" (" +
                        event.secondsToDisplay +
                        "s)"
        );

        txtResults.append(
                "Week " + event.weekNumber + " " +
                        event.day + " " +
                        event.arrivalTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) +
                        " — " +
                        event.studentName +
                        " saw \"" +
                        event.slideName +
                        "\" for " +
                        event.secondsToDisplay +
                        "s\n"
        );

        showSlideById(event.slideId, event.slideName);

        int delayMs =
                (int) ((event.secondsToDisplay * 1000) / playbackSpeedMultiplier);

        playbackTimer = new javax.swing.Timer(delayMs, e -> {
            playbackEventIndex++;
            playNextEvent();
        });

        playbackTimer.setRepeats(false);
        playbackTimer.start();
    }

    private void showSlideById(int slideId, String fallbackName) {

        for (SlideDef s : slideTableModel.getSlides()) {

            if (s.getSlideId() == slideId) {
                previewRotationDegrees = s.getRotationDegrees();

                if (s.getImagePath() != null) {
                    File f = new File(s.getImagePath());
                    if (f.exists()) {
                        showImagePreview(f);
                        return;
                    }
                }

                originalPreviewImage = null;
                lblPreview.setIcon(null);
                lblPreview.setText(s.getSlideName());
                return;
            }
        }

        originalPreviewImage = null;
        lblPreview.setIcon(null);
        lblPreview.setText(fallbackName);
    }

    private void stopRealtime() {

        if (playbackTimer != null) {
            playbackTimer.stop();
        }

        btnRunSimulation.setEnabled(true);
        btnStopRealtime.setEnabled(false);

        lblStatus.setText("Playback stopped.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ColumbiaSignUI::new);
    }
}

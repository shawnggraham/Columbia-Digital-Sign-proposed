import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
=============================================================
Columbia Sign UI (Swing) — UI-only, JSON-save hooks
Fixes in this version:
1) Students tab:
   - Student Name textbox is wider AND definitely editable/focusable
2) Save Students JSON:
   - If no handler is wired, Results panel prints the variable list
     (same style as Slides)
=============================================================
*/

public class ColumbiaSignUI extends JFrame {

    /* ===============================
       Data Contracts (mutable; getters/setters)
       =============================== */
    public static class SlideDef {
        private int slideId;             // stable identity
        private int slideOrder;          // firing order 1..N
        private String slideName;
        private int durationSeconds;
        private String imagePath;        // nullable

        public SlideDef(int slideId, int slideOrder, String slideName, int durationSeconds, String imagePath) {
            this.slideId = slideId;
            this.slideOrder = slideOrder;
            this.slideName = slideName;
            this.durationSeconds = durationSeconds;
            this.imagePath = imagePath;
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

    public static class StudentDef {
        private int studentId;
        private String studentName;
        private int tripsPerWeek;

        public StudentDef(int studentId, String studentName, int tripsPerWeek) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.tripsPerWeek = tripsPerWeek;
        }

        public int getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
        public int getTripsPerWeek() { return tripsPerWeek; }

        public void setStudentName(String studentName) { this.studentName = studentName; }
        public void setTripsPerWeek(int tripsPerWeek) { this.tripsPerWeek = tripsPerWeek; }

        @Override
        public String toString() {
            return "ID: " + studentId + " | " + studentName + " — trips/week: " + tripsPerWeek;
        }
    }

    public static class SignConfig {
        public final int weeksToSimulate;
        public final int schoolDaysPerWeek;
        public final int dailyStartOffsetSec;
        public final double visibleMeanSec;
        public final double visibleStdDevSec;
        public final int defaultSlideSec;

        public SignConfig(int weeksToSimulate,
                          int schoolDaysPerWeek,
                          int dailyStartOffsetSec,
                          double visibleMeanSec,
                          double visibleStdDevSec,
                          int defaultSlideSec) {
            this.weeksToSimulate = weeksToSimulate;
            this.schoolDaysPerWeek = schoolDaysPerWeek;
            this.dailyStartOffsetSec = dailyStartOffsetSec;
            this.visibleMeanSec = visibleMeanSec;
            this.visibleStdDevSec = visibleStdDevSec;
            this.defaultSlideSec = defaultSlideSec;
        }
    }

    public enum SimulationMode { FAST, REAL_TIME }

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

    // Slides tab inputs
    private JButton btnBrowseSlideImage;
    private JTextField txtSlideImagePath;
    private JTextField txtSlideName;
    private JSpinner spnSlideDuration;
    private JButton btnAddSlide;

    // Slides table + buttons
    private JTable tblSlides;
    private SlideTableModel slideTableModel;
    private JButton btnMoveSlideUp;
    private JButton btnMoveSlideDown;
    private JButton btnDeleteSlide;
    private JButton btnSaveSlidesJson;

    // Students tab
    private JTextField txtStudentName;
    private JSpinner spnTripsPerWeek;
    private JButton btnAddOrUpdateStudent;
    private JButton btnClearStudentFields;
    private JButton btnDeleteStudent;
    private JButton btnSaveStudentsJson;
    private JList<StudentDef> studentList;
    private DefaultListModel<StudentDef> studentModel;

    /* ===============================
       UI: Config (Right-Top)
       =============================== */
    private JSpinner spnWeeks;
    private JSpinner spnSchoolDaysPerWeek;
    private JSpinner spnDailyOffset;
    private JSpinner spnVisibleMean;
    private JSpinner spnVisibleStd;
    private JSpinner spnDefaultSlideSec;
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
            for (int i = 0; i < slides.size(); i++) {
                slides.get(i).setSlideOrder(i + 1);
            }
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
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
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

        // Row 0: image browse
        btnBrowseSlideImage = new JButton("Browse Image...");
        txtSlideImagePath = new JTextField();
        txtSlideImagePath.setEditable(false);

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        panel.add(btnBrowseSlideImage, gc);

        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1; gc.gridwidth = 3; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtSlideImagePath, gc);
        gc.gridwidth = 1;

        // Row 1: name
        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Slide Name:"), gc);

        txtSlideName = new JTextField();
        gc.gridx = 1; gc.gridy = 1; gc.weightx = 1; gc.gridwidth = 3; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtSlideName, gc);
        gc.gridwidth = 1;

        // Row 2: duration + add
        gc.gridx = 0; gc.gridy = 2; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Duration (sec):"), gc);

        spnSlideDuration = new JSpinner(new SpinnerNumberModel(20, 1, 3600, 1));
        gc.gridx = 1; gc.gridy = 2; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(spnSlideDuration, gc);

        btnAddSlide = new JButton("Add Slide");
        gc.gridx = 2; gc.gridy = 2; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        panel.add(btnAddSlide, gc);

        // Row 3: table
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

        // Row 4: buttons
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

        // Label
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Student Name:"), gc);

        // Textbox (WIDER + explicitly editable/focusable)
        txtStudentName = new JTextField();
        txtStudentName.setColumns(28);
        txtStudentName.setEditable(true);
        txtStudentName.setEnabled(true);
        txtStudentName.setFocusable(true);
        txtStudentName.setMinimumSize(new Dimension(320, 28));
        txtStudentName.setPreferredSize(new Dimension(420, 28));

        gc.gridx = 1; gc.gridy = row; gc.weightx = 1; gc.gridwidth = 3; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtStudentName, gc);
        gc.gridwidth = 1;
        row++;

        // Trips/week
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Trips Per Week:"), gc);

        spnTripsPerWeek = new JSpinner(new SpinnerNumberModel(4, 1, 20, 1));
        gc.gridx = 1; gc.gridy = row; gc.weightx = 0.5; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(spnTripsPerWeek, gc);

        JPanel rowBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnAddOrUpdateStudent = new JButton("Add Student");
        btnClearStudentFields = new JButton("Clear");
        rowBtns.add(btnAddOrUpdateStudent);
        rowBtns.add(btnClearStudentFields);

        gc.gridx = 2; gc.gridy = row; gc.gridwidth = 2; gc.weightx = 0.5; gc.fill = GridBagConstraints.NONE;
        panel.add(rowBtns, gc);
        gc.gridwidth = 1;
        row++;

        // List
        studentModel = new DefaultListModel<>();
        studentList = new JList<>(studentModel);
        studentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane stScroll = new JScrollPane(studentList);
        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4;
        gc.weightx = 1; gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        panel.add(stScroll, gc);
        gc.gridwidth = 1;
        row++;

        // Delete + Save
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnDeleteStudent = new JButton("Delete Selected");
        btnSaveStudentsJson = new JButton("Save Students JSON");
        bottom.add(btnDeleteStudent);
        bottom.add(btnSaveStudentsJson);

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 4;
        gc.weightx = 1; gc.weighty = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(bottom, gc);

        return panel;
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
        panel.add(new JLabel("Daily Start Offset (sec):"), gc);

        spnDailyOffset = new JSpinner(new SpinnerNumberModel(-60, -3600, 3600, 5));
        gc.gridx = 1; gc.gridy = row; gc.weightx = 1;
        panel.add(spnDailyOffset, gc);

        gc.gridx = 2; gc.gridy = row; gc.weightx = 0;
        panel.add(new JLabel("Default Slide (sec):"), gc);

        spnDefaultSlideSec = new JSpinner(new SpinnerNumberModel(20, 1, 3600, 1));
        gc.gridx = 3; gc.gridy = row; gc.weightx = 1;
        panel.add(spnDefaultSlideSec, gc);

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

        // Browse slide image
        btnBrowseSlideImage.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select a slide image (optional)");
            chooser.setFileFilter(new FileNameExtensionFilter("Images (*.jpg, *.jpeg, *.png)", "jpg", "jpeg", "png"));

            int result = chooser.showOpenDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) return;

            File f = chooser.getSelectedFile();
            if (f == null || !f.exists()) return;

            txtSlideImagePath.setText(f.getAbsolutePath());
            showImagePreview(f);
        });

        // Add slide
        btnAddSlide.addActionListener(e -> {
            String name = (txtSlideName.getText() == null) ? "" : txtSlideName.getText().trim();
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

        // Move slides
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

        // Delete slide
        btnDeleteSlide.addActionListener(e -> {
            int row = tblSlides.getSelectedRow();
            if (row < 0) return;
            slideTableModel.removeAt(row);
            lblStatus.setText("Deleted slide.");
        });

        // Slide selection -> preview
        tblSlides.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = tblSlides.getSelectedRow();
            SlideDef s = slideTableModel.getAt(row);
            if (s == null) return;

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

        // Rotate preview
        btnRotatePreview.addActionListener(e -> {
            if (originalPreviewImage == null) return;
            previewRotationDegrees = (previewRotationDegrees + 90) % 360;
            renderPreviewScaledAndRotated();
        });

        lblPreview.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                if (originalPreviewImage != null) renderPreviewScaledAndRotated();
            }
        });

        /* ===============================
           Students
           =============================== */

        studentList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            StudentDef s = studentList.getSelectedValue();
            if (s == null) return;

            txtStudentName.setText(s.getStudentName());
            spnTripsPerWeek.setValue(s.getTripsPerWeek());
            btnAddOrUpdateStudent.setText("Update Student");
        });

        btnClearStudentFields.addActionListener(e -> clearStudentFields());

        btnAddOrUpdateStudent.addActionListener(e -> {
            String name = (txtStudentName.getText() == null) ? "" : txtStudentName.getText().trim();
            int trips = (Integer) spnTripsPerWeek.getValue();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a student name.", "Missing Student Name", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int selectedIdx = studentList.getSelectedIndex();
            if (selectedIdx >= 0 && "Update Student".equals(btnAddOrUpdateStudent.getText())) {
                StudentDef old = studentModel.get(selectedIdx);
                old.setStudentName(name);
                old.setTripsPerWeek(trips);
                studentList.repaint();
                lblStatus.setText("Updated student.");

                // SAMPLE CALL (update case)
                StudentSample.handleStudent(old.getStudentId(), old.getStudentName(), old.getTripsPerWeek());

            } else {
                StudentDef def = new StudentDef(nextStudentId++, name, trips);
                studentModel.addElement(def);
                lblStatus.setText("Added student.");

                // SAMPLE CALL (add case)
                StudentSample.handleStudent(def.getStudentId(), def.getStudentName(), def.getTripsPerWeek());
            }

            clearStudentFields();
        });

        btnDeleteStudent.addActionListener(e -> {
            int idx = studentList.getSelectedIndex();
            if (idx < 0) return;
            studentModel.remove(idx);
            lblStatus.setText("Deleted student.");
            clearStudentFields();
        });

        /* ===============================
           Save buttons (callbacks only)
           =============================== */

        btnSaveConfigJson.addActionListener(e -> {
            SignConfig cfg = readConfigFromUI();
            if (configSaveHandler != null) {
                configSaveHandler.onSaveConfig(cfg);
                lblStatus.setText("Config handed to JSON handler.");
            } else {
                lblStatus.setText("No config save handler set.");
                txtResults.setText(
                        "CONFIG JSON SAVE (UI-only)\n" +
                                "Variables needed to be stored in Config JSON file are:\n" +
                                "  - weeksToSimulate (int)\n" +
                                "  - schoolDaysPerWeek (int)\n" +
                                "  - dailyStartOffsetSec (int)\n" +
                                "  - visibleMeanSec (double)\n" +
                                "  - visibleStdDevSec (double)\n" +
                                "  - defaultSlideSec (int)\n"
                );
            }
        });

        btnSaveStudentsJson.addActionListener(e -> {
            List<StudentDef> students = snapshotStudentsFromUI();
            if (students.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No students to save.", "Students", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (studentsSaveHandler != null) {
                studentsSaveHandler.onSaveStudents(students);
                lblStatus.setText("Students handed to JSON handler.");
            } else {
                lblStatus.setText("No students save handler set.");
                txtResults.setText(
                        "STUDENTS JSON SAVE (UI-only)\n" +
                                "Variables needed to be stored in Students JSON file are:\n" +
                                "  - studentId (int)\n" +
                                "  - studentName (String)\n" +
                                "  - tripsPerWeek (int)\n\n" +
                                "Notes:\n" +
                                "  - studentId is stable identity and should not change once assigned.\n"
                );
            }
        });

        btnSaveSlidesJson.addActionListener(e -> {
            List<SlideDef> slides = snapshotSlidesFromUI();
            if (slides.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No slides to save.", "Slides", JOptionPane.WARNING_MESSAGE);
                return;
            }

            slideTableModel.renumberOrders();
            slideTableModel.fireTableDataChanged();

            if (slidesSaveHandler != null) {
                slidesSaveHandler.onSaveSlides(slides);
                lblStatus.setText("Slides handed to JSON handler.");
            } else {
                lblStatus.setText("No slides save handler set.");
                txtResults.setText(
                        "SLIDES JSON SAVE (UI-only)\n" +
                                "Variables needed to be stored in Slides JSON file are:\n" +
                                "  - slideId (int)\n" +
                                "  - slideOrder (int)\n" +
                                "  - slideName (String)\n" +
                                "  - durationSeconds (int)\n" +
                                "  - imagePath (String | null)\n\n" +
                                "Notes:\n" +
                                "  - slideOrder is the firing order (1..N) and changes when moved.\n" +
                                "  - slideId is stable identity and does NOT change.\n"
                );
            }
        });

        /* ===============================
           Simulation controls (UI-only)
           =============================== */
        btnRunSimulation.addActionListener(e -> {
            lblStatus.setText("Run clicked (simulation will be wired later).");
        });

        btnStopRealtime.addActionListener(e -> stopRealtime());

        ItemListener speedEnable = e -> cboPlaybackSpeed.setEnabled(rbRealtime.isSelected());
        rbFast.addItemListener(speedEnable);
        rbRealtime.addItemListener(speedEnable);
        cboPlaybackSpeed.setEnabled(false);

        // Make sure student name box can grab focus immediately
        SwingUtilities.invokeLater(() -> txtStudentName.requestFocusInWindow());
    }

    /* ===============================
       Snapshot + config reads
       =============================== */
    private SignConfig readConfigFromUI() {
        int weeks = (Integer) spnWeeks.getValue();
        int days = (Integer) spnSchoolDaysPerWeek.getValue();
        int offset = (Integer) spnDailyOffset.getValue();
        double mean = (Double) spnVisibleMean.getValue();
        double std = (Double) spnVisibleStd.getValue();
        int slideSec = (Integer) spnDefaultSlideSec.getValue();
        return new SignConfig(weeks, days, offset, mean, std, slideSec);
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
        spnTripsPerWeek.setValue(4);
        studentList.clearSelection();
        btnAddOrUpdateStudent.setText("Add Student");
        txtStudentName.requestFocusInWindow();
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
        previewRotationDegrees = 0;
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
       Real-time playback (not implemented in UI-only pass)
       =============================== */
    private void stopRealtime() {
        // placeholder for later
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ColumbiaSignUI::new);
    }
}

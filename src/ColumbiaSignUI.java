import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;


/* so here is how github works  */


public class ColumbiaSignUI extends JFrame {

    /* ===============================
       Slide Controls
       =============================== */
    private JButton btnBrowseSlide;
    private JTextField txtSlidePath;
    private JSpinner spnSlideDuration;
    private JButton btnAddSlide;
    private JButton btnRotate;          // <-- single rotate button
    private JButton btnDeleteSlide;
    private JList<String> slideList;

    /* ===============================
       Global Settings (not wired yet)
       =============================== */
    private JSpinner spnStudentsPerDay;
    private JButton btnSaveStudents;

    private JSpinner spnApproachTime;
    private JButton btnSaveApproachTime;

    /* ===============================
       Preview
       =============================== */
    private JLabel lblPreview;

    /* ===============================
       Simulation
       =============================== */
    private JButton btnRunSimulation;

    /* ===============================
       Single-slide storage
       =============================== */
    private File selectedSlideFile = null;
    private Integer selectedSlideDurationSec = null;

    /* ===============================
       Preview rotation state
       =============================== */
    private int previewRotationDegrees = 0; // 0, 90, 180, 270
    private Image originalPreviewImage = null;

    public ColumbiaSignUI() {
        setTitle("columbia-dash-sign");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(root);

        JPanel slidesPanel = buildSlidesPanel();
        JPanel rightPanel = buildRightPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, slidesPanel, rightPanel);
        split.setResizeWeight(0.33);
        split.setDividerLocation(360);
        root.add(split, BorderLayout.CENTER);

        root.add(buildSimulationPanel(), BorderLayout.SOUTH);

        wireSlideEvents();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    /* ===============================
       Slides Panel (Left)
       =============================== */
    private JPanel buildSlidesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Slides"));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        btnBrowseSlide = new JButton("Browse...");
        txtSlidePath = new JTextField();
        txtSlidePath.setEditable(false);

        // Row 0: Browse + Path
        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        panel.add(btnBrowseSlide, gc);

        gc.gridx = 1; gc.gridy = 0; gc.gridwidth = 3; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(txtSlidePath, gc);
        gc.gridwidth = 1;

        // Row 1: Duration + Add + Rotate
        JLabel lblDuration = new JLabel("Duration (sec):");
        spnSlideDuration = new JSpinner(new SpinnerNumberModel(5, 1, 3600, 1));
        btnAddSlide = new JButton("Add Slide");
        btnRotate = new JButton("Rotate 90°");

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        panel.add(lblDuration, gc);

        gc.gridx = 1; gc.gridy = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(spnSlideDuration, gc);

        gc.gridx = 2; gc.gridy = 1; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        panel.add(btnAddSlide, gc);

        gc.gridx = 3; gc.gridy = 1;
        panel.add(btnRotate, gc);

        // Row 2: Slide list (grows)
        slideList = new JList<>(new DefaultListModel<>());
        JScrollPane listScroll = new JScrollPane(slideList);

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 4;
        gc.weightx = 1; gc.weighty = 1;
        gc.fill = GridBagConstraints.BOTH;
        panel.add(listScroll, gc);

        // Row 3: Delete
        btnDeleteSlide = new JButton("Delete Selected");
        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 4;
        gc.weightx = 1; gc.weighty = 0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(btnDeleteSlide, gc);

        return panel;
    }

    /* ===============================
       Right Panel (Inputs + Preview)
       =============================== */
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(buildDailyInputsPanel(), BorderLayout.NORTH);
        panel.add(buildPreviewPanel(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildDailyInputsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Daily Inputs"));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblStudents = new JLabel("Average Students Per Day:");
        spnStudentsPerDay = new JSpinner(new SpinnerNumberModel(0, 0, 200000, 10));
        btnSaveStudents = new JButton("Save");

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        panel.add(lblStudents, gc);

        gc.gridx = 1; gc.gridy = 0; gc.weightx = 1;
        panel.add(spnStudentsPerDay, gc);

        gc.gridx = 2; gc.gridy = 0; gc.weightx = 0;
        panel.add(btnSaveStudents, gc);

        JLabel lblApproach = new JLabel("Approach Time (seconds):");
        spnApproachTime = new JSpinner(new SpinnerNumberModel(0, 0, 3600, 1));
        btnSaveApproachTime = new JButton("Save");

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        panel.add(lblApproach, gc);

        gc.gridx = 1; gc.gridy = 1; gc.weightx = 1;
        panel.add(spnApproachTime, gc);

        gc.gridx = 2; gc.gridy = 1; gc.weightx = 0;
        panel.add(btnSaveApproachTime, gc);

        return panel;
    }

    private JPanel buildPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Slide Preview"));

        lblPreview = new JLabel("No slide selected", SwingConstants.CENTER);
        lblPreview.setOpaque(true);
        lblPreview.setBackground(Color.WHITE);
        lblPreview.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(lblPreview, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSimulationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Simulation"));

        btnRunSimulation = new JButton("Run Simulation");
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.add(btnRunSimulation);

        panel.add(right, BorderLayout.CENTER);
        return panel;
    }

    /* ===============================
       Enable Slides (Single JPEG) + Rotate
       =============================== */
    private void wireSlideEvents() {

        btnBrowseSlide.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select a JPEG slide");
            chooser.setFileFilter(new FileNameExtensionFilter("JPEG Images (*.jpg, *.jpeg)", "jpg", "jpeg"));

            int result = chooser.showOpenDialog(this);
            if (result != JFileChooser.APPROVE_OPTION) return;

            File f = chooser.getSelectedFile();
            if (f == null || !f.exists()) return;

            selectedSlideFile = f;
            txtSlidePath.setText(f.getAbsolutePath());

            // load + reset rotation
            showImagePreview(f);
        });

        btnAddSlide.addActionListener(e -> {
            if (selectedSlideFile == null) {
                JOptionPane.showMessageDialog(this, "Browse and select a JPEG first.", "Missing Slide", JOptionPane.WARNING_MESSAGE);
                return;
            }

            DefaultListModel<String> model = (DefaultListModel<String>) slideList.getModel();
            if (model.getSize() >= 1) {
                JOptionPane.showMessageDialog(this, "Single-slide mode for now. Delete the existing slide to add another.", "Limit", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            selectedSlideDurationSec = (Integer) spnSlideDuration.getValue();
            String label = selectedSlideFile.getName() + " (" + selectedSlideDurationSec + "s)";
            model.addElement(label);
            slideList.setSelectedIndex(0);
        });

        btnRotate.addActionListener(e -> {
            if (originalPreviewImage == null) return;
            previewRotationDegrees = (previewRotationDegrees + 90) % 360;
            renderPreviewScaledAndRotated();
        });

        btnDeleteSlide.addActionListener(e -> {
            DefaultListModel<String> model = (DefaultListModel<String>) slideList.getModel();
            int idx = slideList.getSelectedIndex();
            if (idx < 0) return;

            model.remove(idx);

            // Clear single-slide storage
            selectedSlideFile = null;
            selectedSlideDurationSec = null;
            txtSlidePath.setText("");

            // Clear preview + rotation state
            originalPreviewImage = null;
            previewRotationDegrees = 0;
            lblPreview.setIcon(null);
            lblPreview.setText("No slide selected");
        });
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ColumbiaSignUI::new);
    }
}

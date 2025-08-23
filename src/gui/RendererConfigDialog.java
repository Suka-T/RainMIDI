package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractCellEditor;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import jlib.core.IDataManager;
import jlib.core.ISoundManager;
import jlib.core.ISystemManager;
import jlib.core.JMPCoreAccessor;
import layout.LayoutConfig;
import layout.LayoutManager;
import plg.AbstractRenderPlugin;
import plg.PropertiesNode;
import plg.PropertiesNode.PropertiesNodeType;
import plg.SystemProperties;
import plg.Utility;

public class RendererConfigDialog extends JDialog implements ActionListener {

    private static final String WORKNUM_LOW = "3";
    private static final String NOTESIMAGENUM_LOW = "10";

    private static final String WORKNUM_MID = "3";
    private static final String NOTESIMAGENUM_MID = "60";

    private static final String WORKNUM_HIG = "20";
    private static final String NOTESIMAGENUM_HIG = "100";

    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    private JTable rendererTable;
    private JTable designTable;

    private DefaultTableModel rendererModel;
    private DefaultTableModel designModel;
    private JTabbedPane tabbedPane;

    private AbstractRenderPlugin targetPlg;

    List<String> synthItemKeys = new ArrayList<String>();
    List<String> synthItemLabels = new ArrayList<String>();

    List<String> systemItemKeys = new ArrayList<String>();
    List<String> designItemKeys = new ArrayList<String>();

    // ユーザー非公開キー
    private List<String> ignoreKeysSystem = Arrays.asList();
    private List<String> ignoreKeysDesign = Arrays.asList(LayoutConfig.LC_CURSOR_POS, LayoutConfig.LC_NOTES_COLOR_ASIGN, LayoutConfig.LC_PB_COLOR,
            LayoutConfig.LC_PB_VISIBLE);

    private Map<Integer, JComboBox<String>> comboBoxMapSys = new HashMap<>();
    private Map<Integer, JComboBox<String>> comboBoxMapLc = new HashMap<>();
    private JComboBox<String> comboBoxSynth;
    private JLabel lblSelectedLayoutLabel;
    private JComboBox<String> comboBoxWindowSize;
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private JCheckBox chckbxNotesSpeedAuto;
    private JSlider sliderNotesSpeed;

    private AtomicBoolean initialized = new AtomicBoolean(false);
    private final ButtonGroup buttonGroup_1 = new ButtonGroup();
    private JRadioButton rdbtnRenderOrderAsc;
    private JRadioButton rdbtnRenderOrderDesc;

    private boolean isCommitClose = false;
    private final ButtonGroup buttonGroup_2 = new ButtonGroup();
    private JRadioButton rdbtnModeRainFall;
    private JRadioButton rdbtnModeSideFlow;
    private JRadioButton rdbtnMonitorNone;
    private JRadioButton rdbtnMonitorType1;
    private JRadioButton rdbtnMonitorType2;
    private final ButtonGroup buttonGroup_3 = new ButtonGroup();
    private JRadioButton rdbtnPerfLowButton;
    private JRadioButton rdbtnPerfMidButton;
    private JRadioButton rdbtnPerfHighButton;

    // 行によってエディタを切り替えるクラス
    class RowSpecificComboBoxEditor extends AbstractCellEditor implements TableCellEditor {
        private final JTextField textField;
        private Component currentEditor;
        private boolean isCombobox = false;
        private int lastSelectedRow = -1;
        private Map<Integer, JComboBox<String>> comboBoxMap;
        private DefaultTableModel model;

        public RowSpecificComboBoxEditor(Map<Integer, JComboBox<String>> comboBoxMap, DefaultTableModel model) {
            this.textField = new JTextField();
            this.comboBoxMap = comboBoxMap;
            this.model = model;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object getCellEditorValue() {
            Object val = null;
            if (isCombobox) {
                val = ((JComboBox<String>) currentEditor).getSelectedItem();
            }
            else {
                val = textField.getText();
            }
            this.model.setValueAt(val, lastSelectedRow, 1);
            // System.out.println(val);
            return val;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (this.comboBoxMap.containsKey(row)) {
                this.comboBoxMap.get(row).setSelectedItem(value);
                currentEditor = this.comboBoxMap.get(row);
                isCombobox = true;
            }
            else {
                textField.setText(value != null ? value.toString() : "");
                currentEditor = textField;
                isCombobox = false;
            }
            lastSelectedRow = row;
            return currentEditor;
        }
    }

    // 行によってエディタを切り替えるクラス
    class SystRowSpecificComboBoxEditor extends RowSpecificComboBoxEditor {
        public SystRowSpecificComboBoxEditor() {
            super(comboBoxMapSys, rendererModel);
        }
    }

    // 行によってエディタを切り替えるクラス
    class LcRowSpecificComboBoxEditor extends RowSpecificComboBoxEditor {
        public LcRowSpecificComboBoxEditor() {
            super(comboBoxMapLc, designModel);
        }
    }

    /**
     * Create the dialog.
     */
    public RendererConfigDialog(AbstractRenderPlugin plg) {
        initialized.set(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                if (JMPCoreAccessor.getSystemManager().isEnableStandAlonePlugin() == true) {
                    AbstractRenderPlugin.PluginInstance.exitStdPlg();
                }
            }
        });
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setTitle("Rain MIDI Launcher");
        this.targetPlg = plg;
        setModal(true);
        setBounds(100, 100, 600, 530);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(0, 0));
        {
            tabbedPane = new JTabbedPane(JTabbedPane.TOP);
            contentPanel.add(tabbedPane);
            {
                JPanel summaryPanel = new JPanel();
                summaryPanel.setBorder(null);
                tabbedPane.addTab("Summary", null, summaryPanel, null);
                summaryPanel.setLayout(new BorderLayout(0, 0));

                JScrollPane scrollPane = new JScrollPane();
                summaryPanel.add(scrollPane, BorderLayout.CENTER);

                JPanel panel = new JPanel();
                panel.setBorder(null);
                scrollPane.setViewportView(panel);
                panel.setLayout(null);

                JPanel audioSummaryPanel = new JPanel();
                audioSummaryPanel.setLayout(null);
                audioSummaryPanel.setBorder(new TitledBorder(null, "Audio", TitledBorder.LEADING, TitledBorder.TOP, null, null));
                audioSummaryPanel.setBounds(12, 10, 543, 71);
                panel.add(audioSummaryPanel);

                comboBoxSynth = new JComboBox<String>();
                comboBoxSynth.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (!initialized.get())
                            return; // 初期化中は無視

                        String synthKey = synthItemKeys.get(comboBoxSynth.getSelectedIndex());
                        setSystemTableParam(SystemProperties.SYSP_AUDIO_SYNTH, (String) synthKey);
                    }
                });
                comboBoxSynth.setBounds(96, 17, 295, 21);
                audioSummaryPanel.add(comboBoxSynth);

                JLabel lblSynthLabel = new JLabel("Synthsizer");
                lblSynthLabel.setBounds(12, 21, 72, 13);
                audioSummaryPanel.add(lblSynthLabel);

                JPanel layoutSummaryPanel = new JPanel();
                layoutSummaryPanel.setLayout(null);
                layoutSummaryPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
                        "Design", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
                layoutSummaryPanel.setBounds(12, 91, 543, 98);
                panel.add(layoutSummaryPanel);

                lblSelectedLayoutLabel = new JLabel("Default Design");
                lblSelectedLayoutLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
                lblSelectedLayoutLabel.setBounds(24, 20, 507, 31);
                layoutSummaryPanel.add(lblSelectedLayoutLabel);

                JButton btnLoadLayoutButton = new JButton("Load Design");
                btnLoadLayoutButton.setActionCommand("LOAD_LAYOUT");
                btnLoadLayoutButton.addActionListener(this);
                btnLoadLayoutButton.setBounds(277, 56, 121, 26);
                layoutSummaryPanel.add(btnLoadLayoutButton);

                JButton btnDefaultButton = new JButton("Default");
                btnDefaultButton.setActionCommand("DEF_LAYOUT");
                btnDefaultButton.addActionListener(this);
                btnDefaultButton.setBounds(410, 56, 121, 26);
                layoutSummaryPanel.add(btnDefaultButton);

                JPanel systemSummaryPanel = new JPanel();
                systemSummaryPanel.setLayout(null);
                systemSummaryPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
                        "System", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
                systemSummaryPanel.setBounds(12, 199, 543, 208);
                panel.add(systemSummaryPanel);

                JLabel lblWindowSizeLabel = new JLabel("Window Size");
                lblWindowSizeLabel.setBounds(12, 23, 72, 13);
                systemSummaryPanel.add(lblWindowSizeLabel);

                comboBoxWindowSize = new JComboBox<String>();
                comboBoxWindowSize.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_WINSIZE, (String) comboBoxWindowSize.getSelectedItem());
                    }
                });
                comboBoxWindowSize.setBounds(96, 19, 113, 21);
                systemSummaryPanel.add(comboBoxWindowSize);

                JLabel lblPerfRadioLabel = new JLabel("Use RAM");
                lblPerfRadioLabel.setBounds(12, 73, 72, 13);
                systemSummaryPanel.add(lblPerfRadioLabel);

                rdbtnPerfLowButton = new JRadioButton("Low");
                rdbtnPerfLowButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_WORKNUM, WORKNUM_LOW);
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_NOTESIMAGENUM, NOTESIMAGENUM_LOW);
                    }
                });
                buttonGroup.add(rdbtnPerfLowButton);
                rdbtnPerfLowButton.setBounds(96, 69, 113, 21);
                systemSummaryPanel.add(rdbtnPerfLowButton);

                rdbtnPerfMidButton = new JRadioButton("Middle");
                rdbtnPerfMidButton.setSelected(true);
                rdbtnPerfMidButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_WORKNUM, WORKNUM_MID);
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_NOTESIMAGENUM, NOTESIMAGENUM_MID);
                    }
                });
                buttonGroup.add(rdbtnPerfMidButton);
                rdbtnPerfMidButton.setBounds(213, 69, 113, 21);
                systemSummaryPanel.add(rdbtnPerfMidButton);

                rdbtnPerfHighButton = new JRadioButton("High");
                rdbtnPerfHighButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_WORKNUM, WORKNUM_HIG);
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_NOTESIMAGENUM, NOTESIMAGENUM_HIG);
                    }
                });
                buttonGroup.add(rdbtnPerfHighButton);
                rdbtnPerfHighButton.setBounds(330, 69, 113, 21);
                systemSummaryPanel.add(rdbtnPerfHighButton);

                JLabel lblNotesSpeedLabel = new JLabel("Notes Speed");
                lblNotesSpeedLabel.setBounds(12, 96, 72, 13);
                systemSummaryPanel.add(lblNotesSpeedLabel);

                chckbxNotesSpeedAuto = new JCheckBox("Auto");
                chckbxNotesSpeedAuto.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        updateNotesSpeed(chckbxNotesSpeedAuto.isSelected(), sliderNotesSpeed.getValue());
                    }
                });
                chckbxNotesSpeedAuto.setSelected(true);
                chckbxNotesSpeedAuto.setBounds(96, 92, 81, 21);
                systemSummaryPanel.add(chckbxNotesSpeedAuto);

                sliderNotesSpeed = new JSlider();
                sliderNotesSpeed.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        if (!initialized.get())
                            return; // 初期化中は無視

                        if (!sliderNotesSpeed.getValueIsAdjusting()) {
                            chckbxNotesSpeedAuto.setSelected(false);
                            updateNotesSpeed(false, sliderNotesSpeed.getValue());
                        }
                    }
                });
                sliderNotesSpeed.setMinimum(1);
                sliderNotesSpeed.setBounds(96, 119, 435, 21);
                systemSummaryPanel.add(sliderNotesSpeed);

                JLabel lblNotesOrderLabel = new JLabel("Notes Layer");
                lblNotesOrderLabel.setBounds(12, 151, 72, 13);
                systemSummaryPanel.add(lblNotesOrderLabel);

                rdbtnRenderOrderAsc = new JRadioButton("Track1 is Back");
                rdbtnRenderOrderAsc.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_LAYERORDER, "asc");
                    }
                });
                buttonGroup_1.add(rdbtnRenderOrderAsc);
                rdbtnRenderOrderAsc.setBounds(96, 147, 113, 21);
                systemSummaryPanel.add(rdbtnRenderOrderAsc);

                rdbtnRenderOrderDesc = new JRadioButton("Track1 is Top");
                rdbtnRenderOrderDesc.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_LAYERORDER, "desc");
                    }
                });
                buttonGroup_1.add(rdbtnRenderOrderDesc);
                rdbtnRenderOrderDesc.setBounds(213, 147, 113, 21);
                systemSummaryPanel.add(rdbtnRenderOrderDesc);

                JLabel lblViewModeLabel = new JLabel("View Mode");
                lblViewModeLabel.setBounds(12, 50, 72, 13);
                systemSummaryPanel.add(lblViewModeLabel);

                rdbtnModeRainFall = new JRadioButton("Rain Fall");
                rdbtnModeRainFall.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_MODE, "rain_fall");
                    }
                });
                buttonGroup_2.add(rdbtnModeRainFall);
                rdbtnModeRainFall.setBounds(96, 46, 113, 21);
                systemSummaryPanel.add(rdbtnModeRainFall);

                rdbtnModeSideFlow = new JRadioButton("Side Flow");
                rdbtnModeSideFlow.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_MODE, "side_flow");
                    }
                });
                buttonGroup_2.add(rdbtnModeSideFlow);
                rdbtnModeSideFlow.setBounds(213, 46, 113, 21);
                systemSummaryPanel.add(rdbtnModeSideFlow);

                JLabel lblMonitorTypeLabel = new JLabel("Monitor Type");
                lblMonitorTypeLabel.setBounds(12, 174, 72, 13);
                systemSummaryPanel.add(lblMonitorTypeLabel);

                rdbtnMonitorNone = new JRadioButton("None");
                buttonGroup_3.add(rdbtnMonitorNone);
                rdbtnMonitorNone.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_MONITOR_TYPE, "none");
                    }
                });
                rdbtnMonitorNone.setBounds(96, 170, 113, 21);
                systemSummaryPanel.add(rdbtnMonitorNone);

                rdbtnMonitorType1 = new JRadioButton("Type1");
                buttonGroup_3.add(rdbtnMonitorType1);
                rdbtnMonitorType1.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_MONITOR_TYPE, "type1");
                    }
                });
                rdbtnMonitorType1.setBounds(213, 170, 113, 21);
                systemSummaryPanel.add(rdbtnMonitorType1);

                rdbtnMonitorType2 = new JRadioButton("Type2");
                buttonGroup_3.add(rdbtnMonitorType2);
                rdbtnMonitorType2.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_MONITOR_TYPE, "type2");
                    }
                });
                rdbtnMonitorType2.setBounds(330, 170, 113, 21);
                systemSummaryPanel.add(rdbtnMonitorType2);
            }
            {
                JPanel rendererPanel = new JPanel();
                tabbedPane.addTab("Detail 1", null, rendererPanel, null);
                rendererPanel.setLayout(new BorderLayout(0, 0));
                {
                    // テーブルのデータとカラム名
                    String[] columnNames = { "Config", "Value" };
                    Object[][] data = { { 1, "A" }, { 2, "B" }, { 3, "C" } };

                    // モデル作成
                    rendererModel = new DefaultTableModel(data, columnNames);

                    rendererTable = new JTable(rendererModel);
                    JScrollPane scrollPane = new JScrollPane(rendererTable);
                    rendererPanel.add(scrollPane, BorderLayout.CENTER);
                }
            }
            {
                JPanel LayoutPanel = new JPanel();
                tabbedPane.addTab("Detail 2", null, LayoutPanel, null);
                LayoutPanel.setLayout(new BorderLayout(0, 0));
                {
                    // テーブルのデータとカラム名
                    String[] columnNames = { "Config", "Value" };
                    Object[][] data = { { 1, "A" }, { 2, "B" }, { 3, "C" } };

                    // モデル作成
                    designModel = new DefaultTableModel(data, columnNames);

                    designTable = new JTable(designModel);
                    JScrollPane scrollPane = new JScrollPane(designTable);
                    LayoutPanel.add(scrollPane);
                }
            }

            JPanel aboutPanel = new JPanel();
            tabbedPane.addTab("About", null, aboutPanel, null);
            aboutPanel.setLayout(new BorderLayout(0, 0));

            JEditorPane editorPane = new JEditorPane();
            editorPane.setContentType("text/html");
            editorPane.setEditable(false);
            aboutPanel.add(editorPane, BorderLayout.CENTER);

            // HTMLコンテンツの作成
            String appName = plg.getAppName();
            String appVersion = plg.getAppVersion();
            String currentYear = plg.getAppYear();
            String company = plg.getAppCompany();

            // MITライセンスの全文
            String licenseText = "MIT License" + "<br><br>" + "Permission is hereby granted, free of charge, to any person obtaining a copy "
                    + "of this software and associated documentation files (the \"Software\"), to deal "
                    + "in the Software without restriction, including without limitation the rights "
                    + "to use, copy, modify, merge, publish, distribute, sublicense, and/or sell "
                    + "copies of the Software, and to permit persons to whom the Software is "
                    + "furnished to do so, subject to the following conditions:<br><br>"
                    + "The above copyright notice and this permission notice shall be included in all "
                    + "copies or substantial portions of the Software.<br><br>"
                    + "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR "
                    + "IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, "
                    + "FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE "
                    + "AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER "
                    + "LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, "
                    + "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE " + "SOFTWARE.";

            String htmlContent = "<html><body style='font-family: Arial, sans-serif; text-align: center; margin: 10px;'>" + "<h1>" + appName + "</h1>"
                    + "<p>Version " + appVersion + "</p>" + "<hr style='width: 50%; margin: 5px auto;'>" + "<p>&copy; " + currentYear + " " + company
                    + ". All Rights Reserved.</p>" + "<p style='font-size: 0.8em; color: gray; text-align: left;'>" + licenseText + "</p>" + "</body></html>";

            editorPane.setText(htmlContent);
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);

            JButton btnLoadToPlayButton = new JButton("Select a file and play");
            btnLoadToPlayButton.setActionCommand("LOAD_TO_PLAY");
            btnLoadToPlayButton.addActionListener(this);
            buttonPane.add(btnLoadToPlayButton);
            {
                JButton okButton = new JButton("Launch");
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
                okButton.addActionListener(this);
            }
        }

        initialized.set(true);
    }

    private void updateNotesSpeed(boolean isAuto, int value) {
        if (isAuto) {
            setSystemTableParam(SystemProperties.SYSP_RENDERER_NOTESSPEED, "auto");
        }
        else {
            setSystemTableParam(SystemProperties.SYSP_RENDERER_NOTESSPEED, String.valueOf(value));
        }

        sliderNotesSpeed.setEnabled(!isAuto);
    }

    private void resetNotesSpeed(boolean isAuto, int value) {
        chckbxNotesSpeedAuto.setSelected(isAuto);
        sliderNotesSpeed.setValue(value);
        sliderNotesSpeed.setEnabled(!isAuto);
    }

    public void setSystemTableParam(String key, String value) {
        if (rendererTable.isEditing()) {
            rendererTable.getCellEditor().stopCellEditing();
        }
        for (int i = 0; i < rendererModel.getRowCount(); i++) {
            String skey = (String) systemItemKeys.get(i);
            if (skey.equals(key)) {
                rendererModel.setValueAt(value, i, 1);
            }
        }
    }

    public void updateItem() {
        updateSystemItems();
        updateDesignItems();

        String layoutName = SystemProperties.getInstance().getPropNode(SystemProperties.SYSP_LAYOUT).getDataString();
        if (layoutName != null && layoutName.isEmpty() == false) {
            lblSelectedLayoutLabel.setText(layoutName);
        }
        else {
            lblSelectedLayoutLabel.setText("Default Design");
        }
    }

    public void updateSystemItems() {
        rendererModel.setRowCount(0);
        comboBoxMapSys.clear();
        systemItemKeys.clear();

        comboBoxWindowSize.removeAllItems();

        int i = 0;
        for (PropertiesNode node : SystemProperties.getInstance().getNodes()) {
            if (ignoreKeysSystem.contains(node.getKey())) {
                continue;
            }

            String keyName = node.getKey();
            if (keyName.equals(SystemProperties.SYSP_RENDERER_WINSIZE)) {
                for (String s : node.getItemArray()) {
                    comboBoxWindowSize.addItem(s);
                }
                comboBoxWindowSize.setSelectedItem(node.getDataString());
            }
            else if (keyName.equals(SystemProperties.SYSP_RENDERER_NOTESSPEED)) {
                String s = node.getDataString();
                if (s.equals("auto")) {
                    resetNotesSpeed(true, 50);
                }
                else {
                    int val = 50;
                    try {
                        val = Integer.parseInt(s);
                    }
                    catch (Exception e) {
                        val = 50;
                    }
                    resetNotesSpeed(false, val);
                }
            }
            else if (keyName.equals(SystemProperties.SYSP_RENDERER_WORKNUM)) {
                String workNum = SystemProperties.getInstance().getPropNode(SystemProperties.SYSP_RENDERER_WORKNUM).getDataString();
                String imgNum = SystemProperties.getInstance().getPropNode(SystemProperties.SYSP_RENDERER_NOTESIMAGENUM).getDataString();
                if (workNum.equals(WORKNUM_LOW) && imgNum.equals(NOTESIMAGENUM_LOW)) {
                    rdbtnPerfLowButton.setSelected(true);
                }
                else if (workNum.equals(WORKNUM_MID) && imgNum.equals(NOTESIMAGENUM_MID)) {
                    rdbtnPerfMidButton.setSelected(true);
                }
                else if (workNum.equals(WORKNUM_HIG) && imgNum.equals(NOTESIMAGENUM_HIG)) {
                    rdbtnPerfHighButton.setSelected(true);
                }
                else {
                    rdbtnPerfLowButton.setSelected(false);
                    rdbtnPerfMidButton.setSelected(false);
                    rdbtnPerfHighButton.setSelected(false);
                }
            }
            else if (keyName.equals(SystemProperties.SYSP_RENDERER_LAYERORDER)) {
                SystemProperties.SyspLayerOrder order = (SystemProperties.SyspLayerOrder) node.getData();
                if (order == SystemProperties.SyspLayerOrder.ASC) {
                    rdbtnRenderOrderAsc.setSelected(true);
                }
                else if (order == SystemProperties.SyspLayerOrder.DESC) {
                    rdbtnRenderOrderDesc.setSelected(true);
                }
            }
            else if (keyName.equals(SystemProperties.SYSP_RENDERER_MODE)) {
                SystemProperties.SyspViewMode mode = (SystemProperties.SyspViewMode) node.getData();
                if (mode == SystemProperties.SyspViewMode.RAIN_FALL) {
                    rdbtnModeRainFall.setSelected(true);
                }
                else if (mode == SystemProperties.SyspViewMode.SIDE_FLOW) {
                    rdbtnModeSideFlow.setSelected(true);
                }
            }
            else if (keyName.equals(SystemProperties.SYSP_RENDERER_MONITOR_TYPE)) {
                SystemProperties.SyspMonitorType type = (SystemProperties.SyspMonitorType) node.getData();
                if (type == SystemProperties.SyspMonitorType.NONE) {
                    rdbtnMonitorNone.setSelected(true);
                }
                else if (type == SystemProperties.SyspMonitorType.TYPE1) {
                    rdbtnMonitorType1.setSelected(true);
                }
                else if (type == SystemProperties.SyspMonitorType.TYPE2) {
                    rdbtnMonitorType2.setSelected(true);
                }
            }

            if (SystemProperties.SwapKeyName.containsKey(keyName)) {
                keyName = SystemProperties.SwapKeyName.get(keyName);
            }
            Object[] row = { keyName, node.getDataString() };
            rendererModel.addRow(row);
            systemItemKeys.add(node.getKey());

            if (node.getType() == PropertiesNodeType.ITEM) {
                if (node.getItems().isEmpty() == false) {
                    JComboBox<String> cb = new JComboBox<String>(node.getItemArray());
                    comboBoxMapSys.put(i, cb);
                }
            }
            else if (node.getType() == PropertiesNodeType.BOOLEAN) {
                String[] boolArray = new String[] { "false", "true" };
                JComboBox<String> cb = new JComboBox<String>(boolArray);
                comboBoxMapSys.put(i, cb);
            }
            i++;
        }
        rendererTable.setRowHeight(20);

        // カスタムエディタを2列目に設定
        TableColumn col = rendererTable.getColumnModel().getColumn(1);
        col.setCellEditor(new SystRowSpecificComboBoxEditor());
    }

    public void updateDesignItems() {
        int i = 0;
        designModel.setRowCount(0);
        comboBoxMapLc.clear();
        designItemKeys.clear();

        for (PropertiesNode node : LayoutManager.getInstance().getNodes()) {
            if (ignoreKeysDesign.contains(node.getKey())) {
                continue;
            }

            String keyName = node.getKey();
            if (LayoutConfig.SwapKeyName.containsKey(keyName)) {
                keyName = LayoutConfig.SwapKeyName.get(keyName);
            }
            Object[] row = { keyName, node.getDataString() };
            designModel.addRow(row);
            designItemKeys.add(node.getKey());

            if (node.getType() == PropertiesNodeType.ITEM) {
                if (node.getItems().isEmpty() == false) {
                    JComboBox<String> cb = new JComboBox<String>(node.getItemArray());
                    comboBoxMapLc.put(i, cb);
                }
            }
            else if (node.getType() == PropertiesNodeType.BOOLEAN) {
                String[] boolArray = new String[] { "false", "true" };
                JComboBox<String> cb = new JComboBox<String>(boolArray);
                comboBoxMapLc.put(i, cb);
            }
            i++;
        }
        designTable.setRowHeight(20);

        // カスタムエディタを2列目に設定
        TableColumn col = designTable.getColumnModel().getColumn(1);
        col.setCellEditor(new LcRowSpecificComboBoxEditor());
    }

    public void updateSynthItem() {
        comboBoxSynth.removeAllItems();
        synthItemKeys.clear();
        synthItemLabels.clear();
        String[] items = JMPCoreAccessor.getSoundManager().getMidiToolkit().getMidiRecieverItems();
        String selected = SystemProperties.getInstance().getPropNode(SystemProperties.SYSP_AUDIO_SYNTH).getDataString();

        int selectedIndex = 0;
        int index = 0;
        for (String s : items) {
            if (selected.equals(s)) {
                selectedIndex = index;
            }

            synthItemKeys.add(s);
            if (s.equals(ISoundManager.AUTO_RECEIVER_NAME)) {
                synthItemLabels.add("* Automatic selection");
            }
            else if (s.equals(ISoundManager.NULL_RECEIVER_NAME)) {
                synthItemLabels.add("* Not sound");
            }
            else if (s.equals(ISoundManager.RENDER_ONLY_RECEIVER_NAME)) {
                synthItemLabels.add("* Rendering Only");
            }
            else {
                synthItemLabels.add(s);
            }
            index++;
        }

        for (String s : synthItemLabels) {
            comboBoxSynth.addItem(s);
        }
        comboBoxSynth.setSelectedIndex(selectedIndex);
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            isCommitClose = false;
            updateItem();
            updateSynthItem();
        }
        super.setVisible(b);
    }

    private void commit() {
        if (rendererTable.isEditing()) {
            rendererTable.getCellEditor().stopCellEditing();
        }
        for (int i = 0; i < rendererModel.getRowCount(); i++) {
            String key = (String) systemItemKeys.get(i);
            String param = (String) rendererModel.getValueAt(i, 1);
            for (PropertiesNode node : SystemProperties.getInstance().getNodes()) {
                if (node.getKey().equalsIgnoreCase(key) == true) {
                    node.setObject(param);
                    break;
                }
            }
        }
        if (designTable.isEditing()) {
            designTable.getCellEditor().stopCellEditing();
        }
        for (int i = 0; i < designModel.getRowCount(); i++) {
            String key = (String) designItemKeys.get(i);
            String param = (String) designModel.getValueAt(i, 1);
            for (PropertiesNode node : LayoutManager.getInstance().getNodes()) {
                if (node.getKey().equalsIgnoreCase(key) == true) {
                    node.setObject(param);
                    break;
                }
            }
        }

        String synthKey = SystemProperties.getInstance().getData(SystemProperties.SYSP_AUDIO_SYNTH).toString();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            JMPCoreAccessor.getDataManager().setConfigParam(IDataManager.CFG_KEY_MIDIOUT, synthKey);
        }, 200, TimeUnit.MILLISECONDS);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        switch (cmd) {
            case "LOAD_LAYOUT": {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Select layout config.");
                chooser.addChoosableFileFilter(new FileNameExtensionFilter("Layout config (*.layout)", "layout"));

                // ファイル選択ダイアログを表示
                Path folder = Paths.get(JMPCoreAccessor.getSystemManager().getSystemPath(ISystemManager.PATH_RES_DIR, targetPlg));
                chooser.setCurrentDirectory(new File(folder.toString())); // 初期フォルダ
                int result = chooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = chooser.getSelectedFile();
                    try {
                        LayoutManager.getInstance().read(selectedFile);
                        String layoutName = Utility.getFileNameNotExtension(selectedFile);
                        if (layoutName != null && layoutName.isEmpty() == false) {
                            setSystemTableParam(SystemProperties.SYSP_LAYOUT, layoutName);
                            lblSelectedLayoutLabel.setText(layoutName);
                        }
                        updateDesignItems();
                    }
                    catch (IOException e1) {
                        LayoutManager.getInstance().initializeConfig();
                        setSystemTableParam(SystemProperties.SYSP_LAYOUT, "");
                        lblSelectedLayoutLabel.setText("Default Design");
                        updateDesignItems();
                    }
                }
                break;
            }
            case "LOAD_TO_PLAY": {
                JFileChooser chooser = new JFileChooser();

                // 複数選択を許可
                chooser.setMultiSelectionEnabled(true);

                // ダイアログを開く
                SystemProperties.getInstance().getPreloadFiles().clear();
                int result = chooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File[] files = chooser.getSelectedFiles(); // 複数ファイル
                    for (File f : files) {
                        SystemProperties.getInstance().getPreloadFiles().add(f);
                    }
                    setVisible(false);

                    commit();
                    isCommitClose = true;

                    // ファイルロードを予約する
                    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                    scheduler.schedule(() -> {
                        SystemProperties.getInstance().preloadAudioFiles();
                    }, 400, TimeUnit.MILLISECONDS);
                }
                break;
            }
            case "DEF_LAYOUT":
                LayoutManager.getInstance().initializeConfig();
                setSystemTableParam(SystemProperties.SYSP_LAYOUT, "");
                lblSelectedLayoutLabel.setText("Default Design");
                updateDesignItems();
                break;
            case "OK":
                commit();
                isCommitClose = true;
                setVisible(false);
                break;
            case "Cancel":
                isCommitClose = false;
                setVisible(false);
                break;
        }
    }

    public boolean isCommitClose() {
        return isCommitClose;
    }
}

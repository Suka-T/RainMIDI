package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

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
    private static final String NOTESIMAGENUM_LOW = "60";
    private static final String USAGE_MIDIRAM_LOW = "5";
    private static final String USAGE_MIDI_ANA_LOW = "1";
    private static final String USAGE_MIDI_EXT_LOW = "1";
    private static final String NOTESIMAGE_BITS_LOW = "rgb565";

    private static final String WORKNUM_MID = "5";
    private static final String NOTESIMAGENUM_MID = "60";
    private static final String USAGE_MIDIRAM_MID = "25";
    private static final String USAGE_MIDI_ANA_MID = "8";
    private static final String USAGE_MIDI_EXT_MID = "6";
    private static final String NOTESIMAGE_BITS_MID = "rgb888";

    private static final String WORKNUM_HIG = "8";
    private static final String NOTESIMAGENUM_HIG = "120";
    private static final String USAGE_MIDIRAM_HIG = "25";
    private static final String USAGE_MIDI_ANA_HIG = "8";
    private static final String USAGE_MIDI_EXT_HIG = "6";
    private static final String NOTESIMAGE_BITS_HIG = "rgb888";
    
    private static final String WORKNUM_MAX = "8";
    private static final String NOTESIMAGENUM_MAX = "300";
    private static final String USAGE_MIDIRAM_MAX = "100";
    private static final String USAGE_MIDI_ANA_MAX = "24";
    private static final String USAGE_MIDI_EXT_MAX = "24";
    private static final String NOTESIMAGE_BITS_MAX = "rgb888";
    
    private static final String NOTES_SPEED_SLOW = "0.4";
    private static final String NOTES_SPEED_NORM = "1.0";
    private static final String NOTES_SPEED_FAST = "2.0";
    private static final String NOTES_SPEED_VFAST = "4.0";

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
    
    private Map<String, Component> tabMap = new HashMap<>();

    // ユーザー非公開キー
    private List<String> ignoreKeysSystem = Arrays.asList(SystemProperties.SYSP_RENDERER_DIMENSION);
    private List<String> ignoreKeysDesign = Arrays.asList(LayoutConfig.LC_CURSOR_POS, LayoutConfig.LC_NOTES_COLOR_ASIGN, LayoutConfig.LC_PB_COLOR,
            LayoutConfig.LC_PB_VISIBLE);

    private Map<Integer, JComboBox<String>> comboBoxMapSys = new HashMap<>();
    private Map<Integer, JComboBox<String>> comboBoxMapLc = new HashMap<>();
    private JComboBox<String> comboBoxSynth;
    private JLabel lblSelectedLayoutLabel;
    private JComboBox<String> comboBoxWindowSize;
    private final ButtonGroup buttonGroup = new ButtonGroup();

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
    private JEditorPane editorPane;
    private JLabel lblSynthDesc;
    private JCheckBox chckbxIgnoreNotesValid;
    private JCheckBox chckbxIgnoreInBetween;
    private JSpinner spinnerIgnoreLow;
    private JSpinner spinnerIgnoreHigh;
    private JRadioButton rdbtnMonitorType3;
    private JCheckBox chckbxInvalidateEffect;
    private JRadioButton rdbtnPerfMaxButton;
    private JRadioButton rdbtnNotesSpeedSlow;
    private JRadioButton rdbtnNotesSpeedNormal;
    private final ButtonGroup buttonGroup_4 = new ButtonGroup();
    private JRadioButton rdbtnNotesSpeedFast;
    private JRadioButton rdbtnNotesSpeedVeryFast;
    private JSlider sliderVolume;
    private JPanel summaryPanel;
    private JPanel rendererPanel;
    private JPanel layoutPanel;
    private JPanel aboutPanel;

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
        setBounds(100, 100, 641, 653);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(0, 0));
        {
            tabbedPane = new JTabbedPane(JTabbedPane.TOP);
            contentPanel.add(tabbedPane);
            {
                summaryPanel = new JPanel();
                summaryPanel.setBorder(null);
                tabbedPane.addTab("Settings", null, summaryPanel, null);
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
                audioSummaryPanel.setBounds(12, 10, 584, 162);
                panel.add(audioSummaryPanel);

                comboBoxSynth = new JComboBox<String>();
                comboBoxSynth.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (!initialized.get())
                            return; // 初期化中は無視

                        String synthKey = synthItemKeys.get(comboBoxSynth.getSelectedIndex());
                        setSystemTableParam(SystemProperties.SYSP_AUDIO_SYNTH, (String) synthKey);
                        updateSynthDescription();
                    }
                });
                comboBoxSynth.setBounds(96, 17, 295, 21);
                audioSummaryPanel.add(comboBoxSynth);

                JLabel lblSynthLabel = new JLabel("MIDI Receiver");
                lblSynthLabel.setBounds(12, 21, 72, 13);
                audioSummaryPanel.add(lblSynthLabel);

                lblSynthDesc = new JLabel("");
                lblSynthDesc.setBounds(106, 48, 379, 13);
                audioSummaryPanel.add(lblSynthDesc);

                chckbxIgnoreInBetween = new JCheckBox("Ignore audio in between two velocity values");
                chckbxIgnoreInBetween.setBounds(96, 71, 303, 21);
                audioSummaryPanel.add(chckbxIgnoreInBetween);

                spinnerIgnoreLow = new JSpinner();
                spinnerIgnoreLow.setBounds(213, 98, 58, 20);
                audioSummaryPanel.add(spinnerIgnoreLow);
                spinnerIgnoreLow.setModel(new SpinnerNumberModel(1, 1, 127, 1));

                JLabel lblLow = new JLabel("Lowest");
                lblLow.setBounds(159, 101, 50, 13);
                audioSummaryPanel.add(lblLow);
                lblLow.setHorizontalAlignment(SwingConstants.RIGHT);

                JLabel lblHigh = new JLabel("Highest");
                lblHigh.setBounds(315, 101, 50, 13);
                audioSummaryPanel.add(lblHigh);
                lblHigh.setHorizontalAlignment(SwingConstants.RIGHT);

                spinnerIgnoreHigh = new JSpinner();
                spinnerIgnoreHigh.setBounds(370, 98, 58, 20);
                audioSummaryPanel.add(spinnerIgnoreHigh);
                spinnerIgnoreHigh.setModel(new SpinnerNumberModel(20, 1, 127, 1));
                
                JLabel lblVolumeLabel = new JLabel("Volume");
                lblVolumeLabel.setBounds(12, 126, 72, 26);
                audioSummaryPanel.add(lblVolumeLabel);
                
                sliderVolume = new JSlider();
                sliderVolume.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent arg0) {
                    }
                });
                sliderVolume.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent arg0) {
                        if (!initialized.get())
                            return; // 初期化中は無視
                        
                        float fPos = (float)sliderVolume.getValue() / (float)sliderVolume.getMaximum();
                        JMPCoreAccessor.getSoundManager().setLineVolume(fPos);
                    }
                });
                sliderVolume.setMaximum(1000);
                sliderVolume.setBounds(96, 126, 435, 26);
                audioSummaryPanel.add(sliderVolume);
                spinnerIgnoreHigh.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent arg0) {
                        int value = (int) spinnerIgnoreHigh.getValue();
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_IGNORENOTES_AUDIO_HIGHEST, "" + value);
                    }
                });
                spinnerIgnoreLow.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent arg0) {
                        int value = (int) spinnerIgnoreLow.getValue();
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_IGNORENOTES_AUDIO_LOWEST, "" + value);
                    }
                });
                chckbxIgnoreInBetween.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_IGNORENOTES_AUDIO_VALID, "" + chckbxIgnoreInBetween.isSelected());
                    }
                });

                JPanel layoutSummaryPanel = new JPanel();
                layoutSummaryPanel.setLayout(null);
                layoutSummaryPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
                        "Design", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
                layoutSummaryPanel.setBounds(12, 182, 584, 116);
                panel.add(layoutSummaryPanel);

                lblSelectedLayoutLabel = new JLabel("Default Design");
                lblSelectedLayoutLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
                lblSelectedLayoutLabel.setBounds(24, 20, 507, 31);
                layoutSummaryPanel.add(lblSelectedLayoutLabel);

                JButton btnLoadLayoutButton = new JButton("Load Design");
                btnLoadLayoutButton.setActionCommand("LOAD_LAYOUT");
                btnLoadLayoutButton.addActionListener(this);
                btnLoadLayoutButton.setBounds(318, 80, 121, 26);
                layoutSummaryPanel.add(btnLoadLayoutButton);

                JButton btnDefaultButton = new JButton("Default");
                btnDefaultButton.setActionCommand("DEF_LAYOUT");
                btnDefaultButton.addActionListener(this);
                btnDefaultButton.setBounds(451, 80, 121, 26);
                layoutSummaryPanel.add(btnDefaultButton);
                
                chckbxInvalidateEffect = new JCheckBox("Invalidate Effect");
                chckbxInvalidateEffect.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_INVALIDATE_EFFECT, chckbxInvalidateEffect.isSelected() ? "true" : "false");
                    }
                });
                chckbxInvalidateEffect.setBounds(24, 57, 169, 21);
                layoutSummaryPanel.add(chckbxInvalidateEffect);
                
                JLabel lblInvalidateEffectDesc = new JLabel("<html>If your PC does not have a GPU,<br>we recommend turning this on.</html>");
                lblInvalidateEffectDesc.setBounds(56, 80, 215, 26);
                layoutSummaryPanel.add(lblInvalidateEffectDesc);

                JPanel systemSummaryPanel = new JPanel();
                systemSummaryPanel.setLayout(null);
                systemSummaryPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
                        "System", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
                systemSummaryPanel.setBounds(12, 308, 584, 195);
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
                        setSystemTableParam(SystemProperties.SYSP_AUDIO_USAGE_MIDI_BUF, USAGE_MIDIRAM_LOW);
                        setSystemTableParam(SystemProperties.SYSP_AUDIO_USAGE_MIDI_ANALYZE_THREAD, USAGE_MIDI_ANA_LOW);
                        setSystemTableParam(SystemProperties.SYSP_AUDIO_USAGE_MIDI_EXTRACT_THREAD, USAGE_MIDI_EXT_LOW);
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_NOTES_COLOR_BITS, NOTESIMAGE_BITS_LOW);
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
                        setSystemTableParam(SystemProperties.SYSP_AUDIO_USAGE_MIDI_BUF, USAGE_MIDIRAM_MID);
                        setSystemTableParam(SystemProperties.SYSP_AUDIO_USAGE_MIDI_ANALYZE_THREAD, USAGE_MIDI_ANA_MID);
                        setSystemTableParam(SystemProperties.SYSP_AUDIO_USAGE_MIDI_EXTRACT_THREAD, USAGE_MIDI_EXT_MID);
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_NOTES_COLOR_BITS, NOTESIMAGE_BITS_MID);
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
                        setSystemTableParam(SystemProperties.SYSP_AUDIO_USAGE_MIDI_BUF, USAGE_MIDIRAM_HIG);
                        setSystemTableParam(SystemProperties.SYSP_AUDIO_USAGE_MIDI_ANALYZE_THREAD, USAGE_MIDI_ANA_HIG);
                        setSystemTableParam(SystemProperties.SYSP_AUDIO_USAGE_MIDI_EXTRACT_THREAD, USAGE_MIDI_EXT_HIG);
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_NOTES_COLOR_BITS, NOTESIMAGE_BITS_HIG);
                    }
                });
                buttonGroup.add(rdbtnPerfHighButton);
                rdbtnPerfHighButton.setBounds(330, 69, 113, 21);
                systemSummaryPanel.add(rdbtnPerfHighButton);
                
                rdbtnPerfMaxButton = new JRadioButton("Max !!");
                rdbtnPerfMaxButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_WORKNUM, WORKNUM_MAX);
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_NOTESIMAGENUM, NOTESIMAGENUM_MAX);
                        setSystemTableParam(SystemProperties.SYSP_AUDIO_USAGE_MIDI_BUF, USAGE_MIDIRAM_MAX);
                        setSystemTableParam(SystemProperties.SYSP_AUDIO_USAGE_MIDI_ANALYZE_THREAD, USAGE_MIDI_ANA_MAX);
                        setSystemTableParam(SystemProperties.SYSP_AUDIO_USAGE_MIDI_EXTRACT_THREAD, USAGE_MIDI_EXT_MAX);
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_NOTES_COLOR_BITS, NOTESIMAGE_BITS_MAX);
                    }
                });
                buttonGroup.add(rdbtnPerfMaxButton);
                rdbtnPerfMaxButton.setBounds(447, 69, 113, 21);
                systemSummaryPanel.add(rdbtnPerfMaxButton);

                JLabel lblNotesSpeedLabel = new JLabel("Notes Speed");
                lblNotesSpeedLabel.setBounds(12, 96, 72, 13);
                systemSummaryPanel.add(lblNotesSpeedLabel);

                JLabel lblNotesOrderLabel = new JLabel("Notes Layer");
                lblNotesOrderLabel.setBounds(12, 119, 72, 13);
                systemSummaryPanel.add(lblNotesOrderLabel);

                rdbtnRenderOrderAsc = new JRadioButton("Track1 is Back");
                rdbtnRenderOrderAsc.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_LAYERORDER, "asc");
                    }
                });
                buttonGroup_1.add(rdbtnRenderOrderAsc);
                rdbtnRenderOrderAsc.setBounds(96, 115, 113, 21);
                systemSummaryPanel.add(rdbtnRenderOrderAsc);

                rdbtnRenderOrderDesc = new JRadioButton("Track1 is Top");
                rdbtnRenderOrderDesc.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_LAYERORDER, "desc");
                    }
                });
                buttonGroup_1.add(rdbtnRenderOrderDesc);
                rdbtnRenderOrderDesc.setBounds(213, 115, 113, 21);
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
                lblMonitorTypeLabel.setBounds(12, 142, 72, 13);
                systemSummaryPanel.add(lblMonitorTypeLabel);

                rdbtnMonitorNone = new JRadioButton("None");
                buttonGroup_3.add(rdbtnMonitorNone);
                rdbtnMonitorNone.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_MONITOR_TYPE, "none");
                    }
                });
                rdbtnMonitorNone.setBounds(96, 138, 113, 21);
                systemSummaryPanel.add(rdbtnMonitorNone);

                rdbtnMonitorType1 = new JRadioButton("Notes Analyzer");
                buttonGroup_3.add(rdbtnMonitorType1);
                rdbtnMonitorType1.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_MONITOR_TYPE, "type1");
                    }
                });
                rdbtnMonitorType1.setBounds(213, 138, 113, 21);
                systemSummaryPanel.add(rdbtnMonitorType1);

                rdbtnMonitorType2 = new JRadioButton("Counter");
                buttonGroup_3.add(rdbtnMonitorType2);
                rdbtnMonitorType2.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_MONITOR_TYPE, "type2");
                    }
                });
                rdbtnMonitorType2.setBounds(330, 138, 113, 21);
                systemSummaryPanel.add(rdbtnMonitorType2);

                rdbtnMonitorType3 = new JRadioButton("Classical");
                buttonGroup_3.add(rdbtnMonitorType3);
                rdbtnMonitorType3.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_MONITOR_TYPE, "type3");
                    }
                });
                rdbtnMonitorType3.setBounds(447, 138, 113, 21);
                systemSummaryPanel.add(rdbtnMonitorType3);

                JLabel lblIgnoreNotesLabel = new JLabel("Ignore Notes");
                lblIgnoreNotesLabel.setBounds(12, 165, 72, 13);
                systemSummaryPanel.add(lblIgnoreNotesLabel);

                chckbxIgnoreNotesValid = new JCheckBox("Invisible Ghost Notes");
                chckbxIgnoreNotesValid.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_IGNORENOTES_RENDER_VALID, "" + chckbxIgnoreNotesValid.isSelected());
                    }
                });
                chckbxIgnoreNotesValid.setBounds(96, 161, 149, 21);
                systemSummaryPanel.add(chckbxIgnoreNotesValid);
                
                rdbtnNotesSpeedSlow = new JRadioButton("Slow");
                rdbtnNotesSpeedSlow.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_NOTESSPEED, NOTES_SPEED_SLOW);
                    }
                });
                buttonGroup_4.add(rdbtnNotesSpeedSlow);
                rdbtnNotesSpeedSlow.setBounds(96, 92, 113, 21);
                systemSummaryPanel.add(rdbtnNotesSpeedSlow);
                
                rdbtnNotesSpeedNormal = new JRadioButton("Normal");
                rdbtnNotesSpeedNormal.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_NOTESSPEED, NOTES_SPEED_NORM);
                    }
                });
                buttonGroup_4.add(rdbtnNotesSpeedNormal);
                rdbtnNotesSpeedNormal.setBounds(213, 92, 113, 21);
                systemSummaryPanel.add(rdbtnNotesSpeedNormal);
                
                rdbtnNotesSpeedFast = new JRadioButton("Fast");
                rdbtnNotesSpeedFast.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_NOTESSPEED, NOTES_SPEED_FAST);
                    }
                });
                buttonGroup_4.add(rdbtnNotesSpeedFast);
                rdbtnNotesSpeedFast.setBounds(330, 92, 113, 21);
                systemSummaryPanel.add(rdbtnNotesSpeedFast);
                
                rdbtnNotesSpeedVeryFast = new JRadioButton("Very Fast");
                rdbtnNotesSpeedVeryFast.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setSystemTableParam(SystemProperties.SYSP_RENDERER_NOTESSPEED, NOTES_SPEED_VFAST);
                    }
                });
                buttonGroup_4.add(rdbtnNotesSpeedVeryFast);
                rdbtnNotesSpeedVeryFast.setBounds(447, 92, 113, 21);
                systemSummaryPanel.add(rdbtnNotesSpeedVeryFast);
                
                JButton btnShowExpertSettings = new JButton("Show Expert Settings");
                btnShowExpertSettings.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        int index = tabbedPane.indexOfTab("About Rain MIDI");
                        if (index > 2) {
//                            tabbedPane.removeTabAt(2);
//                            tabbedPane.removeTabAt(1);
//                            tabbedPane.setSelectedComponent(tabMap.get("Settings"));
                        }
                        else {
                            tabbedPane.insertTab("Expert 1", null, tabMap.get("Expert 1"), null, 1);
                            tabbedPane.insertTab("Expert 2", null, tabMap.get("Expert 2"), null, 2);
                            tabbedPane.setSelectedComponent(tabMap.get("Expert 1"));
                        }
                    }
                });
                btnShowExpertSettings.setBounds(439, 513, 157, 21);
                panel.add(btnShowExpertSettings);
            }
            {
                rendererPanel = new JPanel();
                tabbedPane.addTab("Expert 1", null, rendererPanel, null);
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
                layoutPanel = new JPanel();
                tabbedPane.addTab("Expert 2", null, layoutPanel, null);
                layoutPanel.setLayout(new BorderLayout(0, 0));
                {
                    // テーブルのデータとカラム名
                    String[] columnNames = { "Config", "Value" };
                    Object[][] data = { { 1, "A" }, { 2, "B" }, { 3, "C" } };

                    // モデル作成
                    designModel = new DefaultTableModel(data, columnNames);

                    designTable = new JTable(designModel);
                    JScrollPane scrollPane = new JScrollPane(designTable);
                    layoutPanel.add(scrollPane);
                }
            }

            aboutPanel = new JPanel();
            tabbedPane.addTab("About Rain MIDI", null, aboutPanel, null);
            aboutPanel.setLayout(new BorderLayout(0, 0));

            editorPane = new JEditorPane();
            editorPane.setBackground(UIManager.getColor("TextField.background"));
            editorPane.setContentType("text/html");
            editorPane.setEditable(false);
            editorPane.addHyperlinkListener(new HyperlinkListener() {
                @Override
                public void hyperlinkUpdate(HyperlinkEvent e) {
                    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                        String desc = e.getDescription();
                        if ("ja".equals(desc) || "en".equals(desc)) {
                            updateAbout(desc);
                        }
                        else {
                            // それ以外のリンクはブラウザで開く
                            try {
                                Desktop.getDesktop().browse(new URI(desc));
                            }
                            catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            catch (URISyntaxException e1) {
                                e1.printStackTrace();
                            }
                        }
                        editorPane.setCaretPosition(0); // 上にスクロール
                    }
                }
            });
            
            JScrollPane scrollPane = new JScrollPane(editorPane);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            aboutPanel.add(scrollPane, BorderLayout.CENTER);
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
        
        tabMap.put("Settings", summaryPanel);
        tabMap.put("Expert 1", rendererPanel);
        tabMap.put("Expert 2", layoutPanel);
        tabMap.put("About", aboutPanel);
        tabbedPane.removeTabAt(2);
        tabbedPane.removeTabAt(1);
        tabbedPane.setSelectedComponent(tabMap.get("Settings"));
    }

    public void updateSynthDescription() {
        String description = "";
        String synthKey = synthItemKeys.get(comboBoxSynth.getSelectedIndex());
        if (synthKey.equalsIgnoreCase(ISoundManager.AUTO_RECEIVER_NAME)) {
            description = "";
            description += "Automatically select an \"" + JMPCoreAccessor.getSoundManager().getMidiToolkit().getAutoSelectRecieverName() + "\"";
        }
        lblSynthDesc.setText(description);
    }
    
    public void updateAbout(String langCode) {
        // HTMLコンテンツの作成
        if (this.targetPlg != null) {
            try {
                AboutHtmlReader htmlReader = new AboutHtmlReader();
                editorPane.setText(htmlReader.getContent(langCode));
            }
            catch (Exception e) {
                editorPane.setText("Error");
            }
        }
    }

    public void updateAbout() {
        updateAbout("en");
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

        String layoutName = SystemProperties.getInstance().getPropNode(SystemProperties.SYSP_FILE_LAYOUT).getDataString();
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
                String notesSpeed = SystemProperties.getInstance().getPropNode(SystemProperties.SYSP_RENDERER_NOTESSPEED).getDataString();
                if (notesSpeed.equals(NOTES_SPEED_SLOW)) {
                    rdbtnNotesSpeedSlow.setSelected(true);
                }
                else if (notesSpeed.equals(NOTES_SPEED_NORM)) {
                    rdbtnNotesSpeedNormal.setSelected(true);
                }
                else if (notesSpeed.equals(NOTES_SPEED_FAST)) {
                    rdbtnNotesSpeedFast.setSelected(true);
                }
                else if (notesSpeed.equals(NOTES_SPEED_VFAST)) {
                    rdbtnNotesSpeedVeryFast.setSelected(true);
                }
                else {
                    rdbtnNotesSpeedSlow.setSelected(false);
                    rdbtnNotesSpeedNormal.setSelected(false);
                    rdbtnNotesSpeedFast.setSelected(false);
                    rdbtnNotesSpeedVeryFast.setSelected(false);
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
                else if (workNum.equals(WORKNUM_MAX) && imgNum.equals(NOTESIMAGENUM_MAX)) {
                    rdbtnPerfMaxButton.setSelected(true);
                }
                else {
                    rdbtnPerfLowButton.setSelected(false);
                    rdbtnPerfMidButton.setSelected(false);
                    rdbtnPerfHighButton.setSelected(false);
                    rdbtnPerfMaxButton.setSelected(false);
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
                else if (type == SystemProperties.SyspMonitorType.TYPE3) {
                    rdbtnMonitorType3.setSelected(true);
                }
            }
            else if (keyName.equals(SystemProperties.SYSP_RENDERER_IGNORENOTES_RENDER_VALID)) {
                chckbxIgnoreNotesValid.setSelected((boolean) node.getData());
            }
            else if (keyName.equals(SystemProperties.SYSP_RENDERER_IGNORENOTES_AUDIO_VALID)) {
                chckbxIgnoreInBetween.setSelected((boolean) node.getData());
            }
            else if (keyName.equals(SystemProperties.SYSP_RENDERER_IGNORENOTES_AUDIO_LOWEST)) {
                spinnerIgnoreLow.setValue((int) node.getData());
            }
            else if (keyName.equals(SystemProperties.SYSP_RENDERER_IGNORENOTES_AUDIO_HIGHEST)) {
                spinnerIgnoreHigh.setValue((int) node.getData());
            }
            else if (keyName.equals(SystemProperties.SYSP_RENDERER_INVALIDATE_EFFECT)) {
                chckbxInvalidateEffect.setSelected((boolean)node.getData());
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

        updateSynthDescription();
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            initialized.set(false);
            isCommitClose = false;
            if (SystemProperties.getInstance().isGpuAvailable() == false) {
                SystemProperties.getInstance().getPropNode(SystemProperties.SYSP_RENDERER_INVALIDATE_EFFECT).setObject("true");
            }
            updateItem();
            updateSynthItem();
            updateAbout();
            
            int cur = (int)(JMPCoreAccessor.getSoundManager().getLineVolume() * (float)sliderVolume.getMaximum());
            sliderVolume.setValue(cur);
            
            initialized.set(true);
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
                            setSystemTableParam(SystemProperties.SYSP_FILE_LAYOUT, layoutName);
                            lblSelectedLayoutLabel.setText(layoutName);
                        }
                        updateDesignItems();
                    }
                    catch (IOException e1) {
                        LayoutManager.getInstance().initializeConfig();
                        setSystemTableParam(SystemProperties.SYSP_FILE_LAYOUT, "");
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

                String path = SystemProperties.getInstance().getPropNode(SystemProperties.SYSP_FILE_DEFAULT_PATH).getDataString();
                File dir = new File(path);
                if (path.isEmpty() == false) {
                    if (dir.exists() && dir.isDirectory()) {
                        chooser.setCurrentDirectory(dir);
                    }
                }

                // ダイアログを開く
                SystemProperties.getInstance().getPreloadFiles().clear();
                int result = chooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File[] files = chooser.getSelectedFiles(); // 複数ファイル
                    for (File f : files) {
                        SystemProperties.getInstance().getPreloadFiles().add(f);
                        setSystemTableParam(SystemProperties.SYSP_FILE_DEFAULT_PATH, f.getParent());
                    }
                    setVisible(false);

                    commit();
                    isCommitClose = true;
                }
                break;
            }
            case "DEF_LAYOUT":
                LayoutManager.getInstance().initializeConfig();
                setSystemTableParam(SystemProperties.SYSP_FILE_LAYOUT, "");
                lblSelectedLayoutLabel.setText("Default Design");
                updateDesignItems();
                break;
            case "OK":
                SystemProperties.getInstance().getPreloadFiles().clear();
                commit();
                isCommitClose = true;
                setVisible(false);
                break;
            case "Cancel":
                SystemProperties.getInstance().getPreloadFiles().clear();
                isCommitClose = false;
                setVisible(false);
                break;
        }
    }

    public boolean isCommitClose() {
        return isCommitClose;
    }
}

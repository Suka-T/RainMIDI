package gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import jlib.core.ISystemManager;
import jlib.core.JMPCoreAccessor;
import layout.LayoutConfig;
import layout.LayoutManager;
import layout.parts.CollisionEffectPainter;
import layout.parts.KeyParts;
import layout.parts.KeyboardPainter;
import layout.parts.KeyboardPainter.KindOfKey;
import layout.parts.NotesPainter;
import layout.parts.TickbarPainter;
import layout.parts.key.BlackKeyParts;
import layout.parts.key.WhiteKeyParts;
import plg.AbstractRenderPlugin;
import plg.SystemProperties;
import plg.Utility;

public class DesignViewer extends JDialog {
	
	public static final int FIXED_KEY_COUNT = 18;
	public static final int FIXED_KEY_HK = 11;
	public static final int FIXED_KEY_KK = 7;
	
	private static final int FIXED_TICK_POS = 120; 

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JComboBox<String> comboBoxLayoutFile;
	private JPanel panelViewer;
	
	protected int[] hitEffectPosY = null;
    protected KeyParts[] aHakken = null;
    protected KeyParts[] aKokken = null;
    
    private boolean isCommit = false;
    private AtomicBoolean initialized = new AtomicBoolean(false);
    
    private File selectedLayoutFile = null;
    public File getSelectedLayoutFile() {
    	return selectedLayoutFile;
    }

	/**
	 * Create the dialog.
	 */
	public DesignViewer(Frame owner) {
		super(owner);
		
		setModal(true);
		setTitle("Design Viewer");
		setBounds(120, 120, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		initialized.set(false);
		
		comboBoxLayoutFile = new JComboBox<String>();
		comboBoxLayoutFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
                if (!initialized.get())
                    return; // 初期化中は無視
				
                updateView();
			}
		});
		comboBoxLayoutFile.setBounds(12, 10, 180, 21);
		contentPanel.add(comboBoxLayoutFile);
		
		panelViewer = new JPanel() {
		    @Override
		    protected void paintComponent(Graphics g) {
		        super.paintComponent(g); // 背景のクリアなど基本処理
		        
                if (!initialized.get())
                    return; // 初期化中は無視
		        
		        Graphics2D g2d = (Graphics2D)g;
		        
		        LayoutManager lm = LayoutManager.getInstance();
		        
		        
		        int width = getWidth();
		        int height = getHeight();
		        
		        g.setColor(lm.getPlayerColor().getBgColor());
		        g.fillRect(0, 0, width, height);
		        
		        g.setColor(lm.getPlayerColor().getBdColor());
		        if (LayoutManager.getInstance().isVisibleVerticalBorder()) {
			        int bdHeight = height / 4;
			        for (int i=1; i <= 3; i++) {
			        	g.drawLine(FIXED_TICK_POS, bdHeight * i, width, bdHeight * i);
			        }
		        }
		        
		        NotesPainter ntPainter = lm.getNotesPainter();
		        NotesPainter.Context nContext = ntPainter.newContext();
		        int measCellHeight = getMeasHeight();
		        
		        int ntxOffs = 80;
		        int ntSize = 90;
		        int trk = 0;
		        for (trk = 0; trk < 3; trk++) {
			        nContext.g = g2d;
			        nContext.x = ntxOffs + ntSize * trk;
			        nContext.w = ntSize;
			        nContext.y = measCellHeight * (trk + 3);
			        nContext.h = measCellHeight;
			        nContext.bgColor = LayoutManager.getInstance().getNotesColor(trk).getBgColor();
		            nContext.bdColor = LayoutManager.getInstance().getNotesColor(trk).getBdColor();
		            nContext.colorIndex = trk;
		            nContext.createParam();
			        ntPainter.paintNotes(nContext);
		        }
		        
		        KeyboardPainter keyboardPainter = lm.getKeyboardPainter(SystemProperties.SyspViewMode.RAIN_FALL);
		        int tickBarPositionOffs = 0;
		        if (LayoutManager.getInstance().getCursorType() == LayoutConfig.ECursorType.Keyboard) {
		            tickBarPositionOffs = 3;
		        }
		        
		        if (LayoutManager.getInstance().getCursorType() == LayoutConfig.ECursorType.Keyboard) {
		            /* White Keyboard */
		            Color keyBgColor;
		            boolean isPush = false;
		            keyboardPainter.setKeyboardWidth(FIXED_TICK_POS);
		            for (int i = 0; i < aHakken.length; i++) {
		                keyBgColor = Color.WHITE;
		                keyboardPainter.paintKeyparts(g2d, aHakken[i], keyBgColor, Color.LIGHT_GRAY, isPush, KindOfKey.WHITE);
		            }
		        }

		        if (LayoutManager.getInstance().getCursorType() == LayoutConfig.ECursorType.Keyboard) {
		            /* Black Keyboard */
		            Color keyBgColor;
		            boolean isPush = false;
		            keyboardPainter.setKeyboardWidth(FIXED_TICK_POS);
		            for (int i = 0; i < aKokken.length; i++) {
		                keyBgColor = Color.BLACK;
		                keyboardPainter.paintKeyparts(g2d, aKokken[i], keyBgColor, Color.LIGHT_GRAY, isPush, KindOfKey.BLACK);
		            }
		        }
		        
		        /* 衝突エフェクト描画 */
		        CollisionEffectPainter colEffePainterIn = LayoutManager.getInstance().getCollisionEffectPainterIn();
		        CollisionEffectPainter colEffePainterOut = LayoutManager.getInstance().getCollisionEffectPainterOut();
		        
		        int tickBarPosition = FIXED_TICK_POS;
	            Color hitEffectColor = LayoutManager.getInstance().getCursorColor().getEffeColor();
	            g.setColor(LayoutManager.getInstance().getPlayerColor().getBgColor());
	            int keyHeight = measCellHeight;
	            int effx = tickBarPosition;
	            Color keyBgColor;
	            keyBgColor = LayoutManager.getInstance().rgbToNotesColor(LayoutManager.getInstance().getNotesColor(0).getBgColor().getRGB(), Color.WHITE);
                colEffePainterIn.paintIn(g2d, effx, hitEffectPosY[3], keyHeight, keyBgColor, hitEffectColor);
                colEffePainterOut.paintOut(g2d, effx, hitEffectPosY[3], keyHeight, keyBgColor, hitEffectColor);
                g2d.setComposite(AlphaComposite.SrcOver);
		        
                /* Tickbar描画 */
                TickbarPainter tickbarPainter = LayoutManager.getInstance().getTickbarPainter();
                tickbarPainter.clearCache();
                Color csrColor = LayoutManager.getInstance().getCursorColor().getBdColor();
                tickbarPainter.paintLine(g2d, tickBarPosition + tickBarPositionOffs, 0, tickBarPosition + tickBarPositionOffs, panelViewer.getHeight(), csrColor);
		    }
		};
		panelViewer.setBackground(new Color(0, 0, 0));
		panelViewer.setBounds(12, 41, 410, 179);
		contentPanel.add(panelViewer);
		
		JButton buttonSave = new JButton("File Output");
		buttonSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		        JFileChooser fileChooser = new JFileChooser();
		        fileChooser.setDialogTitle("Save Design Setting File");
		        
		        Path folder = Paths.get(JMPCoreAccessor.getSystemManager().getSystemPath(ISystemManager.PATH_RES_DIR, AbstractRenderPlugin.PluginInstance));
		        File defaultDirectory = folder.toFile();
		        fileChooser.setCurrentDirectory(defaultDirectory);

		        // デフォルトの拡張子フィルタを設定
		        FileNameExtensionFilter filter = new FileNameExtensionFilter("RainMIDI Design (*.layout)", "layout");
		        fileChooser.setFileFilter(filter);

		        int userSelection = fileChooser.showSaveDialog(null);

		        if (userSelection == JFileChooser.APPROVE_OPTION) {
		            File fileToSave = fileChooser.getSelectedFile();
		            String filePath = fileToSave.getAbsolutePath();

		            if (!filePath.toLowerCase().endsWith(".layout")) {
		                fileToSave = new File(filePath + ".layout");
		            }
		            
		            try {
						LayoutManager.getInstance().write(fileToSave);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
		            
		            updateCombobox();
		        }
			}
		});
		buttonSave.setBounds(331, 10, 91, 21);
		contentPanel.add(buttonSave);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						isCommit = true;
						setVisible(false);
					}
				});
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						isCommit = false;
						setVisible(false);
					}
				});
				buttonPane.add(cancelButton);
			}
		}
		
		initialized.set(true);
	}
	
	public void updateCombobox() {
		initialized.set(false);
		comboBoxLayoutFile.removeAllItems();
		comboBoxLayoutFile.addItem("-- Backup --");
		comboBoxLayoutFile.addItem("RainMIDI-Default");
		comboBoxLayoutFile.addItem("RainMIDI-Lightweight");
		
		Path folderPath = Paths.get(JMPCoreAccessor.getSystemManager().getSystemPath(ISystemManager.PATH_RES_DIR, AbstractRenderPlugin.PluginInstance));
        File folder = folderPath.toFile();
        
        if (folder.exists() && folder.isDirectory()) {
            File[] listOfFiles = folder.listFiles();

            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    if (file.isFile()) { // フォルダを除外してファイルのみ追加
                    	if (plg.Utility.checkExtension(file, "layout")) {
                    		comboBoxLayoutFile.addItem(Utility.getFileNameNotExtension(file));
                    	}
                    }
                }
            }
        }
        comboBoxLayoutFile.setSelectedIndex(0);
        initialized.set(true);
	}
	
	public void openDialog() {
		isCommit = false;
		makeKeyboardRsrc();
		
		updateCombobox();
        updateView();
        
        setVisible(true);
	}
	
	private void updateView() {
		if (comboBoxLayoutFile.getSelectedIndex() == 0) {
        	Path folder = Paths.get(JMPCoreAccessor.getSystemManager().getSystemPath(ISystemManager.PATH_DATA_DIR, AbstractRenderPlugin.PluginInstance));
        	Path fullPath = folder.resolve(AbstractRenderPlugin.BACKUP_FILE_NAME);
        	selectedLayoutFile = fullPath.toFile();
            try {
                LayoutManager.getInstance().read(selectedLayoutFile);
            }
            catch (IOException e1) {
            	selectedLayoutFile = null;
                LayoutManager.getInstance().initializeConfig();
            }
        }
        else if (comboBoxLayoutFile.getSelectedIndex() == 1) {
			LayoutManager.getInstance().initializeConfig();
			
			selectedLayoutFile = null;
		}
        else if (comboBoxLayoutFile.getSelectedIndex() == 2) {
			LayoutManager.getInstance().initializeConfigLight();
			
			selectedLayoutFile = null;
		}
		else {
			Path folderPath = Paths.get(JMPCoreAccessor.getSystemManager().getSystemPath(ISystemManager.PATH_RES_DIR, AbstractRenderPlugin.PluginInstance));
	        String fileNameStr = comboBoxLayoutFile.getSelectedItem().toString();
	        Path fullPath = folderPath.resolve(fileNameStr + ".layout");
	        selectedLayoutFile = fullPath.toFile();
            try {
                LayoutManager.getInstance().read(selectedLayoutFile);
            }
            catch (IOException e1) {
            	selectedLayoutFile = null;
                LayoutManager.getInstance().initializeConfig();
            }
		}
		LayoutManager.getInstance().initialize(null);
		LayoutManager.getInstance().setNotesBounds(80, getMeasHeight());
		panelViewer.repaint();
	}
	
	private int getMeasHeight() {
		return 12;
	}

    protected void makeKeyboardRsrc() {
        hitEffectPosY = new int[FIXED_KEY_COUNT];
        int measCellHeight = getMeasHeight();
        int keyHeight = measCellHeight;
        int keyCount = 0;
        int topOffset = (keyHeight * keyCount);
        for (int i = 0; i < FIXED_KEY_COUNT; i++) {
            hitEffectPosY[i] = topOffset + (keyHeight * i);
        }

        aHakken = new KeyParts[FIXED_KEY_HK];
        aKokken = new KeyParts[FIXED_KEY_KK];

        int kkCnt = 0;
        int hkCnt = 0;
        int hkWidth = FIXED_TICK_POS;
        int kkWidth = (int) (hkWidth * 0.7);
        int hakkenHeight = panelViewer.getHeight() / 7;
        for (int i = 0; i < FIXED_KEY_COUNT; i++) {
            int midiNo = 127 - i;
            int key = midiNo % 12;
            switch (key) {
                case 0:
                case 5:
                    aHakken[hkCnt] = new WhiteKeyParts();
                    aHakken[hkCnt].x = FIXED_TICK_POS - hkWidth;
                    aHakken[hkCnt].y = hitEffectPosY[i];
                    aHakken[hkCnt].width = hkWidth;
                    aHakken[hkCnt].height = hakkenHeight;
                    aHakken[hkCnt].orgX = FIXED_TICK_POS - hkWidth;
                    aHakken[hkCnt].orgY = hitEffectPosY[i];
                    aHakken[hkCnt].orgWidth = hkWidth;
                    aHakken[hkCnt].orgHeight = keyHeight;
                    aHakken[hkCnt].y -= (keyHeight / 2);
                    aHakken[hkCnt].midiNo = midiNo;
                    hkCnt++;
                    break;
                case 7:
                case 9:
                case 2:
                    aHakken[hkCnt] = new WhiteKeyParts();
                    aHakken[hkCnt].x = FIXED_TICK_POS - hkWidth;
                    aHakken[hkCnt].y = hitEffectPosY[i];
                    aHakken[hkCnt].width = hkWidth;
                    aHakken[hkCnt].height = hakkenHeight + keyHeight / 2;
                    aHakken[hkCnt].orgX = FIXED_TICK_POS - hkWidth;
                    aHakken[hkCnt].orgY = hitEffectPosY[i];
                    aHakken[hkCnt].orgWidth = hkWidth;
                    aHakken[hkCnt].orgHeight = keyHeight;
                    aHakken[hkCnt].y -= (keyHeight / 2);
                    aHakken[hkCnt].midiNo = midiNo;
                    hkCnt++;
                    break;
                case 4:
                case 11:
                    aHakken[hkCnt] = new WhiteKeyParts();
                    aHakken[hkCnt].x = FIXED_TICK_POS - hkWidth;
                    aHakken[hkCnt].y = hitEffectPosY[i];
                    aHakken[hkCnt].width = hkWidth;
                    aHakken[hkCnt].height = hakkenHeight;
                    aHakken[hkCnt].orgX = FIXED_TICK_POS - hkWidth;
                    aHakken[hkCnt].orgY = hitEffectPosY[i];
                    aHakken[hkCnt].orgWidth = hkWidth;
                    aHakken[hkCnt].orgHeight = keyHeight;
                    aHakken[hkCnt].midiNo = midiNo;
                    hkCnt++;
                    break;
                case 1:
                case 3:
                case 6:
                case 8:
                case 10:
                    aKokken[kkCnt] = new BlackKeyParts();
                    aKokken[kkCnt].x = FIXED_TICK_POS - kkWidth;
                    aKokken[kkCnt].y = hitEffectPosY[i];
                    aKokken[kkCnt].width = kkWidth;
                    aKokken[kkCnt].height = keyHeight;
                    aKokken[kkCnt].orgX = FIXED_TICK_POS - kkWidth;
                    aKokken[kkCnt].orgY = hitEffectPosY[i];
                    aKokken[kkCnt].orgWidth = kkWidth;
                    aKokken[kkCnt].orgHeight = keyHeight;
                    aKokken[kkCnt].midiNo = midiNo;
                    kkCnt++;
                    break;
                default:
                    break;
            }
        }
    }

	public boolean isCommit() {
		return isCommit;
	}
}

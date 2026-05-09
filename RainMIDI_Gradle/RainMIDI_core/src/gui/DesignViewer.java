package gui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import jlib.core.ISystemManager;
import jlib.core.JMPCoreAccessor;
import layout.LayoutConfig;
import layout.LayoutManager;
import layout.parts.CollisionEffectPainter;
import layout.parts.KeyParts;
import layout.parts.KeyboardPainter;
import layout.parts.KeyboardPainter.KindOfKey;
import layout.parts.NotesPainter;
import layout.parts.key.BlackKeyParts;
import layout.parts.key.WhiteKeyParts;
import plg.AbstractRenderPlugin;
import plg.SystemProperties;
import plg.Utility;

public class DesignViewer extends JDialog {
	
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
	public DesignViewer() {
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
				
				if (comboBoxLayoutFile.getSelectedIndex() == 0) {
					LayoutManager.getInstance().initializeConfig();
					
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
				
				panelViewer.repaint();
			}
		});
		comboBoxLayoutFile.setBounds(12, 10, 165, 21);
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
		        int bdHeight = height / 4;
		        for (int i=1; i <= 3; i++) {
		        	g.drawLine(50, bdHeight * i, width, bdHeight * i);
		        }
		        
		        NotesPainter ntPainter = lm.getNotesPainter();
		        NotesPainter.Context nContext = ntPainter.newContext();
		        int measCellHeight = panelViewer.getHeight() / 12;// 5;
		        
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
		        
		        KeyboardPainter keyboardPainter = lm.getKeyboardPainter(SystemProperties.SyspViewMode.SIDE_FLOW);
		        int tickBarPositionOffs = 0;
		        if (LayoutManager.getInstance().getCursorType() == LayoutConfig.ECursorType.Keyboard) {
		            tickBarPositionOffs = 3;
		        }
		        
		        if (LayoutManager.getInstance().getCursorType() == LayoutConfig.ECursorType.Keyboard) {
		            /* White Keyboard */
		            Color keyBgColor;
		            boolean isPush = false;
		            keyboardPainter.setKeyboardWidth(height);
		            for (int i = 0; i < aHakken.length; i++) {
		                keyBgColor = Color.WHITE;
		                keyboardPainter.paintKeyparts(g2d, aHakken[i], keyBgColor, Color.LIGHT_GRAY, isPush, KindOfKey.WHITE);
		            }
		        }

		        if (LayoutManager.getInstance().getCursorType() == LayoutConfig.ECursorType.Keyboard) {
		            /* Black Keyboard */
		            Color keyBgColor;
		            boolean isPush = false;
		            keyboardPainter.setKeyboardWidth(height);
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
		        Color csrColor = LayoutManager.getInstance().getCursorColor().getBdColor();
		        drawNoEffeLine(g2d, tickBarPosition + tickBarPositionOffs, 0, tickBarPosition + tickBarPositionOffs, panelViewer.getHeight(), csrColor);
		        
		        /* Tickbarグローエフェクト描画 */
		        if (LayoutManager.getInstance().isVisibleCursorEffect() == true) {
		            //drawGlowingLine(g2d, tickBarPosition + tickBarPositionOffs, 0, tickBarPosition + tickBarPositionOffs, getOrgHeight(), csrColor);
		            paintGrawLine(g, tickBarPosition + tickBarPositionOffs, 0, tickBarPosition + tickBarPositionOffs, panelViewer.getHeight(), csrColor);
		        }
		    }
		};
		panelViewer.setBackground(new Color(0, 0, 0));
		panelViewer.setBounds(12, 41, 410, 179);
		contentPanel.add(panelViewer);
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
	
	public void openDialog(String selectedName) {
		isCommit = false;
		makeKeyboardRsrc();
		
		initialized.set(false);
		comboBoxLayoutFile.removeAllItems();
		comboBoxLayoutFile.addItem("RainMIDI-Default");
		
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
        
        if (selectedName.isEmpty()) {
        	comboBoxLayoutFile.setSelectedIndex(0);
        }
        else {
        	comboBoxLayoutFile.setSelectedItem(selectedName);
        }
        initialized.set(true);
        
        LayoutManager.getInstance().initialize(null);
        setVisible(true);
	}

    protected void makeKeyboardRsrc() {
        hitEffectPosY = new int[13];
        int measCellHeight = panelViewer.getHeight() / 12;// 5;
        int keyHeight = measCellHeight;
        int keyCount = 0;
        int topOffset = (keyHeight * keyCount);
        for (int i = 0; i < 13; i++) {
            hitEffectPosY[i] = topOffset + (keyHeight * i);
        }

        aHakken = new KeyParts[8];
        aKokken = new KeyParts[5];

        int kkCnt = 0;
        int hkCnt = 0;
        int hkWidth = FIXED_TICK_POS;
        int kkWidth = (int) (hkWidth * 0.7);
        int hakkenHeight = panelViewer.getHeight() / 7;
        for (int i = 0; i < 13; i++) {
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
    
    private static final Stroke DEFAULT_STROKE = new BasicStroke();
    private static final float NO_EFFE_LINE_CORE_STROKE_VAL = 8.0f;     // 中心の線の太さ
    private static final float NO_EFFE_LINE_BORDER_WIDTH = 0.5f;        // 白ボーダーの幅
    private static final BasicStroke NO_EFFE_LINE_BORDER_STROKE = new BasicStroke(
            NO_EFFE_LINE_CORE_STROKE_VAL + NO_EFFE_LINE_BORDER_WIDTH * 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke NO_EFFE_LINE_CORE_STROKE = new BasicStroke(
            NO_EFFE_LINE_CORE_STROKE_VAL, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private void drawNoEffeLine(Graphics2D g2d, int x1, int y1, int x2, int y2, Color baseColor) {
        if (LayoutManager.getInstance().isVisibleCursorEffect() == false) {
            // ======= ボーダー線 =======
            g2d.setStroke(NO_EFFE_LINE_BORDER_STROKE);

            g2d.setColor(Color.BLACK);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // ======= 中心線（コア線） =======
        g2d.setStroke(NO_EFFE_LINE_CORE_STROKE);
        g2d.setColor(baseColor);
        g2d.drawLine(x1, y1, x2, y2);

        // ストロークを戻す
        g2d.setStroke(DEFAULT_STROKE);
    }
    
    private List<BasicStroke> ousStrokes = null;
    private List<Color> ousColors = null;
    private BasicStroke mainStroke = null;
    private Color mainColor = null;
    private List<BasicStroke> mergeStrokes = null;
    private List<Color> mergeColors = null;
    private BasicStroke coreStroke = null;
    private Color coreColor = null;
    protected void paintGrawLine(Graphics g, int x1, int y1, int x2, int y2, Color saberColor) {
        Graphics2D g2 = (Graphics2D) g;
    
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
    
        //Color saberColor = new Color(0, 200, 255); // 青
        mainColor = null;
        if (mainColor == null) {
            ousStrokes = new ArrayList<BasicStroke>();
            ousColors = new ArrayList<Color>();
            
            // ごく薄い外側の光（控えめ）
            for (int i = 8; i >= 1; i--) {
                float alpha = 0.01f * i;   // ← かなり薄い
                ousStrokes.add(new BasicStroke(12f + i * 2f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));
                
                ousColors.add(new Color(
                        saberColor.getRed(),
                        saberColor.getGreen(),
                        saberColor.getBlue(),
                        (int)(255 * alpha)));
            }
            
            // メインの色（白との境界用）
            mainStroke = new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            mainColor = new Color(saberColor.getRed(), saberColor.getGreen(), saberColor.getBlue(), 140);
            
            // 白→色のぼかしゾーン（最重要）
            mergeStrokes = new ArrayList<BasicStroke>();
            mergeColors = new ArrayList<Color>();
            for (int i = 3; i >= 1; i--) {
                float t = i / 3f; // 1.0 → 0.33
                int r = (int)(255 * (1 - t) + saberColor.getRed()   * t);
                int gC= (int)(255 * (1 - t) + saberColor.getGreen()* t);
                int b = (int)(255 * (1 - t) + saberColor.getBlue() * t);
        
                mergeStrokes.add(new BasicStroke(6f + i * 1.2f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));
                mergeColors.add(new Color(r, gC, b, 160));
            }
            
            // 中心の白い芯（太め）
            coreStroke = new BasicStroke(5.5f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND);
            coreColor = new Color(245, 250, 255);
        }
    
        // ごく薄い外側の光（控えめ）
        for (int j = 0; j < ousStrokes.size(); j++) {
            g2.setStroke(ousStrokes.get(j));
            g2.setColor(ousColors.get(j));
            g2.drawLine(x1, y1, x2, y2);
        }
    
        // メインの色（白との境界用）
        g2.setStroke(mainStroke);
        g2.setColor(mainColor);
        g2.drawLine(x1, y1, x2, y2);
    
        // 白→色のぼかしゾーン（最重要）
        for (int j = 0; j < mergeStrokes.size(); j++) {
            g2.setStroke(mergeStrokes.get(j));
            g2.setColor(mergeColors.get(j));
            g2.drawLine(x1, y1, x2, y2);
        }
    
        // 中心の白い芯（太め）
        g2.setStroke(coreStroke);
        g2.setColor(coreColor);
        g2.drawLine(x1, y1, x2, y2);
    }

	public boolean isCommit() {
		return isCommit;
	}
}

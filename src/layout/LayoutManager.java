package layout;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jlib.core.ISystemManager;
import jlib.core.JMPCoreAccessor;
import layout.LayoutConfig.EColorAsign;
import layout.LayoutConfig.EKeyboardDesign;
import layout.LayoutConfig.ENotesDesign;
import layout.parts.KeyboardPainter;
import layout.parts.NotesPainter;
import layout.parts.keyboard.DefaultKeyboardPainter;
import layout.parts.keyboard.SimpleKeyboardPainter;
import layout.parts.keyboard.SmartKeyboardPainter2;
import layout.parts.notes.ArcNotesPainter;
import layout.parts.notes.FlatNotesPainter;
import layout.parts.notes.FrameNotesPainter;
import layout.parts.notes.Normal3dNotesPainter;
import layout.parts.notes.NormalNotesPainter;
import plg.PropertiesNode;
import plg.SystemProperties;
import plg.SystemProperties.SyspViewMode;
import plg.Utility;

public class LayoutManager {
    public static final int DEFAULT_TICK_MEAS = 1;

    private static Map<LayoutConfig.ENotesDesign, NotesPainter> notesPainters = new HashMap<LayoutConfig.ENotesDesign, NotesPainter>() {
        {
            put(ENotesDesign.Normal, new NormalNotesPainter());
            put(ENotesDesign.Normal3D, new Normal3dNotesPainter());
            put(ENotesDesign.Flat, new FlatNotesPainter());
            put(ENotesDesign.Arc, new ArcNotesPainter());
            put(ENotesDesign.Frame, new FrameNotesPainter());
        }
    };

    private static Map<LayoutConfig.EKeyboardDesign, KeyboardPainter> kbPainters = new HashMap<LayoutConfig.EKeyboardDesign, KeyboardPainter>() {
        {
            put(EKeyboardDesign.Default, new DefaultKeyboardPainter());
            put(EKeyboardDesign.Simple, new SimpleKeyboardPainter());
            put(EKeyboardDesign.Smart, new SmartKeyboardPainter2());
        }
    };

    private List<ColorInfo> notesColorInfos = null;
    private ColorInfo cursorColor = null;
    private ColorInfo playerColor = null;
    private ColorInfo pbColor = null;

    private Canvas rootCanvas = null;
    
    private int bmpFormat = BufferedImage.TYPE_INT_RGB;
    
    private long volumeVisibleTime = -1;

    // 現在のレイアウト設定
    private LayoutConfig layout = new LayoutConfig();

    private static LayoutManager instance = new LayoutManager();

    private LayoutManager() {
    }

    public static LayoutManager getInstance() {
        return instance;
    }

    public List<PropertiesNode> getNodes() {
        return layout.getNodes();
    }

    public VolatileImage createDisplayImage(int width, int height) {
        GraphicsConfiguration gc = rootCanvas.getGraphicsConfiguration();
        return gc.createCompatibleVolatileImage(width, height, Transparency.OPAQUE);
    }

    public BufferedImage createBufferdImage(int width, int height) {
        return new BufferedImage(width, height, getBmpFormat());
    }

    public void initialize(Canvas canvas) {
        rootCanvas = canvas;

        List<Color> notesColor = new ArrayList<Color>();
        List<Color> notesBorderColor = new ArrayList<Color>();
        ISystemManager sm = JMPCoreAccessor.getSystemManager();

        EColorAsign colAsign = (EColorAsign) layout.getData(LayoutConfig.LC_NOTES_COLOR_ASIGN);
        int notesColorNum = (int) layout.getData(LayoutConfig.LC_NOTES_COLOR_NUM);
        if (colAsign == LayoutConfig.EColorAsign.Inherit || colAsign == LayoutConfig.EColorAsign.None) {
            for (int i = 0; i < notesColorNum; i++) {
                String key = String.format("ch_color_%d", (i + 1));
                notesColor.add(Utility.convertCodeToHtmlColor(sm.getCommonRegisterValue(key)));
            }
        }
        if (colAsign == LayoutConfig.EColorAsign.Inherit || colAsign == LayoutConfig.EColorAsign.Asign) {
            for (int i = 0; i < notesColorNum; i++) {
                String s = (String) layout.getData(LayoutConfig.LC_NOTES_COLOR + (i + 1));
                notesColor.add(Utility.convertCodeToHtmlColor(s));
            }
        }
        double borderOffset = (double) layout.getData(LayoutConfig.LC_NOTES_COLOR_BORDER_RGB);
        for (Color nc : notesColor) {
            int r = nc.getRed();
            int g = nc.getGreen();
            int b = nc.getBlue();
            int a = nc.getAlpha();
            r = (int) ((double) r * borderOffset);
            g = (int) ((double) g * borderOffset);
            b = (int) ((double) b * borderOffset);
            notesBorderColor.add(new Color(r > 255 ? 255 : r, g > 255 ? 255 : g, b > 255 ? 255 : b, a));
        }
        notesColorInfos = new ArrayList<ColorInfo>();
        for (int i = 0; i < notesColorNum; i++) {
            notesColorInfos.add(new ColorInfo(notesColor.get(i), notesBorderColor.get(i)));
        }
        
        cursorColor = new ColorInfo( //
                Utility.convertCodeToHtmlColor((String) layout.getData(LayoutConfig.LC_CURSOR_COLOR)), //
                Utility.convertCodeToHtmlColor((String) layout.getData(LayoutConfig.LC_CURSOR_COLOR)), //
                Utility.convertCodeToHtmlColor((String) layout.getData(LayoutConfig.LC_CURSOR_EFFE_COLOR))//
        );

        playerColor = new ColorInfo( //
                Utility.convertCodeToHtmlColor((String) layout.getData(LayoutConfig.LC_PLAYER_BGCOLOR)), //
                Utility.convertCodeToHtmlColor((String) layout.getData(LayoutConfig.LC_PLAYER_BDCOLOR)) //
        ); //

        pbColor = new ColorInfo(//
                Utility.convertCodeToHtmlColor((String) layout.getData(LayoutConfig.LC_PB_COLOR)), //
                Utility.convertCodeToHtmlColor((String) layout.getData(LayoutConfig.LC_PB_COLOR)), //
                Utility.convertCodeToHtmlColor((String) layout.getData(LayoutConfig.LC_PB_COLOR)) //
        ); //
    }

    public void initializeConfig() {
        layout.definication();
    }
    
    public void invalidateEffectConfig() {
        layout.invalidateEffectConfig();
    }

    public void read(File f) throws IOException {
        if (f.exists() == true) {
            layout.read(f);
        }
    }

    public ColorInfo getNotesColor(int index) {
        return notesColorInfos.get(index % notesColorInfos.size());
    }

    public LayoutConfig.ECursorType getCursorType() {
        return (LayoutConfig.ECursorType) layout.getData(LayoutConfig.LC_CURSOR_TYPE);
    }

    public LayoutConfig.EColorRule getColorRule() {
        return (LayoutConfig.EColorRule) layout.getData(LayoutConfig.LC_PLAYER_COLOR_RULE);
    }

    public ColorInfo getCursorColor() {
        return cursorColor;
    }

    public ColorInfo getPlayerColor() {
        return playerColor;
    }

    public ColorInfo getPitchbendColor() {
        return pbColor;
    }

    public boolean isVisibleHorizonBorder() {
        return (boolean) layout.getData(LayoutConfig.LC_PLAYER_BORDER_HORIZON_VISIBLE);
    }

    public boolean isVisibleVerticalBorder() {
        return (boolean) layout.getData(LayoutConfig.LC_PLAYER_BORDER_VERTICAL_VISIBLE);
    }

    public boolean isVisiblePbLine() {
        return (boolean) layout.getData(LayoutConfig.LC_PB_VISIBLE);
    }

    public boolean isVisibleCursorEffect() {
        return (boolean) layout.getData(LayoutConfig.LC_CURSOR_EFFE_VISIBLE);
    }

    public boolean isVisibleNotesInEffect() {
        return (boolean) layout.getData(LayoutConfig.LC_NOTES_HITEFFE_IN);
    }

    public boolean isVisibleNotesOutEffect() {
        return (boolean) layout.getData(LayoutConfig.LC_NOTES_HITEFFE_OUT);
    }

    public LayoutConfig.ENotesDesign getNotesDesign() {
        return (LayoutConfig.ENotesDesign) layout.getData(LayoutConfig.LC_NOTES_DESIGN);
    }

    public int getTickBarPosition() {
        int pos = (int) layout.getData(LayoutConfig.LC_CURSOR_POS);
        if (pos == -1) {
            pos = SystemProperties.getInstance().getKeyWidth();
        }
        return pos;
    }

    public NotesPainter getNotesPainter() {
        LayoutConfig.ENotesDesign notesDesign = (LayoutConfig.ENotesDesign) layout.getData(LayoutConfig.LC_NOTES_DESIGN);
        if (JMPCoreAccessor.getSoundManager().getMidiUnit().isRenderingOnlyMode() == true) {
            switch (notesDesign) {
                case Frame:
                    notesDesign = ENotesDesign.Normal;
                    break;
                default:
                    break;
            }
        }
        return notesPainters.get(notesDesign);
    }

    public KeyboardPainter getKeyboardPainter(SystemProperties.SyspViewMode mode) {
        LayoutConfig.EKeyboardDesign kbDesign = (LayoutConfig.EKeyboardDesign) layout.getData(LayoutConfig.LC_KEYBOARD_DESIGN);
        if (mode == SyspViewMode.SIDE_FLOW) {
            switch (kbDesign) {
                case LayoutConfig.EKeyboardDesign.Default:
                    // SideFlowはDefault非対応 
                    kbDesign = LayoutConfig.EKeyboardDesign.Simple;
                    break;
                default:
                    break;
            }
        }
        return kbPainters.get(kbDesign);
    }

    public void setNotesBounds(int width, int height) {
        for (int i = 0; i < notesColorInfos.size(); i++) {
            notesColorInfos.get(i).createGrad(height);
        }
    }

    public int getNotesColorSize() {
        return notesColorInfos.size();
    }

    public Color rgbToNotesColor(int rgb, Color defaultColor) {
        if (rgb != 0) {
            for (int i = 0; i < notesColorInfos.size(); i++) {
                if (notesColorInfos.get(i).getBgColor().getRGB() == rgb) {
                    return notesColorInfos.get(i).getBgColor();
                }
            }
        }
        return defaultColor;
    }

    public int getBmpFormat() {
        return bmpFormat;
    }

    public void setBmpFormat(int bmpFormat) {
        this.bmpFormat = bmpFormat;
    }
    
    public void setVolumeVisibleTime() {
        volumeVisibleTime = System.currentTimeMillis();
    }
    
    public void clearVolumeVisibleTime() {
        volumeVisibleTime = -1;
    }
    
    public long getVolumeVisibleTime() {
        return volumeVisibleTime;
    }
}

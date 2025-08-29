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
import layout.parts.ArcNotesPainter;
import layout.parts.DefaultKeyboardPainter;
import layout.parts.FlatNotesPainter;
import layout.parts.FrameNotesPainter;
import layout.parts.KeyboardPainter;
import layout.parts.NormalNotesPainter;
import layout.parts.NotesPainter;
import layout.parts.SimpleKeyboardPainter;
import plg.PropertiesNode;
import plg.SystemProperties;
import plg.SystemProperties.SyspViewMode;
import plg.Utility;

public class LayoutManager {
    public static final int DEFAULT_TICK_MEAS = 1;
    
    private static Map<LayoutConfig.ENotesDesign, NotesPainter> notesPainters = new HashMap<LayoutConfig.ENotesDesign, NotesPainter>() {
        {
            put(ENotesDesign.Normal, new NormalNotesPainter());
            put(ENotesDesign.Flat, new FlatNotesPainter());
            put(ENotesDesign.Arc, new ArcNotesPainter());
            put(ENotesDesign.Frame, new FrameNotesPainter());
        }
    };
    
    private static Map<LayoutConfig.EKeyboardDesign, KeyboardPainter> kbPainters = new HashMap<LayoutConfig.EKeyboardDesign, KeyboardPainter>() {
        {
            put(EKeyboardDesign.Default, new DefaultKeyboardPainter());
            put(EKeyboardDesign.Simple, new SimpleKeyboardPainter());
        }
    };

    private List<Color> notesColor = null;
    private List<Color> notesBorderColor = null;
    private Color cursorColor = null;
    private Color cursorEffeColor = null;

    private Color bgColor = null;
    private Color bdColor = null;
    private Color pbColor = null;

    private Color bgColorReverse = null;

    private Canvas rootCanvas = null;

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

    public VolatileImage createLayerImage(int width, int height) {
        GraphicsConfiguration gc = rootCanvas.getGraphicsConfiguration();
        return gc.createCompatibleVolatileImage(width, height, Transparency.OPAQUE);
    }

    public BufferedImage createBufferdImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public void initialize(Canvas canvas) {
        rootCanvas = canvas;

        notesColor = new ArrayList<Color>();
        notesBorderColor = new ArrayList<Color>();
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

        cursorColor = Utility.convertCodeToHtmlColor((String) layout.getData(LayoutConfig.LC_CURSOR_COLOR));
        cursorEffeColor = Utility.convertCodeToHtmlColor((String) layout.getData(LayoutConfig.LC_CURSOR_EFFE_COLOR));

        bgColor = Utility.convertCodeToHtmlColor((String) layout.getData(LayoutConfig.LC_PLAYER_BGCOLOR));
        bdColor = Utility.convertCodeToHtmlColor((String) layout.getData(LayoutConfig.LC_PLAYER_BDCOLOR));
        pbColor = Utility.convertCodeToHtmlColor((String) layout.getData(LayoutConfig.LC_PB_COLOR));
        bgColorReverse = ((bgColor.getRed() + bgColor.getGreen() + bgColor.getBlue()) / 3) >= 128 ? Color.BLACK : Color.WHITE;
    }
    
    public void initializeConfig() {
        layout.definication();
    }

    public void read(File f) throws IOException {
        if (f.exists() == true) {
            layout.read(f);
        }
    }

    public Color getNotesColor(int index) {
        return notesColor.get(index % notesColor.size());
    }

    public Color getNotesBorderColor(int index) {
        return notesBorderColor.get(index % notesBorderColor.size());
    }

    public List<Color> getNotesBorderColors() {
        return notesBorderColor;
    }

    public LayoutConfig.ECursorType getCursorType() {
        return (LayoutConfig.ECursorType) layout.getData(LayoutConfig.LC_CURSOR_TYPE);
    }

    public LayoutConfig.EColorRule getColorRule() {
        return (LayoutConfig.EColorRule) layout.getData(LayoutConfig.LC_PLAYER_COLOR_RULE);
    }

    public Color getCursorColor() {
        return cursorColor;
    }

    public Color getCursorEffectColor() {
        return cursorEffeColor;
    }

    public Color getBackColor() {
        return bgColor;
    }

    public Color getBorderColor() {
        return bdColor;
    }

    public Color getPitchbendColor() {
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
            return notesPainters.get(LayoutConfig.ENotesDesign.Normal);
        }
        return notesPainters.get(notesDesign);
    }

    public Color getBgColorReverse() {
        return bgColorReverse;
    }
    
    public KeyboardPainter getKeyboardPainter(SystemProperties.SyspViewMode mode) {
        LayoutConfig.EKeyboardDesign kbDesign = (LayoutConfig.EKeyboardDesign) layout.getData(LayoutConfig.LC_KEYBOARD_DESIGN);
        if (mode == SyspViewMode.SIDE_FLOW) {
            // SideFlowはSimpleのみ対応 
            return kbPainters.get(LayoutConfig.EKeyboardDesign.Simple);
        }
        return kbPainters.get(kbDesign);
    }

}

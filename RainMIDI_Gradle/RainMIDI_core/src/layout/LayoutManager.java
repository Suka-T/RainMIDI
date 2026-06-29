package layout;

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
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
import layout.LayoutConfig.EColEffect;
import layout.LayoutConfig.EColorAsign;
import layout.LayoutConfig.EKeyboardDesign;
import layout.LayoutConfig.ENotesDesign;
import layout.LayoutConfig.ETickbarDesign;
import layout.parts.CollisionEffectPainter;
import layout.parts.KeyboardPainter;
import layout.parts.NotesPainter;
import layout.parts.TickbarPainter;
import layout.parts.collisionEffect.ColorCollisionEffectPainter;
import layout.parts.collisionEffect.NoneCollisionEffectPainter;
import layout.parts.collisionEffect.SimpleCollisionEffectPainter;
import layout.parts.keyboard.DefaultKeyboardPainter;
import layout.parts.keyboard.SimpleKeyboardPainter;
import layout.parts.keyboard.SmartKeyboardPainter;
import layout.parts.notes.ArcNotesPainter;
import layout.parts.notes.FlatNotesPainter;
import layout.parts.notes.FrameNotesPainter;
import layout.parts.notes.Normal3dNotesPainter;
import layout.parts.notes.NormalNotesPainter;
import layout.parts.tickbar.GlowTickbarPainter;
import layout.parts.tickbar.NormalTickbarPainter;
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
            put(EKeyboardDesign.Smart, new SmartKeyboardPainter());
        }
    };

    private static Map<LayoutConfig.EColEffect, CollisionEffectPainter> cePainters = new HashMap<LayoutConfig.EColEffect, CollisionEffectPainter>() {
        {
            put(EColEffect.None, new NoneCollisionEffectPainter());
            put(EColEffect.Simple, new SimpleCollisionEffectPainter());
            put(EColEffect.Color, new ColorCollisionEffectPainter());
        }
    };

    private static Map<LayoutConfig.ETickbarDesign, TickbarPainter> tickBarPainters = new HashMap<LayoutConfig.ETickbarDesign, TickbarPainter>() {
        {
            put(ETickbarDesign.Normal, new NormalTickbarPainter());
            put(ETickbarDesign.Glow, new GlowTickbarPainter());
        }
    };

    private List<ColorInfo> notesColorInfos = null;
    private ColorInfo cursorColor = null;
    private ColorInfo playerColor = null;
    private ColorInfo playerImplColor = null;
    private ColorInfo playerTransColor = null;
    private ColorInfo pbColor = null;
    private ColorInfo fontColor = null;

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
        
        int tp = Transparency.OPAQUE;
        switch (getBmpFormat()) {
        case BufferedImage.TYPE_INT_ARGB:
            tp = Transparency.TRANSLUCENT;
            break;
        default:
            break;
        }
        return gc.createCompatibleVolatileImage(width, height, tp);
    }
    
    public void clearVolatileImage(VolatileImage vImage) {
        Graphics2D g2d = vImage.createGraphics();
        try {
            // 合成規則を「元の色を無視して上書き（Src）」に設定
            g2d.setComposite(AlphaComposite.Src);
            
            // 透明な黒（アルファ値 0）で全体を塗りつぶす
            g2d.setColor(new Color(0, 0, 0, 0));
            g2d.fillRect(0, 0, vImage.getWidth(), vImage.getHeight());
        } finally {
            g2d.dispose(); // グラフィックスリソースを確実に解放
        }
    }
    
    public void clearBufferedImage(BufferedImage bImage) {
        // 念のため、画像が透過（アルファチャンネル）をサポートしているかチェック
        if (!bImage.getColorModel().hasAlpha()) {
            System.err.println("警告: このBufferedImageは透過に対応していません。");
            // TYPE_INT_ARGB などで作成されている必要があります
        }

        Graphics2D g2d = bImage.createGraphics();
        try {
            // 合成規則を「元の色を無視して上書き（Src）」に設定
            g2d.setComposite(AlphaComposite.Src);
            
            // 透明な黒（アルファ値 0）で全体を塗りつぶす
            g2d.setColor(new Color(0, 0, 0, 0));
            g2d.fillRect(0, 0, bImage.getWidth(), bImage.getHeight());
        } finally {
            g2d.dispose(); // グラフィックスリソースを確実に解放
        }
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

        playerTransColor = new ColorInfo( //
                new Color(0, 0, 0, 0), //
                new Color(0, 0, 0, 0) //
        ); //
        
        playerImplColor = new ColorInfo( //
                Utility.convertCodeToHtmlColor((String) layout.getData(LayoutConfig.LC_PLAYER_BGCOLOR)), //
                Utility.convertCodeToHtmlColor((String) layout.getData(LayoutConfig.LC_PLAYER_BDCOLOR)) //
        ); //
        
        if (SystemProperties.getInstance().getCustomBgImage() != null) {
            playerColor = playerTransColor;
        }
        else {
            playerColor = playerImplColor;
        }

        pbColor = new ColorInfo(//
                Utility.convertCodeToHtmlColor((String) layout.getData(LayoutConfig.LC_PB_COLOR)), //
                Utility.convertCodeToHtmlColor((String) layout.getData(LayoutConfig.LC_PB_COLOR)), //
                Utility.convertCodeToHtmlColor((String) layout.getData(LayoutConfig.LC_PB_COLOR)) //
        ); //
        
        fontColor = new ColorInfo(playerImplColor.getBgRevColor(), playerImplColor.getBgColor());
    }

    public void initializeConfig() {
        layout.definication();
    }

    public void initializeClassic() {
        // Classic Design
        layout.definication();
        layout.setData(LayoutConfig.LC_NOTES_DESIGN, "normal3d");
        layout.setData(LayoutConfig.LC_KEYBOARD_DESIGN, "default");
        layout.setData(LayoutConfig.LC_COLLISION_EFFECT_IN, "simple");
        layout.setData(LayoutConfig.LC_COLLISION_EFFECT_OUT, "none");
        layout.setData(LayoutConfig.LC_CURSOR_LINE, "normal");
    }

    public void initializeConfigLight() {
        initializeClassic();
    }

    public void invalidateEffectConfig() {
        layout.invalidateEffectConfig();
    }

    public void read(File f) throws IOException {
        if (f.exists() == true) {
            layout.definication(); // 差分以外をデフォルトにする
            layout.read(f);
        }
    }

    public void write(File f) throws IOException {
        layout.write(f);
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

    public CollisionEffectPainter getCollisionEffectPainterIn() {
        LayoutConfig.EColEffect effe = (LayoutConfig.EColEffect) layout.getData(LayoutConfig.LC_COLLISION_EFFECT_IN);
        return cePainters.get(effe);
    }

    public CollisionEffectPainter getCollisionEffectPainterOut() {
        LayoutConfig.EColEffect effe = (LayoutConfig.EColEffect) layout.getData(LayoutConfig.LC_COLLISION_EFFECT_OUT);
        return cePainters.get(effe);
    }

    public TickbarPainter getTickbarPainter() {
        LayoutConfig.ETickbarDesign type = (LayoutConfig.ETickbarDesign) layout.getData(LayoutConfig.LC_CURSOR_LINE);
        return tickBarPainters.get(type);
    }

    public ColorInfo getFontColor() {
        return fontColor;
    }
}

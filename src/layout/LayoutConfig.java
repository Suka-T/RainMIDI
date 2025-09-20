package layout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import plg.PropertiesNode;
import plg.PropertiesNode.PropertiesNodeType;

public class LayoutConfig {

    public static final String LC_PLAYER_BGCOLOR = "player.bgcolor";
    public static final String LC_PLAYER_BDCOLOR = "player.bdcolor";
    public static final String LC_PLAYER_COLOR_RULE = "player.colorrule";
    public static final String LC_PLAYER_BORDER_VERTICAL_VISIBLE = "player.vborder.visible";
    public static final String LC_PLAYER_BORDER_HORIZON_VISIBLE = "player.hborder.visible";
    public static final String LC_CURSOR_TYPE = "cursor.type";
    public static final String LC_CURSOR_COLOR = "cursor.color";
    public static final String LC_CURSOR_EFFE_COLOR = "cursor.effect.color";
    public static final String LC_CURSOR_EFFE_VISIBLE = "cursor.effect.visible";
    public static final String LC_CURSOR_POS = "cursor.position";
    public static final String LC_PB_COLOR = "pb.basecolor";
    public static final String LC_PB_VISIBLE = "pb.visible";
    public static final String LC_NOTES_DESIGN = "notes.design";
    public static final String LC_NOTES_COLOR_ASIGN = "notes.colasign";
    public static final String LC_NOTES_COLOR_NUM = "notes.colorNum";
    public static final String LC_NOTES_COLOR = "notes.color";
    public static final String LC_NOTES_COLOR_BORDER_RGB = "notes.border.colorRGB";
    public static final String LC_NOTES_HITEFFE_IN = "notes.effect.in.visible";
    public static final String LC_NOTES_HITEFFE_OUT = "notes.effect.out.visible";
    public static final String LC_KEYBOARD_DESIGN = "keyboard.design";
    
    public static final Map<String, String> SwapKeyName = new HashMap<String, String>() {
        {
            put(LC_PLAYER_BGCOLOR, "Renderer background color");
            put(LC_PLAYER_BDCOLOR, "Renderer border color");
            put(LC_PLAYER_COLOR_RULE, "Color function [ Track or Channel ]");
            put(LC_PLAYER_BORDER_VERTICAL_VISIBLE, "Vertical border visible");
            put(LC_PLAYER_BORDER_HORIZON_VISIBLE, "Horizon border visible");
            put(LC_CURSOR_TYPE, "View Keyboard or Line");
            put(LC_CURSOR_COLOR, "Hit Line color");
            put(LC_CURSOR_EFFE_COLOR, "Hit Line effect color");
            put(LC_CURSOR_EFFE_VISIBLE, "Hit Line effect visible");
            put(LC_NOTES_DESIGN, "Notes design");
            put(LC_NOTES_COLOR_NUM, "Use notes color count");
            put(LC_NOTES_COLOR + "1", "Track1 notes color");
            put(LC_NOTES_COLOR + "2", "Track2 notes color");
            put(LC_NOTES_COLOR + "3", "Track3 notes color");
            put(LC_NOTES_COLOR + "4", "Track4 notes color");
            put(LC_NOTES_COLOR + "5", "Track5 notes color");
            put(LC_NOTES_COLOR + "6", "Track6 notes color");
            put(LC_NOTES_COLOR + "7", "Track7 notes color");
            put(LC_NOTES_COLOR + "8", "Track8 notes color");
            put(LC_NOTES_COLOR + "9", "Track9 notes color");
            put(LC_NOTES_COLOR + "10", "Track10 notes color");
            put(LC_NOTES_COLOR + "11", "Track11 notes color");
            put(LC_NOTES_COLOR + "12", "Track12 notes color");
            put(LC_NOTES_COLOR + "13", "Track13 notes color");
            put(LC_NOTES_COLOR + "14", "Track14 notes color");
            put(LC_NOTES_COLOR + "15", "Track15 notes color");
            put(LC_NOTES_COLOR + "16", "Track16 notes color");
            put(LC_NOTES_COLOR_BORDER_RGB, "Notes border color hilight");
            put(LC_NOTES_HITEFFE_IN, "Notes line effect of in");
            put(LC_NOTES_HITEFFE_OUT, "Notes line effect of out");
            put(LC_KEYBOARD_DESIGN, "Keyboard design");
        }
    };

    public static enum ENotesDesign {
        Normal, Normal3D, Flat, Arc, Frame;
    }
    private static Object[] ENotesDesignO = { ENotesDesign.Normal, ENotesDesign.Normal3D, ENotesDesign.Flat, ENotesDesign.Arc, ENotesDesign.Frame };
    private static String[] ENotesDesignS = { "normal", "normal3d", "flat", "arc", "frame" };

    public static enum ECursorType {
        Keyboard, Line;
    }
    private static Object[] ECursorTypeO = { ECursorType.Keyboard, ECursorType.Line };
    private static String[] ECursorTypeS = { "keyboard", "line" };

    public static enum EColorRule {
        Channel, Track;
    }
    private static Object[] EColorRuleO = { EColorRule.Channel, EColorRule.Track };
    private static String[] EColorRuleS = { "channel", "track" };

    public static enum EColorAsign {
        Inherit, Asign, None;
    }
    private static Object[] EColorAsignO = { EColorAsign.Inherit, EColorAsign.Asign, EColorAsign.None };
    private static String[] EColorAsignS = { "inherit", "asign", "none" };

    private static Object[] CursorPosO = { -1 };
    private static String[] CursorPosS = { "top" };
    
    public static enum EKeyboardDesign {
        Default, Simple, Smart;
    }
    private static Object[] EKeyboardDesignO = { EKeyboardDesign.Default, EKeyboardDesign.Simple, EKeyboardDesign.Smart };
    private static String[] EKeyboardDesignS = { "default", "simple", "smart" };

    private List<PropertiesNode> nodes;

    public LayoutConfig() {
        nodes = new ArrayList<>();
        nodes.add(new PropertiesNode(LC_PLAYER_BGCOLOR, PropertiesNodeType.COLOR, "#000000"));
        nodes.add(new PropertiesNode(LC_PLAYER_BDCOLOR, PropertiesNodeType.COLOR, "#202020"));
        nodes.add(new PropertiesNode(LC_PLAYER_COLOR_RULE, PropertiesNodeType.ITEM, EColorRule.Track, EColorRuleS, EColorRuleO));
        nodes.add(new PropertiesNode(LC_PLAYER_BORDER_VERTICAL_VISIBLE, PropertiesNodeType.BOOLEAN, "true"));
        nodes.add(new PropertiesNode(LC_PLAYER_BORDER_HORIZON_VISIBLE, PropertiesNodeType.BOOLEAN, "true"));
        nodes.add(new PropertiesNode(LC_CURSOR_TYPE, PropertiesNodeType.ITEM, ECursorType.Keyboard, ECursorTypeS, ECursorTypeO));
        nodes.add(new PropertiesNode(LC_CURSOR_COLOR, PropertiesNodeType.COLOR, "#7FFFD4"));
        nodes.add(new PropertiesNode(LC_CURSOR_EFFE_COLOR, PropertiesNodeType.COLOR, "#FFFFFF"));
        nodes.add(new PropertiesNode(LC_CURSOR_EFFE_VISIBLE, PropertiesNodeType.BOOLEAN, "true"));
        nodes.add(new PropertiesNode(LC_CURSOR_POS, PropertiesNodeType.INT, "-1", "", "", CursorPosS, CursorPosO));
        nodes.add(new PropertiesNode(LC_PB_COLOR, PropertiesNodeType.COLOR, "#969696"));
        nodes.add(new PropertiesNode(LC_PB_VISIBLE, PropertiesNodeType.BOOLEAN, "false"));
        nodes.add(new PropertiesNode(LC_NOTES_DESIGN, PropertiesNodeType.ITEM, ENotesDesign.Normal3D, ENotesDesignS, ENotesDesignO));
        nodes.add(new PropertiesNode(LC_NOTES_COLOR_ASIGN, PropertiesNodeType.ITEM, EColorAsign.Asign, EColorAsignS, EColorAsignO));
        nodes.add(new PropertiesNode(LC_NOTES_COLOR_NUM, PropertiesNodeType.INT, "10", "1", "16"));
        nodes.add(new PropertiesNode(LC_NOTES_COLOR + "1", PropertiesNodeType.COLOR, "#00E6A8")); // エメラルドグリーン
        nodes.add(new PropertiesNode(LC_NOTES_COLOR + "2", PropertiesNodeType.COLOR, "#00C957")); // クリスマスグリーン
        nodes.add(new PropertiesNode(LC_NOTES_COLOR + "3", PropertiesNodeType.COLOR, "#FFD700")); // ゴールド
        nodes.add(new PropertiesNode(LC_NOTES_COLOR + "4", PropertiesNodeType.COLOR, "#87CEFA")); // ライトブルー
        nodes.add(new PropertiesNode(LC_NOTES_COLOR + "5", PropertiesNodeType.COLOR, "#FF69B4")); // ピンク
        nodes.add(new PropertiesNode(LC_NOTES_COLOR + "6", PropertiesNodeType.COLOR, "#8A2BE2")); // ビビッドパープル
        nodes.add(new PropertiesNode(LC_NOTES_COLOR + "7", PropertiesNodeType.COLOR, "#FF4B4B")); // クリスマスレッド
        nodes.add(new PropertiesNode(LC_NOTES_COLOR + "8", PropertiesNodeType.COLOR, "#FF9E5E")); // 温かいオレンジ
        nodes.add(new PropertiesNode(LC_NOTES_COLOR + "9", PropertiesNodeType.COLOR, "#4BCFFF")); // スカイブルー
        nodes.add(new PropertiesNode(LC_NOTES_COLOR + "10", PropertiesNodeType.COLOR, "#B54BFF")); // ビビッドパープル2
        nodes.add(new PropertiesNode(LC_NOTES_COLOR + "11", PropertiesNodeType.COLOR, "#ffffff"));
        nodes.add(new PropertiesNode(LC_NOTES_COLOR + "12", PropertiesNodeType.COLOR, "#ffffff"));
        nodes.add(new PropertiesNode(LC_NOTES_COLOR + "13", PropertiesNodeType.COLOR, "#ffffff"));
        nodes.add(new PropertiesNode(LC_NOTES_COLOR + "14", PropertiesNodeType.COLOR, "#ffffff"));
        nodes.add(new PropertiesNode(LC_NOTES_COLOR + "15", PropertiesNodeType.COLOR, "#ffffff"));
        nodes.add(new PropertiesNode(LC_NOTES_COLOR + "16", PropertiesNodeType.COLOR, "#ffffff"));
        nodes.add(new PropertiesNode(LC_NOTES_COLOR_BORDER_RGB, PropertiesNodeType.DOUBLE, "0.2", "0.1", "2.0"));
        nodes.add(new PropertiesNode(LC_NOTES_HITEFFE_IN, PropertiesNodeType.BOOLEAN, "true"));
        nodes.add(new PropertiesNode(LC_NOTES_HITEFFE_OUT, PropertiesNodeType.BOOLEAN, "false"));
        nodes.add(new PropertiesNode(LC_KEYBOARD_DESIGN, PropertiesNodeType.ITEM, EKeyboardDesign.Smart, EKeyboardDesignS, EKeyboardDesignO));
        definication();
    }
    
    public void definication() {
        // ロードする文字列
        String propertiesString = "";
        Properties props = new Properties();
        try (StringReader reader = new StringReader(propertiesString)) {
            props.load(reader);
            read(props);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public List<PropertiesNode> getNodes() {
        return nodes;
    }
    
    private PropertiesNode getPropNode(String key) {
        for (PropertiesNode nd : nodes) {
            if (nd.getKey().equalsIgnoreCase(key)) {
                return nd;
            }
        }
        return null;
    }

    private void setPropObject(Properties props, String key) {
        String str = props.getProperty(key);
        PropertiesNode node = getPropNode(key);
        node.setObject(str);
    }
    
    public Object getData(String key) {
        PropertiesNode node = getPropNode(key);
        if (node == null) {
            return null;
        }
        return node.getData();
    }

    public void read(File file) throws FileNotFoundException, IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(file));
        read(props);
    }

    public void read(Properties props) throws FileNotFoundException, IOException {
        for (PropertiesNode nd : nodes) {
            setPropObject(props, nd.getKey());
        }
    }
    
    public void initialize() {
        
    }
}

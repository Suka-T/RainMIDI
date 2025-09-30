package plg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jlib.core.IDataManager;
import jlib.core.ISoundManager;
import jlib.core.ISystemManager;
import jlib.core.JMPCoreAccessor;
import jlib.midi.IMidiUnit;
import plg.PropertiesNode.PropertiesNodeType;

public class SystemProperties {
    public static final String SYSP_FILE_LAYOUT = "file.layout";
    public static final String SYSP_FILE_DEFAULT_PATH = "file.defaultPath";
    public static final String SYSP_AUDIO_SYNTH = "audio.synth";
    public static final String SYSP_RENDERER_MODE = "renderer.mode";
    public static final String SYSP_RENDERER_WORKNUM = "renderer.workerNum";
    public static final String SYSP_RENDERER_FPS = "renderer.fps";
    public static final String SYSP_RENDERER_KEY_FOCUS_FUNC = "renderer.keyFocusFunc";
    public static final String SYSP_RENDERER_LAYERORDER = "renderer.layerOrder";
    public static final String SYSP_RENDERER_NOTESSPEED = "renderer.notesSpeed";
    public static final String SYSP_RENDERER_NOTESIMAGENUM = "renderer.notesImageNum";
    public static final String SYSP_RENDERER_DIMENSION = "renderer.dimension";
    public static final String SYSP_RENDERER_WINSIZE = "renderer.windowSize";
    public static final String SYSP_RENDERER_MONITOR_TYPE = "renderer.monitorType";
    public static final String SYSP_RENDERER_WINEFFECT = "renderer.windowEffect";
    public static final String SYSP_RENDERER_IGNORENOTES_AUDIO_VALID = "renderer.ignoreNotes.audio.valid";
    public static final String SYSP_RENDERER_IGNORENOTES_AUDIO_LOWEST = "renderer.ignoreNotes.audio.lowestVel";
    public static final String SYSP_RENDERER_IGNORENOTES_AUDIO_HIGHEST = "renderer.ignoreNotes.audio.highestVel";
    public static final String SYSP_RENDERER_IGNORENOTES_RENDER_VALID = "renderer.ignoreNotes.render.valid";
    public static final String SYSP_RENDERER_IGNORENOTES_RENDER_LOWEST = "renderer.ignoreNotes.render.lowestVel";
    public static final String SYSP_RENDERER_IGNORENOTES_RENDER_HIGHEST = "renderer.ignoreNotes.render.highestVel";
    public static final String SYSP_DEBUGMODE = "debugMode";

    public static final Map<String, String> SwapKeyName = new HashMap<String, String>() {
        {
            put(SYSP_FILE_LAYOUT, "Preload Layout file name");
            put(SYSP_FILE_DEFAULT_PATH, "Default folder");
            put(SYSP_AUDIO_SYNTH, "MIDI Systhesizer device name");
            put(SYSP_RENDERER_MODE, "Renderer view mode");
            put(SYSP_RENDERER_WORKNUM, "Rendering thread count [2 - 8]");
            put(SYSP_RENDERER_FPS, "Fixed frame rate");
            put(SYSP_RENDERER_KEY_FOCUS_FUNC, "Key Focus Function");
            put(SYSP_RENDERER_LAYERORDER, "Track rendering order");
            put(SYSP_RENDERER_NOTESSPEED, "Notes Speed [1 - 100 | auto]");
            put(SYSP_RENDERER_NOTESIMAGENUM, "Rendering notes image size [3 - 300]");
            put(SYSP_RENDERER_DIMENSION, "Renderer dimension");
            put(SYSP_RENDERER_WINSIZE, "Window size");
            put(SYSP_RENDERER_MONITOR_TYPE, "Monitor view type");
            put(SYSP_RENDERER_WINEFFECT, "Window effect");
            put(SYSP_RENDERER_IGNORENOTES_AUDIO_VALID, "Ignore notes valid of AUDIO");
            put(SYSP_RENDERER_IGNORENOTES_AUDIO_LOWEST, "Ignore notes lowest velocity of AUDIO");
            put(SYSP_RENDERER_IGNORENOTES_AUDIO_HIGHEST, "Ignore notes highest velocity of AUDIO");
            put(SYSP_RENDERER_IGNORENOTES_RENDER_VALID, "Ghost notes invisible");
            put(SYSP_RENDERER_IGNORENOTES_RENDER_LOWEST, "Ghost notes lowest velocity");
            put(SYSP_RENDERER_IGNORENOTES_RENDER_HIGHEST, "Ghost notes highest velocity");
            put(SYSP_DEBUGMODE, "Debug mode enable");
        }
    };

    public static int MAX_NOTES_WIDTH = 2400;
    public static int MIN_NOTES_WIDTH = 160;
    public static int CNT_NOTES_WIDTH = 800;//(MAX_NOTES_WIDTH - MIN_NOTES_WIDTH) / 2;
    
    public static int DEFAULT_DIM_W = 1280;
    public static int DEFAULT_DIM_H = 768;

    public static enum SyspViewMode {
        RAIN_FALL, SIDE_FLOW;
    }

    public static enum SyspKeyFocusFunc {
        MIDI_EVENT, COLOR;
    }
    
    public static enum SyspLayerOrder {
        ASC, DESC;
    }

    public static enum SyspMonitorType {
        NONE, TYPE1, TYPE2, TYPE3;
    }
    
    public static enum SyspWinEffect {
        NONE, CIRCLE_VIGNETTE, TOP_VIGNETTE;
    }

    private static Object[] viewModeItemO = { SyspViewMode.RAIN_FALL, SyspViewMode.SIDE_FLOW };
    private static String[] viewModeItemS = { "rain_fall", "side_flow" };

    private static Object[] keyFocusFuncItemO = { SyspKeyFocusFunc.MIDI_EVENT, SyspKeyFocusFunc.COLOR };
    private static String[] keyFocusFuncItemS = { "midi_event", "color" };
    
    private static Object[] layerOrderItemO = { SyspLayerOrder.ASC, SyspLayerOrder.DESC };
    private static String[] layerOrderItemS = { "asc", "desc" };

    private static Object[] monitorTypeItemO = { SyspMonitorType.NONE, SyspMonitorType.TYPE1, SyspMonitorType.TYPE2, SyspMonitorType.TYPE3 };
    private static String[] monitorTypeItemS = { "none", "type1", "type2", "type3" };

    private static Object[] NotesSpeedItemO = { -1 };
    private static String[] NotesSpeedItemS = { "auto" };

    private static Object[] NotesCountItemO = { -1 };
    private static String[] NotesCountItemS = { "auto" };

    private static Object[] WinSizeItemO = { /* "2560*1440", "1920*1080", */"1280*720", "854*480", "640*360" };
    private static Object[] WinSizeItemD = { /* "2560*1408", "1920*1024", */"1280*768", "896*512", "640*384" };
    private static String[] WinSizeItemS = { /* "1440p", "1080p", */"720p", "480p", "360p", };
    
    private static Object[] winEffeItemO = { SyspWinEffect.NONE, SyspWinEffect.CIRCLE_VIGNETTE };
    private static String[] winEffeItemS = { "none", "circle_vignette" };

    private List<PropertiesNode> nodes;
    private boolean notesWidthAuto = true;
    private int notesWidth = 420;
    private int keyWidth = 50;

    private int dimWidth = DEFAULT_DIM_W;
    private int dimHeight = DEFAULT_DIM_H;

    private double dimOffset = 1.0;

    private int windowWidth = 1280;
    private int windowHeight = 720;

    private List<File> preloadFiles = new ArrayList<File>();
    
    private boolean isGPUAvailable = false;

    private static SystemProperties instance = new SystemProperties();
    private SystemProperties() {
        nodes = new ArrayList<>();

        nodes.add(new PropertiesNode(SYSP_FILE_LAYOUT, PropertiesNodeType.STRING, ""));
        nodes.add(new PropertiesNode(SYSP_FILE_DEFAULT_PATH, PropertiesNodeType.STRING, ""));
        nodes.add(new PropertiesNode(SYSP_AUDIO_SYNTH, PropertiesNodeType.STRING, ISoundManager.AUTO_RECEIVER_NAME));
        nodes.add(new PropertiesNode(SYSP_RENDERER_MODE, PropertiesNodeType.ITEM, SyspViewMode.RAIN_FALL, viewModeItemS, viewModeItemO));
        nodes.add(new PropertiesNode(SYSP_RENDERER_WORKNUM, PropertiesNodeType.INT, "2", "2", "64"));
        nodes.add(new PropertiesNode(SYSP_RENDERER_FPS, PropertiesNodeType.INT, "60", "20", ""));
        nodes.add(new PropertiesNode(SYSP_RENDERER_LAYERORDER, PropertiesNodeType.ITEM, SyspLayerOrder.ASC, layerOrderItemS, layerOrderItemO));
        nodes.add(new PropertiesNode(SYSP_RENDERER_KEY_FOCUS_FUNC, PropertiesNodeType.ITEM, SyspKeyFocusFunc.MIDI_EVENT, keyFocusFuncItemS, keyFocusFuncItemO));
        nodes.add(new PropertiesNode(SYSP_RENDERER_NOTESSPEED, PropertiesNodeType.INT, "-1", "1", "100", NotesSpeedItemS, NotesSpeedItemO));
        nodes.add(new PropertiesNode(SYSP_RENDERER_NOTESIMAGENUM, PropertiesNodeType.INT, "120", "3", "1000", NotesCountItemS, NotesCountItemO));
        nodes.add(new PropertiesNode(SYSP_RENDERER_DIMENSION, PropertiesNodeType.ITEM, "1280*768", WinSizeItemS, WinSizeItemD));
        nodes.add(new PropertiesNode(SYSP_RENDERER_WINSIZE, PropertiesNodeType.ITEM, "1280*720", WinSizeItemS, WinSizeItemO));
        nodes.add(new PropertiesNode(SYSP_RENDERER_MONITOR_TYPE, PropertiesNodeType.ITEM, SyspMonitorType.TYPE1, monitorTypeItemS, monitorTypeItemO));
        nodes.add(new PropertiesNode(SYSP_RENDERER_WINEFFECT, PropertiesNodeType.ITEM, SyspWinEffect.NONE, winEffeItemS, winEffeItemO));
        nodes.add(new PropertiesNode(SYSP_RENDERER_IGNORENOTES_AUDIO_VALID, PropertiesNodeType.BOOLEAN, "true"));
        nodes.add(new PropertiesNode(SYSP_RENDERER_IGNORENOTES_AUDIO_LOWEST, PropertiesNodeType.INT, "1", "1", "128"));
        nodes.add(new PropertiesNode(SYSP_RENDERER_IGNORENOTES_AUDIO_HIGHEST, PropertiesNodeType.INT, "20", "1", "128"));
        nodes.add(new PropertiesNode(SYSP_RENDERER_IGNORENOTES_RENDER_VALID, PropertiesNodeType.BOOLEAN, "false"));
        nodes.add(new PropertiesNode(SYSP_RENDERER_IGNORENOTES_RENDER_LOWEST, PropertiesNodeType.INT, "1", "1", "128"));
        nodes.add(new PropertiesNode(SYSP_RENDERER_IGNORENOTES_RENDER_HIGHEST, PropertiesNodeType.INT, "1", "1", "128"));

        nodes.add(new PropertiesNode(SYSP_DEBUGMODE, PropertiesNodeType.BOOLEAN, "false"));
        
        isGPUAvailable = Utility.isGpuAvailable();
    }

    public static SystemProperties getInstance() {
        return instance;
    }

    public List<PropertiesNode> getNodes() {
        return nodes;
    }

    public PropertiesNode getPropNode(String key) {
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
        if (file.exists()) {
            Properties props = new Properties();
            props.load(new FileInputStream(file));
            for (PropertiesNode nd : nodes) {
                setPropObject(props, nd.getKey());
            }
        }
    }

    public void write(File file) throws FileNotFoundException, IOException {
        Properties props = new Properties();
        for (PropertiesNode nd : nodes) {
            props.setProperty(nd.getKey(), nd.getDataString());
        }
        props.store(new FileOutputStream(file), "");
    }

    public void iniialize() {

        // 以下、ネイティブ変数に分ける
        int notesSpeed = (int) getData(SYSP_RENDERER_NOTESSPEED);
        boolean notesSpeedIsAuto = false;
        if (notesSpeed == -1) {
            notesSpeedIsAuto = true;
            notesSpeed = 50;
        }
        notesWidthAuto = notesSpeedIsAuto;
        notesWidth = MIN_NOTES_WIDTH + (int) ((double) (MAX_NOTES_WIDTH - MIN_NOTES_WIDTH) * ((double) notesSpeed / 100.0));
        if (notesWidth < MIN_NOTES_WIDTH) {
            notesWidth = MIN_NOTES_WIDTH;
        }
        else if (MAX_NOTES_WIDTH < notesWidth) {
            notesWidth = MAX_NOTES_WIDTH;
        }

        // TODO Dimはバグるため720p固定とする
        PropertiesNode dimNode = getPropNode(SYSP_RENDERER_DIMENSION);
        dimNode.setObject("1280*768");
        
        String sDimSize = (String) getData(SYSP_RENDERER_DIMENSION);
        if (sDimSize != null && sDimSize.isBlank() == false) {
            try {
                String[] parts = sDimSize.split("[x×*,]");
                dimWidth = Integer.parseInt(parts[0].trim()); // 幅
                dimHeight = Integer.parseInt(parts[1].trim()); // 高さ
            }
            catch (Exception e) {
                dimWidth = DEFAULT_DIM_W; // 幅
                dimHeight = DEFAULT_DIM_H; // 高さ
            }

            // 128で割り切れるサイズにする
            dimWidth = dimWidth / 128 * 128;
            dimHeight = dimHeight / 128 * 128;
        }

        String sWinSize = (String) getData(SYSP_RENDERER_WINSIZE);
        if (sWinSize != null && sWinSize.isBlank() == false) {
            try {
                String[] parts = sWinSize.split("[x×*,]");
                windowWidth = Integer.parseInt(parts[0].trim()); // 幅
                windowHeight = Integer.parseInt(parts[1].trim()); // 高さ
            }
            catch (Exception e) {
                windowWidth = 1280; // 幅
                windowHeight = 720; // 高さ
            }
        }

        SyspViewMode viewDir = (SyspViewMode) getData(SYSP_RENDERER_MODE);
        int defKeyWidth = 50;
        if (viewDir == SyspViewMode.SIDE_FLOW) {
            dimOffset = (double) dimWidth / (double) DEFAULT_DIM_W;
            defKeyWidth = 50;
        }
        else if (viewDir == SyspViewMode.RAIN_FALL) {
            dimOffset = (double) dimHeight / (double) DEFAULT_DIM_H;
            defKeyWidth = 120;
        }
        keyWidth = (int) ((double) defKeyWidth * dimOffset);
        notesWidth = (int) ((double) notesWidth * dimOffset);
        
        boolean validIgnoreNotesOfAudio = (boolean)SystemProperties.getInstance().getData(SystemProperties.SYSP_RENDERER_IGNORENOTES_AUDIO_VALID);
        int ignoreNotesLowestOfAudio = (int)SystemProperties.getInstance().getData(SystemProperties.SYSP_RENDERER_IGNORENOTES_AUDIO_LOWEST);
        int ignoreNotesHighestOfAudio = (int)SystemProperties.getInstance().getData(SystemProperties.SYSP_RENDERER_IGNORENOTES_AUDIO_HIGHEST);
        if (ignoreNotesLowestOfAudio < 0) {
            ignoreNotesLowestOfAudio = 0;
        }
        else if (ignoreNotesLowestOfAudio > 127) {
            ignoreNotesLowestOfAudio = 127;
        }
        if (ignoreNotesHighestOfAudio < 0) {
            ignoreNotesHighestOfAudio = 0;
        }
        else if (ignoreNotesHighestOfAudio > 127) {
            ignoreNotesHighestOfAudio = 127;
        }
        if (ignoreNotesLowestOfAudio > ignoreNotesHighestOfAudio) {
            validIgnoreNotesOfAudio = false;
        }
        if (validIgnoreNotesOfAudio == false) {
            ignoreNotesLowestOfAudio = 0;
            ignoreNotesHighestOfAudio = 0;
        }
        JMPCoreAccessor.getSoundManager().getMidiUnit().setIgnoreNotesVelocityOfAudio(ignoreNotesLowestOfAudio, ignoreNotesHighestOfAudio);
        
        boolean validIgnoreNotesOfRender = (boolean)SystemProperties.getInstance().getData(SystemProperties.SYSP_RENDERER_IGNORENOTES_RENDER_VALID);
        int ignoreNotesLowestOfRender = (int)SystemProperties.getInstance().getData(SystemProperties.SYSP_RENDERER_IGNORENOTES_RENDER_LOWEST);
        int ignoreNotesHighestOfRender = (int)SystemProperties.getInstance().getData(SystemProperties.SYSP_RENDERER_IGNORENOTES_RENDER_HIGHEST);
        if (ignoreNotesLowestOfRender < 0) {
            ignoreNotesLowestOfRender = 0;
        }
        else if (ignoreNotesLowestOfRender > 127) {
            ignoreNotesLowestOfRender = 127;
        }
        if (ignoreNotesHighestOfRender < 0) {
            ignoreNotesHighestOfRender = 0;
        }
        else if (ignoreNotesHighestOfRender > 127) {
            ignoreNotesHighestOfRender = 127;
        }
        if (ignoreNotesLowestOfRender > ignoreNotesHighestOfRender) {
            validIgnoreNotesOfRender = false;
        }
        if (validIgnoreNotesOfRender == false) {
            ignoreNotesLowestOfRender = 0;
            ignoreNotesHighestOfRender = 0;
        }
        JMPCoreAccessor.getSoundManager().getMidiUnit().setIgnoreNotesVelocityOfMonitor(ignoreNotesLowestOfRender, ignoreNotesHighestOfRender);
        
        String synthKey = getData(SystemProperties.SYSP_AUDIO_SYNTH).toString();
        ScheduledExecutorService scheduler1 = Executors.newScheduledThreadPool(1);
        scheduler1.schedule(() -> {
            JMPCoreAccessor.getDataManager().setConfigParam(IDataManager.CFG_KEY_MIDIOUT, synthKey);
        }, 200, TimeUnit.MILLISECONDS);
        
        // ファイルロードを予約する
        ScheduledExecutorService scheduler2 = Executors.newScheduledThreadPool(1);
        scheduler2.schedule(() -> {
            SystemProperties.getInstance().preloadAudioFiles();
        }, 400, TimeUnit.MILLISECONDS);
    }

    public void preloadAudioFiles() {
        if (preloadFiles.isEmpty() == false) {
            loadAudioFiles(preloadFiles.toArray(new File[0]));
            preloadFiles.clear();
        }
    }

    public void loadAudioFiles(File... files) {
        // 一番先頭のファイルを取得
        if ((files != null) && (files.length > 0)) {

            if (JMPCoreAccessor.getSoundManager().isPlay() == true) {
                JMPCoreAccessor.getSoundManager().stop();
            }

            if (files.length >= 2) {

                String exMidi = JMPCoreAccessor.getSystemManager().getCommonRegisterValue(ISystemManager.COMMON_REGKEY_NO_EXTENSION_MIDI);
                String path1 = files[0].getPath();
                String path2 = files[1].getPath();
                if (Utility.checkExtensions(path1, exMidi.split(",")) == true) {
                    JMPCoreAccessor.getFileManager().loadDualFileToPlay(path1, path2);
                }
                else if (Utility.checkExtensions(path2, exMidi.split(",")) == true) {
                    JMPCoreAccessor.getFileManager().loadDualFileToPlay(path2, path1);
                }
            }
            else {
                String path = files[0].getPath();
                if (Utility.checkExtensions(path, AbstractRenderPlugin.Extensions.split(",")) == true) {
                    JMPCoreAccessor.getFileManager().loadFileToPlay(path);
                }
            }
        }
    }

    public int getWorkerNum() {
        return (int) getPropNode(SYSP_RENDERER_WORKNUM).getData();
    }

    public String getLayoutFile() {
        return (String) getPropNode(SYSP_FILE_LAYOUT).getData();
    }

    public boolean isDebugMode() {
        return (boolean) getPropNode(SYSP_DEBUGMODE).getData();
    }

    public int getFixedFps() {
        return (int) getPropNode(SYSP_RENDERER_FPS).getData();
    }

    public SyspViewMode getViewMode() {
        return (SyspViewMode) getPropNode(SYSP_RENDERER_MODE).getData();
    }

    public SyspKeyFocusFunc getKeyFocusFunc() {
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        if (midiUnit.isRenderingOnlyMode() == true) {
            return SyspKeyFocusFunc.COLOR;
        }
        return (SyspKeyFocusFunc) getPropNode(SYSP_RENDERER_KEY_FOCUS_FUNC).getData();
    }
    
    public SyspLayerOrder getLayerOrder() {
        return (SyspLayerOrder) getPropNode(SYSP_RENDERER_LAYERORDER).getData();
    }

    public SyspMonitorType getMonitorType() {
        return (SyspMonitorType) getPropNode(SYSP_RENDERER_MONITOR_TYPE).getData();
    }
    
    public SyspWinEffect getWinEffect() {
        return (SyspWinEffect) getPropNode(SYSP_RENDERER_WINEFFECT).getData();
    }

    public int getKeyWidth() {
        return keyWidth;
    }

    public int getNotesWidth() {
        return notesWidth;
    }

    public boolean isNotesWidthAuto() {
        return notesWidthAuto;
    }

    public int getNotesImageCount() {
        return (int) getPropNode(SYSP_RENDERER_NOTESIMAGENUM).getData();
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public int getDimWidth() {
        return dimWidth;
    }

    public int getDimHeight() {
        return dimHeight;
    }

    public double getDimOffset() {
        return dimOffset;
    }

    public List<File> getPreloadFiles() {
        return preloadFiles;
    }
    
    public boolean isGhostNotes(int velocity) {
        return JMPCoreAccessor.getSoundManager().getMidiUnit().isGhostNotesOfMonitor(velocity);
    }

    public boolean isGpuAvailable() {
        return isGPUAvailable;
    }
}

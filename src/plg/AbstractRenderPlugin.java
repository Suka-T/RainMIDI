package plg;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import gui.RainFallRendererWindow;
import gui.RendererConfigDialog;
import gui.RendererWindow;
import gui.SideFlowRendererWindow;
import jlib.core.ISystemManager;
import jlib.core.JMPCoreAccessor;
import jlib.player.IPlayerListener;
import jlib.plugin.ISupportExtensionConstraints;
import jlib.plugin.JMidiPlugin;
import layout.LayoutManager;
import plg.SystemProperties.SyspViewMode;

public class AbstractRenderPlugin extends JMidiPlugin implements IPlayerListener, ISupportExtensionConstraints {
    
    public static final String APP_NAME = "Rain MIDI";
    public static final String APP_VERSION = "1.10";
    public static final String APP_YEAR = "2025";
    public static final String APP_COMPANY = "Suka";

    public static String Extensions = "";
    public static AbstractRenderPlugin PluginInstance = null;
    
    private static final String PROP_FILE_NAME = "renderer.properties";
    
    private boolean exitFlag = false;
    public List<RendererWindow> winArray = null;
    private RendererConfigDialog launchWindow = null;
    
    private JWindow splash = null;
    
    public void exitStdPlg() {
        SystemProperties.getInstance().exit();
        
        exitFlag = true;
    }

    public AbstractRenderPlugin() {
        PluginInstance = this;
        winArray = Collections.synchronizedList(new ArrayList<>());
    }

    protected RendererWindow createMainWindow() {
        RendererWindow win = null;
        if (SyspViewMode.RAIN_FALL == SystemProperties.getInstance().getViewMode()) {
            win = new RainFallRendererWindow(
                    SystemProperties.getInstance().getWindowWidth(), 
                    SystemProperties.getInstance().getWindowHeight(),
                    SystemProperties.getInstance().isWindowMaximized());
        }
        else if (SyspViewMode.SIDE_FLOW == SystemProperties.getInstance().getViewMode()) {
            win = new SideFlowRendererWindow(
                    SystemProperties.getInstance().getWindowWidth(), 
                    SystemProperties.getInstance().getWindowHeight(),
                    SystemProperties.getInstance().isWindowMaximized());
        }
        return win;
    }
    
    public String getAppName() {
        return "My App";
    }
    
    public String getAppVersion() {
        return "X.XX";
    }
    
    public String getAppYear() {
        return "";
    }
    
    public String getAppCompany() {
        return "";
    }
    
    public void launch() {
        try {
            if (JMPCoreAccessor.getSystemManager().isEnableStandAlonePlugin() == true) {
                if (SwingUtilities.isEventDispatchThread()) {
                    launchWindow = new RendererConfigDialog(this);
                    launchWindow.setVisible(true);
                }
                else {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        
                        @Override
                        public void run() {
                            launchWindow = new RendererConfigDialog(AbstractRenderPlugin.this);
                            launchWindow.setVisible(true);
                        }
                    });
                }
            }
            else {
                startRendererWindow();
            }
        }
        catch (Exception e1) {
            e1.printStackTrace();
        }
    }
    
    public void writeSystemProp() throws FileNotFoundException, IOException {
        Path folder = Paths.get(JMPCoreAccessor.getSystemManager().getSystemPath(ISystemManager.PATH_DATA_DIR, this));
        Path fullPath = folder.resolve(PROP_FILE_NAME);
        File propFile = new File(fullPath.toString());
        
        SystemProperties.getInstance().write(propFile);
    }
    
    public void startRendererWindow() {
        try {
            
            SystemProperties.getInstance().initialize();
            
            if (JMPCoreAccessor.getSystemManager().isEnableStandAlonePlugin() == true) {
                // SystemPropertiesの保存 
                writeSystemProp();
                
                if (JMPCoreAccessor.getSoundManager().getMidiUnit().isValidSequence()) {
                    JMPCoreAccessor.getSoundManager().initPosition();
                }
            }
            
            if (SwingUtilities.isEventDispatchThread()) {
                RendererWindow win = createMainWindow();
                win.init();
                win.setVisible(true);
                win.adjustTickBar();
                winArray.add(win);
            }
            else {
                SwingUtilities.invokeAndWait(new Runnable() {
    
                    @Override
                    public void run() {
                        RendererWindow win = createMainWindow();
                        win.init();
                        win.setVisible(true);
                        win.adjustTickBar();
                        winArray.add(win);
                    }
                });
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize() {
        if (JMPCoreAccessor.getSystemManager().isEnableStandAlonePlugin() == true) {
            splash = new JWindow();
            splash.setSize(300, 200);
    
            JLabel label = new JLabel("Loading...", SwingConstants.CENTER);
            // 背景色設定
            label.setOpaque(true);
            label.setBackground(new Color(49, 74, 134));
            label.setForeground(new Color(112, 185, 255));
            splash.add(label);
            label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 40));
    
            splash.setLocationRelativeTo(null);
            splash.setVisible(true);
        }
        
        createExtensions();

        Path folder = Paths.get(JMPCoreAccessor.getSystemManager().getSystemPath(ISystemManager.PATH_DATA_DIR, this));
        Path fullPath = folder.resolve(PROP_FILE_NAME);
        File propFile = new File(fullPath.toString());
        try {
            SystemProperties.getInstance().reset();
            SystemProperties.getInstance().read(propFile);
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }

        try {

            String layoutFilename = SystemProperties.getInstance().getLayoutFile();
            if (!layoutFilename.contains(".")) {
                layoutFilename += ".layout";
                folder = Paths.get(JMPCoreAccessor.getSystemManager().getSystemPath(ISystemManager.PATH_RES_DIR, this));
                fullPath = folder.resolve(layoutFilename);
                LayoutManager.getInstance().read(new File(fullPath.toString()));
            }
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        
        if (splash != null) {
            splash.setVisible(false);
        }
        
        if (SystemProperties.getInstance().getPreloadFiles().isEmpty() == true) {
            launch();
        }
        else {
            startRendererWindow();
        }
    }

    private void createExtensions() {
        String exMidi = JMPCoreAccessor.getSystemManager().getCommonRegisterValue("extension_midi");
        String exMXML = JMPCoreAccessor.getSystemManager().getCommonRegisterValue("extension_musicxml");
        Extensions = exMidi + "," + exMXML;
    }

    @Override
    public boolean isEnable() {
        return super.isEnable();
    }

    @Override
    public void exit() {
        for (RendererWindow win : winArray) {
            win.setVisible(false);
        }
        winArray.clear();
        exitFlag = true;
    }

    @Override
    public void open() {
        for (RendererWindow win : winArray) {
            win.setVisible(true);
            win.adjustTickBar();
        }
    }

    @Override
    public void close() {
        for (RendererWindow win : winArray) {
            win.setVisible(false);
        }
    }

    @Override
    public boolean isOpen() {
        if (JMPCoreAccessor.getSystemManager().isEnableStandAlonePlugin() == true) {
            return exitFlag == false;
        }
        else {
            boolean isOpen = false;
            for (RendererWindow win : winArray) {
                if (win.isVisible()) {
                    isOpen = true;
                    break;
                }
            }
            
            return isOpen;
        }
    }

    @Override
    public void update() {
        super.update();
    }

    @Override
    protected void noteOn(int channel, int midiNumber, int velocity, long timeStamp, short senderType) {
    }

    @Override
    protected void noteOff(int channel, int midiNumber, long timeStamp, short senderType) {
    }

    @Override
    protected void programChange(int channel, int programNumber, long timeStamp, short senderType) {
    }

    @Override
    protected void pitchBend(int channel, int pbValue, long timeStamp, short senderType) {
    }

    @Override
    public void loadFile(File file) {
        super.loadFile(file);
        for (RendererWindow win : winArray) {
            win.loadFile();
        }
    }

    @Override
    public void startSequencer() {
    }

    @Override
    public void stopSequencer() {
    }

    @Override
    public void updateTickPosition(long before, long after) {
        if (before != after) {
            for (RendererWindow win : winArray) {
                win.adjustTickBar();
            }
        }
    }

    @Override
    public String allowedExtensions() {
        return Extensions;
    }

    @Override
    public void updateSequencer() {
        for (RendererWindow win : winArray) {
            win.adjustTickBar();
        }
    }

}

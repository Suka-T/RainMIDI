package plg;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public static String Extensions = "";
    public static RendererWindow MainWindow = null;
    
    private static final String PROP_FILE_NAME = "renderer.properties";

    public AbstractRenderPlugin() {
    }

    protected void createMainWindow() {
        if (SyspViewMode.RAIN_FALL == SystemProperties.getInstance().getViewMode()) {
            MainWindow = new RainFallRendererWindow(
                    SystemProperties.getInstance().getWindowWidth(), 
                    SystemProperties.getInstance().getWindowHeight());
        }
        else if (SyspViewMode.SIDE_FLOW == SystemProperties.getInstance().getViewMode()) {
            MainWindow = new SideFlowRendererWindow(
                    SystemProperties.getInstance().getWindowWidth(), 
                    SystemProperties.getInstance().getWindowHeight());
        }
        MainWindow.init();
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

    @Override
    public void initialize() {
        createExtensions();

        Path folder = Paths.get(JMPCoreAccessor.getSystemManager().getSystemPath(ISystemManager.PATH_DATA_DIR, this));
        Path fullPath = folder.resolve(PROP_FILE_NAME);
        File propFile = new File(fullPath.toString());
        try {
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
            
            if (JMPCoreAccessor.getSystemManager().isEnableStandAlonePlugin() == true) {
                RendererConfigDialog cfgDlg = new RendererConfigDialog(this);
                cfgDlg.setVisible(true);
                
                if (cfgDlg.isCommitClose() == false) {
                    return;
                }
            }
            
            SystemProperties.getInstance().iniialize();
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        
        try {
            // SystemPropertiesの保存 
            SystemProperties.getInstance().write(propFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (SwingUtilities.isEventDispatchThread()) {
            createMainWindow();
        }
        else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        createMainWindow();
                    }
                });
            }
            catch (InvocationTargetException | InterruptedException e) {
                e.printStackTrace();
            }
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
    }

    @Override
    public void open() {
        MainWindow.setVisible(true);
        MainWindow.adjustTickBar();
    }

    @Override
    public void close() {
        MainWindow.setVisible(false);
    }

    @Override
    public boolean isOpen() {
        return MainWindow.isVisible();
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
        MainWindow.loadFile();
    }

    @Override
    public void startSequencer() {
        // MainWindow.adjustTickBar();
    }

    @Override
    public void stopSequencer() {
    }

    @Override
    public void updateTickPosition(long before, long after) {
        if (before != after) {
            MainWindow.adjustTickBar();
        }
    }

    @Override
    public String allowedExtensions() {
        return Extensions;
    }

    @Override
    public void updateSequencer() {
        MainWindow.adjustTickBar();
    }

}

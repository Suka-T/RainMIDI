

import plg.AbstractRenderPlugin;

public class RainMIDI extends AbstractRenderPlugin {
    
    public static final String APP_NAME = "Rain MIDI";
    public static final String APP_VERSION = "1.01";
    public static final String APP_YEAR = "2025";
    public static final String APP_COMPANY = "Suka";

    public static void main(String[] args) {
        System.out.println(APP_NAME);
    }

    public RainMIDI() {
        super();
    }
    
    @Override
    public String getAppName() {
        return APP_NAME;
    }
    
    @Override
    public String getAppVersion() {
        return APP_VERSION;
    }
    
    @Override
    public String getAppYear() {
        return APP_YEAR;
    }
    
    @Override
    public String getAppCompany() {
        return APP_COMPANY;
    }
    
    @Override
    public void initialize() {
        super.initialize();
        AbstractRenderPlugin.MainWindow.setTitle(APP_NAME);
    }
}

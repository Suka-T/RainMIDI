

import gui.LicenseString;
import plg.AbstractRenderPlugin;

public class RainMIDI extends AbstractRenderPlugin {

    public static void main(String[] args) {
        System.out.println(LicenseString.APP_NAME);
    }

    public RainMIDI() {
        super();
    }
    
    @Override
    public String getAppName() {
        return LicenseString.APP_NAME;
    }
    
    @Override
    public String getAppVersion() {
        return LicenseString.APP_VERSION;
    }
    
    @Override
    public String getAppYear() {
        return LicenseString.APP_YEAR;
    }
    
    @Override
    public String getAppCompany() {
        return LicenseString.APP_COMPANY;
    }
    
    @Override
    public void initialize() {
        super.initialize();
    }
}

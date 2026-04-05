

import plg.AbstractRenderPlugin;

public class RainMIDI extends AbstractRenderPlugin {

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
    }
}

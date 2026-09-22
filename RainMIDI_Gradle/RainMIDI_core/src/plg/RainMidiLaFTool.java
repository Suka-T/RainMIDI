package plg;

import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import plg.SystemProperties.SyspLaF;

public class RainMidiLaFTool {
    public static void setLookAndFeel(SyspLaF laf) {
        try {
            switch (laf) {
            case DARK:
                FlatDarkLaf.setup();
                break;
            case LIGHT:
                FlatLightLaf.setup();
                break;
            case LEGACY:
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                break;
            default:
                break;
            }
        }
        catch (Exception e) {
            e.printStackTrace(); // Unknown Error. 
        }
    }
}

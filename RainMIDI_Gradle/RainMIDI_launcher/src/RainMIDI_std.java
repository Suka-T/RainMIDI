import java.io.File;

import jlib.core.IDataManager;
import jmp.JMPFlags;
import jmp.JMPLoader;
import jmp.file.ConfigDatabaseWrapper;
import plg.SystemProperties;
import std.StandAlonePluginInvoker;

public class RainMIDI_std {
    public static void main(String[] args) {        
        JMPLoader.UsePluginDirectory = false;
        JMPLoader.UseConfigFile = false;
        JMPLoader.UseHistoryFile = false;
        JMPLoader.UseSkinFile = false;
        JMPFlags.UseUnsynchronizedMidiPacket = false;
        JMPFlags.DualFileLoadToSoundFunc = 2;
        JMPFlags.PreActivation = true;
        JMPFlags.UseRenderedNotesCount = true;
        JMPFlags.ShowFileErrorDialog = true;

        ConfigDatabaseWrapper cfg = new ConfigDatabaseWrapper();
        cfg.setConfigParamToBoolean(IDataManager.CFG_KEY_SHOW_STARTUP_DEVICE_SETUP, false);
        cfg.setConfigParamToBoolean(IDataManager.CFG_KEY_USE_VIDEO_PLAYER, false);
        
        if (args.length > 0) {
            // ファイル起動 
            File f = new File(args[0]);
            if (f.exists()) {
                SystemProperties.getInstance().getPreloadFiles().clear();
                SystemProperties.getInstance().getPreloadFiles().add(f);
            }
        }
        
        StandAlonePluginInvoker.exec(args, cfg, new RainMIDI());
    }
}

import jlib.core.IDataManager;
import jmp.JMPFlags;
import jmp.JMPLoader;
import jmp.file.ConfigDatabaseWrapper;
import std.StandAlonePluginInvoker;

public class RainMIDI_std {
    public static void main(String[] args) {        
        JMPLoader.UsePluginDirectory = false;
        JMPLoader.UseConfigFile = false;
        JMPLoader.UseHistoryFile = false;
        JMPLoader.UseSkinFile = false;
        JMPFlags.UseUnsynchronizedMidiPacket = false;
        JMPFlags.DualFileLoadToSoundFunc = 2;

        ConfigDatabaseWrapper cfg = new ConfigDatabaseWrapper();
        cfg.setConfigParamToBoolean(IDataManager.CFG_KEY_SHOW_STARTUP_DEVICE_SETUP, false);
        StandAlonePluginInvoker.exec(args, cfg, new RainMIDI());
    }
}

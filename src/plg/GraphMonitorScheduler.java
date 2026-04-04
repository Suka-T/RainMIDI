package plg;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jlib.core.JMPCoreAccessor;
import jlib.midi.INotesMonitor;

public class GraphMonitorScheduler {
    
    private final ScheduledExecutorService osStateScheduler = Executors.newSingleThreadScheduledExecutor();
    
    private LongRingBuffer npsBuffer = null;
    private LongRingBuffer polyBuffer = null;

    public GraphMonitorScheduler() {
        npsBuffer = new LongRingBuffer(100);
        polyBuffer = new LongRingBuffer(100);
        osStateScheduler.scheduleAtFixedRate(this::updateStats, 0, 50, TimeUnit.MILLISECONDS);
    }
    
    public void exit() {
        osStateScheduler.shutdown();
    }
    
    private void updateStats() {
        INotesMonitor notesMonitor = JMPCoreAccessor.getSoundManager().getNotesMonitor();
        npsBuffer.add((long)notesMonitor.getNps());
        polyBuffer.add((long)notesMonitor.getPolyphony());
        npsBuffer.updateSnapshot();
        polyBuffer.updateSnapshot();
    }
    
    public void clearRingBuffer() {
        npsBuffer.clear();
        polyBuffer.clear();
    }
    
    public long[] getNpsSnapshot() {
        return npsBuffer.getSnapshot();
    }
    
    public long[] getPolySnapshot() {
        return polyBuffer.getSnapshot();
    }
}

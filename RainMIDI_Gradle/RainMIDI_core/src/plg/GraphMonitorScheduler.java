package plg;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jlib.core.JMPCoreAccessor;
import jlib.midi.INotesMonitor;

public class GraphMonitorScheduler {
    
    private final ScheduledExecutorService osStateScheduler = Executors.newSingleThreadScheduledExecutor();
    
    private static final int CYCLIC_MILLS = 50;
    private static final int PEEKEND_COUNT = 1000 / CYCLIC_MILLS;
    
    private LongRingBuffer npsBuffer = null;
    private LongRingBuffer polyBuffer = null;
    
    private long npsPeekCount = -1;
    private long tmpNpsPeekStart = 0;
    private long npsPeekMax = 100;
    private long npsPeekMin = 0;
    
    private long polyPeekCount = -1;
    private long tmpPolyPeekStart = 0;
    private long polyPeekMax = 100;
    private long polyPeekMin = 0;
    
    private boolean locked = false;

    public GraphMonitorScheduler() {
        npsBuffer = new LongRingBuffer(100);
        polyBuffer = new LongRingBuffer(100);
        osStateScheduler.scheduleAtFixedRate(this::updateStats, 0, CYCLIC_MILLS, TimeUnit.MILLISECONDS);
    }
    
    public void exit() {
        osStateScheduler.shutdown();
    }
    
    private void updateStats() {
        INotesMonitor notesMonitor = JMPCoreAccessor.getSoundManager().getNotesMonitor();
        if (locked == false) {
            npsBuffer.add((long)notesMonitor.getNps());
            polyBuffer.add((long)notesMonitor.getPolyphony());
            npsBuffer.updateSnapshot();
            polyBuffer.updateSnapshot();
            
            long maxData = npsBuffer.getPeekMax();
            long minData = npsBuffer.getPeekMin();
            if (100 > maxData) maxData = 100;
            if (0 > minData) minData = 0;
            
            if (npsPeekMax != maxData) {
            	if (npsPeekMax > maxData) {
            		if (npsPeekCount == -1) {
            			npsPeekCount = 0;
            			tmpNpsPeekStart = npsPeekMax;
            		}
            		else {
            			long len = tmpNpsPeekStart - maxData; 
            			double elapsed = (double)npsPeekCount / (double)PEEKEND_COUNT;
            			double eased = Utility.calcEasedZoomInOut(elapsed);
            			npsPeekMax = (long)((double)maxData + (double)len * (1.0 - eased));
            			npsPeekCount++;
            			if (npsPeekCount >= PEEKEND_COUNT) {
            				npsPeekCount = -1;
            				npsPeekMax = maxData;
            			}
            		}
            	}
            	else {
            		npsPeekCount = -1;
            		npsPeekMax = maxData;
            	}
            }
            npsPeekMin = minData;
            
            maxData = polyBuffer.getPeekMax();
            minData = polyBuffer.getPeekMin();
            if (30 > maxData) maxData = 30;
            if (0 > minData) minData = 0;
            
            if (polyPeekMax != maxData) {
            	if (polyPeekMax > maxData) {
            		if (polyPeekCount == -1) {
            			polyPeekCount = 0;
            			tmpPolyPeekStart = polyPeekMax;
            		}
            		else {
            			long len = tmpPolyPeekStart - maxData; 
            			double elapsed = (double)polyPeekCount / (double)PEEKEND_COUNT;
        	        	double eased = (elapsed == 0.0) ? 0.0 : 1.0 - Math.pow(2, -10 * elapsed);
        	        	polyPeekMax = (long)((double)maxData + (double)len * (1.0 - eased));
        	        	polyPeekCount++;
        	        	if (polyPeekCount >= PEEKEND_COUNT) {
        	        		polyPeekCount = -1;
        	        		polyPeekMax = maxData;
            			}
            		}
            	}
            	else {
            		polyPeekCount = -1;
            		polyPeekMax = maxData;
            	}
            }
            polyPeekMin = minData;
        }
    }
    
    public void lockCount() {
        locked = true;
    }
    
    public void releaseCount() {
        locked = false;
    }
    
    public void clearRingBuffer() {
        npsBuffer.clear();
        polyBuffer.clear();
        npsBuffer.updateSnapshot();
        polyBuffer.updateSnapshot();
    }
    
    public long[] getNpsSnapshot() {
        return npsBuffer.getSnapshot();
    }
    
    public long[] getPolySnapshot() {
        return polyBuffer.getSnapshot();
    }

	public long getNpsPeekMax() {
		return npsPeekMax;
	}

	public long getPolyPeekMax() {
		return polyPeekMax;
	}

	public long getNpsPeekMin() {
		return npsPeekMin;
	}

	public long getPolyPeekMin() {
		return polyPeekMin;
	}
}

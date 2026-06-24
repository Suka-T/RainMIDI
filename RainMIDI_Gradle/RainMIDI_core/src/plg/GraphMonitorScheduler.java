package plg;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jlib.core.JMPCoreAccessor;
import jlib.midi.INotesMonitor;

public class GraphMonitorScheduler {

    private final ScheduledExecutorService osStateScheduler = Executors.newSingleThreadScheduledExecutor();

    private static final int CYCLIC_MILLS = 50;

    // 減少するときは、現在の値と目標値の少しずつ近づく（毎フレーム滑らかに減速）
    // 値を小さくするほどゆっくり変化し、大きくするほど素早く追従。
    private static final double TRACKING_GRAPH_DOWN_SPEED = 0.5;

    private LongRingBuffer npsBuffer = null;
    private LongRingBuffer polyBuffer = null;

    private long npsPeekMax = 100;
    private long npsPeekMin = 0;

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
            npsBuffer.add((long) notesMonitor.getNps());
            polyBuffer.add((long) notesMonitor.getPolyphony());
            npsBuffer.updateSnapshot();
            polyBuffer.updateSnapshot();

            long maxData = npsBuffer.getPeekMax();
            long minData = npsBuffer.getPeekMin();
            if (30 > maxData)
                maxData = 30;
            if (0 > minData)
                minData = 0;
            npsPeekMax = getTrackingPeekMax(npsPeekMax, maxData);
            npsPeekMin = minData;

            maxData = polyBuffer.getPeekMax();
            minData = polyBuffer.getPeekMin();
            if (30 > maxData)
                maxData = 30;
            if (0 > minData)
                minData = 0;
            polyPeekMax = getTrackingPeekMax(polyPeekMax, maxData);
            polyPeekMin = minData;
        }
    }

    public long getTrackingPeekMax(long peekMax, long nextMax) {
        long ret = peekMax;
        if (peekMax < nextMax) {
            // 増加するときは一瞬で追従
            ret = nextMax;
        }
        else if (peekMax > nextMax) {
            double trackingSpeed = TRACKING_GRAPH_DOWN_SPEED;
            long diff = peekMax - nextMax;

            ret = peekMax - Math.max(1, (long) ((double) diff * trackingSpeed));

            if (ret < nextMax)
                ret = nextMax; // 行き過ぎ防止
        }
        return ret;
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

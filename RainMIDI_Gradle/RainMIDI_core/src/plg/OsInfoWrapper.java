package plg;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OsInfoWrapper {
    
    private FloatRingBuffer usageCpu = new FloatRingBuffer(16);
    private FloatRingBuffer usageRam = new FloatRingBuffer(16);
    private com.sun.management.OperatingSystemMXBean osBean;
    private final ScheduledExecutorService osStateScheduler = Executors.newSingleThreadScheduledExecutor();

    public OsInfoWrapper() {
        osBean = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
        osStateScheduler.scheduleAtFixedRate(this::updateStats, 0, 1, TimeUnit.SECONDS);
    }
    
    public void exit() {
        osStateScheduler.shutdown();
    }
    
    private void updateStats() {
        double cpuVal = osBean.getSystemCpuLoad();
        usageCpu.add((float)cpuVal);
        usageCpu.updateSnapshot();
        
        long totalMem = osBean.getTotalPhysicalMemorySize();
        long freeMem  = osBean.getFreePhysicalMemorySize();
        long usedMem  = totalMem - freeMem;
        double ramVal = (double)usedMem / (double)totalMem;
        usageRam.add((float)ramVal);
        usageRam.updateSnapshot();
    }

    public float getUsageCpu() {
        return usageCpu.getCurentValue();
    }
    
    public float[] getUsageCpuBuffer() {
        return usageCpu.getSnapshot();
    }

    public float getUsageRam() {
        return usageRam.getCurentValue();
    }
    
    public float[] getUsageRamBuffer() {
        return usageRam.getSnapshot();
    }
}

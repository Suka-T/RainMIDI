package plg;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OsInfoWrapper {
    
    private double usageCpu = 0.0;
    private double usageRam = 0.0;
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
        usageCpu = osBean.getSystemCpuLoad();
        
        long totalMem = osBean.getTotalPhysicalMemorySize();
        long freeMem  = osBean.getFreePhysicalMemorySize();
        long usedMem  = totalMem - freeMem;
        usageRam = (double)usedMem / (double)totalMem;
    }

    public double getUsageCpu() {
        return usageCpu;
    }

    public double getUsageRam() {
        return usageRam;
    }

}

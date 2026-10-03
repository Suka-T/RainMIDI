package layout.parts;

import java.awt.Graphics;

import layout.parts.monitor.MonitorData;
import plg.Utility;

public abstract class MonitorPainter {

    public MonitorPainter() {
    }

    public void initialize() {

    }

    public void formatWithCommas(long number, StringBuilder out) {
        Utility.formatWithCommas(number, out);
    }

    public abstract void paintMonitor(Graphics g, MonitorData info);
}

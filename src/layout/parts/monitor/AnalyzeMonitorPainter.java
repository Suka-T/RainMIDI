package layout.parts.monitor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.text.DecimalFormat;

import jlib.core.JMPCoreAccessor;
import jlib.midi.IMidiUnit;
import jlib.midi.INotesMonitor;
import layout.LayoutManager;
import layout.parts.MonitorPainter;
import plg.SystemProperties;
import plg.SystemProperties.OsBeanWrapper;
import plg.Utility;

public class AnalyzeMonitorPainter extends MonitorPainter {
    private static final DecimalFormat DF = new DecimalFormat("0.0");
    
    private Font info1Font = null;
    private StringBuilder sb = new StringBuilder();
    private static final int FONT_SIZE = 28;
    public AnalyzeMonitorPainter() {
        if (Utility.isWindows()) {
            info1Font = new Font("Calibri", Font.PLAIN, FONT_SIZE);
        }
        else {
            info1Font = new Font(Font.SANS_SERIF, Font.PLAIN, FONT_SIZE);
        }
    }

    @Override
    public void paintMonitor(Graphics g, MonitorData info) {
        INotesMonitor notesMonitor = JMPCoreAccessor.getSoundManager().getNotesMonitor();
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        
        int sx = 10;
        int sy = FONT_SIZE + 2;
        int sh = FONT_SIZE;
        Color backStrColor = LayoutManager.getInstance().getPlayerColor().getBgColor();
        Color topStrColor = LayoutManager.getInstance().getPlayerColor().getBgRevColor();
        ;
        g.setFont(info1Font);

        sb.setLength(0);
        sb.append("TIME: ");
        long val1 = JMPCoreAccessor.getSoundManager().getPositionSecond() / 60;
        if (val1 < 10)
            sb.append('0');
        sb.append(val1);
        sb.append(":");
        long val2 = JMPCoreAccessor.getSoundManager().getPositionSecond() % 60;
        if (val2 < 10)
            sb.append('0');
        sb.append(val2);
        sb.append(" / ");
        val1 = JMPCoreAccessor.getSoundManager().getLengthSecond() / 60;
        if (val1 < 10)
            sb.append('0');
        sb.append(val1);
        sb.append(":");
        val2 = JMPCoreAccessor.getSoundManager().getLengthSecond() % 60;
        if (val2 < 10)
            sb.append('0');
        sb.append(val2);
        g.setColor(backStrColor);
        g.drawString(sb.toString(), sx + 1, sy + 1);
        g.setColor(topStrColor);
        g.drawString(sb.toString(), sx, sy);
        sy += sh;

        sb.setLength(0);
        sb.append("TICK: ");
        val1 = JMPCoreAccessor.getSoundManager().getMidiUnit().getTickPosition();
        formatWithCommas(val1, sb);
        g.setColor(backStrColor);
        g.drawString(sb.toString(), sx + 1, sy + 1);
        g.setColor(topStrColor);
        g.drawString(sb.toString(), sx, sy);
        sy += sh;

        sb.setLength(0);
        sb.append("NOTES: ");
        val1 = notesMonitor.getNotesCount();
        formatWithCommas(val1, sb);
        g.setColor(backStrColor);
        g.drawString(sb.toString(), sx + 1, sy + 1);
        g.setColor(topStrColor);
        g.drawString(sb.toString(), sx, sy);
        sy += sh;

        sb.setLength(0);
        sb.append("MAX NT: ");
        val2 = notesMonitor.getNumOfNotes();
        formatWithCommas(val2, sb);
        g.setColor(backStrColor);
        g.drawString(sb.toString(), sx + 1, sy + 1);
        g.setColor(topStrColor);
        g.drawString(sb.toString(), sx, sy);
        sy += sh;

        sb.setLength(0);
        val1 = (long) notesMonitor.getNps();
        sb.append("NPS: ");
        formatWithCommas(val1, sb);
        sb.append(" / ");
        val1 = (long) notesMonitor.getMaxNps();
        formatWithCommas(val1, sb);
        g.setColor(backStrColor);
        g.drawString(sb.toString(), sx + 1, sy + 1);
        g.setColor(topStrColor);
        g.drawString(sb.toString(), sx, sy);
        sy += sh;

        if (midiUnit.isRenderingOnlyMode() == false) {
            sb.setLength(0);
            val1 = (long) notesMonitor.getPolyphony();
            sb.append("POLY: ");
            formatWithCommas(val1, sb);
            val1 = (long) notesMonitor.getMaxPolyphony();
            sb.append(" / ");
            formatWithCommas(val1, sb);
            g.setColor(backStrColor);
            g.drawString(sb.toString(), sx + 1, sy + 1);
            g.setColor(topStrColor);
            g.drawString(sb.toString(), sx, sy);
            sy += sh;
        }

        sb.setLength(0);
        val1 = (int) midiUnit.getTempoInBPM();
        val2 = (int) ((midiUnit.getTempoInBPM() - val1) * 100);
        sb.append("BPM: ").append(val1).append(".").append(val2);
        g.setColor(backStrColor);
        g.drawString(sb.toString(), sx + 1, sy + 1);
        g.setColor(topStrColor);
        g.drawString(sb.toString(), sx, sy);
        sy += sh;
        
        sy += (sh / 2);
        
        OsBeanWrapper osBeasW = SystemProperties.getInstance().getOsBeanWrapper();

        sb.setLength(0);
        sb.append("CPU: ").append(DF.format(osBeasW.usageCpu * 100.0)).append("%");
        g.setColor(backStrColor);
        g.drawString(sb.toString(), sx + 1, sy + 1);
        g.setColor(topStrColor);
        g.drawString(sb.toString(), sx, sy);
        sy += sh;
        
        sb.setLength(0);
        sb.append("RAM: ").append(DF.format(osBeasW.usageRam * 100.0)).append("%");
        g.setColor(backStrColor);
        g.drawString(sb.toString(), sx + 1, sy + 1);
        g.setColor(topStrColor);
        g.drawString(sb.toString(), sx, sy);
        sy += sh;
        
        sb.setLength(0);
        val1 = info.fps;
        sb.append("FPS: ").append(val1);
        g.setColor(backStrColor);
        g.drawString(sb.toString(), sx + 1, sy + 1);
        g.setColor(topStrColor);
        g.drawString(sb.toString(), sx, sy);
        sy += sh;
        
    }

}

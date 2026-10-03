package layout.parts.monitor;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import jlib.core.JMPCoreAccessor;
import jlib.midi.IMidiUnit;
import jlib.midi.INotesMonitor;
import layout.LayoutManager;
import layout.parts.MonitorPainter;
import plg.GraphMonitorScheduler;
import plg.SystemProperties;
import plg.Utility;

public class GraphMonitorPainter extends MonitorPainter {
    private Font info1Font = null;
    private StringBuilder sb = new StringBuilder();
    private static final int FONT_SIZE = 28;
    
    private ChartDrawer npsChart = new ChartDrawer("NPS", new Color(255, 255, 0, 220));
    private ChartDrawer polyChart = new ChartDrawer("POLY", new Color(255, 100, 220, 220));

    public GraphMonitorPainter() {
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
        GraphMonitorScheduler graphMonSche = SystemProperties.getInstance().getGraphMonScheduler();

        int sx = 10;
        int sy = FONT_SIZE + 2;
        int sh = FONT_SIZE;

        int grapW = 100;
        int grapH = 60;
        int grapX = sx;
        int grapY = sy + 20;
        if (!SystemProperties.getInstance().isVisibleRsrcMonitor()) {
            grapW = 200;
            grapH = 80;
        }
        long[] data;
        long dataMax = 0;

        Graphics2D gGrap = (Graphics2D) g;
        gGrap.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color backStrColor = LayoutManager.getInstance().getFontColor().getBdColor();
        Color topStrColor = LayoutManager.getInstance().getFontColor().getBgColor();
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

//        sb.setLength(0);
//        sb.append("TICK: ");
//        val1 = JMPCoreAccessor.getSoundManager().getMidiUnit().getTickPosition();
//        formatWithCommas(val1, sb);
//        g.setColor(backStrColor);
//        g.drawString(sb.toString(), sx + 1, sy + 1);
//        g.setColor(topStrColor);
//        g.drawString(sb.toString(), sx, sy);
//        sy += sh;

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
        sb.append("MAX NPS: ");
        val2 = (long) notesMonitor.getMaxNps();
        formatWithCommas(val2, sb);
        g.setColor(backStrColor);
        g.drawString(sb.toString(), sx + 1, sy + 1);
        g.setColor(topStrColor);
        g.drawString(sb.toString(), sx, sy);
        sy += sh;

        if (midiUnit.isRenderingOnlyMode() == false) {
            sb.setLength(0);
            sb.append("MAX POLY: ");
            val2 = (long) notesMonitor.getMaxPolyphony();
            formatWithCommas(val2, sb);
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

        // データの点と点を線で結ぶ
        grapX = sx;
        grapY = sy - 10;
        dataMax = graphMonSche.getNpsPeekMax();
        data = graphMonSche.getNpsSnapshot();
        npsChart.setData(data, (long) notesMonitor.getNps(), dataMax);
        npsChart.setVisibleCurrent(true);
        npsChart.drawGraph(gGrap, grapX, grapY, grapW, grapH);
        sy += grapH;

        if (midiUnit.isRenderingOnlyMode() == false) {
            // データの点と点を線で結ぶ2
            grapX = sx;
            grapY = sy + sh - 10;
            dataMax = graphMonSche.getPolyPeekMax();
            data = graphMonSche.getPolySnapshot();
            polyChart.setData(data, (long) notesMonitor.getPolyphony(), dataMax);
            polyChart.setVisibleCurrent(true);
            polyChart.drawGraph(gGrap, grapX, grapY, grapW, grapH);
            sy += grapH;
        }
    }
}

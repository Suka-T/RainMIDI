package layout.parts.monitor;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import jlib.core.JMPCoreAccessor;
import jlib.midi.IMidiUnit;
import jlib.midi.INotesMonitor;
import layout.LayoutManager;
import layout.parts.MonitorPainter;
import plg.GraphMonitorScheduler;
import plg.SystemProperties;
import plg.Utility;

public class NotesCountMonitorPainter extends MonitorPainter {

    private Font info2Font = null;
    private StringBuilder sb = new StringBuilder();
    
    private ChartDrawer npsChart = new ChartDrawer("NPS", Color.CYAN);
    private ChartDrawer polyChart = new ChartDrawer("POLY", Color.PINK);

    public NotesCountMonitorPainter() {
        if (Utility.isWindows()) {
            info2Font = new Font("Calibri", Font.PLAIN, 64);
        }
        else {
            info2Font = new Font(Font.SANS_SERIF, Font.PLAIN, 64);
        }
    }

    @Override
    public void paintMonitor(Graphics g, MonitorData info) {
        INotesMonitor notesMonitor = JMPCoreAccessor.getSoundManager().getNotesMonitor();
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        GraphMonitorScheduler graphMonSche = SystemProperties.getInstance().getGraphMonScheduler();

        int sx = 0;
        int sy = 65;
        Color backStrColor = LayoutManager.getInstance().getFontColor().getBdColor();
        Color topStrColor = LayoutManager.getInstance().getFontColor().getBgColor();

        g.setFont(info2Font);

        sb.setLength(0);

        long val1 = 0;
        sb.setLength(0);
        val1 = notesMonitor.getNotesCount();
        formatWithCommas(val1, sb);

        String text = sb.toString();
        FontMetrics fm = g.getFontMetrics();
        int width = fm.stringWidth(text);

        sx = (info.width - width) / 2;
        g.setColor(backStrColor);
        g.drawString(sb.toString(), sx + 1, sy + 1);
        g.setColor(topStrColor);
        g.drawString(sb.toString(), sx, sy);
        
        sy = 5;
        
        int graphCntr = (info.width / 2);
        int grapMergin = 210;
        int grapW = 256;
        int grapH = 96;
        int grapX = graphCntr - grapW - grapMergin;
        int grapY = sy;
        long dataMax = graphMonSche.getNpsPeekMax();
        long[] data = graphMonSche.getNpsSnapshot();
        npsChart.setData(data, (long) notesMonitor.getNps(), dataMax);
        npsChart.setVisibleCurrent(false);
        npsChart.drawGraph((Graphics2D)g, grapX, grapY, grapW, grapH);

        if (midiUnit.isRenderingOnlyMode() == false) {
            grapX = graphCntr + grapMergin;
            grapY = sy;
            dataMax = graphMonSche.getPolyPeekMax();
            data = graphMonSche.getPolySnapshot();
            polyChart.setData(data, (long) notesMonitor.getPolyphony(), dataMax);
            polyChart.setVisibleCurrent(false);
            polyChart.drawGraph((Graphics2D)g, grapX, grapY, grapW, grapH);
            sy += grapH;
        }
    }

}

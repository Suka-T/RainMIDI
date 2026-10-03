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

    private ChartDrawer npsChart = new ChartDrawer("NPS", new Color(255, 255, 0, 220));
    private ChartDrawer polyChart = new ChartDrawer("POLY", new Color(255, 100, 220, 220));

    private static final Color[] GLOW_COLORS = {
            new Color(100, 200, 255, 20),
            new Color(140, 190, 255, 35),
            new Color(190, 165, 255, 55),
            new Color(230, 150, 245, 80)
        };

    private static final Color TEXT_COLOR = new Color(255, 235, 255, 255);

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

        drawGlowString(g, sx, sy, sb.toString());

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
        npsChart.drawGraph((Graphics2D) g, grapX, grapY, grapW, grapH);

        if (midiUnit.isRenderingOnlyMode() == false) {
            grapX = graphCntr + grapMergin;
            grapY = sy;
            dataMax = graphMonSche.getPolyPeekMax();
            data = graphMonSche.getPolySnapshot();
            polyChart.setData(data, (long) notesMonitor.getPolyphony(), dataMax);
            polyChart.setVisibleCurrent(false);
            polyChart.drawGraph((Graphics2D) g, grapX, grapY, grapW, grapH);
            sy += grapH;
        }
    }
    
    public void drawGlowString(Graphics g, int x, int y, String str) {
        if (SystemProperties.getInstance().isValidGlowFont()) {
            for (int i = 8, index = 0; i >= 2; i -= 2, index++) {
                g.setColor(GLOW_COLORS[index]);

                g.drawString(str, x - i, y);
                g.drawString(str, x + i, y);
                g.drawString(str, x, y - i);
                g.drawString(str, x, y + i);
            }

            g.setColor(TEXT_COLOR);
            g.drawString(str, x, y);
        }
        else {
            Color backStrColor = LayoutManager.getInstance().getFontColor().getBdColor();
            Color topStrColor = LayoutManager.getInstance().getFontColor().getBgColor();
            g.setColor(backStrColor);
            g.drawString(str, x + 1, y + 1);
            g.setColor(topStrColor);
            g.drawString(str, x, y);
        }
    }

}

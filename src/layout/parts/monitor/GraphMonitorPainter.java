package layout.parts.monitor;

import java.awt.BasicStroke;
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
import plg.LongRingBuffer;
import plg.SystemProperties;
import plg.Utility;

public class GraphMonitorPainter extends MonitorPainter {
    private Font info1Font = null;
    private StringBuilder sb = new StringBuilder();
    private static final int FONT_SIZE = 28;
    
    private static final BasicStroke GRAPH_BORDER_STROKE = new BasicStroke(
            1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke GRAPH_FRAMEBORDER_STROKE = new BasicStroke(2.0f);
    private static final Font GRAPH_FONT = new Font(SystemProperties.getInstance().getGeneralFontName(), Font.PLAIN, 12);
    private static final Font GRAPH_TITLE_FONT = new Font(SystemProperties.getInstance().getGeneralFontName(), Font.PLAIN, 21);
    private static Color GRAPH_BG_COLOR = new Color(0, 0, 0, 100);

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
        
        LongRingBuffer npsBuffer = SystemProperties.getInstance().getNpsBuffer();
        LongRingBuffer polyBuffer = SystemProperties.getInstance().getPolyBuffer();
        
        int sx = 10;
        int sy = FONT_SIZE + 2;
        int sh = FONT_SIZE;
        
        int grapW = 100;
        int grapH = 60;
        int grapX = sx;
        int grapY = sy + 20;
        int gwRes = 0;
        long[] data;
        long dataMax = 0;
        npsBuffer.add((long)notesMonitor.getNps());
        polyBuffer.add((long)notesMonitor.getPolyphony());
        npsBuffer.updateSnapshot();
        polyBuffer.updateSnapshot();
        
        Graphics2D gGrap = (Graphics2D) g;
        gGrap.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
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
        val1 = (int) midiUnit.getTempoInBPM();
        val2 = (int) ((midiUnit.getTempoInBPM() - val1) * 100);
        sb.append("BPM: ").append(val1).append(".").append(val2);
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
        
        // データの点と点を線で結ぶ
        grapX = sx;
        grapY = sy - 10;
        sb.setLength(0);
        sb.append("NPS");
        gGrap.setFont(GRAPH_TITLE_FONT);
        gGrap.setColor(Color.WHITE);
        gGrap.drawString(sb.toString(), grapX + 2, grapY + 21);
        gGrap.setColor(GRAPH_BG_COLOR);
        gGrap.fillRect(grapX, grapY, grapW, grapH);
        gGrap.setStroke(GRAPH_BORDER_STROKE);
        data = npsBuffer.getSnapshot();
        dataMax = (long) notesMonitor.getMaxNps();
        if (dataMax < 32) {
            dataMax = 32;
        }
        gwRes = data.length - 1;
        gGrap.setColor(Color.CYAN);
        gGrap.setStroke(GRAPH_BORDER_STROKE);
        for (int i = 0; i < data.length - 1; i++) {
            long dt1 = data[i] < 0 ? 0 : data[i];
            long dt2 = data[i + 1] < 0 ? 0 : data[i + 1];
            int x1 = grapX + (i * grapW / gwRes);
            int y1 = grapY + (grapH - (int) (dt1 * grapH / dataMax));
            int x2 = grapX + ((i + 1) * grapW / gwRes);
            int y2 = grapY + (grapH - (int) (dt2 * grapH / dataMax));
            gGrap.drawLine(x1, y1, x2, y2);
        }
        sb.setLength(0);
        val1 = (long) notesMonitor.getNps();
        formatWithCommas(val1, sb);
        sb.append(" / ");
        val1 = (long) notesMonitor.getMaxNps();
        formatWithCommas(val1, sb);
        gGrap.setFont(GRAPH_FONT);
        gGrap.setColor(backStrColor);
        gGrap.drawString(sb.toString(), grapX + 1, grapY + grapH + 16);
        gGrap.setColor(topStrColor);
        gGrap.drawString(sb.toString(), grapX, grapY + grapH + 15);
        gGrap.setStroke(GRAPH_FRAMEBORDER_STROKE);
        gGrap.setColor(Color.WHITE);
        gGrap.drawRect(grapX - 1, grapY, grapW + 2, grapH + 1);
        sy += grapH;

        if (midiUnit.isRenderingOnlyMode() == false) {
            // データの点と点を線で結ぶ2
            grapX = sx;
            grapY = sy + sh - 10;
            sb.setLength(0);
            sb.append("POLY");
            gGrap.setFont(GRAPH_TITLE_FONT);
            gGrap.setColor(Color.WHITE);
            gGrap.drawString(sb.toString(), grapX + 2, grapY + 21);
            gGrap.setColor(GRAPH_BG_COLOR);
            gGrap.fillRect(grapX, grapY, grapW, grapH);
            gGrap.setStroke(GRAPH_BORDER_STROKE);
            data = polyBuffer.getSnapshot();
            dataMax = (long) notesMonitor.getMaxPolyphony();
            if (dataMax < 32) {
                dataMax = 32;
            }
            gwRes = data.length - 1;
            gGrap.setColor(Color.PINK);
            gGrap.setStroke(GRAPH_BORDER_STROKE);
            for (int i = 0; i < data.length - 1; i++) {
                long dt1 = data[i] < 0 ? 0 : data[i];
                long dt2 = data[i + 1] < 0 ? 0 : data[i + 1];
                int x1 = grapX + (i * grapW / gwRes);
                int y1 = grapY + (grapH - (int) (dt1 * grapH / dataMax));
                int x2 = grapX + ((i + 1) * grapW / gwRes);
                int y2 = grapY + (grapH - (int) (dt2 * grapH / dataMax));
                gGrap.drawLine(x1, y1, x2, y2);
            }
            sb.setLength(0);
            val1 = (long) notesMonitor.getPolyphony();
            formatWithCommas(val1, sb);
            val1 = (long) notesMonitor.getMaxPolyphony();
            sb.append(" / ");
            formatWithCommas(val1, sb);
            gGrap.setFont(GRAPH_FONT);
            gGrap.setColor(backStrColor);
            gGrap.drawString(sb.toString(), grapX + 1, grapY + grapH + 16);
            gGrap.setColor(topStrColor);
            gGrap.drawString(sb.toString(), grapX, grapY + grapH + 15);
            gGrap.setStroke(GRAPH_FRAMEBORDER_STROKE);
            gGrap.setColor(Color.WHITE);
            gGrap.drawRect(grapX - 1, grapY, grapW + 2, grapH + 1);
            sy += grapH;
        }
    }

}

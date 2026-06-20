package layout.parts.monitor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
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
    
    private static final BasicStroke GRAPH_BORDER_STROKE = new BasicStroke(
            1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke GRAPH_FRAMEBORDER_STROKE = new BasicStroke(2.0f);
    private static final Font GRAPH_FONT = new Font(SystemProperties.getInstance().getGeneralFontName(), Font.PLAIN, 14);
    private static final Font GRAPH_GUIDE_FONT = new Font(SystemProperties.getInstance().getGeneralFontName(), Font.PLAIN, 10);
    private static final Font GRAPH_TITLE_FONT = new Font(SystemProperties.getInstance().getGeneralFontName(), Font.PLAIN, 21);
    private static Color GRAPH_TITLE_COLOR = new Color(255, 255, 255, 255);
    private static Color GRAPH_BG_COLOR = new Color(0, 0, 0, 100);
    
    private static final BasicStroke GRAPH_GUIDE_STROKE = new BasicStroke(1.0f);
    private static Color GRAPH_GUIDE_COLOR = new Color(225, 225, 225, 128);
    private static Color GRAPH_GUIDE_TEXT_COLOR = new Color(225, 225, 225, 180);

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
        
        Color backStrColor = LayoutManager.getInstance().getPlayerColor().getBgColor();
        Color topStrColor = LayoutManager.getInstance().getPlayerColor().getBgRevColor();
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
        data = graphMonSche.getNpsSnapshot();
        dataMax = graphMonSche.getNpsPeekMax();
        sb.setLength(0);
        drawGraph(gGrap, sb, grapX, grapY, grapW, grapH, data, dataMax, "NPS", Color.CYAN);
        
        sb.setLength(0);
        val1 = (long) notesMonitor.getNps();
        formatWithCommas(val1, sb);
        gGrap.setFont(GRAPH_FONT);
        gGrap.setColor(backStrColor);
        gGrap.drawString(sb.toString(), grapX + 1, grapY + grapH + 18);
        gGrap.setColor(topStrColor);
        gGrap.drawString(sb.toString(), grapX, grapY + grapH + 17);
        gGrap.setStroke(GRAPH_FRAMEBORDER_STROKE);
        gGrap.setColor(Color.WHITE);
        gGrap.drawRect(grapX - 1, grapY, grapW + 2, grapH + 1);
        sy += grapH;

        if (midiUnit.isRenderingOnlyMode() == false) {
            // データの点と点を線で結ぶ2
            grapX = sx;
            grapY = sy + sh - 10;
            data = graphMonSche.getPolySnapshot();
            dataMax = graphMonSche.getPolyPeekMax();
            sb.setLength(0);
            drawGraph(gGrap, sb, grapX, grapY, grapW, grapH, data, dataMax, "POLY", Color.PINK);

            sb.setLength(0);
            val1 = (long) notesMonitor.getPolyphony();
            formatWithCommas(val1, sb);
            gGrap.setFont(GRAPH_FONT);
            gGrap.setColor(backStrColor);
            gGrap.drawString(sb.toString(), grapX + 1, grapY + grapH + 18);
            gGrap.setColor(topStrColor);
            gGrap.drawString(sb.toString(), grapX, grapY + grapH + 17);
            gGrap.setStroke(GRAPH_FRAMEBORDER_STROKE);
            gGrap.setColor(Color.WHITE);
            gGrap.drawRect(grapX - 1, grapY, grapW + 2, grapH + 1);
            sy += grapH;
        }
    }

    private void drawGraph(Graphics2D gGrap, StringBuilder sb, int grapX, int grapY, int grapW, int grapH, long[] data, long dataMax, String title, Color graphColor) {
        sb.append(title);
        gGrap.setFont(GRAPH_TITLE_FONT);
        gGrap.setColor(GRAPH_BG_COLOR);
        gGrap.fillRect(grapX, grapY, grapW, grapH);
        gGrap.setColor(GRAPH_TITLE_COLOR);
        gGrap.drawString(sb.toString(), grapX + 2, grapY + 21);
        int gwRes = data.length - 1;
        
        gGrap.setFont(GRAPH_GUIDE_FONT);
        
        long step = 50;
        sb.setLength(0);
        if (dataMax > 0) {
            double rawStep = (double) dataMax / 3.8; 
            double log10 = Math.log10(rawStep);
            double power = Math.pow(10, Math.floor(log10));
            double normalized = rawStep / power;

            if (normalized < 1.2)       step = (long) (1 * power);
            else if (normalized < 2.5)  step = (long) (2 * power);
            else if (normalized < 6.0)  step = (long) (5 * power); // 500Kなどの「5」を維持
            else                        step = (long) (10 * power);

            if (step < 10) step = 10; 
        }
        if (step <= 0) step = 10;

        // --- 2. ガイドラインの描画ループ（ここは変更なし、自動で2本の線になります） ---
        gGrap.setStroke(GRAPH_GUIDE_STROKE);
    	for (long lineVal = step; lineVal < dataMax; lineVal += step) {
            
            int lineY = grapY + (grapH - (int) (lineVal * grapH / dataMax));
            gGrap.setColor(GRAPH_GUIDE_COLOR);
            gGrap.drawLine(grapX, lineY, grapX + grapW, lineY);
            
            sb.setLength(0);
            if (lineVal >= 1000000) {
                // --- M表記の処理 ---
                long remainder = lineVal % 1000000;
                sb.append((int)(lineVal / 1000000));
                
                // 10万の位が5なら「.5」を付け足す (例: 1,500,000 -> 1.5M)
                if (remainder >= 500000) {
                    sb.append(".5");
                }
                sb.append("M");
                
            } else if (lineVal >= 1000) {
                // --- K表記の処理 ---
                long remainder = lineVal % 1000;
                sb.append((int)(lineVal / 1000));
                
                // 100の位が5なら「.5」を付け足す (例: 1,500 -> 1.5K)
                if (remainder >= 500) {
                    sb.append(".5");
                }
                sb.append("K");
                
            } else {
                // 1000未満
                sb.append((int)(lineVal));
            }
            
            String label = sb.toString();
            FontMetrics fm = gGrap.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            
            gGrap.setColor(GRAPH_GUIDE_TEXT_COLOR);
            gGrap.drawString(label, grapX + grapW - labelWidth - 5, lineY + 10); 
        }
        gGrap.setColor(graphColor);
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
    }
}

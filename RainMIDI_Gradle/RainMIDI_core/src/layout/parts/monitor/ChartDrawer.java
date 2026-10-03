package layout.parts.monitor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import layout.LayoutManager;
import plg.SystemProperties;
import plg.Utility;

public class ChartDrawer {
    public static final BasicStroke GRAPH_BORDER_STROKE = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    public static final BasicStroke GRAPH_FRAMEBORDER_STROKE = new BasicStroke(2.0f);
    public static Color GRAPH_TITLE_COLOR = new Color(255, 255, 255, 255);
    public static Color GRAPH_BG_COLOR = new Color(0, 0, 0, 100);

    public static final BasicStroke GRAPH_GUIDE_STROKE = new BasicStroke(1.0f);
    public static Color GRAPH_GUIDE_COLOR = new Color(225, 225, 225, 128);
    public static Color GRAPH_GUIDE_TEXT_COLOR = new Color(225, 225, 225, 180);
    
    protected final Font GRAPH_FONT = new Font(SystemProperties.getInstance().getGeneralFontName(), Font.PLAIN, 14);
    protected final Font GRAPH_GUIDE_FONT = new Font(SystemProperties.getInstance().getGeneralFontName(), Font.PLAIN, 10);
    protected final Font GRAPH_TITLE_FONT = new Font(SystemProperties.getInstance().getGeneralFontName(), Font.PLAIN, 21);
    
    protected String title = "";
    protected Color graphColor;
    protected StringBuilder sb;
    
    protected long[] data;
    protected long dataMax = 0;
    protected long currentData = 0;
    protected boolean isVisibleCurrent = true;

    public ChartDrawer(String title, Color graphColor) {
        this.title = title;
        this.graphColor = graphColor;
        this.sb = new StringBuilder();
    }
    
    public void setData(long[] data, long current, long dataMax) {
        this.data = data;
        this.dataMax = dataMax;
        this.currentData = current;
    }

    public void drawGraph(Graphics2D gGrap, int grapX, int grapY, int grapW, int grapH) {
        sb.setLength(0);
        sb.append(title);
        gGrap.setFont(GRAPH_TITLE_FONT);
        gGrap.setColor(GRAPH_BG_COLOR);
        gGrap.fillRect(grapX, grapY, grapW, grapH);
        gGrap.setColor(GRAPH_TITLE_COLOR);
        gGrap.drawString(sb.toString(), grapX + 2, grapY + 21);
        int gwRes = data.length - 1;

        gGrap.setFont(GRAPH_GUIDE_FONT);

        // 天井に空間を設ける
        int areaY = grapY + 5;
        int areaH = grapH - 5;

        long step = 50;
        sb.setLength(0);
        if (dataMax > 0) {
            double rawStep = (double) dataMax / 3.8;
            double log10 = Math.log10(rawStep);
            double power = Math.pow(10, Math.floor(log10));
            double normalized = rawStep / power;

            if (normalized < 1.2)
                step = (long) (1 * power);
            else if (normalized < 2.5)
                step = (long) (2 * power);
            else if (normalized < 6.0)
                step = (long) (5 * power); // 500Kなどの「5」を維持
            else
                step = (long) (10 * power);

            if (step < 10)
                step = 10;
        }
        if (step <= 0)
            step = 10;

        // ガイドラインの描画ループ
        gGrap.setStroke(GRAPH_GUIDE_STROKE);
        
        long lineVal = step;
        for (; lineVal < dataMax; lineVal += step) {

            int lineY = areaY + (areaH - (int) (lineVal * areaH / dataMax));
            gGrap.setColor(GRAPH_GUIDE_COLOR);
            gGrap.drawLine(grapX, lineY, grapX + grapW, lineY);

            sb.setLength(0);
            if (lineVal >= 1000000) {
                // --- M表記の処理 ---
                long remainder = lineVal % 1000000;
                sb.append((int) (lineVal / 1000000));

                // 10万の位が5なら「.5」を付け足す (例: 1,500,000 -> 1.5M)
                if (remainder >= 500000) {
                    sb.append(".5");
                }
                sb.append("M");

            }
            else if (lineVal >= 1000) {
                // --- K表記の処理 ---
                long remainder = lineVal % 1000;
                sb.append((int) (lineVal / 1000));

                // 100の位が5なら「.5」を付け足す (例: 1,500 -> 1.5K)
                if (remainder >= 500) {
                    sb.append(".5");
                }
                sb.append("K");

            }
            else {
                // 1000未満
                sb.append((int) (lineVal));
            }

            String label = sb.toString();
            FontMetrics fm = gGrap.getFontMetrics();
            int labelWidth = fm.stringWidth(label);

            gGrap.setColor(GRAPH_GUIDE_TEXT_COLOR);
            gGrap.drawString(label, grapX + grapW - labelWidth - 5, lineY + 10);
        }
        gGrap.setColor(graphColor);
        gGrap.setStroke(GRAPH_BORDER_STROKE);
        
        long dt1, dt2;
        int x1, x2, y1, y2;
        int i = 0;
        for (i = 0; i < data.length - 1; i++) {
            dt1 = data[i] < 0 ? 0 : data[i];
            dt2 = data[i + 1] < 0 ? 0 : data[i + 1];
            x1 = grapX + (i * grapW / gwRes);
            y1 = areaY + (areaH - (int) (dt1 * areaH / dataMax));
            x2 = grapX + ((i + 1) * grapW / gwRes);
            y2 = areaY + (areaH - (int) (dt2 * areaH / dataMax));
            gGrap.drawLine(x1, y1, x2, y2);
        }

        gGrap.setStroke(GRAPH_FRAMEBORDER_STROKE);
        gGrap.setColor(Color.WHITE);
        gGrap.drawRect(grapX - 1, grapY, grapW + 2, grapH + 1);
        
        if (this.isVisibleCurrent == true) {
            Color backStrColor = LayoutManager.getInstance().getFontColor().getBdColor();
            Color topStrColor = LayoutManager.getInstance().getFontColor().getBgColor();
            sb.setLength(0);
            Utility.formatWithCommas(this.currentData, sb);
            gGrap.setFont(GRAPH_FONT);
            gGrap.setColor(backStrColor);
            gGrap.drawString(sb.toString(), grapX + 1, grapY + grapH + 18);
            gGrap.setColor(topStrColor);
            gGrap.drawString(sb.toString(), grapX, grapY + grapH + 17);
        }
    }

    public void setVisibleCurrent(boolean isVisibleCurrent) {
        this.isVisibleCurrent = isVisibleCurrent;
    }
}

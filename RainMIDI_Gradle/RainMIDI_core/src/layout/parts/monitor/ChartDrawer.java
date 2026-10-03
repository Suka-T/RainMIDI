package layout.parts.monitor;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

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
    
    private final AlphaComposite alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
    private final Color outGrowColor = new Color(40, 120, 255, 30);
    private final Color midGrowColor = new Color(80, 200, 255, 100);
    private final Color coreColor = new Color(235, 250, 255, 230);
    private final BasicStroke outGrowStroke = new BasicStroke(8.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private final BasicStroke midGrowStroke = new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private final BasicStroke coreStroke = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private BufferedImage lineImage = null;
    
    private static final Color[] GLOW_COLORS = {
            new Color(100, 200, 255, 20),
            new Color(140, 190, 255, 35),
            new Color(190, 165, 255, 55),
            new Color(230, 150, 245, 80)
        };

    private static final Color TEXT_COLOR = new Color(255, 235, 255, 255);
    
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
        drawGlowString(gGrap, grapX + 2, grapY + 21, sb.toString());
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
        drawGrowRect(gGrap, grapX - 1, grapY, grapW + 2, grapH + 1);
    }
    
    public void drawGrowRect(Graphics2D g, int dx, int dy, int dw, int dh) {
        if (lineImage == null || lineImage.getWidth() != dw || lineImage.getHeight() != dh) {
            int x = 5;
            int y = 5;
            int w = dw;
            int h = dh;
                    
            lineImage = new BufferedImage(w + 10, h + 10, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = (Graphics2D) lineImage.createGraphics();
            
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
            // 加算合成っぽくする
            Composite oldComp = g2.getComposite();
            g2.setComposite(alphaComp);
    
            /* 外側グロー（ぼかし） */
            g2.setStroke(outGrowStroke);
            g2.setColor(outGrowColor); // 薄い水色
            g2.drawRect(x, y, w, h);
    
            /* 中間グロー */
            g2.setStroke(midGrowStroke);
            g2.setColor(midGrowColor);
            g2.drawRect(x, y, w, h);
    
            /* コア（芯） */
            g2.setStroke(coreStroke);
            g2.setColor(coreColor);
            g2.drawRect(x, y, w, h);
    
            g2.setComposite(oldComp);
        }
        g.drawImage(lineImage, dx - 5, dy - 5, dw + 10, dh + 10, null);
    }

    public void setVisibleCurrent(boolean isVisibleCurrent) {
        this.isVisibleCurrent = isVisibleCurrent;
    }
    
    public void drawGlowString(Graphics g, int x, int y, String str) {
        if (SystemProperties.getInstance().isValidGlowFont()) {
            for (int i = 4, index = 0; i >= 1; i -= 1, index++) {
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

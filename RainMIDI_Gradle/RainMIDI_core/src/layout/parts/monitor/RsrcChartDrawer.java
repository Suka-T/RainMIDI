package layout.parts.monitor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.text.DecimalFormat;

public class RsrcChartDrawer extends ChartDrawer {
    private static final DecimalFormat DF = new DecimalFormat("0.0");
    
    protected float[] dataF;
    protected float dataMaxF = 0;
    protected float currentDataF = 0;
    
    public RsrcChartDrawer(String title, Color graphColor) {
        super(title, graphColor);
    }
    
    public void setDataFloat(float[] data, float current, float dataMax) {
        this.dataF = data;
        this.dataMaxF = dataMax;
        this.currentDataF = current;
    }
    
    public void drawGraph(Graphics2D gGrap, int grapX, int grapY, int grapW, int grapH) {
        sb.setLength(0);
        sb.append(title);
        gGrap.setColor(GRAPH_BG_COLOR);
        gGrap.fillRect(grapX, grapY, grapW, grapH);
        gGrap.setFont(GRAPH_TITLE_FONT);
        drawGlowString(gGrap, grapX + 2, grapY + 21, sb.toString());
        gGrap.setStroke(GRAPH_BORDER_STROKE);
        int gwRes = dataF.length - 1;
        gGrap.setColor(Color.GREEN);
        gGrap.setStroke(GRAPH_BORDER_STROKE);
        float dt1, dt2;
        int x1, x2, y1, y2;
        int i = 0;
        for (; i < dataF.length - 1; i++) {
            dt1 = dataF[i];
            dt2 = dataF[i + 1];
            x1 = grapX + (i * grapW / gwRes);
            y1 = grapY + (grapH - (int) (dt1 * grapH / 1.0f));
            x2 = grapX + ((i + 1) * grapW / gwRes);
            y2 = grapY + (grapH - (int) (dt2 * grapH / 1.0f));
            gGrap.drawLine(x1, y1, x2, y2);
        }
        sb.setLength(0);
        sb.append(DF.format(currentDataF * 100.0)).append("%");
        gGrap.setFont(GRAPH_FONT);
        gGrap.setColor(Color.WHITE);
        gGrap.drawString(sb.toString(), grapX, grapY + grapH + 17);
        gGrap.setStroke(GRAPH_FRAMEBORDER_STROKE);
        drawGrowRect(gGrap, grapX - 1, grapY, grapW + 2, grapH + 1);
    }
}

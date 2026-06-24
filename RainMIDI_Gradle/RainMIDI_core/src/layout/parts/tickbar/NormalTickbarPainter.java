package layout.parts.tickbar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import layout.parts.TickbarPainter;

public class NormalTickbarPainter extends TickbarPainter {

    private static final float NO_EFFE_LINE_CORE_STROKE_VAL = 8.0f; // 中心の線の太さ
    private static final float NO_EFFE_LINE_BORDER_WIDTH = 0.5f; // 白ボーダーの幅
    private static final BasicStroke NO_EFFE_LINE_BORDER_STROKE = new BasicStroke(NO_EFFE_LINE_CORE_STROKE_VAL + NO_EFFE_LINE_BORDER_WIDTH * 2,
            BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke NO_EFFE_LINE_CORE_STROKE = new BasicStroke(NO_EFFE_LINE_CORE_STROKE_VAL, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private void drawNoEffeLine(Graphics2D g2d, int x1, int y1, int x2, int y2, Color baseColor) {
        // ======= ボーダー線 =======
        g2d.setStroke(NO_EFFE_LINE_BORDER_STROKE);

        g2d.setColor(Color.BLACK);
        g2d.drawLine(x1, y1, x2, y2);

        // ======= 中心線（コア線） =======
        g2d.setStroke(NO_EFFE_LINE_CORE_STROKE);
        g2d.setColor(baseColor);
        g2d.drawLine(x1, y1, x2, y2);

        // ストロークを戻す
        g2d.setStroke(DEFAULT_STROKE);
    }

    @Override
    public void paintLine(Graphics g, int x1, int y1, int x2, int y2, Color lineColor) {
        drawNoEffeLine((Graphics2D) g, x1, y1, x2, y2, lineColor);
    }

}

package layout.parts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Stroke;

public abstract class TickbarPainter {
    protected static final Stroke DEFAULT_STROKE = new BasicStroke();

    public abstract void paintLine(Graphics g, int x1, int y1, int x2, int y2, Color lineColor);

    public void clearCache() {
    };
}

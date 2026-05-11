package layout.parts.tickbar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import layout.parts.TickbarPainter;

public class GlowTickbarPainter extends TickbarPainter {
    private List<BasicStroke> ousStrokes = null;
    private List<Color> ousColors = null;
    private BasicStroke mainStroke = null;
    private Color mainColor = null;
    private List<BasicStroke> mergeStrokes = null;
    private List<Color> mergeColors = null;
    private BasicStroke coreStroke = null;
    private Color coreColor = null;
    
    @Override
    public void clearCache() {
    	mainColor = null;
    };
    
    private static final float NO_EFFE_LINE_CORE_STROKE_VAL = 8.0f;     // 中心の線の太さ
    private static final BasicStroke NO_EFFE_LINE_CORE_STROKE = new BasicStroke(
            NO_EFFE_LINE_CORE_STROKE_VAL, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private void drawNoEffeLine(Graphics2D g2d, int x1, int y1, int x2, int y2, Color baseColor) {
        // ======= 中心線（コア線） =======
        g2d.setStroke(NO_EFFE_LINE_CORE_STROKE);
        g2d.setColor(baseColor);
        g2d.drawLine(x1, y1, x2, y2);

        // ストロークを戻す
        g2d.setStroke(DEFAULT_STROKE);
    }
    
    protected void paintGrawLine(Graphics g, int x1, int y1, int x2, int y2, Color saberColor) {
        Graphics2D g2 = (Graphics2D) g;
    
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (mainColor == null) { // 描画処理の軽量化 
            ousStrokes = new ArrayList<BasicStroke>();
            ousColors = new ArrayList<Color>();
            
            // ごく薄い外側の光（控えめ）
            for (int i = 8; i >= 1; i--) {
                float alpha = 0.01f * i;   // ← かなり薄い
                ousStrokes.add(new BasicStroke(12f + i * 2f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));
                
                ousColors.add(new Color(
                        saberColor.getRed(),
                        saberColor.getGreen(),
                        saberColor.getBlue(),
                        (int)(255 * alpha)));
            }
            
            // メインの色（白との境界用）
            mainStroke = new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            mainColor = new Color(saberColor.getRed(), saberColor.getGreen(), saberColor.getBlue(), 140);
            
            // 白→色のぼかしゾーン（最重要）
            mergeStrokes = new ArrayList<BasicStroke>();
            mergeColors = new ArrayList<Color>();
            for (int i = 3; i >= 1; i--) {
                float t = i / 3f; // 1.0 → 0.33
                int r = (int)(255 * (1 - t) + saberColor.getRed()   * t);
                int gC= (int)(255 * (1 - t) + saberColor.getGreen()* t);
                int b = (int)(255 * (1 - t) + saberColor.getBlue() * t);
        
                mergeStrokes.add(new BasicStroke(6f + i * 1.2f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));
                mergeColors.add(new Color(r, gC, b, 160));
            }
            
            // 中心の白い芯（太め）
            coreStroke = new BasicStroke(5.5f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND);
            coreColor = new Color(245, 250, 255);
        }
    
        // ごく薄い外側の光（控えめ）
        for (int j = 0; j < ousStrokes.size(); j++) {
            g2.setStroke(ousStrokes.get(j));
            g2.setColor(ousColors.get(j));
            g2.drawLine(x1, y1, x2, y2);
        }
    
        // メインの色（白との境界用）
        g2.setStroke(mainStroke);
        g2.setColor(mainColor);
        g2.drawLine(x1, y1, x2, y2);
    
        // 白→色のぼかしゾーン（最重要）
        for (int j = 0; j < mergeStrokes.size(); j++) {
            g2.setStroke(mergeStrokes.get(j));
            g2.setColor(mergeColors.get(j));
            g2.drawLine(x1, y1, x2, y2);
        }
    
        // 中心の白い芯（太め）
        g2.setStroke(coreStroke);
        g2.setColor(coreColor);
        g2.drawLine(x1, y1, x2, y2);
    }
    
	@Override
	public void paintLine(Graphics g, int x1, int y1, int x2, int y2, Color lineColor) {
		drawNoEffeLine((Graphics2D)g, x1, y1, x2, y2, lineColor);
		paintGrawLine(g, x1, y1, x2, y2, lineColor);
	}

}

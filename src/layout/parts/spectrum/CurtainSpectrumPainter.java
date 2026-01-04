package layout.parts.spectrum;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import layout.parts.SpectrumPainter;

public class CurtainSpectrumPainter extends SpectrumPainter {

    private final Color curtainColor = new Color(0, 220, 255, 140);
    
    public CurtainSpectrumPainter() {
    }

    @Override
    public void drawWave(Graphics2D g2, int w, int h, float[] spectWave, int spectSamples) {
        Path2D path = createClosePath(w, h, spectWave, spectSamples);
        g2.setColor(curtainColor);
        g2.fill(path);
    }

}

package layout.parts.spectrum;

import java.awt.Color;
import java.awt.Graphics2D;

import layout.parts.SpectrumPainter;

public class DigitalSpectrumPainter extends SpectrumPainter {
    
    private final Color curtainColor = new Color(0, 220, 255, 140);

    @Override
    public void drawWave(Graphics2D g2, int w, int h, float[] spectWave, int spectSamples) {

        int barCount = 64;

        int blockHeight = 6;
        int blockGap = 2;

        float barWidth = (float) w / barCount;

        g2.setColor(curtainColor);
        
        for (int i = 0; i < barCount; i++) {

            int index = i * spectSamples / barCount;

            float value = Math.abs(spectWave[index]) * 0.15f;
            value = Math.min(1.0f, value);

            int blocks = (int) (value * h / (blockHeight + blockGap));

            int x = (int) (i * barWidth);
            int blockWidth = (int) barWidth - 2;

            for (int j = 0; j < blocks; j++) {

                int y = h - (j + 1) * (blockHeight + blockGap);

                g2.fillRect(x, y, blockWidth, blockHeight);
            }
        }
    }

}

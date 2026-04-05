package layout.parts.spectrum;

import java.awt.Graphics;
import java.awt.Graphics2D;

import layout.parts.SpectrumPainter;

public class NoneSpectrumPainter extends SpectrumPainter {

    public NoneSpectrumPainter() {}
    
    @Override
    public void paintSpectram(Graphics g, int paneWidth, int paneHeight, float[] spectWave, float[] noiseBuf, int spectSamples) {
        /* 何もしない */
    }
    
    @Override
    protected void updateWavePoly(float[] spectWave, float[] noiseBuf, int spectSamples) {
        /* 何もしない */
    }

    @Override
    public void drawWave(Graphics2D g2, int w, int h, float[] spectWave, int spectSamples) {
        /* 何もしない */
    }
}

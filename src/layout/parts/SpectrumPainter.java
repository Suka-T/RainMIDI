package layout.parts;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import jlib.core.JMPCoreAccessor;
import jlib.midi.INotesMonitor;
import layout.LayoutConfig.EColorRule;
import layout.LayoutManager;
import plg.SystemProperties;
import plg.SystemProperties.SyspLayerOrder;
import plg.SystemProperties.SyspSpectrumPosition;

public abstract class SpectrumPainter {
    
    public SpectrumPainter() {}
    private Path2D path = new Path2D.Float();
    
    protected float noteToWaveFreq(int note) {
        // 表示向けレンジ（調整しやすい）
        float min = 0.5f;
        float max = 8.0f;

        float norm = (note - 36f) / 60f; // C2〜C7
        norm = Math.max(0f, Math.min(norm, 1f));

        return min + norm * (max - min);
    }

    public void paintSpectram(Graphics g, int paneWidth, int paneHeight, float[] spectWave, float[] noiseBuf, int spectSamples) {
        Graphics2D g2 = (Graphics2D)g.create();
        updateWavePoly(spectWave, noiseBuf, spectSamples);
        drawWave(g2, paneWidth, paneHeight, spectWave, spectSamples);
        g2.dispose();
    }
    
    protected void updateWavePoly(float[] spectWave, float[] noiseBuf, int spectSamples) {
        //phase += 0.05f;
        
        final float FIX_AMP = 32f;

        int poly = JMPCoreAccessor.getSoundManager().getNotesMonitor().getPolyphony();
        INotesMonitor notesMonitor = JMPCoreAccessor.getSoundManager().getNotesMonitor();
        boolean isAsc = false;
        if (SystemProperties.getInstance().getLayerOrder() == SyspLayerOrder.ASC) {
            isAsc = true;
        }
        
        for (int i = 0; i < spectSamples; i++) {

            noiseBuf[i] = noiseBuf[i] * 0.85f
                        + (float)(Math.random() * 2 - 1) * 0.15f;

            float x = (float)i / (spectSamples - 1);
            float v = 0f;

            for (int midiNo = 0; midiNo < 128; midiNo++) {
                int track = 0;
                if (LayoutManager.getInstance().getColorRule() == EColorRule.Track) {
                    track = notesMonitor.getTopNoteOnTrack(midiNo, isAsc);
                }
                else {
                    track = notesMonitor.getTopNoteOnChannel(midiNo, isAsc);
                }
                if (track != -1) {
                    float freq = noteToWaveFreq(midiNo);
                    float amp  = FIX_AMP / 127f;
    
                    v += Math.sin(2 * Math.PI * x * freq) * amp;
                }
            }

            if (poly > 0) {
                v /= Math.pow(poly, 0.25);
                v += noiseBuf[i] * (0.12f + 0.02f * poly);
                v = (float)Math.tanh(v * 1.1f);
            }
            spectWave[i] = spectWave[i] * 0.6f + v * 0.4f;
        }

    }
    
    protected Path2D createNoClosePath(int w, int h, float[] spectWave, int spectSamples) {
        // 最小値を探す
        float min = Float.MAX_VALUE;
        for (int i = 0; i < spectSamples; i++) {
            if (spectWave[i] < min) min = spectWave[i];
        }
        // 上に持ち上げる量（min が 0 になる）
        float offset = 0f;
        double dAmp = SystemProperties.getInstance().getSpectrumAmp();
        int amp = (int)((double)h * dAmp);
        
        int top = 0;
        if (SystemProperties.getInstance().getSpectrumPosition() == SyspSpectrumPosition.CENTER) {
            offset = 0f;
            top = h / 2;
        }
        else if (SystemProperties.getInstance().getSpectrumPosition() == SyspSpectrumPosition.TOP) {
            offset = (min < 0f) ? -min : 0f;
            top = 0;
        }
        else if (SystemProperties.getInstance().getSpectrumPosition() == SyspSpectrumPosition.BOTTOM) {
            offset = (min < 0f) ? -min : 0f;
            top = h;
        }

        path.reset();
        path.moveTo(0, top);
        for (int i = 0; i < spectSamples; i++) {
            int x = i * w / (spectSamples - 1);
            float v = spectWave[i] + offset;
            int y = 0;
            if (SystemProperties.getInstance().getSpectrumPosition() == SyspSpectrumPosition.CENTER) {
                y = top - (int)(v * amp);
            }
            else if (SystemProperties.getInstance().getSpectrumPosition() == SyspSpectrumPosition.TOP) {
                y = top + (int)(v * amp);
            }
            else if (SystemProperties.getInstance().getSpectrumPosition() == SyspSpectrumPosition.BOTTOM) {
                y = top - (int)(v * amp);
            }
            path.lineTo(x, y);
        }
        path.lineTo(w, top);
        
        return path;
    }
    
    protected Path2D createClosePath(int w, int h, float[] spectWave, int spectSamples) {
        Path2D path = createNoClosePath(w, h, spectWave, spectSamples);
        path.closePath();
        return path;
    }
    
    public abstract void drawWave(Graphics2D g2, int w, int h, float[] spectWave, int spectSamples);
}

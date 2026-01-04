package layout.parts.spectrum;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

import layout.parts.SpectrumPainter;

public class LightningSpectrumPainter extends SpectrumPainter {

    public LightningSpectrumPainter() {
    }

    @Override
    public void drawWave(Graphics2D g2, int w, int h, float[] spectWave, int spectSamples) {
        Path2D path = createNoClosePath(w, h, spectWave, spectSamples);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        // 加算合成っぽくする
        Composite oldComp = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 1.0f));

        /* =========================
         * 外側グロー（ぼかし）
         * ========================= */
        g2.setStroke(new BasicStroke(
                8.0f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));

        g2.setColor(new Color(80, 200, 255, 40)); // 薄い水色
        g2.draw(path);

        /* =========================
         * 中間グロー
         * ========================= */
        g2.setStroke(new BasicStroke(
                4.0f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));

        g2.setColor(new Color(80, 220, 255, 120));
        g2.draw(path);

        /* =========================
         * コア（芯）
         * ========================= */
        g2.setStroke(new BasicStroke(
                1.5f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));

        g2.setColor(new Color(220, 250, 255, 220));
        g2.draw(path);

        g2.setComposite(oldComp);
    }

}

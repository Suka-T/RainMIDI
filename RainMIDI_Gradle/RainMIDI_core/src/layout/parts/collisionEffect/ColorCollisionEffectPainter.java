package layout.parts.collisionEffect;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import layout.parts.CollisionEffectPainter;

public class ColorCollisionEffectPainter extends CollisionEffectPainter {

    public ColorCollisionEffectPainter() {
    }

    @Override
    public void paintIn(Graphics g, int x, int y, int keySize, Color focusColor, Color defColor) {
        /* 非対応 */
    }

    @Override
    public void paintOut(Graphics g, int x, int y, int keySize, Color focusColor, Color defColor) {
        Graphics2D g2d = (Graphics2D)g;
        int i = 0;
        int keyWidth = 120;
        int effeX = x;
        int effeY1 = y;
        int effeY2 = effeY1 + keySize - 1;
        int effeW = (int)((double)keyWidth * 0.2);
        float alpha = 1.0f;
        g2d.setColor(focusColor);
        for (; i < effeW; i++) {
            alpha = 1.0f - ((float)(i + 1) / (float)effeW);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.drawLine(effeX - i, effeY1, effeX - i, effeY2);
        }
        g2d.setComposite(AlphaComposite.SrcOver);
    }

}

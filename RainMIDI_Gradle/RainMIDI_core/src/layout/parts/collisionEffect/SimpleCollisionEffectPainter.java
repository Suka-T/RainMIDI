package layout.parts.collisionEffect;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import layout.parts.CollisionEffectPainter;

public class SimpleCollisionEffectPainter extends CollisionEffectPainter {
    
    public static final int HIT_EFFECT_STEPS = 16;
    
    protected AlphaComposite[] hitEffeSteps = null;

    public SimpleCollisionEffectPainter() {
        hitEffeSteps = new AlphaComposite[HIT_EFFECT_STEPS];
        for (int j = 0; j < 16; j++) {
            float alpha = (1.0f - ((float) j / 16.0f)) * 0.9f;
            hitEffeSteps[j] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        }
    }

    @Override
    public void paintIn(Graphics g, int x, int y, int keySize, Color focusColor, Color defColor) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(defColor);
        int effx = x;
        int keyHeight = keySize;
        int inEffWidth = 4;
        for (int j = 0; j < HIT_EFFECT_STEPS; j++) {
            g2d.setComposite(hitEffeSteps[j]);
            g2d.fillRect(effx + (inEffWidth * j), y, inEffWidth, keyHeight);
        }
        g2d.setComposite(AlphaComposite.SrcOver);
    }

    @Override
    public void paintOut(Graphics g, int x, int y, int keySize, Color focusColor, Color defColor) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(defColor);
        int effx = x;
        int keyHeight = keySize;
        int outEffWidth = 2;
        for (int j = 0; j < HIT_EFFECT_STEPS; j++) {
            g2d.setComposite(hitEffeSteps[j]);
            g2d.fillRect(effx - (outEffWidth * j) - outEffWidth, y, outEffWidth, keyHeight);
        }
        g2d.setComposite(AlphaComposite.SrcOver);
    }

}

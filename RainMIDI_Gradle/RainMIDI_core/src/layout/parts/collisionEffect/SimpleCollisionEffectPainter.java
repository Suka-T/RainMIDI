package layout.parts.collisionEffect;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import layout.parts.CollisionEffectPainter;

public class SimpleCollisionEffectPainter extends CollisionEffectPainter {

    public static final int HIT_EFFECT_STEPS = 16;

    protected AlphaComposite[] hitEffeSteps = null;
    
    private BufferedImage inImage = null;
    private BufferedImage outImage = null;

    public SimpleCollisionEffectPainter() {
        hitEffeSteps = new AlphaComposite[HIT_EFFECT_STEPS];
        for (int j = 0; j < 16; j++) {
            float alpha = (1.0f - ((float) j / 16.0f)) * 0.9f;
            hitEffeSteps[j] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        }
    }
    
    @Override
    public void preload() {
        super.preload();
        
        {
            int x = 0;
            int y = 0;
            int width = 4 * HIT_EFFECT_STEPS;
            int height = 20;
            inImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            
            Graphics2D g2d = (Graphics2D) inImage.createGraphics();
            g2d.setColor(Color.WHITE);
            int effx = x;
            int keyHeight = height;
            int inEffWidth = 4;
            for (int j = 0; j < HIT_EFFECT_STEPS; j++) {
                g2d.setComposite(hitEffeSteps[j]);
                g2d.fillRect(effx + (inEffWidth * j), y, inEffWidth, keyHeight);
            }
            g2d.setComposite(AlphaComposite.SrcOver);
            g2d.dispose();
        }
        
        {
            int x = 0;
            int y = 0;
            int width = 2 * HIT_EFFECT_STEPS;
            int height = 20;
            outImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            
            Graphics2D g2d = (Graphics2D) outImage.createGraphics();
            g2d.setColor(Color.WHITE);
            int keyHeight = height;
            int outEffWidth = 2;
            int effx = x + (outEffWidth * HIT_EFFECT_STEPS);
            for (int j = 0; j < HIT_EFFECT_STEPS; j++) {
                g2d.setComposite(hitEffeSteps[j]);
                g2d.fillRect(effx - (outEffWidth * j) - outEffWidth, y, outEffWidth, keyHeight);
            }
            g2d.setComposite(AlphaComposite.SrcOver);
            g2d.dispose();
        }
    }

    @Override
    public void paintIn(Graphics g, int x, int y, int keySize, Color focusColor, Color defColor) {
        int w = 4 * HIT_EFFECT_STEPS;
        int h = keySize;
        g.drawImage(inImage, x, y, w, h, null);
    }

    @Override
    public void paintOut(Graphics g, int x, int y, int keySize, Color focusColor, Color defColor) {
        int w = 2 * HIT_EFFECT_STEPS;
        int h = keySize;
        g.drawImage(outImage, x - w, y, w, h, null);
    }

}

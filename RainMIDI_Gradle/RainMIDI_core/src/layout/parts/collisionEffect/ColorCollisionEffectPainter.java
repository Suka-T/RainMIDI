package layout.parts.collisionEffect;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import layout.LayoutManager;
import layout.parts.CollisionEffectPainter;

public class ColorCollisionEffectPainter extends CollisionEffectPainter {

    public static final int HIT_EFFECT_STEPS = 16;
    
    protected Map<Integer, BufferedImage> imageMap = null;
    protected AlphaComposite[] hitEffeSteps = null;
    
    public ColorCollisionEffectPainter() {
        imageMap =  new HashMap<Integer, BufferedImage>();
        hitEffeSteps = new AlphaComposite[HIT_EFFECT_STEPS];
        for (int j = 0; j < 16; j++) {
            float alpha = (1.0f - ((float) j / 16.0f)) * 0.9f;
            hitEffeSteps[j] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        }
    }
    
    @Override
    public void preload() {
        super.preload();
        
        for (int ci = 0; ci < LayoutManager.getInstance().getNotesColorSize(); ci++) {
            int x = 0;
            int y = 0;
            int width = 2 * HIT_EFFECT_STEPS;
            int height = 20;
            Color col = LayoutManager.getInstance().getNotesColor(ci).getBgColor();
            
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) image.createGraphics();
            g2d.setColor(col);
            int keyHeight = height;
            int outEffWidth = 2;
            int effx = x + (outEffWidth * HIT_EFFECT_STEPS);
            for (int j = 0; j < HIT_EFFECT_STEPS; j++) {
                g2d.setComposite(hitEffeSteps[j]);
                g2d.fillRect(effx - (outEffWidth * j) - outEffWidth, y, outEffWidth, keyHeight);
            }
            g2d.setComposite(AlphaComposite.SrcOver);
            g2d.dispose();
            
            int rgb = col.getRGB();
            imageMap.put(rgb, image);
        }
    }

    @Override
    public void paintIn(Graphics g, int x, int y, int keySize, Color focusColor, Color defColor) {
        /* 非対応 */
    }

    @Override
    public void paintOut(Graphics g, int x, int y, int keySize, Color focusColor, Color defColor) {
/*
        Graphics2D g2d = (Graphics2D) g;
        int i = 0;
        int keyWidth = 120;
        int effeX = x;
        int effeY1 = y;
        int effeY2 = effeY1 + keySize - 1;
        int effeW = (int) ((double) keyWidth * 0.2);
        float alpha = 1.0f;
        g2d.setColor(focusColor);
        for (; i < effeW; i++) {
            alpha = 1.0f - ((float) (i + 1) / (float) effeW);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.drawLine(effeX - i, effeY1, effeX - i, effeY2);
        }
        g2d.setComposite(AlphaComposite.SrcOver);
*/
        BufferedImage image = imageMap.get(focusColor.getRGB());
        if (image != null) {
            int w = 2 * HIT_EFFECT_STEPS;
            int h = keySize;
            g.drawImage(image, x - w, y, w, h, null);
        }
    }

}

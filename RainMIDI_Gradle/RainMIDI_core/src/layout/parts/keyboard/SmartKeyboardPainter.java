package layout.parts.keyboard;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.image.BufferedImage;

import layout.parts.KeyParts;
import layout.parts.KeyboardPainter;

/* 軽量版 */
public class SmartKeyboardPainter extends KeyboardPainter {
    private Paint whiteKeyGrad = null;
    private AlphaComposite blackKeyAlpha = null;
    
    private BufferedImage wkImage = null;
    private BufferedImage bkImage = null;
    
    private static final int WK_WIDTH = 120;
    private static final int WK_HEIGHT = 20;
    
    private static final int BK_WIDTH = 100;
    private static final int BK_HEIGHT = 10;

    public SmartKeyboardPainter() {
    }
    
    @Override
        public void preload() {
            super.preload();
            
            int steps = getKeyboardWidth();
            whiteKeyGrad = new LinearGradientPaint(steps, 0, 0, 0, new float[] { 0f, 1f },
                    new Color[] { new Color(1f, 1f, 1f, 1.0f), new Color(1f, 1f, 1f, 0f) });
            blackKeyAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
            
            {
                int x = 0;
                int y = 0;
                int width = WK_WIDTH;
                int height = WK_HEIGHT;
                whiteKeyGrad = new LinearGradientPaint(width, 0, 0, 0, new float[] { 0f, 1f },
                        new Color[] { new Color(1f, 1f, 1f, 1.0f), new Color(1f, 1f, 1f, 0f) });
                wkImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

                Graphics2D g2d = (Graphics2D) wkImage.createGraphics();
                g2d.setPaint(whiteKeyGrad);
                g2d.fillRect(x, y, width, height);
                g2d.setComposite(AlphaComposite.SrcOver);
                g2d.dispose();
            }
            
            {
                int x = 0;
                int y = 0;
                int width = BK_WIDTH;
                int height = BK_HEIGHT;
                blackKeyAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
                bkImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                
                Graphics2D g2d = (Graphics2D) bkImage.createGraphics();
                g2d.setColor(Color.BLACK);
                g2d.setComposite(blackKeyAlpha);
                g2d.fillRect(x, y, width, height);
                g2d.setComposite(AlphaComposite.SrcOver);
                g2d.dispose();
            }
        }

    @Override
    public void setKeyboardWidth(int width) {
        if (width != getKeyboardWidth()) {

        }
        super.setKeyboardWidth(width);
    }

    private void drawWhiteKeyImpl(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        int x = parts.x;
        int y = parts.y;
        int w = parts.width;
        int h = parts.height - 1;
        g.drawImage(wkImage, x, y, w, h, null);
    }

    private void drawBlackKeyImpl(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        int x = parts.x;
        int y = parts.y;
        int w = parts.width;
        int h = parts.height;
        g.drawImage(bkImage, x, y, w, h, null);
    }

    @Override
    protected void paintWhiteKeyDefault(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        drawWhiteKeyImpl(g, parts, bgColor, bdColor, false);
    }

    @Override
    protected void paintWhiteKeyPush(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        drawWhiteKeyImpl(g, parts, bgColor, bdColor, true);
    }

    @Override
    protected void paintBlackKeyDefault(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        drawBlackKeyImpl(g, parts, bgColor, bdColor, false);
    }

    @Override
    protected void paintBlackKeyPush(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        drawBlackKeyImpl(g, parts, bgColor, bdColor, true);
    }
}

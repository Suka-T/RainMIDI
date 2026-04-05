package layout.parts.keyboard;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import layout.parts.KeyParts;
import layout.parts.KeyboardPainter;

/* フォーカスきれい版 GC許容 */
public class SmartKeyboardPainter2 extends KeyboardPainter {

    public SmartKeyboardPainter2() {
    }
    
    private int getTrimedX(int x, int width) {
        return x + (width - getTrimedWidth(width));
    }
    
    private int getTrimedWidth(int width) {
        return (int) ((double) width/* * 0.8*/);
    }
    
    private void drawWhiteKeyImpl(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        Graphics2D g2d = (Graphics2D)g;
        int trimedX = getTrimedX(parts.orgX, parts.orgWidth);
        int trimedWidth = getTrimedWidth(parts.orgWidth);
        int keyWidth = getTrimedWidth(getKeyboardWidth());
        float alpha = 1.0f;
        int effeX = trimedX + trimedWidth - 1;
        int effeW = keyWidth;
        int effeY1 = parts.orgY;
        int effeH = parts.orgHeight;
        int effeY2 = effeY1 + effeH - 1;
        int i = 0;
        g.setColor(Color.WHITE);
        for (; i < effeW; i++) {
            alpha = 1.0f - ((float)(i + 1) / (float)effeW);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.drawLine(effeX - i, effeY1, effeX - i, effeY2);
        }
        g2d.setComposite(AlphaComposite.SrcOver);
        
//        if (isPush) {
//            i = 0;
//            effeX = trimedX + trimedWidth - 1;
//            effeW = (int)((double)keyWidth * 0.2);
//            g.setColor(bgColor);
//            for (; i < effeW; i++) {
//                alpha = 1.0f - ((float)(i + 1) / (float)effeW);
//                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
//                g2d.drawLine(effeX - i, effeY1, effeX - i, effeY2);
//            }
//            g2d.setComposite(AlphaComposite.SrcOver);
//        }
    }
    
    private void drawBlackKeyImpl(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        Graphics2D g2d = (Graphics2D)g;
        int trimedX = getTrimedX(parts.orgX, parts.orgWidth);
        int trimedWidth = getTrimedWidth(parts.orgWidth);
        int keyWidth = getTrimedWidth(getKeyboardWidth());
        float alpha = 1.0f;
        int effeX = trimedX + trimedWidth - 1;
        int effeW = keyWidth;
        int effeW2 = (int)((double)keyWidth * 0.7);
        int effeY1 = parts.orgY;
        int effeH = parts.orgHeight;
        int effeY2 = effeY1 + effeH - 1;
        int i = 0;
        for (; i < effeW; i++) {
            alpha = 1.0f - ((float)(i + 1) / (float)effeW);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.setColor(i < effeW2 ? Color.DARK_GRAY : Color.WHITE);
            g2d.drawLine(effeX - i, effeY1, effeX - i, effeY2);
        }
        g2d.setComposite(AlphaComposite.SrcOver);
        
//        if (isPush) {
//            i = 0;
//            effeX = trimedX + trimedWidth - 1;
//            effeW = (int)((double)keyWidth * 0.2);
//            g.setColor(bgColor);
//            for (; i < effeW; i++) {
//                alpha = 1.0f - ((float)(i + 1) / (float)effeW);
//                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
//                g2d.drawLine(effeX - i, effeY1, effeX - i, effeY2);
//            }
//            g2d.setComposite(AlphaComposite.SrcOver);
//        }
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

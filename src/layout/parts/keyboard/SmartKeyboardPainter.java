package layout.parts.keyboard;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import layout.parts.KeyParts;
import layout.parts.KeyboardPainter;

public class SmartKeyboardPainter extends KeyboardPainter {

    public SmartKeyboardPainter() {
    }
    
    private int getTrimedX(int x, int width) {
        return x + (width - getTrimedWidth(width));
    }
    
    private int getTrimedWidth(int width) {
        return (int)((double)width * 0.8);
    }
    
    private void drawKeyImpl(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
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
        g.setColor(bgColor);
        for (; i < effeW; i++) {
            alpha = 1.0f - ((float)(i + 1) / (float)effeW);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.setColor(bgColor);
            g2d.drawLine(effeX - i, effeY1, effeX - i, effeY2);
        }
        g2d.setComposite(AlphaComposite.SrcOver);
    }

    @Override
    protected void paintWhiteKeyDefault(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        drawKeyImpl(g, parts, bgColor, bdColor, isPush);
    }

    @Override
    protected void paintWhiteKeyPush(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        drawKeyImpl(g, parts, bgColor, bdColor, isPush);
    }

    @Override
    protected void paintBlackKeyDefault(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        drawKeyImpl(g, parts, Color.DARK_GRAY, bdColor, isPush);
    }

    @Override
    protected void paintBlackKeyPush(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        drawKeyImpl(g, parts, bgColor, bdColor, isPush);
    }
}

package layout.parts.keyboard;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;

import layout.parts.KeyParts;
import layout.parts.KeyboardPainter;

public class SmartKeyboardPainter extends KeyboardPainter {
    private Paint whiteKeyGrad = null;
    private AlphaComposite blackKeyAlpha = null;

    public SmartKeyboardPainter() {
    }

    @Override
    public void setKeyboardWidth(int width) {
        if (width != getKeyboardWidth()) {
            int steps = getTrimedWidth(width);
            whiteKeyGrad = new LinearGradientPaint(steps, 0, 0, 0, 
                    new float[] { 0f, 1f },
                    new Color[] { new Color(1f, 1f, 1f, 1.0f), new Color(1f, 1f, 1f, 0f) }
            );
            blackKeyAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
        }
        super.setKeyboardWidth(width);
    }

    private int getTrimedX(int x, int width) {
        return x + (width - getTrimedWidth(width));
    }

    private int getTrimedWidth(int width) {
        return (int) ((double) width/* * 0.8*/);
    }

    private void drawWhiteKeyImpl(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        Graphics2D g2d = (Graphics2D) g;
        int trimedX = getTrimedX(parts.orgX, parts.orgWidth);
        int trimedWidth = getTrimedWidth(parts.orgWidth);
        int keyWidth = getTrimedWidth(getKeyboardWidth());
        int effeX = trimedX + trimedWidth - 1;
        int effeW = keyWidth;
        int effeY1 = parts.orgY;
        int effeH = parts.orgHeight;
        g2d.setPaint(whiteKeyGrad);
        g2d.fillRect(effeX - effeW, effeY1, effeW, effeH);
        g2d.setComposite(AlphaComposite.SrcOver);

        if (isPush) {
            effeX = trimedX + trimedWidth - 1;
            effeW = (int) ((double) keyWidth * 0.2);
            g.setColor(bgColor);
            g2d.fillRect(effeX - effeW - 2, effeY1 + 2, effeW, effeH - 4);
        }
    }

    private void drawBlackKeyImpl(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        Graphics2D g2d = (Graphics2D) g;
        int trimedX = getTrimedX(parts.orgX, parts.orgWidth);
        int trimedWidth = getTrimedWidth(parts.orgWidth);
        int keyWidth = getTrimedWidth(getKeyboardWidth());
        int effeX = trimedX + trimedWidth - 1;
        int effeW = keyWidth;
        int effeY1 = parts.orgY;
        int effeH = parts.orgHeight;
        g2d.setPaint(whiteKeyGrad);
        g2d.fillRect(effeX - effeW, effeY1, effeW, effeH);
        g.setColor(Color.BLACK);
        g2d.setComposite(blackKeyAlpha);
        g2d.fillRect(effeX - (int)((float)effeW * 0.7f), effeY1, (int)((float)effeW * 0.7f), effeH);
        g2d.setComposite(AlphaComposite.SrcOver);

        if (isPush) {
            effeX = trimedX + trimedWidth - 1;
            effeW = (int) ((double) keyWidth * 0.2);
            g.setColor(bgColor);
            g2d.fillRect(effeX - effeW - 2, effeY1 + 2, effeW, effeH - 4);
        }
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

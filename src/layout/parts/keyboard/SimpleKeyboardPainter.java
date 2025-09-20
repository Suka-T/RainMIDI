package layout.parts.keyboard;

import java.awt.Color;
import java.awt.Graphics;

import layout.parts.KeyParts;
import layout.parts.KeyboardPainter;

public class SimpleKeyboardPainter extends KeyboardPainter {

    public SimpleKeyboardPainter() {}
    
    private void drawKeyImpl(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        g.setColor(bgColor);
        g.fillRect(parts.x, parts.y, parts.width, parts.height);
        g.setColor(Color.GRAY);
        g.drawRect(parts.x, parts.y, parts.width, parts.height);
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
        drawKeyImpl(g, parts, bgColor, bdColor, isPush);
    }

    @Override
    protected void paintBlackKeyPush(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        drawKeyImpl(g, parts, bgColor, bdColor, isPush);
    }

}

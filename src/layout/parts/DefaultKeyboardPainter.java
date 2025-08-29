package layout.parts;

import java.awt.Color;
import java.awt.Graphics;

public class DefaultKeyboardPainter extends KeyboardPainter {

    public DefaultKeyboardPainter() {
    }

    @Override
    void paintWhiteKeyDefault(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        g.setColor(bgColor);
        g.fill3DRect(parts.x, parts.y, parts.width, parts.height, true);
        g.setColor(Color.LIGHT_GRAY);
        g.fill3DRect(parts.x, parts.y, (int)((double)parts.width * 0.06), parts.height, true);
        g.setColor(Color.GRAY);
        g.drawRect(parts.x, parts.y, parts.width, parts.height);
    }

    @Override
    void paintWhiteKeyPush(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        g.setColor(bgColor);
        g.fill3DRect(parts.x, parts.y, parts.width, parts.height, true);
        g.setColor(Color.LIGHT_GRAY);
        g.fill3DRect(parts.x, parts.y, (int)((double)parts.width * 0.02), parts.height, true);
        g.setColor(Color.GRAY);
        g.drawRect(parts.x, parts.y, parts.width, parts.height);
    }

    @Override
    void paintBlackKeyDefault(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        g.setColor(bgColor);
        g.fill3DRect(parts.x, parts.y, parts.width, parts.height, true);
        g.setColor(Color.DARK_GRAY);
        g.fill3DRect(parts.x, parts.y, (int)((double)parts.width * 0.08), parts.height, true);
        g.setColor(Color.GRAY);
        g.drawRect(parts.x, parts.y, parts.width, parts.height);
    }

    @Override
    void paintBlackKeyPush(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        g.setColor(bgColor);
        g.fill3DRect(parts.x, parts.y, parts.width, parts.height, true);
        g.setColor(Color.DARK_GRAY);
        g.fill3DRect(parts.x, parts.y, (int)((double)parts.width * 0.02), parts.height, true);
        g.setColor(Color.GRAY);
        g.drawRect(parts.x, parts.y, parts.width, parts.height);
    }

}

package layout.parts;

import java.awt.Color;
import java.awt.Graphics;

public class SimpleKeyboardPainter extends KeyboardPainter {

    public SimpleKeyboardPainter() {}

    @Override
    void paintWhiteKeyDefault(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        g.setColor(bgColor);
        g.fillRect(parts.x, parts.y, parts.width, parts.height);
        g.setColor(Color.GRAY);
        g.drawRect(parts.x, parts.y, parts.width, parts.height);
    }

    @Override
    void paintWhiteKeyPush(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        g.setColor(bgColor);
        g.fillRect(parts.x, parts.y, parts.width, parts.height);
        g.setColor(Color.GRAY);
        g.drawRect(parts.x, parts.y, parts.width, parts.height);
    }

    @Override
    void paintBlackKeyDefault(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        g.setColor(bgColor);
        g.fillRect(parts.x, parts.y, parts.width, parts.height);
        g.setColor(Color.GRAY);
        g.drawRect(parts.x, parts.y, parts.width, parts.height);
    }

    @Override
    void paintBlackKeyPush(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        g.setColor(bgColor);
        g.fillRect(parts.x, parts.y, parts.width, parts.height);
        g.setColor(Color.GRAY);
        g.drawRect(parts.x, parts.y, parts.width, parts.height);
    }

}

package layout.parts;

import java.awt.Color;
import java.awt.Graphics;

public class DefaultKeyboardPainter extends KeyboardPainter {

    public DefaultKeyboardPainter() {
    }

    @Override
    void paintWhiteKeyDefault(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        int shadowW = (int)((double)parts.width * 0.06);
        g.setColor(bgColor);
        g.fill3DRect(parts.x, parts.y, parts.width, parts.height, true);
        g.setColor(Color.LIGHT_GRAY);
        g.fill3DRect(parts.x, parts.y, shadowW, parts.height, true);
        g.setColor(Color.GRAY);
        g.drawLine(parts.x + shadowW - 1, parts.y, parts.x + shadowW  - 1, parts.y + parts.height - 1);
        g.drawRect(parts.x, parts.y, parts.width, parts.height);
    }

    @Override
    void paintWhiteKeyPush(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        int shadowW = (int)((double)parts.width * 0.02);
        g.setColor(bgColor);
        g.fill3DRect(parts.x, parts.y, parts.width, parts.height, true);
        //g.setColor(Color.LIGHT_GRAY);
        //g.fill3DRect(parts.x, parts.y, shadowW, parts.height, true);
        g.setColor(Color.GRAY);
        g.fill3DRect(parts.x, parts.y, shadowW, parts.height, true);
        g.drawRect(parts.x, parts.y, parts.width, parts.height);
    }

    @Override
    void paintBlackKeyDefault(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        int poffs = 0;
        int offs = (int)((double)parts.width * 0.05);
        g.setColor(bgColor);
        g.fill3DRect(parts.x + offs - poffs, parts.y, parts.width, parts.height, true);
        g.setColor(Color.DARK_GRAY);
        g.fill3DRect(parts.x + offs - poffs, parts.y, offs + poffs, parts.height, true);
        g.drawRect(parts.x + offs - poffs + 1, parts.y + 1, parts.width - 1, parts.height - 2);
        g.setColor(Color.GRAY);
        g.drawRect(parts.x + offs - poffs, parts.y, parts.width, parts.height);
    }

    @Override
    void paintBlackKeyPush(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        int offs = (int)((double)parts.width * 0.05);
        g.setColor(bgColor);
        g.fill3DRect(parts.x + offs, parts.y, parts.width - offs, parts.height, true);
        g.setColor(Color.DARK_GRAY);
        g.fill3DRect(parts.x + offs, parts.y, (int)((double)(parts.width - offs) * 0.02), parts.height, true);
        g.setColor(Color.GRAY);
        g.drawRect(parts.x + offs, parts.y, parts.width - offs, parts.height);
    }

}

package layout.parts.key;

import java.awt.Color;
import java.awt.Graphics;

import layout.parts.KeyParts;

public class WhiteKeyParts extends KeyParts {

    public WhiteKeyParts() {
    }

    @Override
    public void draw(Graphics g, Color bgColor, Color bdColor, boolean isPush) {
        if (isPush) {
            g.setColor(bgColor);
            g.fill3DRect(x, y, width, height, true);
            g.setColor(Color.LIGHT_GRAY);
            g.fill3DRect(x, y, (int)((double)width * 0.02), height, true);
            g.setColor(Color.GRAY);
            g.drawRect(x, y, width, height);
        }
        else {
            g.setColor(bgColor);
            g.fill3DRect(x, y, width, height, true);
            g.setColor(Color.LIGHT_GRAY);
            g.fill3DRect(x, y, (int)((double)width * 0.06), height, true);
            g.setColor(Color.GRAY);
            g.drawRect(x, y, width, height);
        }
    }
}

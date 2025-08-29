package layout.parts;

import java.awt.Color;
import java.awt.Graphics;

public class BlackKeyParts extends KeyParts {

    public BlackKeyParts() {
    }

    @Override
    public void draw(Graphics g, Color bgColor, Color bdColor, boolean isPush) {
        if (isPush) {
            g.setColor(bgColor);
            g.fill3DRect(x, y, width, height, true);
            g.setColor(Color.DARK_GRAY);
            g.fill3DRect(x, y, (int)((double)width * 0.02), height, true);
            g.setColor(Color.GRAY);
            g.drawRect(x, y, width, height);
        }
        else {
            g.setColor(bgColor);
            g.fill3DRect(x, y, width, height, true);
            g.setColor(Color.DARK_GRAY);
            g.fill3DRect(x, y, (int)((double)width * 0.08), height, true);
            g.setColor(Color.GRAY);
            g.drawRect(x, y, width, height);
        }
    }
}

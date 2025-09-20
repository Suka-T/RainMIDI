package layout.parts;

import java.awt.Color;
import java.awt.Graphics;

public abstract class KeyboardPainter {
    
    public static enum KindOfKey {
        WHITE,
        BLACK
    }

    public KeyboardPainter() {}
    public void paintKeyparts(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush, KindOfKey kind) {
        if (KindOfKey.WHITE == kind) {
            if (isPush) {
                paintWhiteKeyPush(g, parts, bgColor, bdColor, isPush);
            }
            else {
                paintWhiteKeyDefault(g, parts, bgColor, bdColor, isPush);
            }
        }
        else if (KindOfKey.BLACK == kind) {
            if (isPush) {
                paintBlackKeyPush(g, parts, bgColor, bdColor, isPush);
            }
            else {
                paintBlackKeyDefault(g, parts, bgColor, bdColor, isPush);
            }
        }
    }
    
    public abstract void paintWhiteKeyDefault(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush);
    public abstract void paintWhiteKeyPush(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush);
    public abstract void paintBlackKeyDefault(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush);
    public abstract void paintBlackKeyPush(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush);
}

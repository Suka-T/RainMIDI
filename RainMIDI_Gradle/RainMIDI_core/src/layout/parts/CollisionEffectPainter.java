package layout.parts;

import java.awt.Color;
import java.awt.Graphics;

public abstract class CollisionEffectPainter {

    public CollisionEffectPainter() {
    }
    
    public abstract void paintIn(Graphics g, int x, int y, int keySize, Color focusColor, Color defColor);
    public abstract void paintOut(Graphics g, int x, int y, int keySize, Color focusColor, Color defColor);

}

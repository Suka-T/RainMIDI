package layout.parts.collisionEffect;

import java.awt.Color;
import java.awt.Graphics;

import layout.parts.CollisionEffectPainter;

public class NoneCollisionEffectPainter extends CollisionEffectPainter {

    public NoneCollisionEffectPainter() {}

    @Override
    public void paintIn(Graphics g, int x, int y, int keySize, Color focusColor, Color defColor) {
    }

    @Override
    public void paintOut(Graphics g, int x, int y, int keySize, Color focusColor, Color defColor) {
    }

}

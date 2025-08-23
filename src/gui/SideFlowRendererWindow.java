package gui;

import java.awt.Graphics;

public class SideFlowRendererWindow extends RendererWindow {

    public SideFlowRendererWindow(int winW, int winH) {
        super(winW, winH);
    }

    @Override
    protected void makeKeyboardRsrc() {
        super.makeKeyboardRsrc();
    }

    @Override
    protected void copyFromNotesImage(Graphics g) {
        super.copyFromNotesImage(g);
    }

    @Override
    protected int getEffectWidth(int dir) {
        return super.getEffectWidth(dir);
    }
}

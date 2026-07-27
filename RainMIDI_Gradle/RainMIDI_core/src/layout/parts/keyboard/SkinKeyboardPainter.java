package layout.parts.keyboard;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import layout.parts.KeyParts;
import layout.parts.KeyboardPainter;

public class SkinKeyboardPainter extends KeyboardPainter {
    public SkinKeyboardPainter() {
    }
    
    @Override
    public void preload() {
        // TODO 自動生成されたメソッド・スタブ
        super.preload();
        
        layout.LayoutManager.getInstance().makeKeyboadSkin();
    }
    
    private void drawWhiteKeyImpl(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 基本座標の計算
        int x = parts.x;
        int y = parts.y;
        int w = parts.width;
        int h = parts.height;
        if (isPush) {
            g.drawImage(layout.LayoutManager.getInstance().getKeyboadSkinImageWP(bgColor.getRGB()), x, y, w, h, null);
        }
        else {
            g.drawImage(layout.LayoutManager.getInstance().getKeyboadSkinImageW(), x, y, w, h, null);
        }
    }

    private void drawBlackKeyImpl(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 基本座標の計算
        int x = parts.x;
        int y = parts.y;
        int w = parts.width;
        int h = parts.height;
        if (isPush) {
            g.drawImage(layout.LayoutManager.getInstance().getKeyboadSkinImageBP(bgColor.getRGB()), x, y, w, h, null);
        }
        else {
            g.drawImage(layout.LayoutManager.getInstance().getKeyboadSkinImageB(), x, y, w, h, null);
        }
    }

    @Override
    protected void paintWhiteKeyDefault(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        drawWhiteKeyImpl(g, parts, bgColor, bdColor, false);
    }

    @Override
    protected void paintWhiteKeyPush(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        drawWhiteKeyImpl(g, parts, bgColor, bdColor, true);
    }

    @Override
    protected void paintBlackKeyDefault(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        drawBlackKeyImpl(g, parts, bgColor, bdColor, false);
    }

    @Override
    protected void paintBlackKeyPush(Graphics g, KeyParts parts, Color bgColor, Color bdColor, boolean isPush) {
        drawBlackKeyImpl(g, parts, bgColor, bdColor, true);
    }

}

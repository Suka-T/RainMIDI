package gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import layout.LayoutManager;
import layout.parts.KeyParts;
import layout.parts.key.BlackKeyParts;
import layout.parts.key.WhiteKeyParts;
import plg.SystemProperties;

public class RainFallRendererWindow extends RendererWindow {

    public RainFallRendererWindow(int winW, int winH) {
        super(winW, winH);
    }

    @Override
    public int getOrgWidth() {
        return SystemProperties.getInstance().getDimHeight();
    }

    @Override
    public int getOrgHeight() {
        return SystemProperties.getInstance().getDimWidth();
    }

    @Override
    protected void makeKeyboardRsrc() {
        hitEffectPosY = new int[128];
        int keyHeight = getMeasCellHeight();
        int keyCount = (127 - getTopMidiNumber());
        int topOffset = (keyHeight * keyCount);
        for (int i = 0; i < 128; i++) {
            hitEffectPosY[i] = topOffset + (keyHeight * i);
        }

        aHakken = new KeyParts[75];
        aKokken = new KeyParts[53];

        int kkCnt = 0;
        int hkCnt = 0;
        int hkWidth = getKeyboardWidth();
        int kkWidth = (int) (hkWidth * 0.7);
        int hakkenHeight = (128 * keyHeight) / 75;
        for (int i = 0; i < 128; i++) {
            int midiNo = 127 - i;
            int key = midiNo % 12;
            switch (key) {
                case 0:
                    aHakken[hkCnt] = new WhiteKeyParts();
                    aHakken[hkCnt].x = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                    aHakken[hkCnt].y = hitEffectPosY[i];
                    aHakken[hkCnt].width = hkWidth;
                    aHakken[hkCnt].height = (hakkenHeight + keyHeight / 2) - 4;
                    aHakken[hkCnt].orgX = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                    aHakken[hkCnt].orgY = hitEffectPosY[i];
                    aHakken[hkCnt].orgWidth = hkWidth;
                    aHakken[hkCnt].orgHeight = keyHeight;
                    aHakken[hkCnt].y -= (int) (keyHeight / 1.5) + 1;
                    aHakken[hkCnt].midiNo = midiNo;
                    hkCnt++;
                    break;
                case 5:
                    aHakken[hkCnt] = new WhiteKeyParts();
                    aHakken[hkCnt].x = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                    aHakken[hkCnt].y = hitEffectPosY[i];
                    aHakken[hkCnt].width = hkWidth;
                    aHakken[hkCnt].height = (hakkenHeight + keyHeight / 2) - 4;
                    aHakken[hkCnt].orgX = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                    aHakken[hkCnt].orgY = hitEffectPosY[i];
                    aHakken[hkCnt].orgWidth = hkWidth;
                    aHakken[hkCnt].orgHeight = keyHeight;
                    aHakken[hkCnt].y -= (int) (keyHeight / 1.5) + 1;
                    aHakken[hkCnt].midiNo = midiNo;
                    hkCnt++;
                    break;
                case 7:
                    aHakken[hkCnt] = new WhiteKeyParts();
                    aHakken[hkCnt].x = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                    aHakken[hkCnt].y = hitEffectPosY[i];
                    aHakken[hkCnt].width = hkWidth;
                    aHakken[hkCnt].height = (hakkenHeight + keyHeight / 2) - 3;
                    aHakken[hkCnt].orgX = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                    aHakken[hkCnt].orgY = hitEffectPosY[i];
                    aHakken[hkCnt].orgWidth = hkWidth;
                    aHakken[hkCnt].orgHeight = keyHeight;
                    aHakken[hkCnt].y -= (keyHeight / 2);
                    aHakken[hkCnt].midiNo = midiNo;
                    hkCnt++;
                    break;
                case 9:
                    aHakken[hkCnt] = new WhiteKeyParts();
                    aHakken[hkCnt].x = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                    aHakken[hkCnt].y = hitEffectPosY[i];
                    aHakken[hkCnt].width = hkWidth;
                    aHakken[hkCnt].height = (hakkenHeight + keyHeight / 2) - 3;
                    aHakken[hkCnt].orgX = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                    aHakken[hkCnt].orgY = hitEffectPosY[i];
                    aHakken[hkCnt].orgWidth = hkWidth;
                    aHakken[hkCnt].orgHeight = keyHeight;
                    aHakken[hkCnt].y -= (keyHeight / 3);
                    aHakken[hkCnt].midiNo = midiNo;
                    hkCnt++;
                    break;
                case 2:
                    aHakken[hkCnt] = new WhiteKeyParts();
                    aHakken[hkCnt].x = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                    aHakken[hkCnt].y = hitEffectPosY[i];
                    aHakken[hkCnt].width = hkWidth;
                    aHakken[hkCnt].height = (hakkenHeight + keyHeight / 2) - 4;
                    aHakken[hkCnt].orgX = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                    aHakken[hkCnt].orgY = hitEffectPosY[i];
                    aHakken[hkCnt].orgWidth = hkWidth;
                    aHakken[hkCnt].orgHeight = keyHeight;
                    aHakken[hkCnt].y -= (keyHeight / 2.5) - 0;
                    aHakken[hkCnt].midiNo = midiNo;
                    hkCnt++;
                    break;
                case 4:
                    aHakken[hkCnt] = new WhiteKeyParts();
                    aHakken[hkCnt].x = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                    aHakken[hkCnt].y = hitEffectPosY[i];
                    aHakken[hkCnt].width = hkWidth;
                    aHakken[hkCnt].height = (hakkenHeight + keyHeight / 3) - 3;
                    aHakken[hkCnt].orgX = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                    aHakken[hkCnt].orgY = hitEffectPosY[i];
                    aHakken[hkCnt].orgWidth = hkWidth;
                    aHakken[hkCnt].orgHeight = keyHeight;
                    aHakken[hkCnt].midiNo = midiNo;
                    hkCnt++;
                    break;
                case 11:
                    aHakken[hkCnt] = new WhiteKeyParts();
                    aHakken[hkCnt].x = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                    aHakken[hkCnt].y = hitEffectPosY[i];
                    aHakken[hkCnt].width = hkWidth;
                    aHakken[hkCnt].height = (hakkenHeight + (int) (keyHeight / 1.5)) - 5;
                    aHakken[hkCnt].orgX = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                    aHakken[hkCnt].orgY = hitEffectPosY[i];
                    aHakken[hkCnt].orgWidth = hkWidth;
                    aHakken[hkCnt].orgHeight = keyHeight;
                    aHakken[hkCnt].midiNo = midiNo;
                    hkCnt++;
                    break;
                case 1:
                case 3:
                case 6:
                case 8:
                case 10:
                    aKokken[kkCnt] = new BlackKeyParts();
                    aKokken[kkCnt].x = LayoutManager.getInstance().getTickBarPosition() - kkWidth;
                    aKokken[kkCnt].y = hitEffectPosY[i];
                    aKokken[kkCnt].width = kkWidth;
                    aKokken[kkCnt].height = keyHeight;
                    aKokken[kkCnt].orgX = LayoutManager.getInstance().getTickBarPosition() - kkWidth;
                    aKokken[kkCnt].orgY = hitEffectPosY[i];
                    aKokken[kkCnt].orgWidth = kkWidth;
                    aKokken[kkCnt].orgHeight = keyHeight;
                    aKokken[kkCnt].midiNo = midiNo;
                    kkCnt++;
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void copyFromNotesImage(Graphics g) {
        Graphics2D lotG2d = (Graphics2D) g;

        // 元状態を保存
        AffineTransform oldTransform = lotG2d.getTransform();
        Object oldInterpolation = lotG2d.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        Object oldAA = lotG2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        Object oldRendering = lotG2d.getRenderingHint(RenderingHints.KEY_RENDERING);

        // 補間方法を設定
        lotG2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, SystemProperties.getInstance().getImageInterpol());
        lotG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        lotG2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int panelW = getContentPane().getWidth();
        int panelH = getContentPane().getHeight();

        int imgW = orgScreenImage.getWidth(null);
        int imgH = orgScreenImage.getHeight(null);

        // 回転後の画像サイズ（横768 × 縦1280）→これを1280×768に無理やり拡大
        lotG2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, SystemProperties.getInstance().getImageInterpol());

        // 回転の中心に移動（ウィンドウの中心）
        lotG2d.translate(panelW / 2.0, panelH / 2.0);
        
        // 反転設定 
        if (SystemProperties.getInstance().isViewReverse() == true) {
            lotG2d.rotate(Math.toRadians(90));
        }
        else {
            lotG2d.rotate(Math.toRadians(-90));
        }

        // スケーリング（アスペクト比を無視してウィンドウ全体に引き伸ばす）
        double scaleX = (double) panelH / imgW; // 幅と高さが逆になることに注意
        double scaleY = (double) panelW / imgH;
        lotG2d.scale(scaleX, -scaleY);

        // 画像中心を原点に合わせて描画
        lotG2d.translate(-imgW / 2.0, -imgH / 2.0);
        lotG2d.drawImage(orgScreenImage, 0, 0, null);

        // 元状態に戻す
        lotG2d.setTransform(oldTransform);
        lotG2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterpolation);
        lotG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
        lotG2d.setRenderingHint(RenderingHints.KEY_RENDERING, oldRendering);
    }

    @Override
    protected int getEffectWidth(int dir) {
        return (dir < 0) ? 3 : 4;
    }
}

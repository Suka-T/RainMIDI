package image;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gui.RendererWindow;
import jlib.core.JMPCoreAccessor;
import layout.LayoutManager;

public class ImageWorker implements Runnable {
    protected int leftMeasTh = 0;
    //protected volatile BufferedImage offScreenImage;
    protected Image offScreenImage;
    protected Graphics2D offScreenGraphic;
    private boolean isExec = false;
    private int width = 0;
    private int height = 0;
    private ExecutorService service = null;
    protected RendererWindow window = null;
    private boolean isForcedEnd = false;
    
    protected boolean isAvailableGpu = false;
    
    private long debugRenderTime = 0;

    public ImageWorker(RendererWindow window, int width, int height, boolean isAvailableGpu) {
        super();
        this.window = window;
        this.width = width;
        this.height = height;
        this.isAvailableGpu = isAvailableGpu;
    }

    public void start() {
        int cores = Runtime.getRuntime().availableProcessors();
        service = Executors.newFixedThreadPool(Math.max(1, cores - 1));
    }

    public void stop() {
        service.shutdown();

        isExec = false;
    }

    public void makeImage() {
        if (isExec == false) {
            isExec = true;
            service.execute(this);
        }
    }

    public boolean isExec() {
        return isExec;
    }

    public void disposeImage() {
        if (offScreenImage != null) {
            offScreenImage.flush();
            offScreenImage = null;
        }
    }

    public Image getImage() {
        return offScreenImage;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getImageWidth() {
        return getWidth();
    }

    public int getImageHeight() {
        return getHeight();
    }

    @Override
    public void run() {
        try {
            debugRenderTime = 0;
            
            if (window.isVisible() == false) {
                if (offScreenImage != null) {
                    // イメージオブジェクトのメモリを解放
                    disposeImage();
                }
                isForcedEnd = false;
                isExec = false;
                return;
            }
            
            long start = System.currentTimeMillis();
            
            if (calcViewport() == false) {
                if (offScreenImage != null) {
                    // イメージオブジェクトのメモリを解放
                    disposeImage();
                }
                isForcedEnd = false;
                isExec = false;
                return;
            }

            if (isAvailableGpu) {
                if (offScreenImage == null) {
                    offScreenImage = LayoutManager.getInstance().createDisplayImage(getImageWidth(), getImageHeight());
                    offScreenGraphic = ((VolatileImage)offScreenImage).createGraphics();
                }
            }
            else {
                if (offScreenImage == null) {
                    offScreenImage = LayoutManager.getInstance().createBufferdImage(getImageWidth(), getImageHeight());
                    offScreenGraphic = ((BufferedImage)offScreenImage).createGraphics();
                }
            }

            offScreenGraphic.setColor(LayoutManager.getInstance().getPlayerColor().getBgColor());
            offScreenGraphic.fillRect(0, 0, getImageWidth(), getImageHeight());

            // オフスクリーン描画
            paintImage(offScreenGraphic);
            
            long end = System.currentTimeMillis();
            debugRenderTime = end - start;
            
            isForcedEnd = false;
            isExec = false;
        }
        catch (Throwable e) {
            JMPCoreAccessor.getSystemManager().errorHandle(e);
        }
    }

    public int getLeftMeasTh() {
        return leftMeasTh;
    }

    public void setLeftMeasTh(int leftMeasTh) {
        this.leftMeasTh = leftMeasTh;
    }
    
    protected boolean calcViewport() {
        /* 継承先で処理を記述 */
        return false;
    }

    protected void paintImage(Graphics g) {
        /* 継承先で処理を記述 */
    }

    public void reset() {
        /* 継承先で処理を記述 */
    }

    public void dispose() {
        stop();
        disposeImage();

        service.close();
    }

    public long getDebugRenderTime() {
        return debugRenderTime;
    }

    public void forcedEnd() {
        this.isForcedEnd = true;
    }
    
    public void clearForcedEnd() {
        this.isForcedEnd = false;
    }

    protected boolean doForcedEnd() {
        return this.isForcedEnd;
    }
}

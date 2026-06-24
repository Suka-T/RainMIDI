package gui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.TransferHandler;

import image.ImagerWorkerManager;
import jlib.core.ISystemManager;
import jlib.core.JMPCoreAccessor;
import jlib.midi.IMidiUnit;
import jlib.midi.INotesMonitor;
import layout.LayoutConfig;
import layout.LayoutConfig.EColorRule;
import layout.LayoutManager;
import layout.parts.CollisionEffectPainter;
import layout.parts.KeyParts;
import layout.parts.KeyboardPainter;
import layout.parts.KeyboardPainter.KindOfKey;
import layout.parts.MonitorPainter;
import layout.parts.SpectrumPainter;
import layout.parts.TickbarPainter;
import layout.parts.key.BlackKeyParts;
import layout.parts.key.WhiteKeyParts;
import layout.parts.monitor.MonitorData;
import plg.AbstractRenderPlugin;
import plg.OsInfoWrapper;
import plg.SystemProperties;
import plg.SystemProperties.SyspKeyFocusFunc;
import plg.SystemProperties.SyspLayerOrder;
import plg.SystemProperties.SyspMonitorType;
import plg.SystemProperties.SyspWinEffect;
import plg.ViewportManager;

public class RendererWindow extends JFrame implements MouseListener, MouseMotionListener, MouseWheelListener, Runnable {
    private static final Stroke DEFAULT_STROKE = new BasicStroke();

    private static final DecimalFormat DF = new DecimalFormat("0.0");

    private long delayNano = 0;

    // 次のページにフリップするpx数
    public static final int NEXT_FLIP_COUNT = 0;

    public static final int HIT_EFFECT_STEPS = 16;

    private static final BasicStroke GRAPH_BORDER_STROKE = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    private static final BasicStroke GRAPH_FRAMEBORDER_STROKE = new BasicStroke(2.0f);
    private static Color GRAPH_BG_COLOR = new Color(0, 0, 0, 100);

    protected Canvas canvas;
    protected BufferStrategy strategy;

    protected int frameCount = 0;
    protected int fps = 0;

    protected ImagerWorkerManager imageWorkerMgr = null;

    protected int leftMeas = 0;
    protected int zeroPosition = 0;
    protected int measCellWidth = 420;
    protected int measCellHeight = 0; // orgDispHeight / 128;//5;
    protected int dispMeasCount = 0;

    // private int topMidiNumber = 128 - ((orgDispHeight - (measCellHeight *
    // 128)) / measCellHeight) / 2;
    protected int topMidiNumber = 127;

    protected int[] hitEffectPosY = null;
    protected AlphaComposite[] hitEffeSteps = null;

    private volatile boolean running = false;
    protected Thread renderThread;

    protected KeyParts[] aHakken = null;
    protected KeyParts[] aKokken = null;

    protected boolean isFirstRendering = false;

    protected RainControl currentControl = null;
    protected UmbrellaUI umbrellaUI = null;
    protected VolumeControl volumeControl = null;

    protected KeyboardPainter keyboardPainter = null;
    protected MonitorPainter monitorPainter = null;
    protected SpectrumPainter spectrumPainter = null;
    protected TickbarPainter tickbarPainter = null;

    protected boolean isAvailableGpu = true;
    protected boolean useVramNotesImage = false;
    protected BufferedImage backBuffer = null;
    protected Graphics backBufferGrapics = null;
    protected Graphics backBufferCanvasGrapics = null;
    protected int backBufferWidth = -1;
    protected int backBufferHeight = -1;

    private Font msgFont = null;
    private Font msgFontS = null;
    private Font msgFontSS = null;
    private Font graphFont = null;
    private Font graphTitleFont = null;

    protected FrameLimiter frameLimiter = null;

    private long debugRenderTime = 0;

    private final int dummySpectSamples = 256;
    private float[] dummySpectWave = new float[dummySpectSamples];
    // private float phase = 0f;
    private float[] noiseBuf = new float[dummySpectSamples];

    protected ViewportManager viewportManager;

    public int getOrgWidth() {
        return SystemProperties.getInstance().getDimWidth();
    }

    public int getOrgHeight() {
        return SystemProperties.getInstance().getDimHeight();
    }

    private void windowCloseFunc() {
        setVisible(false);
        AbstractRenderPlugin.PluginInstance.winArray.remove(this);
        this.dispose();
        imageWorkerMgr.dispose();
        System.gc();

        if (AbstractRenderPlugin.PluginInstance.winArray.isEmpty()) {
            JMPCoreAccessor.getSoundManager().stop();

            if (JMPCoreAccessor.getWindowManager().getMainWindow().isWindowVisible() == true) {
                JMPCoreAccessor.getWindowManager().getMainWindow().setWindowVisible(false);
            }

            JMPCoreAccessor.getSoundManager().removeMidiSequence();

            SystemProperties.getInstance().exitForRenderWindow();

            AbstractRenderPlugin.PluginInstance.launch();
        }
    }

    /**
     * Create the frame.
     */
    public RendererWindow(int winW, int winH, boolean maximized) {
        this.setTitle("Rain MIDI");
        List<Image> icons = List.of(new ImageIcon(RendererWindow.class.getResource("/icon/app16.png")).getImage(),
                new ImageIcon(RendererWindow.class.getResource("/icon/app32.png")).getImage(),
                new ImageIcon(RendererWindow.class.getResource("/icon/app48.png")).getImage(),
                new ImageIcon(RendererWindow.class.getResource("/icon/app256.png")).getImage());

        this.setIconImages(icons);

        this.setTransferHandler(new DropFileHandler());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                windowCloseFunc();
            }
        });

        setLocation(10, 10);
        getContentPane().setPreferredSize(new Dimension(winW, winH));
        pack();

        if (maximized) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }

        setLayout(new BorderLayout(0, 0));

        canvas = new Canvas();
        canvas.setBackground(Color.BLACK);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        contentPane.add(canvas, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        canvas.addMouseListener(this);
        canvas.addMouseMotionListener(this);
        canvas.addMouseWheelListener(this);
        this.addComponentListener(new ComponentListener() {

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentResized(ComponentEvent e) {
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });

        measCellWidth = 480; // 暫定で初期値を入れておく
        measCellHeight = getOrgHeight() / 128;// 5;

        LayoutManager.getInstance().initialize(canvas);
        LayoutManager.getInstance().setNotesBounds(measCellWidth, measCellHeight);
        delayNano = 1_000_000_000 / SystemProperties.getInstance().getFixedFps();

        makeKeyboardRsrc();

        umbrellaUI = new UmbrellaUI(this);
        volumeControl = new VolumeControl();
        keyboardPainter = LayoutManager.getInstance().getKeyboardPainter(SystemProperties.getInstance().getViewMode());
        monitorPainter = SystemProperties.getInstance().getMonitorPainter();
        spectrumPainter = SystemProperties.getInstance().getSpectrumPainter();
        tickbarPainter = LayoutManager.getInstance().getTickbarPainter();
        tickbarPainter.clearCache();

        msgFont = new Font(SystemProperties.getInstance().getGeneralFontName(), Font.PLAIN, 28);
        msgFontS = new Font(SystemProperties.getInstance().getGeneralFontName(), Font.PLAIN, 18);
        msgFontSS = new Font(SystemProperties.getInstance().getGeneralFontName(), Font.PLAIN, 14);
        graphFont = new Font(SystemProperties.getInstance().getGeneralFontName(), Font.PLAIN, 14);
        graphTitleFont = new Font(SystemProperties.getInstance().getGeneralFontName(), Font.PLAIN, 21);

        isAvailableGpu = SystemProperties.getInstance().isAvailavleGpu();
        useVramNotesImage = SystemProperties.getInstance().isUseVramImage();
        System.out.println("isAvailableGpu=" + isAvailableGpu + ",useVramNotesImage=" + useVramNotesImage);

        hitEffeSteps = new AlphaComposite[HIT_EFFECT_STEPS];
        for (int j = 0; j < 16; j++) {
            float alpha = (1.0f - ((float) j / 16.0f)) * 0.9f;
            hitEffeSteps[j] = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        }

        frameLimiter = new FrameLimiter();
        frameLimiter.setFps(SystemProperties.getInstance().getFixedFps());
    }

    public int getKeyboardWidth() {
        return SystemProperties.getInstance().getKeyWidth();
    }

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
            case 5:
                aHakken[hkCnt] = new WhiteKeyParts();
                aHakken[hkCnt].x = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                aHakken[hkCnt].y = hitEffectPosY[i];
                aHakken[hkCnt].width = hkWidth;
                aHakken[hkCnt].height = hakkenHeight;
                aHakken[hkCnt].orgX = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                aHakken[hkCnt].orgY = hitEffectPosY[i];
                aHakken[hkCnt].orgWidth = hkWidth;
                aHakken[hkCnt].orgHeight = keyHeight;
                aHakken[hkCnt].y -= (keyHeight / 2);
                aHakken[hkCnt].midiNo = midiNo;
                hkCnt++;
                break;
            case 7:
            case 9:
            case 2:
                aHakken[hkCnt] = new WhiteKeyParts();
                aHakken[hkCnt].x = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                aHakken[hkCnt].y = hitEffectPosY[i];
                aHakken[hkCnt].width = hkWidth;
                aHakken[hkCnt].height = hakkenHeight + keyHeight / 2;
                aHakken[hkCnt].orgX = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                aHakken[hkCnt].orgY = hitEffectPosY[i];
                aHakken[hkCnt].orgWidth = hkWidth;
                aHakken[hkCnt].orgHeight = keyHeight;
                aHakken[hkCnt].y -= (keyHeight / 2);
                aHakken[hkCnt].midiNo = midiNo;
                hkCnt++;
                break;
            case 4:
            case 11:
                aHakken[hkCnt] = new WhiteKeyParts();
                aHakken[hkCnt].x = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                aHakken[hkCnt].y = hitEffectPosY[i];
                aHakken[hkCnt].width = hkWidth;
                aHakken[hkCnt].height = hakkenHeight;
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
    public void setVisible(boolean b) {
        boolean oldVisible = isVisible();
        super.setVisible(b);

        if (b) {
            if (oldVisible == false) {
                if (isAvailableGpu) {
                    canvas.requestFocusInWindow();
                    canvas.createBufferStrategy(2); // ダブルバッファリング
                    strategy = canvas.getBufferStrategy();
                }

                running = true;
                renderThread = new Thread(this::run, "RenderThread");
                renderThread.start();

                imageWorkerMgr.start();

                adjustTickBar();

                viewportManager.start();
            }
        }
        else {
            if (running == true) {
                running = false;
                try {
                    if (renderThread != null) {
                        renderThread.join();
                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }

                viewportManager.stop();

                imageWorkerMgr.stop();
            }
        }
    }

    public int getFPS() {
        return fps;
    }

    @Override
    public void run() {
        final long frameInterval = delayNano; // 1フレームあたりナノ秒
        long lastTime = System.nanoTime();
        long fpsCounterTime = lastTime;
        int frameCount = 0;

        // スリープの精度を上げるためのしきい値（1ms未満の端数はビジーループで調整する）
        final long sleepThresholdNanos = TimeUnit.MILLISECONDS.toNanos(1);

        while (running) {
            try {
                long now = System.nanoTime();
                long elapsed = now - lastTime;

                if (elapsed >= frameInterval) {
                    // もし大幅に処理落ちしていた場合、時間を追いつかせる（追いつき処理）
                    // これにより、重いフレームがあっても全体の再生速度が引きずられるのを防ぐ
                    int maxCatchup = 2;
                    int catchupCount = 0;

                    while (elapsed >= frameInterval && catchupCount < maxCatchup) {
                        lastTime += frameInterval;
                        elapsed -= frameInterval;
                        catchupCount++;
                    }

                    // もし2回追いついてもまだ現在時刻に追いついていない（大遅刻している）場合
                    // lastTimeが過去に置き去りになるのを防ぐため、現在の時間でリセットして仕切り直す
                    if (elapsed >= frameInterval) {
                        lastTime = now;
                    }

                    // 描画処理を実行（追いつきが発生していても、描画は現在の最新状態で1回だけ行う）
                    if (isAvailableGpu) {
                        render();
                    }
                    else {
                        renderSoft();
                    }
                    frameCount++;
                }
                else {
                    // 次のフレームまでの待ち時間を計算
                    long sleepNanos = frameInterval - elapsed;

                    // 1ミリ秒以上の余裕があるときだけ、1回だけ高精度にスリープする
                    if (sleepNanos >= sleepThresholdNanos) {
                        LockSupport.parkNanos(sleepNanos - (sleepThresholdNanos / 2));
                    }
                    else {
                        // 1ミリ秒未満の極めてわずかな隙間は、あえてスレッドを休ませず
                        // CPUの実行権を少しだけ譲る（Thread.onSpinWait）ことで、ナノ秒単位の超高精度なタイミングを作ります
                        // Thread.onSpinWait();
                        Thread.yield(); // CPU負荷軽減のためyieldにする
                    }
                }

                // FPS計測はループの最外殻で行うことで、処理落ちに影響されない正確な1秒を測る
                long currentNow = System.nanoTime();
                if (currentNow - fpsCounterTime >= TimeUnit.SECONDS.toNanos(1)) {
                    fps = frameCount;
                    frameCount = 0;
                    // fpsCounterTime = currentNow;
                    fpsCounterTime += TimeUnit.SECONDS.toNanos(1);
                }
            }
            catch (Throwable e) {
                JMPCoreAccessor.getSystemManager().errorHandle(e, true, true);
            }
        }
    }

//    @Override
//    public void run() {
//        final long frameInterval = delayNano; // 1フレームあたりナノ秒
//        long lastTime = System.nanoTime();
//        long fpsCounterTime = lastTime;
//        int frameCount = 0;
//
//        while (running) {
//            try {
//                long now = System.nanoTime();
//                long elapsed = now - lastTime;
//
//                if (elapsed >= frameInterval) {
//                    if (isAvailableGpu) {
//                        render(); // 描画処理
//                    }
//                    else {
//                        renderSoft(); // ソフトレンダリング
//                    }
//                    frameCount++;
//                    lastTime += frameInterval;
//
//                    // FPS計測（1秒ごと）
//                    if (now - fpsCounterTime >= TimeUnit.SECONDS.toNanos(1)) {
//                        fps = frameCount;
//                        frameCount = 0;
//                        fpsCounterTime = now;
//                    }
//                }
//                else {
//                    // 次フレームまで余裕があればスリープ
//                    long sleepNanos = frameInterval - elapsed;
//                    if (sleepNanos > 0) {
//                        long sleepMillis = sleepNanos / 1_000_000;
//                        int sleepNanoRemainder = (int) (sleepNanos % 1_000_000);
//                        if (sleepMillis > 0)
//                            Thread.sleep(sleepMillis);
//                        if (sleepNanoRemainder > 0)
//                            LockSupport.parkNanos(sleepNanoRemainder);
//                    }
//                }
//            }
//            catch (Throwable e) {
//                JMPCoreAccessor.getSystemManager().errorHandle(e);
//            }
//        }
//    }

    protected void render() {
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
        try {
            paintDisplay(g);
        }
        finally {
            // Graphics オブジェクトの解放
            g.dispose();
        }
        strategy.show();
    }

    protected void renderSoft() {
        int width = getWidth();
        int height = getHeight();

        if (backBuffer == null || backBufferWidth != width || backBufferHeight != height) {
            if (backBufferGrapics != null) {
                backBufferGrapics.dispose();
            }

            backBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            backBufferGrapics = backBuffer.createGraphics();

            backBufferWidth = backBuffer.getWidth();
            backBufferHeight = backBuffer.getHeight();
        }

        try {
            paintDisplay(backBufferGrapics);
        }
        catch (Exception e) {
            e.printStackTrace(); // 描画中の例外でループが止まるのを防ぐ
        }

        Graphics2D screen = (Graphics2D) canvas.getGraphics();
        if (screen != null) {
            try {
                screen.drawImage(backBuffer, 0, 0, null);
            }
            finally {
                screen.dispose(); // 取得したGraphicsは必ず使い捨てる
            }
        }
    }

    public void init() {
        imageWorkerMgr = new ImagerWorkerManager(this, getOrgWidth(), getOrgHeight(), useVramNotesImage);
        viewportManager = new ViewportManager();

        updateViewport();
    }

    public void prepareLoadFile() {
        SystemProperties.getInstance().getGraphMonScheduler().lockCount();
        SystemProperties.getInstance().getGraphMonScheduler().clearRingBuffer();
    }

    public void loadFile() {
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        if (midiUnit.isValidSequence() == false || running == false || isVisible() == false) {
            return;
        }

        debugRenderTime = 0;

        isFirstRendering = true;

        setLeftMeas(0);
        resetPage();

        SystemProperties.getInstance().getGraphMonScheduler().clearRingBuffer();
        SystemProperties.getInstance().getGraphMonScheduler().releaseCount();

        isFirstRendering = false;
    }

    public void adjustTickBar() {
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        if (midiUnit.isValidSequence() == false) {
            return;
        }

        if (isVisible() == true) {
            resetPage();
        }
    }

    private StringBuilder sb = new StringBuilder(64); // 初期容量を指定
    // protected volatile VolatileImage orgScreenImage = null;
    protected volatile Image orgScreenImage = null;
    protected volatile Graphics orgScreenGraphic = null;

    protected volatile Image bufferScreenImage = null;
    protected volatile Graphics bufferScreenGraphic = null;

    protected int bufferScreenImageWidth = -1;
    protected int bufferScreenImageHeight = -1;

    private static final String[] topStrs = { //
            "、ヽ｀、ヽ｀个o(･ω･｡)｀ヽ、｀ヽ", //
            "ヽ｀、ヽ｀、个o(･ω･｡)ヽ、｀ヽ｀" //
    };//
    private int topStrCnt = 0;
    private int topStrFlip = 0;

    protected String getTopString() {
        if (topStrCnt > 30) {
            topStrFlip = (topStrFlip + 1) % topStrs.length;
            topStrCnt = 0;
        }

        if (frameLimiter.isEventted30()) {
            topStrCnt++;
        }
        return topStrs[topStrFlip];
    }

    protected void copyFromNotesImage(Graphics g) {
        Dimension dim = this.getContentPane().getSize();

        if (SystemProperties.getInstance().isViewReverse() == false) {
            Graphics2D lotG2d = (Graphics2D) g;

            // 1. 元状態の保存
            AffineTransform oldTransform = lotG2d.getTransform();

            // 2. レンダリングヒントの設定
            lotG2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, SystemProperties.getInstance().getImageInterpol());
            lotG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            lotG2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

            int panelW = getContentPane().getWidth();
            int panelH = getContentPane().getHeight();
            int imgW = orgScreenImage.getWidth(null);
            int imgH = orgScreenImage.getHeight(null);

            // 3. 行列計算を一発で組み立てる
            AffineTransform tx = new AffineTransform();

            // 現在の仕様（180度回転 + Y軸反転）の座標変換を1つの数式にまとめる
            // ウィンドウ中心への移動と、画像サイズに合わせたスケーリングを同時に行う
            double scaleX = (double) panelW / imgW;
            double scaleY = (double) panelH / imgH;

            tx.translate(panelW / 2.0, panelH / 2.0);
            tx.scale(scaleX, -scaleY);
            tx.rotate(Math.toRadians(180)); // 180度は Math.PI でもOK
            tx.translate(-imgW / 2.0, -imgH / 2.0);

            // 4. 変換行列を適用して描画
            lotG2d.transform(tx);
            lotG2d.drawImage(orgScreenImage, 0, 0, null);

            // 5. 元状態に戻す
            lotG2d.setTransform(oldTransform);
        }
        else {
            g.drawImage(orgScreenImage, 0, 0, (int) dim.getWidth(), (int) dim.getHeight(), 0, 0, orgScreenImage.getWidth(null), orgScreenImage.getHeight(null),
                    null);
        }
    }

    protected void updateViewport() {
        int paneHeight = getContentPane().getHeight();
        viewportManager.updateOffs(paneHeight, getOrgHeight(), measCellHeight);
    }

    protected void copyFromScreenImage(Graphics g) {
        int paneWidth = getContentPane().getWidth();
        int paneHeight = getContentPane().getHeight();

        int curS1 = viewportManager.getOffsetCoordE();
        int curE2 = viewportManager.getOffsetCoordS();
        int cY1 = curS1;
        int cY2 = paneHeight - curE2 - 1;
        int cH = cY2 - curS1 + 1;
        int cW = (int) ((double) paneWidth * ((double) cH / (double) paneHeight));

        int clipX = 0;
        if (SystemProperties.getInstance().isViewReverse() == false) {
            clipX = paneWidth - cW;
        }
        else {
            clipX = 0;
        }
        int clipY = cY1;
        int clipW = cW;
        int clipH = cH;

        int dX1 = clipX;
        int dY1 = clipY;
        int dX2 = clipX + clipW - 1;
        int dY2 = clipY + clipH - 1;
        g.drawImage(bufferScreenImage, 0, 0, bufferScreenImageWidth - 1, bufferScreenImageHeight - 1, dX1, dY1, dX2, dY2, null);
    }

    private double angle = 0;
    private Color armColor = null;
    private Color[] armColors = new Color[12];
    private float spinR = 0f;
    private float spinG = 0f;
    private float spinB = 0f;

    private void drawSpinner(Graphics2D g2d) {
        int w = getContentPane().getWidth();
        int h = getContentPane().getHeight();
        int spinnerRadius = 120; // スピナーのサイズ半径

        if (frameLimiter.isEventted60()) {
            angle += 0.1;
        }

        if (armColor == null) {
            armColor = LayoutManager.getInstance().getCursorColor().getBgColor();
            spinR = (float) armColor.getRed() / 255.0f;
            spinG = (float) armColor.getGreen() / 255.0f;
            spinB = (float) armColor.getBlue() / 255.0f;

            for (int i = 0; i < 12; i++) {
                float alpha = (i + 1) / 12f;
                armColors[i] = new Color(spinR, spinG, spinB, alpha);
            }
        }

        g2d.translate(w / 2, h / 2);
        g2d.rotate(angle);

        // 回転アームを描画（12本）
        for (int i = 0; i < 12; i++) {
            g2d.setColor(armColors[i]);
            int armWidth = 32;
            int armHeight = 10;
            g2d.fillRoundRect(spinnerRadius, -armHeight / 2, armWidth, armHeight, 10, 10);
            g2d.rotate(Math.PI / 6);
        }

        // 描画座標を元に戻す
        g2d.rotate(-angle);
        g2d.translate(-w / 2, -h / 2);
    }

    public void paintVolume(Graphics g) {
        g.setFont(msgFontS);
        g.setColor(LayoutManager.getInstance().getPlayerColor().getBgRevColor());
        FontMetrics fm = g.getFontMetrics();
        int paneWidth = getContentPane().getWidth();
        int paneHeight = getContentPane().getHeight();
        int volConWidth = 240;
        int volConHeight = 16;
        int volConX = (paneWidth - volConWidth) / 2;
        int volConY = (paneHeight - volConHeight) / 2 + 125;
        if (volumeControl.isVisible()) {
            sb.setLength(0);
            sb.append("Volume: ");
            int stringWidth = fm.stringWidth(sb.toString());
            int stringHeight = fm.getHeight();
            int strX = volConX - stringWidth;
            int strY = volConY + (stringHeight / 2);
            g.drawString(sb.toString(), strX, strY);
        }
        volumeControl.setLocation(volConX, volConY, volConWidth, volConHeight);
        volumeControl.paint(g);
    }

    public void renderMidiNotesDisplay(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        int paneWidth = getContentPane().getWidth();
        int paneHeight = getContentPane().getHeight();

        /* ノーツ描画 */
        GraphicsConfiguration gc = getGraphicsConfiguration();
        if (isAvailableGpu) {
            VolatileImage vi = (VolatileImage) orgScreenImage;
            if (vi == null || vi.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE) {
                orgScreenImage = LayoutManager.getInstance().createDisplayImage(getOrgWidth(), getOrgHeight());
                orgScreenGraphic = ((VolatileImage) orgScreenImage).createGraphics();
            }
        }
        else {
            BufferedImage bi = (BufferedImage) orgScreenImage;
            if (bi == null) {
                orgScreenImage = LayoutManager.getInstance().createBufferdImage(getOrgWidth(), getOrgHeight());
                orgScreenGraphic = ((BufferedImage) orgScreenImage).createGraphics();
            }
        }

        boolean updateBuffer = false;
        if (bufferScreenImage == null) {
            updateBuffer = true;
        }
        else if (bufferScreenImageWidth != paneWidth || bufferScreenImageHeight != paneHeight) {
            updateBuffer = true;
        }

        if (isAvailableGpu) {
            VolatileImage vi = (VolatileImage) bufferScreenImage;
            if (vi == null || vi.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE) {
                updateBuffer = true;
            }
        }

        if (updateBuffer == true) {
            updateViewport();

            if (isAvailableGpu) {
                bufferScreenImage = LayoutManager.getInstance().createDisplayImage(paneWidth, paneHeight);
                bufferScreenGraphic = ((VolatileImage) bufferScreenImage).createGraphics();
            }
            else {
                bufferScreenImage = LayoutManager.getInstance().createBufferdImage(paneWidth, paneHeight);
                bufferScreenGraphic = ((BufferedImage) bufferScreenImage).createGraphics();
            }

            bufferScreenImageWidth = bufferScreenImage.getWidth(null);
            bufferScreenImageHeight = bufferScreenImage.getHeight(null);
        }

        paintContents(orgScreenGraphic);

        Graphics2D bg2 = (Graphics2D) bufferScreenGraphic;

        // 補間方法を設定
        bg2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, SystemProperties.getInstance().getImageInterpol()); // バイリニア補間
        bg2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        copyFromNotesImage(bg2);

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, SystemProperties.getInstance().getImageInterpol()); // バイリニア補間
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        copyFromScreenImage(g2);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private MonitorData monitorInfo = new MonitorData();

    public void paintDisplay(Graphics g) {
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        frameLimiter.frameEvent();
        int paneWidth = getContentPane().getWidth();
        int paneHeight = getContentPane().getHeight();

        Graphics2D g2 = (Graphics2D) g;
        g.clearRect(0, 0, paneWidth, paneHeight);

        renderMidiNotesDisplay(g);

        // スペクトラム表示
        spectrumPainter.paintSpectram(g, paneWidth, paneHeight, dummySpectWave, noiseBuf, dummySpectSamples);

        if (JMPCoreAccessor.getSystemManager().getStatus(ISystemManager.SYSTEM_STATUS_ID_FILE_LOADING) == true && isFirstRendering == false) {
            int fsize = 28;
            g.setFont(msgFont);
            g.setColor(LayoutManager.getInstance().getCursorColor().getBgColor());
            FontMetrics fm = g.getFontMetrics();
            int stringWidth = 0;
            int stringHeight = 0;

            sb.setLength(0);
            sb.append("＼＿ヘ(Д｀*)");
            stringWidth = fm.stringWidth(sb.toString());
            stringHeight = fm.getHeight();
            int strX = (paneWidth - stringWidth) / 2;
            int strY = (paneHeight - stringHeight) / 2 + 20;
            g.drawString(sb.toString(), strX, strY - (fsize / 2));
            sb.setLength(0);
            sb.append("Now loading");
            stringWidth = fm.stringWidth(sb.toString());
            for (int i = 0; i < (cnt / 10); i++) {
                sb.append(".");
            }

            if (cnt >= 30) {
                cnt = 0;
            }
            cnt++;
            stringHeight = fm.getHeight();
            strX = (paneWidth - stringWidth) / 2;
            g.drawString(sb.toString(), strX, strY + (fsize / 2));
            strY += fsize;

            if (midiUnit.isProgressNowAnalyzing() == true) {
                strY += 5;
                fsize = 16;
                g.setFont(msgFontS);
                fm = g.getFontMetrics();

                sb.setLength(0);
                sb.append("NOTES: ").append(midiUnit.getProgressNotesCount());
                stringWidth = fm.stringWidth(sb.toString());
                strX = (paneWidth - stringWidth) / 2;
                g.drawString(sb.toString(), strX, strY + (fsize / 2));
                strY += fsize + 2;

                sb.setLength(0);
                sb.append("TRACK: ").append(midiUnit.getProgressFinTrackNum()).append("/").append(midiUnit.getNumOfTrack());
                stringWidth = fm.stringWidth(sb.toString());
                strX = (paneWidth - stringWidth) / 2;
                g.drawString(sb.toString(), strX, strY + (fsize / 2));
                strY += fsize + 2;
            }
            drawSpinner((Graphics2D) g);
            volumeControl.setVisible(false);
        }
        else if (midiUnit.isValidSequence() == false && isFirstRendering == false) {
            g.setFont(msgFont);
            FontMetrics fm = g.getFontMetrics();
            g.setColor(LayoutManager.getInstance().getPlayerColor().getBgRevColor());
            sb.setLength(0);
            sb.append(getTopString());
            int stringWidth = fm.stringWidth(sb.toString());
            int stringHeight = fm.getHeight();
            int strX = (paneWidth - stringWidth) / 2;
            int strY = (paneHeight - stringHeight) / 2;
            g.drawString(sb.toString(), strX, strY - 20);
            sb.setLength(0);
            sb.append("Drag and Drop your MIDI or MIDI & AUDIO files here.");
            stringWidth = fm.stringWidth(sb.toString());
            stringHeight = fm.getHeight();
            strX = (paneWidth - stringWidth) / 2;
            g.drawString(sb.toString(), strX, strY + 20);

            volumeControl.setVisible(true);
        }
        else if (imageWorkerMgr.getNotesImage() == null || isFirstRendering == true) {
            // 描画が追いついていない
            int fsize = 28;

            int finWorker = 0;
            for (int i = 0; i < imageWorkerMgr.getNumOfWorker(); i++) {
                if (imageWorkerMgr.getWorker(i).isExec() == false) {
                    finWorker++;
                }
            }

            g.setColor(LayoutManager.getInstance().getCursorColor().getBgColor());
            g.setFont(msgFont);
            FontMetrics fm = g.getFontMetrics();
            sb.setLength(0);
            sb.append("...φ(｡_｡*)");
            int stringWidth = fm.stringWidth(sb.toString());
            int stringHeight = fm.getHeight();
            int strX = (paneWidth - stringWidth) / 2;
            int strY = (paneHeight - stringHeight) / 2 + 20;
            g.drawString(sb.toString(), strX, strY - (fsize / 2));
            sb.setLength(0);
            sb.append("Rendering now");
            stringWidth = fm.stringWidth(sb.toString());
            stringHeight = fm.getHeight();
            strX = (paneWidth - stringWidth) / 2;
            g.drawString(sb.toString(), strX, strY + (fsize / 2));

            g.setFont(msgFontS);
            sb.setLength(0);
            sb.append(finWorker).append("/").append(imageWorkerMgr.getNumOfWorker());
            fm = g.getFontMetrics();
            stringWidth = fm.stringWidth(sb.toString());
            stringHeight = fm.getHeight();
            strX = (paneWidth - stringWidth) / 2;
            strY = (paneHeight - stringHeight) / 2 + 38;
            g.drawString(sb.toString(), strX, strY + (fsize / 2));

            drawSpinner((Graphics2D) g);
            volumeControl.setVisible(false);
        }
        else {
            volumeControl.setVisible(false);
        }

        if (SystemProperties.getInstance().isDebugMode() == true) {
            INotesMonitor notesMonitor = JMPCoreAccessor.getSoundManager().getNotesMonitor();
            for (int i = 0; i < imageWorkerMgr.getNumOfWorker(); i++) {
                int dbx = 10 + (i * 15);
                if (imageWorkerMgr.getWorker(i).isExec() == false) {
                    g.setColor(Color.GREEN);
                }
                else {
                    g.setColor(Color.RED);
                }
                g.fillRect(dbx, paneHeight - 35, 10, 10);
                g.setColor(Color.BLACK);
                g.drawRect(dbx, paneHeight - 35, 10, 10);
                if (debugRenderTime < imageWorkerMgr.getWorker(i).getDebugRenderTime()) {
                    debugRenderTime = imageWorkerMgr.getWorker(i).getDebugRenderTime();
                }
            }

            OsInfoWrapper osInfo = SystemProperties.getInstance().getOsInfo();

            g.setFont(msgFontSS);
            sb.setLength(0);
            int val1 = (int) midiUnit.getTempoInBPM();
            int val2 = (int) ((midiUnit.getTempoInBPM() - val1) * 100);
            sb.append("CPU:").append(DF.format(osInfo.getUsageCpu() * 100.0)).append("%") //
                    .append(", RAM:").append(DF.format(osInfo.getUsageRam() * 100.0)).append("%") //
                    .append(", FPS:").append(getFPS()).append(", BPM:").append(val1).append(".").append(val2) //
                    .append(", PPQ:").append(midiUnit.getResolution()) //
                    .append(", TICK:").append(midiUnit.getTickPosition()).append("/").append(midiUnit.getTickLength()) //
                    .append(", NOTES:").append(notesMonitor.getNotesCount()).append("/").append(notesMonitor.getNumOfNotes()) //
                    .append(", RENDER:").append(debugRenderTime).append("ms"); //
            int strX = 10;
            int strY = paneHeight - 10;
            g.setColor(Color.BLACK);
            g.drawString(sb.toString(), strX + 1, strY + 1);
            g.setColor(Color.GREEN);
            g.drawString(sb.toString(), strX, strY);
        }

        if (LayoutManager.getInstance().getVolumeVisibleTime() != -1) {
            long current = System.currentTimeMillis();
            long elapsed = current - LayoutManager.getInstance().getVolumeVisibleTime();
            if (elapsed > 3000) {
                LayoutManager.getInstance().clearVolumeVisibleTime();
                volumeControl.setVisible(false);
            }
            else {
                volumeControl.setVisible(true);
            }
        }

        paintVolume(g);

        paintWindowEffect(g);

        if (SystemProperties.getInstance().isVisibleRsrcMonitor()) {
            OsInfoWrapper osInfo = SystemProperties.getInstance().getOsInfo();

            Graphics2D gGrap = (Graphics2D) g.create();
            gGrap.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color backStrColor = LayoutManager.getInstance().getPlayerColor().getBgColor();
            Color topStrColor = LayoutManager.getInstance().getPlayerColor().getBgRevColor();

            int grapW = 100;
            int grapH = 60;
            int grapX = this.getWidth() - grapW - 30;
            int grapY = 10;
            int gwRes = 0;
            float[] data;

            if (SystemProperties.getInstance().getMonitorType() == SyspMonitorType.TYPE1) {
                grapX = 120;
                grapY = 216;
            }

            // CPU
            sb.setLength(0);
            sb.append("CPU");
            gGrap.setColor(GRAPH_BG_COLOR);
            gGrap.fillRect(grapX, grapY, grapW, grapH);
            gGrap.setFont(graphTitleFont);
            gGrap.setColor(backStrColor);
            gGrap.drawString(sb.toString(), grapX + 3, grapY + 22);
            gGrap.setColor(topStrColor);
            gGrap.drawString(sb.toString(), grapX + 2, grapY + 21);
            gGrap.setStroke(GRAPH_BORDER_STROKE);
            data = osInfo.getUsageCpuBuffer();
            gwRes = data.length - 1;
            gwRes = data.length - 1;
            gGrap.setColor(Color.GREEN);
            gGrap.setStroke(GRAPH_BORDER_STROKE);
            for (int i = 0; i < data.length - 1; i++) {
                float dt1 = data[i];
                float dt2 = data[i + 1];
                int x1 = grapX + (i * grapW / gwRes);
                int y1 = grapY + (grapH - (int) (dt1 * grapH / 1.0f));
                int x2 = grapX + ((i + 1) * grapW / gwRes);
                int y2 = grapY + (grapH - (int) (dt2 * grapH / 1.0f));
                gGrap.drawLine(x1, y1, x2, y2);
            }
            sb.setLength(0);
            sb.append(DF.format(osInfo.getUsageCpu() * 100.0)).append("%");
            gGrap.setFont(graphFont);
            gGrap.setColor(Color.WHITE);
            gGrap.drawString(sb.toString(), grapX, grapY + grapH + 17);
            gGrap.setStroke(GRAPH_FRAMEBORDER_STROKE);
            gGrap.setColor(Color.WHITE);
            gGrap.drawRect(grapX - 1, grapY, grapW + 2, grapH + 1);

            grapY += grapH + 28;

            // RAM
            sb.setLength(0);
            sb.append("RAM");
            gGrap.setColor(GRAPH_BG_COLOR);
            gGrap.fillRect(grapX, grapY, grapW, grapH);
            gGrap.setFont(graphTitleFont);
            gGrap.setColor(backStrColor);
            gGrap.drawString(sb.toString(), grapX + 3, grapY + 22);
            gGrap.setColor(topStrColor);
            gGrap.drawString(sb.toString(), grapX + 2, grapY + 21);
            gGrap.setStroke(GRAPH_BORDER_STROKE);
            data = osInfo.getUsageRamBuffer();
            gwRes = data.length - 1;
            gwRes = data.length - 1;
            gGrap.setColor(Color.GREEN);
            gGrap.setStroke(GRAPH_BORDER_STROKE);
            for (int i = 0; i < data.length - 1; i++) {
                float dt1 = data[i];
                float dt2 = data[i + 1];
                int x1 = grapX + (i * grapW / gwRes);
                int y1 = grapY + (grapH - (int) (dt1 * grapH / 1.0f));
                int x2 = grapX + ((i + 1) * grapW / gwRes);
                int y2 = grapY + (grapH - (int) (dt2 * grapH / 1.0f));
                gGrap.drawLine(x1, y1, x2, y2);
            }
            sb.setLength(0);
            sb.append(DF.format(osInfo.getUsageRam() * 100.0)).append("%");
            gGrap.setFont(graphFont);
            gGrap.setColor(Color.WHITE);
            gGrap.drawString(sb.toString(), grapX, grapY + grapH + 17);
            gGrap.setStroke(GRAPH_FRAMEBORDER_STROKE);
            gGrap.setColor(Color.WHITE);
            gGrap.drawRect(grapX - 1, grapY, grapW + 2, grapH + 1);

        }

        monitorInfo.fps = getFPS();
        monitorInfo.width = paneWidth;
        monitorInfo.height = paneHeight;

        monitorPainter.paintMonitor(g, monitorInfo);
        umbrellaUI.paint(g);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    private LinearGradientPaint topVigPaint = null;

    protected void paintWindowEffect(Graphics g) {
        int w = getContentPane().getWidth();
        int h = getContentPane().getHeight();

        if (SystemProperties.getInstance().getWinEffect() == SyspWinEffect.CIRCLE_VIGNETTE) {
            Graphics2D effeG2 = (Graphics2D) g.create();

            effeG2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            effeG2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            Color bc = LayoutManager.getInstance().getPlayerColor().getBgRev2Color();
            int r = bc.getRed();
            int g2 = bc.getGreen();
            int b = bc.getBlue();

            // 画面の対角線の半分を半径とする
            float radius = (float) (Math.sqrt(w * w + h * h) / 2.0);

            float[] colorF = new float[] { 0.0f, 0.3f, 0.65f, 0.85f, 1.0f };
            Color[] colorGrad = new Color[] { new Color(r, g2, b, 0), // 中央30%までは完全透明
                    new Color(r, g2, b, 0), // 30%地点（ここから色が乗り始める）
                    new Color(r, g2, b, 70), // 65%地点：画面の中間層もほんのり暗く包む
                    new Color(r, g2, b, 160), // 85%地点：かなり色が濃くなる
                    new Color(r, g2, b, 240) // 四隅（最外周）：ほぼ不透明に近い濃さに
            };

            RadialGradientPaint paint = new RadialGradientPaint(new Point(w / 2, h / 2), radius, colorF, colorGrad);

            effeG2.setPaint(paint);
            effeG2.fillRect(0, 0, w, h); // 画面全体を塗る

            effeG2.dispose();
        }
        else if (SystemProperties.getInstance().getWinEffect() == SyspWinEffect.TOP_VIGNETTE) {
            float darkHeight = h * 0.4f; // 上40%を暗く

            if (topVigPaint == null) {
                topVigPaint = new LinearGradientPaint(0, 0, 0, darkHeight, new float[] { 0f, 1f }, new Color[] { new Color(0, 0, 0, 180), // 上：かなり暗い
                        new Color(0, 0, 0, 0) // 下：透明
                });
            }

            Graphics2D effeG2 = (Graphics2D) g.create();
            Paint old = effeG2.getPaint();
            effeG2.setPaint(topVigPaint);
            effeG2.fillRect(0, 0, w, (int) darkHeight);
            effeG2.setPaint(old);
        }
    }

    private void calcDispMeasCount() {
        int x = getZeroPosition();
        int measLen = 0;
        while (x <= getOrgWidth() * (SystemProperties.getInstance().getNotesImageCount() - 1)) {
            x += getMeasCellWidth();
            measLen++;
        }
        dispMeasCount = measLen;
    }

    public int getDispMeasCount() {
        return dispMeasCount;
    }

    protected int getEffectWidth(int dir) {
        return (dir < 0) ? 2 : 4;
    }

    private boolean isBetweenColor(int rgbA, int rgbB, int rgbC) {
        int ar = (rgbA >> 16) & 0xFF;
        int ag = (rgbA >> 8) & 0xFF;
        int ab = (rgbA) & 0xFF;

        int br = (rgbB >> 16) & 0xFF;
        int bg = (rgbB >> 8) & 0xFF;
        int bb = (rgbB) & 0xFF;

        int cr = (rgbC >> 16) & 0xFF;
        int cg = (rgbC >> 8) & 0xFF;
        int cb = (rgbC) & 0xFF;

        return inBetween(ar, br, cr) && inBetween(ag, bg, cg) && inBetween(ab, bb, cb);
    }

    private boolean inBetween(int value, int min, int max) {
        return (value >= Math.min(min, max)) && (value <= Math.max(min, max));
    }

    public int getKeyColor(int midiNo) {
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        if (SystemProperties.getInstance().getKeyFocusFunc() == SyspKeyFocusFunc.COLOR) {
            Image notesImg = imageWorkerMgr.getNotesImage();
            if (notesImg == null || JMPCoreAccessor.getSoundManager().isPlay() == false) {
                return 0;
            }
            long tickPos = midiUnit.getTickPosition();
            long relPosTick = tickPos + midiUnit.getResolution() * getLeftMeas();
            // 相対tick位置を座標に変換(TICK × COORD / RESOLUTION)
            int tickX = (int) ((double) relPosTick * (double) getMeasCellWidth() / (double) midiUnit.getResolution());
            int effePickX = tickX + LayoutManager.getInstance().getTickBarPosition();
            int effePickY = hitEffectPosY[127 - midiNo] + (getMeasCellHeight() / 2);
            int rgb = -1;
            int bgrgb = LayoutManager.getInstance().getPlayerColor().getBgColor().getRGB();
            int bdrgb = LayoutManager.getInstance().getPlayerColor().getBdColor().getRGB();
            if ((0 <= effePickX && effePickX < notesImg.getWidth(null)) && (0 <= effePickY && effePickY < notesImg.getHeight(null))) {
                rgb = ((BufferedImage) notesImg).getRGB(effePickX, effePickY);

                for (int i = 0; i < LayoutManager.getInstance().getNotesColorSize(); i++) {
                    Color bc = LayoutManager.getInstance().getNotesColor(i).getBdColor();
                    if (rgb == bc.getRGB()) {
                        rgb = LayoutManager.getInstance().getNotesColor(i).getBgColor().getRGB();
                        break;
                    }

                    Color grad1 = LayoutManager.getInstance().getNotesColor(i).getGradColorBegin();
                    Color grad2 = LayoutManager.getInstance().getNotesColor(i).getGradColorEnd();
                    if (isBetweenColor(rgb, grad1.getRGB(), grad2.getRGB())) {
                        rgb = LayoutManager.getInstance().getNotesColor(i).getBgColor().getRGB();
                        break;
                    }
                }

            }
            else {
                rgb = 0;
            }

            if (rgb != 0 && rgb != bgrgb && rgb != bdrgb) {
                return rgb;
            }
            else {
                return 0;
            }
        }
        else {
            INotesMonitor notesMonitor = JMPCoreAccessor.getSoundManager().getNotesMonitor();
            boolean isAsc = false;
            if (SystemProperties.getInstance().getLayerOrder() == SyspLayerOrder.ASC) {
                isAsc = true;
            }
            int track = 0;
            if (LayoutManager.getInstance().getColorRule() == EColorRule.Track) {
                track = notesMonitor.getTopNoteOnTrack(midiNo, isAsc);
            }
            else {
                track = notesMonitor.getTopNoteOnChannel(midiNo, isAsc);
            }
            if (track != -1) {
                Color color = LayoutManager.getInstance().getNotesColor(track).getBgColor();
                return color.getRGB();
            }
            else {
                return 0;
            }
        }
    }

    private int cnt = 0;

    protected void paintContents(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g.setColor(LayoutManager.getInstance().getPlayerColor().getBgColor());
        g.fillRect(0, 0, getOrgWidth(), getOrgHeight());

        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();

        // フリップ
        calcDispMeasCount();
        if (midiUnit.isRunning() == true) {
            flipPage();
        }

        Image notesImg = imageWorkerMgr.getNotesImage();
        boolean validNotesImg = false;
        if (JMPCoreAccessor.getSystemManager().getStatus(ISystemManager.SYSTEM_STATUS_ID_FILE_LOADING) == false && isFirstRendering == false
                && imageWorkerMgr.getNotesImage() != null) {
            validNotesImg = true;
        }

        if (validNotesImg == true) {
            // 現在の画面に表示する相対tick位置を求める
            long tickPos = midiUnit.getTickPosition();
            long relPosTick = tickPos + midiUnit.getResolution() * getLeftMeas();
            // 相対tick位置を座標に変換(TICK × COORD / RESOLUTION)
            int tickX = (int) ((double) relPosTick * (double) getMeasCellWidth() / (double) midiUnit.getResolution());
            g.drawImage(notesImg, -tickX, 0, null);
        }

        int rgb = -1;

        // ストロークを戻す
        g2d.setStroke(DEFAULT_STROKE);

        int tickBarPositionOffs = 0;
        if (LayoutManager.getInstance().getCursorType() == LayoutConfig.ECursorType.Keyboard) {
            tickBarPositionOffs = 3;
        }

        rgb = -1;
        if (LayoutManager.getInstance().getCursorType() == LayoutConfig.ECursorType.Keyboard) {
            /* White Keyboard */
            Color keyBgColor;
            boolean isPush = false;
            keyboardPainter.setKeyboardWidth(getKeyboardWidth());
            for (int i = 0; i < aHakken.length; i++) {
                rgb = getKeyColor(aHakken[i].midiNo);
                isPush = true;
                if (rgb == 0) {
                    rgb = Color.WHITE.getRGB();
                    isPush = false;
                }
                keyBgColor = LayoutManager.getInstance().rgbToNotesColor(rgb, Color.WHITE);
                keyboardPainter.paintKeyparts(g2d, aHakken[i], keyBgColor, Color.LIGHT_GRAY, isPush, KindOfKey.WHITE);
            }
        }

        if (LayoutManager.getInstance().getCursorType() == LayoutConfig.ECursorType.Keyboard) {
            /* Black Keyboard */
            Color keyBgColor;
            boolean isPush = false;
            keyboardPainter.setKeyboardWidth(getKeyboardWidth());
            for (int i = 0; i < aKokken.length; i++) {
                rgb = getKeyColor(aKokken[i].midiNo);
                isPush = true;
                if (rgb == 0) {
                    rgb = Color.BLACK.getRGB();
                    isPush = false;
                }
                keyBgColor = LayoutManager.getInstance().rgbToNotesColor(rgb, Color.BLACK);
                keyboardPainter.paintKeyparts(g2d, aKokken[i], keyBgColor, Color.LIGHT_GRAY, isPush, KindOfKey.BLACK);
            }
        }

        /* 衝突エフェクト描画 */
        CollisionEffectPainter colEffePainterIn = LayoutManager.getInstance().getCollisionEffectPainterIn();
        CollisionEffectPainter colEffePainterOut = LayoutManager.getInstance().getCollisionEffectPainterOut();

        int tickBarPosition = LayoutManager.getInstance().getTickBarPosition();
        if (tickBarPosition > 0 && validNotesImg == true) {
            Color hitEffectColor = LayoutManager.getInstance().getCursorColor().getEffeColor();
            g.setColor(LayoutManager.getInstance().getPlayerColor().getBgColor());
            int keyHeight = getMeasCellHeight();
            int effx = tickBarPosition;
            Color keyBgColor;
            for (int i = 0; i < 128; i++) {
                boolean isFocus = false;
                rgb = getKeyColor(127 - i);
                if (rgb != 0) {
                    isFocus = true;
                }
                else {
                    isFocus = false;
                }

                if (isFocus == true) {
                    keyBgColor = LayoutManager.getInstance().rgbToNotesColor(rgb, Color.WHITE);
                    colEffePainterIn.paintIn(g2d, effx, hitEffectPosY[i], keyHeight, keyBgColor, hitEffectColor);
                    colEffePainterOut.paintOut(g2d, effx, hitEffectPosY[i], keyHeight, keyBgColor, hitEffectColor);
                    g2d.setComposite(AlphaComposite.SrcOver);
                }
            }
        }

        /* Tickbar描画 */
        Color csrColor = LayoutManager.getInstance().getCursorColor().getBdColor();
        tickbarPainter.paintLine(g2d, tickBarPosition + tickBarPositionOffs, 0, tickBarPosition + tickBarPositionOffs, getOrgHeight(), csrColor);
    }

    public void resetPage() {
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();

        double fbpm = 120.0;
        switch (SystemProperties.getInstance().getNotesSpeedBase()) {
        case AVERAGE:
            fbpm = midiUnit.getAverageTempoInBPM();
            break;
        case MEDIAN:
            fbpm = midiUnit.getMedianTempoInBPM();
            break;
        case FIRST:
            fbpm = midiUnit.getFirstTempoInBPM();
            break;
        case DOMINANT:
        default:
            fbpm = midiUnit.getDominantTempoInBPM();
            break;

        }
        double baseBpm = 140.0 * SystemProperties.getInstance().getNotesSpeed(); // NotesSpeed
                                                                                 // =
                                                                                 // baseとの乖離ってことにする
        int newCellWidth = (int) (480.0 * (baseBpm / fbpm) * SystemProperties.getInstance().getDimOffset());
        if (newCellWidth < SystemProperties.MIN_NOTES_WIDTH) {
            newCellWidth = SystemProperties.MIN_NOTES_WIDTH;
        }
        else if (newCellWidth > SystemProperties.MAX_NOTES_WIDTH) {
            newCellWidth = SystemProperties.MAX_NOTES_WIDTH;
        }

        // 半端に動作しているワーカーを終了させる
        imageWorkerMgr.forcedEnd();

        setMeasCellWidth(newCellWidth);

        calcDispMeasCount();

        int startMeas = (int) midiUnit.getTickPosition() / midiUnit.getResolution();
        setLeftMeas(-startMeas);

        if (isFirstRendering) {
            imageWorkerMgr.firstRender(getLeftMeas(), dispMeasCount, NEXT_FLIP_COUNT);
        }
        else {
            imageWorkerMgr.reset(getLeftMeas(), dispMeasCount, NEXT_FLIP_COUNT);
        }
    }

    private void flipPage() {
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        int offsetLeftMeas = getLeftMeas();
        offsetLeftMeas = (offsetLeftMeas < 0) ? -(offsetLeftMeas) : offsetLeftMeas;
        int flipMergin = -(NEXT_FLIP_COUNT);
        int flipLine = (offsetLeftMeas + dispMeasCount + flipMergin);
        long flipTick = flipLine * midiUnit.getResolution();
        if (midiUnit.getTickPosition() >= flipTick) {
            setLeftMeas(-(flipLine));
            offsetLeftMeas = getLeftMeas();
            imageWorkerMgr.flipPage(offsetLeftMeas, dispMeasCount, NEXT_FLIP_COUNT);
        }
    }

    public int getZeroPosition() {
        return zeroPosition;
    }

    public void setZeroPosition(int zeroPosition) {
        this.zeroPosition = zeroPosition;
    }

    public int getMeasCellWidth() {
        return measCellWidth;
    }

    public void setMeasCellWidth(int measCellWidth) {
        this.measCellWidth = measCellWidth;
    }

    public int getMeasCellHeight() {
        return measCellHeight;
    }

    // public void setMeasCellHeight(int measCellHeight) {
    // this.measCellHeight = measCellHeight;
    // }

    @Override
    public void mouseClicked(MouseEvent e) {
        // umbrellaUI.mouseClicked(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (volumeControl.onPress(e)) {
            currentControl = volumeControl;
        }
        else {
            currentControl = umbrellaUI;
        }

        if (currentControl != null) {
            currentControl.mousePressed(e);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (currentControl != null) {
            currentControl.mouseReleased(e);
            currentControl = null;
        }
        else {
            umbrellaUI.mouseReleased(e);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (currentControl != null) {
            currentControl.mouseEntered(e);
        }
        else {
            umbrellaUI.mouseEntered(e);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (currentControl != null) {
            currentControl.mouseExited(e);
        }
    }

    public int getTopMidiNumber() {
        return topMidiNumber;
    }

    public void setTopMidiNumber(int topMidiNumber) {
        this.topMidiNumber = topMidiNumber;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (currentControl != null) {
            currentControl.mouseDragged(e);
        }
        else {
            umbrellaUI.mouseDragged(e);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (currentControl != null) {
            currentControl.mouseMoved(e);
        }
        else {
            umbrellaUI.mouseMoved(e);
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
    }

    public int getLeftMeas() {
        return leftMeas;
    }

    public void setLeftMeas(int leftMeas) {
        this.leftMeas = leftMeas;
    }

    /**
     *
     * ドラッグ＆ドロップハンドラー
     *
     */
    public class DropFileHandler extends TransferHandler {
        /**
         * ドロップされたものを受け取るか判断 (アイテムのときだけ受け取る)
         */
        @Override
        public boolean canImport(TransferSupport support) {
            if (support.isDrop() == false) {
                // ドロップ操作でない場合は受け取らない
                return false;
            }

            if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor) == false) {
                // ファイルでない場合は受け取らない
                return false;
            }

            return true;
        }

        /**
         * ドロップされたアイテムを受け取る
         */
        @Override
        public boolean importData(TransferSupport support) {
            // ドロップアイテム受理の確認
            if (canImport(support) == false) {
                return false;
            }

            // ドロップ処理
            Transferable t = support.getTransferable();
            try {
                // ドロップアイテム取得
                catchLoadItem(t.getTransferData(DataFlavor.javaFileListFlavor));
                return true;
            }
            catch (Exception e) {
                /* 受け取らない */
            }
            return false;
        }
    }

    public void catchLoadItem(Object item) {
        @SuppressWarnings("unchecked")
        List<File> files = (List<File>) item;

        // 一番先頭のファイルを取得
        if ((files != null) && (files.size() > 0)) {
            SystemProperties.getInstance().loadAudioFiles(files.toArray(new File[0]));
        }
    }
}

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
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
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
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

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
import layout.parts.KeyParts;
import layout.parts.KeyboardPainter;
import layout.parts.KeyboardPainter.KindOfKey;
import layout.parts.MonitorPainter;
import layout.parts.key.BlackKeyParts;
import layout.parts.key.WhiteKeyParts;
import layout.parts.monitor.MonitorData;
import plg.AbstractRenderPlugin;
import plg.OsInfoWrapper;
import plg.SystemProperties;
import plg.SystemProperties.SyspKeyFocusFunc;
import plg.SystemProperties.SyspLayerOrder;
import plg.SystemProperties.SyspWinEffect;
import plg.Utility;

public class RendererWindow extends JFrame implements MouseListener, MouseMotionListener, MouseWheelListener, Runnable {

    private static final DecimalFormat DF = new DecimalFormat("0.0");

    private long delayNano = 0;

    // 次のページにフリップするpx数
    public static final int NEXT_FLIP_COUNT = 0;

    public static final int HIT_EFFECT_STEPS = 16;

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

    protected boolean isAvailableGpu = true;
    protected BufferedImage backBuffer = null;

    private Font msgFont = null;
    private Font msgFontS = null;
    private Font msgFontSS = null;
    
    protected FrameLimiter frameLimiter = null;

    private long debugRenderTime = 0;

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

        if (JMPCoreAccessor.getSystemManager().isEnableStandAlonePlugin() == true) {
            JMPCoreAccessor.getSoundManager().stop();

            if (JMPCoreAccessor.getWindowManager().getMainWindow().isWindowVisible() == true) {
                JMPCoreAccessor.getWindowManager().getMainWindow().setWindowVisible(false);
            }

            JMPCoreAccessor.getSoundManager().removeMidiSequence();

            AbstractRenderPlugin.PluginInstance.launch();
        }
    }

    /**
     * Create the frame.
     */
    public RendererWindow(int winW, int winH) {
        this.setTitle("Rain MIDI");
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

        umbrellaUI = new UmbrellaUI();
        volumeControl = new VolumeControl();
        keyboardPainter = LayoutManager.getInstance().getKeyboardPainter(SystemProperties.getInstance().getViewMode());
        monitorPainter = SystemProperties.getInstance().getMonitorPainter();

        msgFont = new Font(SystemProperties.getInstance().getGeneralFontName(), Font.PLAIN, 28);
        msgFontS = new Font(SystemProperties.getInstance().getGeneralFontName(), Font.PLAIN, 18);
        msgFontSS = new Font(SystemProperties.getInstance().getGeneralFontName(), Font.PLAIN, 14);

        isAvailableGpu = Utility.isGpuAvailable();

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
                canvas.requestFocusInWindow();
                canvas.createBufferStrategy(2); // ダブルバッファリング
                strategy = canvas.getBufferStrategy();

                running = true;
                renderThread = new Thread(this::run, "RenderThread");
                renderThread.start();

                imageWorkerMgr.start();

                adjustTickBar();
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

        while (running) {
            try {
                long now = System.nanoTime();
                long elapsed = now - lastTime;

                if (elapsed >= frameInterval) {
                    if (isAvailableGpu) {
                        render(); // 描画処理
                    }
                    else {
                        renderSoft(); // ソフトレンダリング
                    }
                    frameCount++;
                    lastTime += frameInterval;

                    // FPS計測（1秒ごと）
                    if (now - fpsCounterTime >= TimeUnit.SECONDS.toNanos(1)) {
                        fps = frameCount;
                        frameCount = 0;
                        fpsCounterTime = now;
                    }
                }
                else {
                    // 次フレームまで余裕があればスリープ
                    long sleepNanos = frameInterval - elapsed;
                    if (sleepNanos > 0) {
                        long sleepMillis = sleepNanos / 1_000_000;
                        int sleepNanoRemainder = (int) (sleepNanos % 1_000_000);
                        if (sleepMillis > 0)
                            Thread.sleep(sleepMillis);
                        if (sleepNanoRemainder > 0)
                            LockSupport.parkNanos(sleepNanoRemainder);
                    }
                }
            }
            catch (Throwable e) {
                JMPCoreAccessor.getSystemManager().errorHandle(e);
            }
        }
    }

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
        // CPU上オフスクリーン描画用のバックバッファーを用意
        if (backBuffer == null) {
            backBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D g = backBuffer.createGraphics();
        try {
            paintDisplay(g);
        }
        finally {
            g.dispose();
        }

        // バッファを画面に転送
        Graphics2D screen = (Graphics2D) canvas.getGraphics();
        try {
            screen.drawImage(backBuffer, 0, 0, null);
        }
        finally {
            screen.dispose();
        }

        if (backBuffer.getWidth() != getWidth() || backBuffer.getHeight() != getHeight()) {
            backBuffer = null;
        }
    }

    public void init() {
        imageWorkerMgr = new ImagerWorkerManager(this, getOrgWidth(), getOrgHeight());
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
    protected volatile VolatileImage orgScreenImage = null;
    protected volatile Graphics orgScreenGraphic = null;

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
        g.drawImage(orgScreenImage, 0, 0, (int) dim.getWidth(), (int) dim.getHeight(), 0, 0, orgScreenImage.getWidth(), orgScreenImage.getHeight(), null);
    }

    private double angle = 0;

    private void drawSpinner(Graphics2D g2d) {
        int w = getContentPane().getWidth();
        int h = getContentPane().getHeight();
        int spinnerRadius = 120; // スピナーのサイズ半径

        if (frameLimiter.isEventted60()) {
            angle += 0.1;
        }

        Color armColor = LayoutManager.getInstance().getCursorColor().getBgColor();

        g2d.translate(w / 2, h / 2);
        g2d.rotate(angle);

        float fr = (float) armColor.getRed() / 255.0f;
        float fg = (float) armColor.getGreen() / 255.0f;
        float fb = (float) armColor.getBlue() / 255.0f;

        // 回転アームを描画（12本）
        for (int i = 0; i < 12; i++) {
            float alpha = (i + 1) / 12f;
            g2d.setColor(new Color(fr, fg, fb, alpha));
            int armWidth = 32;
            int armHeight = 10;
            g2d.fillRoundRect(spinnerRadius, -armHeight / 2, armWidth, armHeight, 10, 10);
            g2d.rotate(Math.PI / 6);
        }

        // 描画座標を元に戻す
        g2d.rotate(-angle);
        g2d.translate(-w / 2, -h / 2);
    }

    private void drawNoEffeLine(Graphics2D g2d, int x1, int y1, int x2, int y2, Color baseColor) {
        // ======= 調整用パラメータ =======
        float coreStroke = 8.0f; // 中心の線の太さ
        float borderWidth = 0.5f; // 白ボーダーの幅

        if (LayoutManager.getInstance().isVisibleCursorEffect() == false) {
            // ======= ボーダー線 =======
            g2d.setStroke(new BasicStroke(coreStroke + borderWidth * 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g2d.setColor(Color.BLACK);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // ======= 中心線（コア線） =======
        g2d.setStroke(new BasicStroke(coreStroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(baseColor);
        g2d.drawLine(x1, y1, x2, y2);

        // ストロークを戻す（任意）
        g2d.setStroke(new BasicStroke());

    }

    private void drawGlowingLine(Graphics2D g2d, int x1, int y1, int x2, int y2, Color baseColor) {

        // ======= 調整用パラメータ =======
        // float coreStroke = 5.0f;
        float glowMaxStroke = 24.0f;
        float glowMinStroke = 12.0f;
        float glowStep = 4.0f;

        // ======= カラー分解（ベースカラー → RGB） =======
        int r = baseColor.getRed();
        int g = baseColor.getGreen();
        int b = baseColor.getBlue();

        // ======= グロー層（外側から内側へ） =======
        for (float stroke = glowMaxStroke; stroke >= glowMinStroke; stroke -= glowStep) {
            g2d.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(new Color(r, g, b, 20)); // 外周：淡く光らせる
            g2d.drawLine(x1, y1, x2, y2);
        }

        // ======= 中間グロー層（やや濃い） =======
        // g2d.setStroke(new BasicStroke(glowMinStroke - 1));
        // g2d.setColor(new Color(Math.min(255, r + 40), Math.min(255, g + 40),
        // Math.min(255, b + 40), 100));
        // g2d.drawLine(x1, y1, x2, y2);

        // ======= 中心線（コア線） =======
        // g2d.setStroke(new BasicStroke(coreStroke));
        // g2d.setColor(new Color(Math.min(255, r + 80), Math.min(255, g + 80),
        // Math.min(255, b + 80), 220));
        // g2d.drawLine(x1, y1, x2, y2);

        g2d.setStroke(new BasicStroke());
    }
    
    public void paintVolume(Graphics g) {
        g.setFont(msgFontS);
        g.setColor(LayoutManager.getInstance().getPlayerColor().getBgRevColor());
        FontMetrics fm = g.getFontMetrics();
        sb.setLength(0);
        sb.append("Volume: ");
        int paneWidth = getContentPane().getWidth();
        int paneHeight = getContentPane().getHeight();
        int volConWidth = 240;
        int volConHeight = 16;
        int volConX = (paneWidth - volConWidth) / 2;
        int volConY = (paneHeight - volConHeight) / 2 + 125;
        int stringWidth = fm.stringWidth(sb.toString());
        int stringHeight = fm.getHeight();
        int strX = volConX - stringWidth;
        int strY = volConY + (stringHeight / 2);
        g.drawString(sb.toString(), strX, strY);
        volumeControl.setVisible(true);
        volumeControl.setLocation(volConX, volConY, volConWidth, volConHeight);
        volumeControl.paint(g);
    }

    private MonitorData monitorInfo = new MonitorData();
    public void paintDisplay(Graphics g) {
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        
        frameLimiter.frameEvent();

        /* ノーツ描画 */
        GraphicsConfiguration gc = getGraphicsConfiguration();
        if (orgScreenImage == null || orgScreenImage.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE) {
            orgScreenImage = LayoutManager.getInstance().createDisplayImage(getOrgWidth(), getOrgHeight());
            orgScreenGraphic = orgScreenImage.createGraphics();
        }

        int paneWidth = getContentPane().getWidth();
        int paneHeight = getContentPane().getHeight();

        paintContents(orgScreenGraphic);

        Graphics2D g2 = (Graphics2D) g;

        // 補間方法を設定
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, SystemProperties.getInstance().getImageInterpol()); // バイリニア補間
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        copyFromNotesImage(g);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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

                // sb.setLength(0);
                // sb.append("TICK: ").append(midiUnit.getProgressReadTick());
                // stringWidth = fm.stringWidth(sb.toString());
                // strX = (paneWidth - stringWidth) / 2;
                // g.drawString(sb.toString(), strX, strY + (fsize / 2));
                // strY += fsize + 2;

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
            drawSpinner(g2);
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
            
            paintVolume(g);
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

        paintWindowEffect(g);

        monitorInfo.fps = getFPS();
        monitorInfo.width = paneWidth;
        monitorInfo.height = paneHeight;

        monitorPainter.paintMonitor(g, monitorInfo);
        umbrellaUI.paint(g);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    protected void paintWindowEffect(Graphics g) {
        int w = getContentPane().getWidth();
        int h = getContentPane().getHeight();

        if (SystemProperties.getInstance().getWinEffect() == SyspWinEffect.CIRCLE_VIGNETTE) {
            Graphics2D effeG2 = (Graphics2D) g.create();
            Color bc = LayoutManager.getInstance().getPlayerColor().getBgRev2Color();
            float radius = (float) (Math.max(w, h) * 0.9f);

            Color[] colorGrad = null;
            float[] colorF = null;
            colorF = new float[] { 0.0f, 0.6f, 1.0f };
            colorGrad = new Color[] { new Color(bc.getRed(), bc.getGreen(), bc.getBlue(), 0), new Color(bc.getRed(), bc.getGreen(), bc.getBlue(), 120),
                    new Color(bc.getRed(), bc.getGreen(), bc.getBlue(), 240) };

            RadialGradientPaint paint = new RadialGradientPaint(new Point(w / 2, h / 2), radius, colorF, colorGrad);

            effeG2.setPaint(paint);

            // 確認用は円形で塗る
            effeG2.fillOval(w / 2 - (int) radius, h / 2 - (int) radius, (int) (radius * 2), (int) (radius * 2));
            effeG2.dispose();
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
            BufferedImage notesImg = (BufferedImage) imageWorkerMgr.getNotesImage();
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
            if ((0 <= effePickX && effePickX < notesImg.getWidth()) && (0 <= effePickY && effePickY < notesImg.getHeight())) {
                rgb = notesImg.getRGB(effePickX, effePickY);

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

    private void paintContents(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g.setColor(LayoutManager.getInstance().getPlayerColor().getBgColor());
        g.fillRect(0, 0, getOrgWidth(), getOrgHeight());

        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();

        // フリップ
        calcDispMeasCount();
        if (midiUnit.isRunning() == true) {
            flipPage();
        }

        BufferedImage notesImg = (BufferedImage) imageWorkerMgr.getNotesImage();
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

        /* 衝突エフェクト描画 */
        int rgb = -1;
        int tickBarPosition = LayoutManager.getInstance().getTickBarPosition();
        if (tickBarPosition > 0 && validNotesImg == true) {
            boolean isVisibleNotesIn = LayoutManager.getInstance().isVisibleNotesInEffect();
            boolean isVisibleNotesOut = LayoutManager.getInstance().isVisibleNotesOutEffect();
            if (isVisibleNotesIn || isVisibleNotesOut) {
                Color hitEffectColor = LayoutManager.getInstance().getCursorColor().getEffeColor();
                g.setColor(LayoutManager.getInstance().getPlayerColor().getBgColor());
                int keyHeight = getMeasCellHeight();
                int inEffWidth = getEffectWidth(1);
                int outEffWidth = getEffectWidth(-1);
                int effx = 0;
                g.setColor(hitEffectColor);
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
                        effx = tickBarPosition;
                        for (int j = 0; j < HIT_EFFECT_STEPS; j++) {
                            g2d.setComposite(hitEffeSteps[j]);
                            if (isVisibleNotesIn)
                                g2d.fillRect(effx + (inEffWidth * j), hitEffectPosY[i], inEffWidth, keyHeight);
                            if (isVisibleNotesOut)
                                g2d.fillRect(effx - (outEffWidth * j) - outEffWidth, hitEffectPosY[i], outEffWidth, keyHeight);
                        }
                        g2d.setComposite(AlphaComposite.SrcOver);
                    }
                }
            }
        }

        int tickBarPositionOffs = 0;
        if (LayoutManager.getInstance().getCursorType() == LayoutConfig.ECursorType.Keyboard) {
            tickBarPositionOffs = 3;
        }

        /* Tickbar描画 */
        Color csrColor = LayoutManager.getInstance().getCursorColor().getBdColor();
        drawNoEffeLine(g2d, tickBarPosition + tickBarPositionOffs, 0, tickBarPosition + tickBarPositionOffs, getOrgHeight(), csrColor);
        /* Tickbarグローエフェクト描画 */
        if (LayoutManager.getInstance().isVisibleCursorEffect() == true) {
            drawGlowingLine(g2d, tickBarPosition + tickBarPositionOffs, 0, tickBarPosition + tickBarPositionOffs, getOrgHeight(), csrColor);
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
    }

    public void resetPage() {
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        double fbpm = midiUnit.getFirstTempoInBPM();
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
        //umbrellaUI.mouseClicked(e);
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

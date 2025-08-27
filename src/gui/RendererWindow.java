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
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.TransferHandler;

import image.ImagerWorkerManager;
import jlib.core.ISystemManager;
import jlib.core.JMPCoreAccessor;
import jlib.midi.IMidiUnit;
import jlib.midi.INotesMonitor;
import layout.LayoutConfig;
import layout.LayoutManager;
import plg.AbstractRenderPlugin;
import plg.SystemProperties;
import plg.SystemProperties.SyspLayerOrder;
import plg.SystemProperties.SyspMonitorType;

public class RendererWindow extends JFrame implements MouseListener, MouseMotionListener, MouseWheelListener, Runnable {

    private long delayNano = 0;

    // 次のページにフリップするpx数
    public static final int NEXT_FLIP_COUNT = 0;

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

    private volatile boolean running = false;
    protected Thread renderThread;

    public class KeyInfo {
        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;
        int midiNo = 0;
    }

    protected KeyInfo[] aHakken = null;
    protected KeyInfo[] aKokken = null;

    protected boolean isFirstRendering = false;
    
    protected UmbrellaUI umbrellaUI = null;

    public int getOrgWidth() {
        return SystemProperties.getInstance().getDimWidth();
    }

    public int getOrgHeight() {
        return SystemProperties.getInstance().getDimHeight();
    }
    
    private void windowCloseFunc() {
        setVisible(false);
        AbstractRenderPlugin.PluginInstance.winArray.remove(this);
        
        if (JMPCoreAccessor.getSystemManager().isEnableStandAlonePlugin() == true) {
            JMPCoreAccessor.getSoundManager().stop();
            
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

        measCellWidth = SystemProperties.getInstance().getNotesWidth();
        measCellHeight = getOrgHeight() / 128;// 5;

        LayoutManager.getInstance().initialize(canvas);
        delayNano = 1_000_000_000 / SystemProperties.getInstance().getFixedFps();

        makeKeyboardRsrc();
        
        umbrellaUI = new UmbrellaUI();
    }

    public void formatWithCommas(long number, StringBuilder out) {

        // 負数対応（符号のみ処理）
        boolean negative = number < 0;
        if (negative) {
            number = -number;
        }

        // 数字を一時バッファに逆順で格納
        char[] buffer = new char[20];
        int index = buffer.length;

        int digitCount = 0;
        do {
            if (digitCount > 0 && digitCount % 3 == 0) {
                buffer[--index] = ',';
            }
            buffer[--index] = (char) ('0' + (number % 10));
            number /= 10;
            digitCount++;
        }
        while (number > 0);

        if (negative) {
            buffer[--index] = '-';
        }

        out.append(buffer, index, buffer.length - index);
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

        aHakken = new KeyInfo[75];
        aKokken = new KeyInfo[53];

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
                    aHakken[hkCnt] = new KeyInfo();
                    aHakken[hkCnt].x = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                    aHakken[hkCnt].y = hitEffectPosY[i];
                    aHakken[hkCnt].width = hkWidth;
                    aHakken[hkCnt].height = hakkenHeight;
                    aHakken[hkCnt].y -= (keyHeight / 2);
                    aHakken[hkCnt].midiNo = midiNo;
                    hkCnt++;
                    break;
                case 7:
                case 9:
                case 2:
                    aHakken[hkCnt] = new KeyInfo();
                    aHakken[hkCnt].x = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                    aHakken[hkCnt].y = hitEffectPosY[i];
                    aHakken[hkCnt].width = hkWidth;
                    aHakken[hkCnt].height = hakkenHeight + keyHeight / 2;
                    aHakken[hkCnt].y -= (keyHeight / 2);
                    aHakken[hkCnt].midiNo = midiNo;
                    hkCnt++;
                    break;
                case 4:
                case 11:
                    aHakken[hkCnt] = new KeyInfo();
                    aHakken[hkCnt].x = LayoutManager.getInstance().getTickBarPosition() - hkWidth;
                    aHakken[hkCnt].y = hitEffectPosY[i];
                    aHakken[hkCnt].width = hkWidth;
                    aHakken[hkCnt].height = hakkenHeight;
                    aHakken[hkCnt].midiNo = midiNo;
                    hkCnt++;
                    break;
                case 1:
                case 3:
                case 6:
                case 8:
                case 10:
                    aKokken[kkCnt] = new KeyInfo();
                    aKokken[kkCnt].x = LayoutManager.getInstance().getTickBarPosition() - kkWidth;
                    aKokken[kkCnt].y = hitEffectPosY[i];
                    aKokken[kkCnt].width = kkWidth;
                    aKokken[kkCnt].height = keyHeight;
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
        long startTime = System.nanoTime();
        long nextFrameTime = startTime;
        frameCount = 0;

        while (running) {
            long now = System.nanoTime();

            if (now >= nextFrameTime) {
                render();
                frameCount++;

                long elapsed = now - startTime;
                if (elapsed >= TimeUnit.SECONDS.toNanos(1)) {
                    fps = frameCount;
                    frameCount = 0;
                    startTime = now;
                }

                nextFrameTime += delayNano;
            }
            else {
                // 次のフレームまで余裕があれば軽く寝る
                long sleepTimeMillis = (nextFrameTime - now) / 1_000_000;
                if (sleepTimeMillis > 0) {
                    try {
                        Thread.sleep(sleepTimeMillis);
                    }
                    catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }
    }

    protected void render() {
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
        try {
            paintDisplay(g);
            
            umbrellaUI.paint(g);
        }
        finally {
            // Graphics オブジェクトの解放
            g.dispose();
        }
        strategy.show();
    }

    public void init() {
        imageWorkerMgr = new ImagerWorkerManager(this, getOrgWidth(), getOrgHeight());
    }

    public void loadFile() {
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        if (midiUnit.isValidSequence() == false || running == false || isVisible() == false) {
            return;
        }

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
    private Font info1Font = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
    private Font info2Font = new Font(Font.SANS_SERIF, Font.PLAIN, 36);
    protected volatile VolatileImage orgScreenImage = null;
    protected volatile Graphics orgScreenGraphic = null;

    private static final String[] topStrs = { //
            "、ヽ｀、ヽ｀个o(･ω･｡)｀ヽ、｀ヽ", //
            "ヽ｀、ヽ｀、个o(･ω･｡)ヽ、｀ヽ｀" //
    };//
    private int topStrCnt = 0;
    private int topStrFlip = 0;

    protected String getTopString() {
        if (topStrCnt > SystemProperties.getInstance().getFixedFps() / 2) {
            // 500msに１回フリップ
            topStrFlip = (topStrFlip + 1) % topStrs.length;
            topStrCnt = 0;
        }
        topStrCnt++;
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

        angle += 0.1;

        Color armColor = LayoutManager.getInstance().getCursorColor();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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
        float coreStroke = 5.0f;

        // ======= 中心線（コア線） =======
        g2d.setStroke(new BasicStroke(coreStroke));
        g2d.setColor(baseColor);
        g2d.drawLine(x1, y1, x2, y2);

        g2d.setStroke(new BasicStroke());
    }

    private void drawGlowingLine(Graphics2D g2d, int x1, int y1, int x2, int y2, Color baseColor) {
        // ======= 調整用パラメータ =======
        float coreStroke = 5.0f;
        float glowMaxStroke = 18.0f;
        float glowMinStroke = 9.0f;
        float glowStep = 3.0f;

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
        g2d.setStroke(new BasicStroke(glowMinStroke - 1));
        g2d.setColor(new Color(Math.min(255, r + 40), Math.min(255, g + 40), Math.min(255, b + 40), 100));
        g2d.drawLine(x1, y1, x2, y2);

        // ======= 中心線（コア線） =======
        g2d.setStroke(new BasicStroke(coreStroke));
        g2d.setColor(new Color(Math.min(255, r + 80), Math.min(255, g + 80), Math.min(255, b + 80), 220));
        g2d.drawLine(x1, y1, x2, y2);

        g2d.setStroke(new BasicStroke());
    }

    public void paintDisplay(Graphics g) {
        INotesMonitor notesMonitor = JMPCoreAccessor.getSoundManager().getNotesMonitor();
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();

        /* ノーツ描画 */
        GraphicsConfiguration gc = getGraphicsConfiguration();
        if (orgScreenImage == null || orgScreenImage.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE) {
            orgScreenImage = LayoutManager.getInstance().createLayerImage(getOrgWidth(), getOrgHeight());
            orgScreenGraphic = orgScreenImage.createGraphics();
        }

        int paneWidth = getContentPane().getWidth();
        int paneHeight = getContentPane().getHeight();

        paintContents(orgScreenGraphic);

        Graphics2D g2 = (Graphics2D) g;

        // 補間方法を設定
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); // バイリニア補間
        // g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        // RenderingHints.VALUE_INTERPOLATION_BICUBIC); //高速
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        copyFromNotesImage(g);

        if (JMPCoreAccessor.getSystemManager().getStatus(ISystemManager.SYSTEM_STATUS_ID_FILE_LOADING) == true && isFirstRendering == false) {
            int fsize = 28;
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fsize));
            g.setColor(LayoutManager.getInstance().getCursorColor());
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
            drawSpinner((Graphics2D) g);
        }
        else if (midiUnit.isValidSequence() == false && isFirstRendering == false) {
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 32));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(LayoutManager.getInstance().getBgColorReverse());
            sb.setLength(0);
            sb.append(getTopString());
            int stringWidth = fm.stringWidth(sb.toString());
            int stringHeight = fm.getHeight();
            int strX = (paneWidth - stringWidth) / 2;
            int strY = (paneHeight - stringHeight) / 2;
            g.drawString(sb.toString(), strX, strY - 20);
            sb.setLength(0);
            sb.append("Drag and Drop your MIDI or MIDI and AUDIO files here.");
            stringWidth = fm.stringWidth(sb.toString());
            stringHeight = fm.getHeight();
            strX = (paneWidth - stringWidth) / 2;
            g.drawString(sb.toString(), strX, strY + 20);
        }
        else if (imageWorkerMgr.getNotesImage() == null || isFirstRendering == true) {
            // 描画が追いついていない
            int fsize = 28;
            g.setColor(LayoutManager.getInstance().getCursorColor());
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fsize));
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
            drawSpinner((Graphics2D) g);
        }
        else {
        }

        if (SystemProperties.getInstance().getMonitorType() == SyspMonitorType.TYPE1) {
            int sx = 10;
            int sy = 20;
            int sh = 18;
            Color backStrColor = LayoutManager.getInstance().getBackColor();
            Color topStrColor = LayoutManager.getInstance().getBgColorReverse();
            ;
            g.setFont(info1Font);

            sb.setLength(0);
            sb.append("TIME: ");
            long val1 = JMPCoreAccessor.getSoundManager().getPositionSecond() / 60;
            if (val1 < 10)
                sb.append('0');
            sb.append(val1);
            sb.append(":");
            long val2 = JMPCoreAccessor.getSoundManager().getPositionSecond() % 60;
            if (val2 < 10)
                sb.append('0');
            sb.append(val2);
            sb.append(" / ");
            val1 = JMPCoreAccessor.getSoundManager().getLengthSecond() / 60;
            if (val1 < 10)
                sb.append('0');
            sb.append(val1);
            sb.append(":");
            val2 = JMPCoreAccessor.getSoundManager().getLengthSecond() % 60;
            if (val2 < 10)
                sb.append('0');
            sb.append(val2);
            g.setColor(backStrColor);
            g.drawString(sb.toString(), sx + 1, sy + 1);
            g.setColor(topStrColor);
            g.drawString(sb.toString(), sx, sy);
            sy += sh;

            sb.setLength(0);
            sb.append("TICK: ");
            val1 = JMPCoreAccessor.getSoundManager().getMidiUnit().getTickPosition();
            formatWithCommas(val1, sb);
            g.setColor(backStrColor);
            g.drawString(sb.toString(), sx + 1, sy + 1);
            g.setColor(topStrColor);
            g.drawString(sb.toString(), sx, sy);
            sy += sh;

            if (midiUnit.isRenderingOnlyMode() == false) {
                sb.setLength(0);
                sb.append("NOTES: ");
                val1 = notesMonitor.getNotesCount();
                val2 = notesMonitor.getNumOfNotes();
                formatWithCommas(val1, sb);
                sb.append(" / ");
                formatWithCommas(val2, sb);
                g.setColor(backStrColor);
                g.drawString(sb.toString(), sx + 1, sy + 1);
                g.setColor(topStrColor);
                g.drawString(sb.toString(), sx, sy);
                sy += sh;
            }
            else {
                sb.setLength(0);
                sb.append("NOTES: ");
                val2 = notesMonitor.getNumOfNotes();
                formatWithCommas(val2, sb);
                g.setColor(backStrColor);
                g.drawString(sb.toString(), sx + 1, sy + 1);
                g.setColor(topStrColor);
                g.drawString(sb.toString(), sx, sy);
                sy += sh;
            }

            if (midiUnit.isRenderingOnlyMode() == false) {
                sb.setLength(0);
                val1 = (long) notesMonitor.getNps();
                sb.append("NPS: ");
                formatWithCommas(val1, sb);
                sb.append(" / ");
                val1 = (long) notesMonitor.getMaxNps();
                formatWithCommas(val1, sb);
                g.setColor(backStrColor);
                g.drawString(sb.toString(), sx + 1, sy + 1);
                g.setColor(topStrColor);
                g.drawString(sb.toString(), sx, sy);
                sy += sh;
            }

            if (midiUnit.isRenderingOnlyMode() == false) {
                sb.setLength(0);
                val1 = (long) notesMonitor.getPolyphony();
                sb.append("POLY: ").append(val1);
                g.setColor(backStrColor);
                g.drawString(sb.toString(), sx + 1, sy + 1);
                g.setColor(topStrColor);
                g.drawString(sb.toString(), sx, sy);
                sy += sh;
            }

            sb.setLength(0);
            val1 = (int) midiUnit.getTempoInBPM();
            val2 = (int) ((midiUnit.getTempoInBPM() - val1) * 100);
            sb.append("BPM: ").append(val1).append(".").append(val2);
            g.setColor(backStrColor);
            g.drawString(sb.toString(), sx + 1, sy + 1);
            g.setColor(topStrColor);
            g.drawString(sb.toString(), sx, sy);
            sy += sh;

            sb.setLength(0);
            val1 = getFPS();
            sb.append("FPS: ").append(val1);
            g.setColor(backStrColor);
            g.drawString(sb.toString(), sx + 1, sy + 1);
            g.setColor(topStrColor);
            g.drawString(sb.toString(), sx, sy);
            sy += sh;

            if (SystemProperties.getInstance().isDebugMode() == true) {
                for (int i = 0; i < imageWorkerMgr.getNumOfWorker(); i++) {
                    int dbx = sx + (i * 15);
                    if (imageWorkerMgr.getWorker(i).isExec() == false) {
                        g.setColor(Color.GREEN);
                    }
                    else {
                        g.setColor(Color.RED);
                    }
                    g.fillRect(dbx, sy + 5, 10, 10);
                }
            }
        }
        else if (SystemProperties.getInstance().getMonitorType() == SyspMonitorType.TYPE2) {
            int sx = 10;
            int sy = 40;
            Color backStrColor = LayoutManager.getInstance().getBackColor();
            Color topStrColor = LayoutManager.getInstance().getBgColorReverse();
            ;
            g.setFont(info2Font);

            sb.setLength(0);

            long val1 = 0;
            if (midiUnit.isRenderingOnlyMode() == false) {
                sb.setLength(0);
                val1 = notesMonitor.getNotesCount();
                formatWithCommas(val1, sb);

                String text = sb.toString();
                FontMetrics fm = g.getFontMetrics();
                int width = fm.stringWidth(text);

                sx = (getWidth() - width) / 2;
                g.setColor(backStrColor);
                g.drawString(sb.toString(), sx + 1, sy + 1);
                g.setColor(topStrColor);
                g.drawString(sb.toString(), sx, sy);
            }
        }
        else {
            /* mode = NONE */
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

    public int getKeyColor(int midiNo) {
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        if (midiUnit.isRenderingOnlyMode() == true) {
            BufferedImage notesImg = (BufferedImage) imageWorkerMgr.getNotesImage();
            if (notesImg == null) {
                return -1;
            }
            long tickPos = midiUnit.getTickPosition();
            long relPosTick = tickPos + midiUnit.getResolution() * getLeftMeas();
            // 相対tick位置を座標に変換(TICK × COORD / RESOLUTION)
            int tickX = (int) ((double) relPosTick * (double) getMeasCellWidth() / (double) midiUnit.getResolution());
            int effePickX = tickX + LayoutManager.getInstance().getTickBarPosition();
            int effePickY = hitEffectPosY[127 - midiNo] + (getMeasCellHeight() / 2);
            int rgb = -1;
            int bgrgb = LayoutManager.getInstance().getBackColor().getRGB();
            int bdrgb = LayoutManager.getInstance().getBorderColor().getRGB();
            if ((0 <= effePickX && effePickX < notesImg.getWidth()) && (0 <= effePickY && effePickY < notesImg.getHeight())) {
                rgb = notesImg.getRGB(effePickX, effePickY);

                for (int i = 0; i < LayoutManager.getInstance().getNotesBorderColors().size(); i++) {
                    Color bc = LayoutManager.getInstance().getNotesBorderColor(i);
                    if (rgb == bc.getRGB()) {
                        rgb = LayoutManager.getInstance().getNotesColor(i).getRGB();
                        break;
                    }
                }

            }
            else {
                rgb = -1;
            }

            if (rgb != -1 && rgb != bgrgb && rgb != bdrgb) {
                return rgb;
            }
            else {
                return -1;
            }
        }
        else {
            INotesMonitor notesMonitor = JMPCoreAccessor.getSoundManager().getNotesMonitor();
            boolean isAsc = false;
            if (SystemProperties.getInstance().getLayerOrder() == SyspLayerOrder.ASC) {
                isAsc = true;
            }
            int track = notesMonitor.getTopNoteOnTrack(midiNo, isAsc);
            if (track != -1) {
                Color color = LayoutManager.getInstance().getNotesColor(track);
                return color.getRGB();
            }
            else {
                return -1;
            }
        }
    }

    private int cnt = 0;

    private void paintContents(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g.setColor(LayoutManager.getInstance().getBackColor());
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

        int tickBarPosition = LayoutManager.getInstance().getTickBarPosition();
        int effOrgX = tickBarPosition;
        int rgb = -1;
        if (LayoutManager.getInstance().getCursorType() == LayoutConfig.ECursorType.Keyboard) {
            /* Keyboard */
            Color keyBgColor;
            for (int i = 0; i < aHakken.length; i++) {
                rgb = getKeyColor(aHakken[i].midiNo);
                if (rgb == -1) {
                    rgb = Color.WHITE.getRGB();
                }
                keyBgColor = new Color(rgb);

                g.setColor(keyBgColor);
                g.fill3DRect(aHakken[i].x, aHakken[i].y, aHakken[i].width, aHakken[i].height, true);
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(aHakken[i].x, aHakken[i].y, aHakken[i].width, aHakken[i].height);
            }
            for (int i = 0; i < aKokken.length; i++) {
                rgb = getKeyColor(aKokken[i].midiNo);
                if (rgb == -1) {
                    rgb = Color.BLACK.getRGB();
                }
                keyBgColor = new Color(rgb);

                g.setColor(keyBgColor);
                g.fill3DRect(aKokken[i].x, aKokken[i].y, aKokken[i].width, aKokken[i].height, true);
            }
        }

        /* 衝突エフェクト描画 */
        if (tickBarPosition > 0 && validNotesImg == true) {
            boolean isVisibleCurEffe = LayoutManager.getInstance().isVisibleCursorEffect();
            if (isVisibleCurEffe == true) {
                Color hitEffectColor = LayoutManager.getInstance().getCursorEffectColor();
                g.setColor(LayoutManager.getInstance().getBackColor());
                int keyHeight = getMeasCellHeight();
                int inEffWidth = getEffectWidth(1);
                int outEffWidth = getEffectWidth(-1);
                int effx = 0;
                g.setColor(hitEffectColor);
                for (int i = 0; i < 128; i++) {
                    boolean isFocus = false;
                    rgb = getKeyColor(127 - i);
                    if (rgb != -1) {
                        isFocus = true;
                    }
                    else {
                        isFocus = false;
                    }

                    if (isFocus == true) {
                        effx = effOrgX;
                        for (int j = 0; j < 16; j++) {
                            float alpha = (1.0f - ((float) j / 16.0f)) * 0.9f;
                            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                            g2d.fillRect(effx + (inEffWidth * j), hitEffectPosY[i] - 1, inEffWidth, keyHeight + 2);
                            g2d.fillRect(effx - (outEffWidth * j) - outEffWidth, hitEffectPosY[i], outEffWidth, keyHeight);
                        }
                        /*
                         * for (int j = 0; j < 10; j++) { float alpha = 1.0f - j
                         * * 0.1f; g2d.setComposite(AlphaComposite.getInstance(
                         * AlphaComposite.SRC_OVER, alpha)); g2d.fillRect(effx,
                         * hitEffectPosY[i], effWidth, keyHeight); effx +=
                         * effWidth; }
                         */
                        g2d.setComposite(AlphaComposite.SrcOver);
                    }
                }
            }
        }

        /* Tickbar描画 */
        Color csrColor = LayoutManager.getInstance().getCursorColor();
        if (LayoutManager.getInstance().isVisibleCursorEffect() == true) {
            drawGlowingLine(g2d, tickBarPosition, 0, tickBarPosition, getOrgHeight(), csrColor);
        }
        else {
            drawNoEffeLine(g2d, tickBarPosition, 0, tickBarPosition, getOrgHeight(), csrColor);
        }

        g2d.setColor(LayoutManager.getInstance().getBackColor());
        for (int i = 0; i < 15; i++) {
            float alpha = (1.0f - ((float) i / 15.0f)) * 0.9f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.fillRect(getOrgWidth() - (4 * (i + 1)), 0, 4, getOrgHeight());
        }
        g2d.setComposite(AlphaComposite.SrcOver);
    }

    public void resetPage() {
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        if (SystemProperties.getInstance().isNotesWidthAuto() == true) {
            double fbpm = midiUnit.getFirstTempoInBPM();
            int newCellWidth = (int) (480.0 * (120.0 / fbpm) * SystemProperties.getInstance().getDimOffset());
            if (newCellWidth < 160) {
                newCellWidth = 160;
            }
            else if (newCellWidth > 2400) {
                newCellWidth = 2400;
            }
            setMeasCellWidth(newCellWidth);
        }
        
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
        int startMeas = (int) midiUnit.getTickPosition() / midiUnit.getResolution();
        int offsetLeftMeas = getLeftMeas();
        offsetLeftMeas = (offsetLeftMeas < 0) ? -(offsetLeftMeas) : offsetLeftMeas;
        int flipMergin = -(NEXT_FLIP_COUNT);
        int flipLine = (offsetLeftMeas + dispMeasCount + flipMergin);
        if (startMeas >= flipLine) {
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

    public void setMeasCellHeight(int measCellHeight) {
        this.measCellHeight = measCellHeight;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        umbrellaUI.mouseClicked(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        umbrellaUI.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        umbrellaUI.mouseReleased(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        umbrellaUI.mouseEntered(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        umbrellaUI.mouseExited(e);
    }

    public int getTopMidiNumber() {
        return topMidiNumber;
    }

    public void setTopMidiNumber(int topMidiNumber) {
        this.topMidiNumber = topMidiNumber;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        umbrellaUI.mouseDragged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        umbrellaUI.mouseMoved(e);
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

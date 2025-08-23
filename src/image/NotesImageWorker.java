package image;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import gui.RendererWindow;
import jlib.core.JMPCoreAccessor;
import jlib.midi.IMidiUnit;
import jlib.midi.INotesMonitor;
import jlib.midi.MappedParseFunc;
import jlib.midi.MidiByte;
import layout.LayoutConfig;
import layout.LayoutManager;
import layout.parts.NotesPainter;
import plg.AbstractRenderPlugin;
import plg.SystemProperties;
import plg.SystemProperties.SyspLayerOrder;

public class NotesImageWorker extends ImageWorker {
    public static final Color FIX_FOCUS_NOTES_BGCOLOR = Color.WHITE;
    public static final Color FIX_FOCUS_NOTES_BDCOLOR = Color.GREEN;

    private class NoteOnCache {
        public long tick = -1;
        public int trackIndex = 0;
        public int channel = 0;
        public int data1 = 0;
        public int data2 = 0;

        NoteOnCache() {
            init();
        }

        public void init() {
            tick = -1;
            trackIndex = 0;
        }
    }

    private BasicStroke normalStroke = new BasicStroke(1.0f);
    private BasicStroke bdStroke = new BasicStroke(2.0f);
    private BasicStroke pbStroke = new BasicStroke(2.0f);
    private NoteOnCache[][] noteOnEvents = null;
    private List<Integer> pbBufferX = null;
    private List<Integer> pbBufferY = null;

    public NotesImageWorker(int width, int height) {
        super(width, height);

        noteOnEvents = new NoteOnCache[16][];
        for (int i = 0; i < 16; i++) {
            noteOnEvents[i] = new NoteOnCache[128];
            for (int j = 0; j < noteOnEvents[i].length; j++) {
                noteOnEvents[i][j] = new NoteOnCache();
            }
        }
        pbBufferX = new ArrayList<Integer>();
        pbBufferY = new ArrayList<Integer>();
    }

    @Override
    public void reset() {
    }

    @Override
    public void run() {
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        if (midiUnit.isValidSequence() == false) {
            return;
        }

        super.run();
    }

    @Override
    public void disposeImage() {
        super.disposeImage();
    }

    @Override
    public int getImageWidth() {
        return (getWidth() * SystemProperties.getInstance().getNotesImageCount()) + (getWidth() / 2);
    }

    @Override
    protected void paintImage(Graphics g) {
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        if (midiUnit.isValidSequence() == false) {
            return;
        }

        paintBorder(g);
        paintNotes(g, getLeftMeasTh());
    }

    protected void paintBorder(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        RendererWindow mainWindow = AbstractRenderPlugin.MainWindow;
        g.setColor(LayoutManager.getInstance().getBorderColor());
        g2d.setStroke(bdStroke);
        int x = mainWindow.getZeroPosition();
        int y = mainWindow.getMeasCellHeight() * 3;
        if (LayoutManager.getInstance().isVisibleHorizonBorder() == true) {
            y = getImageHeight();
            while (y >= 0) {
                g.drawLine(x, y, x + getImageWidth(), y);
                y -= mainWindow.getMeasCellHeight() * 12;
            }
        }
        x = mainWindow.getZeroPosition();
        y = 0;
        if (LayoutManager.getInstance().isVisibleVerticalBorder() == true) {
            while (x <= getImageWidth()) {
                g.drawLine(x, y, x, y + getImageHeight());
                x += (mainWindow.getMeasCellWidth() * 4);
            }
        }
        g2d.setStroke(normalStroke);
    }

    private RendererWindow mainWindow = AbstractRenderPlugin.MainWindow;
    private int imgWidth = 0;
    private long vpStartTick = 0;
    private long vpEndTick = 0;
    private long mpStartTick = 0;
    private long mpEndTick = 0;
    private int offsetCoordX = 0;
    private int topOffset = 0;

    protected void paintNotes(Graphics g, int leftMeas) {
        Graphics2D g2d = (Graphics2D) g;
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        INotesMonitor notesMonitor = JMPCoreAccessor.getSoundManager().getNotesMonitor();

        if (midiUnit.isValidSequence() == false) {
            return;
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        NotesPainter.Context nContext = LayoutManager.getInstance().getNotesPainter().newContext();
        nContext.g = g;

        paintBorder(g);

        imgWidth = getImageWidth();

        // 上部位置の調整
        topOffset = (mainWindow.getMeasCellHeight() * (127 - mainWindow.getTopMidiNumber()));
        offsetCoordX = LayoutManager.getInstance().getTickBarPosition();
        int offsetCoordXtoMeas = offsetCoordX / mainWindow.getMeasCellWidth();
        int offsetCoordXtoTick = offsetCoordXtoMeas * midiUnit.getResolution();
        int totalMeasCount = (int) ((double) mainWindow.getDispMeasCount() * 1.0);

        long absLeftMeas = -(leftMeas);
        long vpLenTick = (totalMeasCount * midiUnit.getResolution());
        vpStartTick = absLeftMeas * midiUnit.getResolution() - offsetCoordXtoTick;
        vpEndTick = vpStartTick + vpLenTick + (offsetCoordXtoTick * 2);

        int pbMaxHeight = 100;
        int pbCenterY = (pbMaxHeight / 2) + 100;

        if (LayoutManager.getInstance().isVisiblePbLine() == true) {
            g.setColor(LayoutManager.getInstance().getPitchbendColor());
            g.drawLine(0, pbCenterY, getImageWidth(), pbCenterY);
        }

        if (notesMonitor.getNumOfTrack() <= 0) {
            return;
        }

        mpStartTick = vpStartTick - vpLenTick;
        if (mpStartTick < 0) {
            mpStartTick = 0;
        }
        mpEndTick = vpEndTick + vpLenTick;

        System.out.println("render notes: " + mpStartTick + "-" + mpEndTick);

        int trkBegin = 0;
        int trkEnd = 0;
        int trkDir = 0;
        if (SystemProperties.getInstance().getLayerOrder() == SyspLayerOrder.ASC) {
            trkBegin = 0;
            trkEnd = notesMonitor.getNumOfTrack();
            trkDir = 1;
        }
        else {
            trkBegin = notesMonitor.getNumOfTrack() - 1;
            trkEnd = -1;
            trkDir = -1;
        }

        for (int trkIndex = trkBegin; trkIndex != trkEnd; trkIndex += trkDir) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 128; j++) {
                    noteOnEvents[i][j].init();
                }
            }

            pbBufferX.clear();
            pbBufferY.clear();

            g2d.setStroke(normalStroke);

            try {
                midiUnit.parseMappedByteBuffer((short) trkIndex, new MappedParseFunc(mpStartTick, mpEndTick) {

                    @Override
                    public void sysexMessage(int trk, long tick, int statusByte, byte[] sysexData, int length) {
                    }

                    @Override
                    public void shortMessage(int trk, long tick, int statusByte, int data1, int data2) {
                        int command = statusByte & 0xF0;
                        int channel = statusByte & 0x0F;
                        if ((command == MidiByte.Status.Channel.ChannelVoice.Fst.NOTE_ON) && (data2 > 0)) {
                            noteOnEvents[channel][data1].tick = tick;
                            noteOnEvents[channel][data1].trackIndex = trk;
                            noteOnEvents[channel][data1].channel = channel;
                            noteOnEvents[channel][data1].data1 = data1;
                            noteOnEvents[channel][data1].data2 = data2;
                        }
                        else if ((command == MidiByte.Status.Channel.ChannelVoice.Fst.NOTE_OFF)
                                || (command == MidiByte.Status.Channel.ChannelVoice.Fst.NOTE_ON && data2 <= 0)) {
                            // Note OFF
                            paintNt(nContext, trk, leftMeas, tick, channel, data1, data2);
                        }
                        else {
                        }
                    }

                    @Override
                    public void metaMessage(int trk, long tick, int type, byte[] metaData, int length) {
                    }
                    
                    @Override
                    public boolean interrupt() {
                        return isExec() == false;
                    }
                });
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            // for (int i = 0; i < 16; i++) {
            // for (int j = 0; j < 128; j++) {
            // if (noteOnEvents[i][j].tick != -1) {
            // paintNt(nContext, noteOnEvents[i][j].trackIndex, leftMeas,
            // mpEndTick, noteOnEvents[i][j].channel, noteOnEvents[i][j].data1,
            // noteOnEvents[i][j].data2);
            // }
            // }
            // }
        }
    }

    private void paintNt(NotesPainter.Context nContext, int trk, int leftMeas, long endTick, int channel, int data1, int data2) {
        // Note OFF
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        long endEvent = endTick;
        long startEvent = noteOnEvents[channel][data1].tick;
        noteOnEvents[channel][data1].init();

        // if ((startEvent != -1) && ((endEvent <= startEvent) || (vpStartTick >
        // endEvent))) {
        if ((startEvent == -1) || ((endEvent <= startEvent) || (vpStartTick > endEvent)) || (AbstractRenderPlugin.MainWindow.isVisible() == false)) {
            // 無効データは何もしない
        }
        else {
            // 描画開始
            int startMeas = (int) ((double) startEvent / (double) midiUnit.getResolution()) + leftMeas;
            int startOffset = (int) ((double) startEvent % (double) midiUnit.getResolution());
            nContext.x = (int) (mainWindow.getMeasCellWidth() * (startMeas + (double) startOffset / midiUnit.getResolution())) + offsetCoordX;
            nContext.y = ((127 - data1) * mainWindow.getMeasCellHeight()) + topOffset;

            nContext.w = (int) (mainWindow.getMeasCellWidth() * (double) (endEvent - startEvent) / midiUnit.getResolution());
            nContext.h = mainWindow.getMeasCellHeight();

            if (nContext.w < 2) {
                nContext.w = 2;
            }

            if (LayoutManager.getInstance().getColorRule() == LayoutConfig.EColorRule.Channel) {
                nContext.bgColor = LayoutManager.getInstance().getNotesColor(channel);
                nContext.bdColor = LayoutManager.getInstance().getNotesBorderColor(channel);
            }
            else {
                nContext.bgColor = LayoutManager.getInstance().getNotesColor(trk);
                nContext.bdColor = LayoutManager.getInstance().getNotesBorderColor(trk);
            }

            int x1 = nContext.x;
            int x2 = nContext.x + nContext.w - 1;
            if ((x2 < 0) || (imgWidth <= x1)) {

            }
            else {
                if (x1 < 0) {
                    x1 = 0;
                }
                if (x2 >= imgWidth) {
                    x2 = imgWidth - 1;
                }
                nContext.x = x1;
                nContext.w = x2 - x1 + 1;
                if (nContext.w < 2) {
                    nContext.w = 2;
                }
                LayoutManager.getInstance().getNotesPainter().paintNotes(nContext);
            }
        }
    }
}

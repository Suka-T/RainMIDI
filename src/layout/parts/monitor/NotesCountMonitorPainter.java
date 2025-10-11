package layout.parts.monitor;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import jlib.core.JMPCoreAccessor;
import jlib.midi.IMidiUnit;
import jlib.midi.INotesMonitor;
import layout.LayoutManager;
import layout.parts.MonitorPainter;

public class NotesCountMonitorPainter extends MonitorPainter {

    private Font info2Font = null;
    private StringBuilder sb = new StringBuilder();

    public NotesCountMonitorPainter() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            info2Font = new Font("Calibri", Font.PLAIN, 64);
        }
//        else if (os.contains("mac")) {
//        }
//        else if (os.contains("nux") || os.contains("nix")) {
//        }
        else {
            info2Font = new Font(Font.SANS_SERIF, Font.PLAIN, 64);
        }
    }

    @Override
    public void paintMonitor(Graphics g, MonitorData info) {
        INotesMonitor notesMonitor = JMPCoreAccessor.getSoundManager().getNotesMonitor();
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();

        int sx = 0;
        int sy = 65;
        Color backStrColor = LayoutManager.getInstance().getPlayerColor().getBgColor();
        Color topStrColor = LayoutManager.getInstance().getPlayerColor().getBgRevColor();

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

            sx = (info.width - width) / 2;
            g.setColor(backStrColor);
            g.drawString(sb.toString(), sx + 1, sy + 1);
            g.setColor(topStrColor);
            g.drawString(sb.toString(), sx, sy);
        }
    }

}

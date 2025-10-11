package layout.parts.monitor;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import jlib.core.JMPCoreAccessor;
import jlib.midi.IMidiUnit;
import layout.LayoutManager;
import layout.parts.MonitorPainter;

public class ClassicalMonitorPainter extends MonitorPainter {

    private Font info3Font = null;
    private StringBuilder sb = new StringBuilder();
    public ClassicalMonitorPainter() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            info3Font = new Font("Calibri", Font.PLAIN, 28);
        }
        else {
            info3Font = new Font(Font.SANS_SERIF, Font.PLAIN, 28);
        }
    }

    @Override
    public void paintMonitor(Graphics g, MonitorData info) {
        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
        
        int sx = 0;
        int sy = 30;
        int sh = 28;
        Color backStrColor = LayoutManager.getInstance().getPlayerColor().getBgColor();
        Color topStrColor = LayoutManager.getInstance().getPlayerColor().getBgRevColor();
        long val1, val2;
        int width;
        String text;
        FontMetrics fm;
        g.setFont(info3Font);

        sb.setLength(0);
        val1 = (int) midiUnit.getTempoInBPM();
        val2 = (int) ((midiUnit.getTempoInBPM() - val1) * 100);
        sb.append(val1).append(".").append(val2).append(" BPM");
        text = sb.toString();
        fm = g.getFontMetrics();
        width = fm.stringWidth(text);
        sx = (info.width - width) / 2;
        g.setColor(backStrColor);
        g.drawString(sb.toString(), sx + 1, sy + 1);
        g.setColor(topStrColor);
        g.drawString(sb.toString(), sx, sy);
        sy += sh;

        if (midiUnit.isRenderingOnlyMode() == false) {
            sb.setLength(0);
            val1 = midiUnit.getSignatureInfo().getNumerator();
            val2 = midiUnit.getSignatureInfo().getDenominator();
            sb.append(val1).append("/").append(val2).append(" ").append(midiUnit.getSignatureInfo().getAccidental());

            text = sb.toString();
            fm = g.getFontMetrics();
            width = fm.stringWidth(text);

            sx = (info.width - width) / 2;
            g.setColor(backStrColor);
            g.drawString(sb.toString(), sx + 1, sy + 1);
            g.setColor(topStrColor);
            g.drawString(sb.toString(), sx, sy);
            sy += sh;
        }
    }

}

package layout.parts.monitor;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import jlib.core.JMPCoreAccessor;
import jlib.midi.IMidiUnit;
import layout.LayoutManager;
import layout.parts.MonitorPainter;
import plg.SystemProperties;
import plg.Utility;

public class ClassicalMonitorPainter extends MonitorPainter {

    private Font info3Font = null;
    private StringBuilder sb = new StringBuilder();
    
    private static final Color[] GLOW_COLORS = {
            new Color(100, 200, 255, 20),
            new Color(140, 190, 255, 35),
            new Color(190, 165, 255, 55),
            new Color(230, 150, 245, 80)
        };

    private static final Color TEXT_COLOR = new Color(255, 235, 255, 255);

    public ClassicalMonitorPainter() {
        if (Utility.isWindows()) {
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
        drawGlowString(g, sx, sy, sb.toString());
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
            drawGlowString(g, sx, sy, sb.toString());
            sy += sh;
        }
    }
    
    public void drawGlowString(Graphics g, int x, int y, String str) {
        if (SystemProperties.getInstance().isValidGlowFont()) {
            for (int i = 4, index = 0; i >= 1; i -= 1, index++) {
                g.setColor(GLOW_COLORS[index]);
    
                g.drawString(str, x - i, y);
                g.drawString(str, x + i, y);
                g.drawString(str, x, y - i);
                g.drawString(str, x, y + i);
            }
    
            g.setColor(TEXT_COLOR);
            g.drawString(str, x, y);
        }
        else {
            Color backStrColor = LayoutManager.getInstance().getFontColor().getBdColor();
            Color topStrColor = LayoutManager.getInstance().getFontColor().getBgColor();
            g.setColor(backStrColor);
            g.drawString(str, x + 1, y + 1);
            g.setColor(topStrColor);
            g.drawString(str, x, y);
        }
    }

}

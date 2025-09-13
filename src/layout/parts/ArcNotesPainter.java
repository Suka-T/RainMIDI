package layout.parts;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics2D;

public class ArcNotesPainter extends NotesPainter {

    private static final BasicStroke notesBdStroke = new BasicStroke(0.2f);
    private static final AlphaComposite bdAlpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);
    private static final BasicStroke normalStroke = new BasicStroke(1.0f);

    @Override
    public void paintNotes(Context context) {
        Graphics2D g2d = (Graphics2D) context.g;
        if (context.iW > 1) {
            g2d.setColor(context.bgColor);
            g2d.fillRoundRect(context.iX, context.iY, context.iW, context.iH, 8, 8);
            g2d.setStroke(notesBdStroke);
            g2d.setColor(context.bdColor);
            g2d.setComposite(bdAlpha);
            g2d.drawRoundRect(context.iX, context.iY, context.iW, context.iH, 8, 8);
            g2d.setComposite(AlphaComposite.SrcOver);
            g2d.setStroke(normalStroke);
        }
        else {
            g2d.setColor(context.bdColor);
            g2d.drawLine(context.iX, context.iY, context.iX, context.iY + context.iH - 1);
        }
    }

}

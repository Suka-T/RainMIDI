package layout.parts.notes;

import java.awt.Graphics2D;

import layout.LayoutManager;
import layout.parts.NotesPainter;

public class ArcNotesPainter extends NotesPainter {
    
    @Override
    public void paintNotes(Context context) {
        int r = 0;
        Graphics2D g2d = (Graphics2D) context.g;
        if (context.iW <= 1) {
            g2d.setColor(context.bdColor);
            g2d.drawLine(context.iX, context.iY, context.iX, context.iY + context.iH - 1);
        }
        else if (1 < context.iW && context.iW <= 8) {
            int i = 0;
            for (; i < context.iH; i++) {
                g2d.setColor(LayoutManager.getInstance().getNotesColor(context.colorIndex).getGradColor(i));
                g2d.drawLine(context.iX, context.iY + i, context.iX + context.iW - 1, context.iY + i);
                g2d.setColor(context.bdColor);
                if (i == 0 || i == context.iH - 1) {
                    g2d.drawLine(context.iX + r, context.iY + i, context.iX + context.iW - r - 1, context.iY + i);
                }
                else {
                    g2d.drawLine(context.iX + r, context.iY + i, context.iX + r, context.iY + i);
                    g2d.drawLine(context.iX + context.iW - r - 1, context.iY + i, context.iX + context.iW - r - 1, context.iY + i);
                }
            }
        }
        else {
            int i = 0;
            final int arc = 3;
            for (; i < context.iH; i++) {
                g2d.setColor(LayoutManager.getInstance().getNotesColor(context.colorIndex).getGradColor(i));
                if (i < arc - 1) {
                    r = arc - i - 1;
                    g2d.drawLine(context.iX + r, context.iY + i, context.iX + context.iW - r - 1, context.iY + i);
                }
                else if (i >= context.iH - arc) {
                    r = arc - (context.iH - i);
                    g2d.drawLine(context.iX + r, context.iY + i, context.iX + context.iW - r - 1, context.iY + i);
                }
                else {
                    r = 0;
                    g2d.drawLine(context.iX, context.iY + i, context.iX + context.iW - 1, context.iY + i);
                }
                
                g2d.setColor(context.bdColor);
                if (i == 0 || i == context.iH - 1) {
                    g2d.drawLine(context.iX + r, context.iY + i, context.iX + context.iW - r - 1, context.iY + i);
                }
                else {
                    g2d.drawLine(context.iX + r, context.iY + i, context.iX + r, context.iY + i);
                    g2d.drawLine(context.iX + context.iW - r - 1, context.iY + i, context.iX + context.iW - r - 1, context.iY + i);
                }
            }
        }
    }

}

package layout.parts.notes;

import java.awt.Graphics2D;

import layout.parts.NotesPainter;

public class FlatNotesPainter extends NotesPainter {

    @Override
    public void paintNotes(Context context) {
        Graphics2D g2d = (Graphics2D) context.g;
        if (context.iW > 1) {
            g2d.setColor(context.bgColor);
            g2d.fillRect(context.iX, context.iY, context.iW, context.iH);
        }
        else {
            g2d.setColor(context.bgColor);
            g2d.drawLine(context.iX, context.iY, context.iX, context.iY + context.iH - 1);
        }
    }

}

package layout.parts;

import java.awt.Graphics2D;

public class NormalNotesPainter extends NotesPainter {
    public NormalNotesPainter() {

    }

    @Override
    public void paintNotes(Context context) {
        Graphics2D g2d = (Graphics2D) context.g;
        g2d.setColor(context.bgColor);
        g2d.fillRect(context.x, context.y, context.w, context.h);
        g2d.setColor(context.bdColor);
        g2d.drawRect(context.x, context.y, context.w, context.h);
    }

}

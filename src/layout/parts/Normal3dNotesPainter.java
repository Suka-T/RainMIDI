package layout.parts;

import java.awt.Graphics2D;

import layout.LayoutManager;

public class Normal3dNotesPainter extends NotesPainter {

    public Normal3dNotesPainter() {
        // TODO 自動生成されたコンストラクター・スタブ
    }

    @Override
    public void paintNotes(Context context) {
        Graphics2D g2d = (Graphics2D) context.g;
        int i = 0;
        for (; i < context.h; i++) {
            //g2d.setColor(layout.LayoutManager.getInstance().getNotesGradation(context.colorIndex, i));
            g2d.setColor(LayoutManager.getInstance().getNotesColor(context.colorIndex).getGradColor(i));
            g2d.drawLine(context.x, context.y + i, context.x + context.w - 1, context.y + i);
        }
        g2d.setColor(context.bdColor);
        g2d.drawRect(context.x, context.y, context.w, context.h);
    }

}

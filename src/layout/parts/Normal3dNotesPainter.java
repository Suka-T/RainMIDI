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
        if (context.iW > 1) {
            int i = 0;
            for (; i < context.iH; i++) {
                //g2d.setColor(layout.LayoutManager.getInstance().getNotesGradation(context.colorIndex, i));
                g2d.setColor(LayoutManager.getInstance().getNotesColor(context.colorIndex).getGradColor(i));
                g2d.drawLine(context.iX, context.iY + i, context.iX + context.iW - 1, context.iY + i);
            }
            g2d.setColor(context.bdColor);
            g2d.drawRect(context.iX, context.iY, context.iW, context.iH);
        }
        else {
            g2d.setColor(context.bdColor);
            g2d.drawLine(context.iX, context.iY, context.iX, context.iY + context.iH - 1);
        }
    }

}

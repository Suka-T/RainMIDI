package layout.parts;

import java.awt.Color;
import java.awt.Graphics;

public abstract class NotesPainter {
    public class Context {
        public Context() {
        }

        public Graphics g;
        public int x;
        public int y;
        public int w;
        public int h;
        public Color bgColor;
        public Color bdColor;
    };

    public Context newContext() {
        return new Context();
    }

    public abstract void paintNotes(Context context);
}

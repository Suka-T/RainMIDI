package layout.parts;

import java.awt.Color;
import java.awt.Graphics;

public abstract class NotesPainter {
    public class Context {
        public Context() {
        }

        public void setParam(double x, double y, double w, double h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;

            createParam();
        }

        public void createParam() {
            this.iX = (int) Math.round(this.x);
            this.iY = (int) Math.round(this.y);
            this.iW = (int) Math.round(this.w);
            this.iH = (int) Math.round(this.h);
            if (this.iW < 1) {
                this.iW = 1;
            }
        }

        public Graphics g;
        public double x;
        public double y;
        public double w;
        public double h;

        public int iX;
        public int iY;
        public int iW;
        public int iH;

        public Color bgColor;
        public Color bdColor;
        public int colorIndex;
    };

    public Context newContext() {
        return new Context();
    }

    public abstract void paintNotes(Context context);

}

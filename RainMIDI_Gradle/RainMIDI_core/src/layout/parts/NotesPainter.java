package layout.parts;

import java.awt.Color;
import java.awt.Graphics;

public abstract class NotesPainter {
    public class Context {
        public Context() {
        }
        
        public void setParam(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            
            createParam();
        }
        
        public void createParam() {
            this.iX = Math.round(this.x);
            this.iY = Math.round(this.y);
            this.iW = Math.round(this.w);
            this.iH = Math.round(this.h);
            if (this.iW < 1) {
                this.iW = 1;
            }
        }

        public Graphics g;
        public float x;
        public float y;
        public float w;
        public float h;
        
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

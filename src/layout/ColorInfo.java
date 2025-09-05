package layout;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ColorInfo {
    protected Color bgColor = null;
    protected Color bdColor = null;
    protected Color effeColor = null;
    protected Color bgRevColor = null;

    protected List<Color> gradColor = null;
    protected int gradSize = -1;
    public ColorInfo(Color bgColor, Color bdColor) {
        this.bgColor = bgColor;
        this.bdColor = bdColor;
        this.effeColor = Color.WHITE;
        this.gradColor = new ArrayList<Color>();
        this.gradSize = -1;
        this.bgRevColor = ((bgColor.getRed() + bgColor.getGreen() + bgColor.getBlue()) / 3) >= 128 ? Color.BLACK : Color.WHITE;
    }
    
    public ColorInfo(Color bgColor, Color bdColor, Color effeColor) {
        this.bgColor = bgColor;
        this.bdColor = bdColor;
        this.effeColor = effeColor;
        this.gradColor = new ArrayList<Color>();
        this.gradSize = -1;
        this.bgRevColor = ((bgColor.getRed() + bgColor.getGreen() + bgColor.getBlue()) / 3) >= 128 ? Color.BLACK : Color.WHITE;
    }
    
    public Color getBgColor() {
        return bgColor;
    }
    
    public Color getBdColor() {
        return bdColor;
    }
    
    public Color getEffeColor() {
        return effeColor;
    }
    
    public Color getBgRevColor() {
        return bgRevColor;
    }
    
    public Color getGradColor(int index) {
        if (gradSize == -1) {
            return bgColor;
        }
        if (index >= gradSize) {
            index = gradSize - 1;
        }
        return gradColor.get(index);
    }
    
    public void createGrad(int size) {
        int bgR = bgColor.getRed();
        int bgG = bgColor.getGreen();
        int bgB = bgColor.getBlue();

        int bdR = bdColor.getRed();
        int bdG = bdColor.getGreen();
        int bdB = bdColor.getBlue();
        
        double offs = 1.0;
        if (bdR - bgR > 0) {
            offs = 0.25;
        }
        else if (bdR - bgR < 0) {
            offs = 1.25;
        }
        bdR = (int)((double)bdR * offs);
        bdG = (int)((double)bdG * offs);
        bdB = (int)((double)bdB * offs);

        int spanR = (bdR - bgR) / size;
        int spanG = (bdG - bgG) / size;
        int spanB = (bdB - bgB) / size;

        int i = 0;
        for (; i < size; i++) {
            int r = bgR + spanR * i;
            int g = bgG + spanG * i;
            int b = bgB + spanB * i;
            Color c = new Color(r < 0 ? 0 : r > 255 ? 255 : r, g < 0 ? 0 : g > 255 ? 255 : g, b < 0 ? 0 : b > 255 ? 255 : b);
            gradColor.add(c);
        }
        gradSize = size;
    }
}

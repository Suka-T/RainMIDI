package layout;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ColorInfo {
    protected Color bgColor = null;
    protected Color bdColor = null;
    protected Color shadowColor = null;
    protected Color effeColor = null;
    protected Color bgRevColor = null;
    protected Color bgRev2Color = null;

    protected List<Color> gradColor = null;
    protected int gradSize = -1;
    public ColorInfo(Color bgColor, Color bdColor) {
        this.bgColor = bgColor;
        this.bdColor = bdColor;
        this.effeColor = Color.WHITE;
        this.gradColor = new ArrayList<Color>();
        this.gradColor.add(bgColor);
        this.gradSize = 1;
        this.bgRevColor = ((bgColor.getRed() + bgColor.getGreen() + bgColor.getBlue()) / 3) >= 128 ? Color.BLACK : Color.WHITE;
        this.bgRev2Color = ((bgColor.getRed() + bgColor.getGreen() + bgColor.getBlue()) / 3) >= 128 ? Color.WHITE : Color.BLACK;
        createShadowColor();
    }
    
    public ColorInfo(Color bgColor, Color bdColor, Color effeColor) {
        this.bgColor = bgColor;
        this.bdColor = bdColor;
        this.effeColor = effeColor;
        this.gradColor = new ArrayList<Color>();
        this.gradColor.add(bgColor);
        this.gradSize = 1;
        this.bgRevColor = ((bgColor.getRed() + bgColor.getGreen() + bgColor.getBlue()) / 3) >= 128 ? Color.BLACK : Color.WHITE;
        this.bgRev2Color = ((bgColor.getRed() + bgColor.getGreen() + bgColor.getBlue()) / 3) >= 128 ? Color.WHITE : Color.BLACK;
        createShadowColor();
    }
    
    private void createShadowColor() {
        double borderOffset = 0.5;
        int r = this.bgColor.getRed();
        int g = this.bgColor.getGreen();
        int b = this.bgColor.getBlue();
        //int a = this.bgColor.getAlpha();
        r = (int) ((double) r * borderOffset);
        g = (int) ((double) g * borderOffset);
        b = (int) ((double) b * borderOffset);
        this.shadowColor = new Color(r, g, b);
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
    
    public Color getBgRev2Color() {
        return bgRev2Color;
    }
    
    public Color getGradColorBegin() {
        return getGradColor(0);
    }
    
    public Color getGradColorEnd() {
        return getGradColor(gradSize - 1);
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

        int bdR = shadowColor.getRed();
        int bdG = shadowColor.getGreen();
        int bdB = shadowColor.getBlue();

        int spanR = (bdR - bgR) / size;
        int spanG = (bdG - bgG) / size;
        int spanB = (bdB - bgB) / size;

        int i = 0;
        gradColor.clear();
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

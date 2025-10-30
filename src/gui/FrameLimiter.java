package gui;

public class FrameLimiter {
    private boolean wasEvented30 = false;
    private int count30 = 0;
    private int maxCount30 = 0;
    
    private boolean wasEvented60 = false;
    private int count60 = 0;
    private int maxCount60 = 0;

    public FrameLimiter() {
    }
    
    public void setFps(int fps) {
        this.count30 = 0;
        this.wasEvented30 = false;
        this.maxCount30 = fps / 30;
        if (this.maxCount30 <= 0) {
            this.maxCount30 = 1;
        }
        
        this.count60 = 0;
        this.wasEvented60 = false;
        this.maxCount60 = fps / 60;
        if (this.maxCount60 <= 0) {
            this.maxCount60 = 1;
        }
    }
    
    public void frameEvent() {
        this.count30++;
        if (this.maxCount30 <= this.count30) {
            this.wasEvented30 = true;
            this.count30 = 0;
        }
        else {
            this.wasEvented30 = false;
        }
        
        this.count60++;
        if (this.maxCount60 <= this.count60) {
            this.wasEvented60 = true;
            this.count60 = 0;
        }
        else {
            this.wasEvented60 = false;
        }
    }
    
    public boolean isEventted30() {
        return wasEvented30;
    }
    
    public boolean isEventted60() {
        return wasEvented60;
    }
}

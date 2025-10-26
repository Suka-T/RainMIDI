package gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.event.MouseEvent;

import jlib.core.JMPCoreAccessor;

public class VolumeControl extends RainControl {
    private Paint volGrad = null;
    private boolean isVisible = false;
    private int pressX = -1;
    private int pressY = -1;
    private int mouseX = -1;
    private int mouseY = -1;
    
    private int x = 0;
    private int y = 0;
    private int width = 0;
    private int height = 0;
    
    public VolumeControl() {
        super();
    }
    
    public void paint(Graphics g) {
        if (isVisible) {
            float volWidth = (float)width * JMPCoreAccessor.getSoundManager().getLineVolume();
            
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.SrcOver);
            g2d.setColor(Color.BLACK);
            g2d.fillRect(x, y, width, height);
            g2d.setPaint(volGrad);
            g2d.fillRect(x, y, (int)volWidth, height);
            g2d.setColor(Color.WHITE);
            g2d.drawRect(x, y, width, height);
            g2d.dispose();
        }
    }
    
    public void setLocation(int x, int y, int w, int h) {
        if (this.x != x || this.y != y || this.width != w || this.height != h) {
            volGrad = new LinearGradientPaint(x, y, x + w - 1, y, 
                    new float[] { 0f, 1f },
                    new Color[] { new Color(0f, 1f, 0f, 1.0f), new Color(0.6f, 0f, 0f, 1f) }
            );
        }
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }
    
    public void setVisible(boolean b) {
        isVisible = b;
    }
    
    private void updateVolume() {
        int cur = mouseX - this.x;
        if (cur < 0) {
            cur = 0;
        }
        else if (cur > this.width) {
            cur = this.width;
        }
        float volume = (float)cur / (float)(this.width);
        JMPCoreAccessor.getSoundManager().setLineVolume(volume);
    }
    
    public boolean onPress(MouseEvent e) {
        if (x <= e.getX() && e.getX() <= x + width - 1 && y <= e.getY() && e.getY() <= y + height - 1) {
            return true;
        }
        return false;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (pressX != -1 && pressY != -1) {
            mouseX = e.getX();
            mouseY = e.getY();
            
            updateVolume();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (pressX != -1 && pressY != -1) {
            mouseX = e.getX();
            mouseY = e.getY();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        pressX = -1;
        pressY = -1;
        mouseX = -1;
        mouseY = -1;
        isVisible = false;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON3) {
            if (isVisible == true) {
                pressX = e.getX();
                pressY = e.getY();
                mouseX = pressX;
                mouseY = pressY;
                
                updateVolume();
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            pressX = -1;
            pressY = -1;
            mouseX = -1;
            mouseY = -1;
        }
    }

}

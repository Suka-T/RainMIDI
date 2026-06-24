package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import jlib.core.JMPCoreAccessor;

public class ChromaKeyWindow extends RendererWindow {
	
	private static final Color[] backColorLst = new Color[] {
			new Color(0, 255, 0), // Green
			new Color(0, 0, 255), // Blue
			new Color(0, 255, 255), // Cyan
			Color.PINK,
			Color.ORANGE,
			Color.MAGENTA,
			new Color(0, 0, 0), // Black
	};
	
	private int colorIndex = -1;
	private Color backColor = new Color(0, 255, 0); // Green

	public ChromaKeyWindow(int winW, int winH, boolean maximized) {
		super(winW, winH, maximized);
	}
	
	@Override
	public void renderMidiNotesDisplay(Graphics g) {
        Graphics2D lotG2d = (Graphics2D) g;

        int panelW = getContentPane().getWidth();
        int panelH = getContentPane().getHeight();
        lotG2d.setColor(backColor);
        lotG2d.fillRect(0,  0, panelW, panelH); 
	}
    
    @Override
    public void paintDisplay(Graphics g) {
    	super.paintDisplay(g);
    	
    	if (!JMPCoreAccessor.getSoundManager().isPlay()) {
	    	int paneWidth = getContentPane().getWidth();
	        int iconSize = 20;
	        int x = 0;
	        int y = 20;
	        
	        for (int i = 0; i < backColorLst.length; i++) {
		        x = paneWidth - ((iconSize + 5) * i) - 40;
		        g.setColor(backColorLst[i]);
		        g.fillRect(x, y, iconSize, iconSize);
		        g.setColor(Color.LIGHT_GRAY);
		        g.drawRect(x, y, iconSize, iconSize);
	        }
    	}
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
    	int paneWidth = getContentPane().getWidth();
        //int paneHeight = getContentPane().getHeight();
        
    	int mx = e.getX();
    	int my = e.getY();
    	
        int iconSize = 20;
        int x = 0;
        int y = 20;
        
        for (int i = 0; i < backColorLst.length; i++) {
        	x = paneWidth - ((iconSize + 5) * i) - 40;
            if ((x <= mx && mx <= x + iconSize) && (y <= my && my <= y + iconSize)) {
            	colorIndex = i;
            	return;
            }
        }
    	super.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    	if (colorIndex != -1) {
    		backColor = backColorLst[colorIndex];
    		colorIndex = -1;
    	}
    	else {
    		super.mouseReleased(e);
    	}
    }
}

package plg;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jlib.core.JMPCoreAccessor;
import jlib.midi.IMidiUnit;

public class ViewportManager {
	
	private static final int DEFAULT_TRANS_COUNT = 100;
	private ScheduledExecutorService scheduler = null;
	
	protected int maxViewport = 2;
    protected int defViewport = 0;
    protected int curViewport = 0;
    protected int oldViewport = 0;
    protected int viewportTransCount = 0;
    
    protected int[] viewportOffsS1 = new int[5];
    protected int[] viewportOffsE2 = new int[5];
    
    protected int curS1 = 0;
    protected int curE2 = 0;
    
    protected boolean updateOffsFlag = false;
    protected int curPanelWidth = 0;
    protected int curOrgWidth = 0;
    protected int curMeasCellHeight = 0;
	
	public ViewportManager() {
        defViewport = SystemProperties.getInstance().getViewportNum();
        maxViewport = SystemProperties.getInstance().getViewportMax();
        curViewport = 0;
        oldViewport = curViewport;
        viewportTransCount = DEFAULT_TRANS_COUNT;
	}
	
	public void updateOffs(int panelWidth, int orgWidth, int measCellHeight) {
        updateOffsFlag = true;
        curPanelWidth = panelWidth;
        curOrgWidth = orgWidth;
        curMeasCellHeight = measCellHeight;
        if (defViewport != -1) {
        	updateOffsImpl(curPanelWidth, curOrgWidth, curMeasCellHeight);
        	updateOffsFlag = false;
        }
	}
	
	protected void updateOffsImpl(int panelWidth, int orgWidth, int measCellHeight) {
    	double vpScale = (double)panelWidth / (double)orgWidth;
    	// Full
        viewportOffsS1[0] = 0;
    	viewportOffsE2[0] = 0;
    	
    	// Large
        viewportOffsS1[1] = (int)((double)measCellHeight * 12.0 * vpScale);
        viewportOffsE2[1] = (int)((double)measCellHeight * 8.0 * vpScale);
        
    	// 88Keys
        viewportOffsS1[2] = (int)((double)measCellHeight * 21.0 * vpScale);
        viewportOffsE2[2] = (int)((double)measCellHeight * 19.0 * vpScale);
        
    	// 76Keys
        viewportOffsS1[3] = (int)((double)measCellHeight * 28.0 * vpScale);
        viewportOffsE2[3] = (int)((double)measCellHeight * 24.0 * vpScale);
        
    	// 61Keys(Unsupported)
        viewportOffsS1[4] = (int)((double)measCellHeight * 36.0 * vpScale);
        viewportOffsE2[4] = (int)((double)measCellHeight * 31.0 * vpScale);
	}
	
	public void start() {
		if (defViewport == -1) {
			scheduler = Executors.newSingleThreadScheduledExecutor();
			scheduler.scheduleAtFixedRate(this::updateStats, 0, 10, TimeUnit.MILLISECONDS);
		}
		else {
			curS1 = viewportOffsS1[defViewport];
	        curE2 = viewportOffsE2[defViewport];
		}
	}
	
    public void stop() {
    	if (scheduler != null) {
    		scheduler.shutdown();
    		scheduler = null;
    	}
    }
	
    private void updateStats() {
    	try {
	        IMidiUnit midiUnit = JMPCoreAccessor.getSoundManager().getMidiUnit();
	        
	        if (updateOffsFlag == true) {
	        	updateOffsImpl(curPanelWidth, curOrgWidth, curMeasCellHeight);
	        	updateOffsFlag = false;
	        }
	        
	        if (defViewport == -1) {
	        	curViewport = midiUnit.getRenderedNoteRange(midiUnit.getTickPosition());
	        	if (curViewport > maxViewport) {
	        		curViewport = maxViewport;
	        	}
	        	if (viewportOffsS1.length <= curViewport) {
	        		curViewport = viewportOffsS1.length - 1;
	        	}
	        }
	        else {
	        	curViewport = defViewport;
	        }
	        
	        curS1 = viewportOffsS1[curViewport];
	        curE2 = viewportOffsE2[curViewport];
	        if (curViewport != oldViewport) {
	        	double elapsed = (double) viewportTransCount / (double) DEFAULT_TRANS_COUNT;
	        	//double eased = 1.0 - (1.0 - elapsed) * (1.0 - elapsed);
	        	//double eased = elapsed * elapsed;
	        	double eased = Utility.calcEasedZoomInOut(elapsed);
	        	int oldS1 = viewportOffsS1[oldViewport];
	            int oldE2 = viewportOffsE2[oldViewport];
	            
	        	int moveVpS = Math.abs(oldS1 - curS1);
	        	int moveVpE = Math.abs(oldE2 - curE2);
	        	
	        	if (oldViewport < curViewport) {
	        		curS1 = oldS1 + (int)((double)moveVpS * (1.0 - eased));
	        		curE2 = oldE2 + (int)((double)moveVpE * (1.0 - eased));
	        	}
	        	else {
	        		curS1 = oldS1 - (int)((double)moveVpS * (1.0 - eased));
	        		curE2 = oldE2 - (int)((double)moveVpE * (1.0 - eased));        		
	        	}
	        	
	        	if (viewportTransCount <= 0) {
	        		oldViewport = curViewport;
	        		viewportTransCount = DEFAULT_TRANS_COUNT;
	        	}
	        	else {
	        		viewportTransCount--;
	        	}
	        }
    	}
    	catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public int getOffsetCoordS() {
    	return curS1;
    }
    
    public int getOffsetCoordE() {
    	return curE2;
    }
}

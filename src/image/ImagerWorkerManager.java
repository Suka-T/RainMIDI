package image;

import java.awt.Image;

import gui.RendererWindow;
import plg.SystemProperties;

public class ImagerWorkerManager {

    private volatile ImageWorker[] workers = null;
    private int currentWorkerIndex = 0;

    public ImagerWorkerManager(RendererWindow window, int width, int height) {
        workers = new NotesImageWorker[SystemProperties.getInstance().getWorkerNum()];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new NotesImageWorker(window, width, height);
        }
    }

    public int getNumOfWorker() {
        return workers.length;
    }

    public ImageWorker getWorker(int index) {
        return workers[index];
    }

    public void start() {
        for (ImageWorker w : workers) {
            w.start();
        }
        currentWorkerIndex = 0;
    }

    public void stop() {
        for (ImageWorker w : workers) {
            w.stop();
        }
    }

    public Image getNotesImage() {
        if (workers[currentWorkerIndex].isExec() == false) {
            return workers[currentWorkerIndex].getImage();
        }
        return null;
    }
    
    public void waitForRenderingFin() {
        try {
            // 全てのワーカーのレンダリングが終わるまで待つ
            int workerCnt;
            do {
                workerCnt = 0;
                for (ImageWorker w : workers) {
                    if (w.isExec() == false) workerCnt++;
                }
                Thread.sleep(10);
            } while (workers.length > workerCnt);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void firstRender(int leftMeas, int dispMeas, int flipCount) {
        int offsetLeftMeas = Math.abs(leftMeas);
        int flipMergin = -(flipCount);
        for (int i = 0; i < workers.length; i++) {
            int flipLine = offsetLeftMeas + ((dispMeas + flipMergin) * i);
            workers[i].reset();
            workers[i].setLeftMeasTh(-(flipLine));
            workers[i].disposeImage();
            workers[i].makeImage();
        }
        currentWorkerIndex = 0;
        
        // 全てのワーカーのレンダリングが終わるまで待つ
        waitForRenderingFin();
    }
    
    public void reset(int leftMeas, int dispMeas, int flipCount) {
        int offsetLeftMeas = Math.abs(leftMeas);
        int flipMergin = -(flipCount);
        for (int i = 0; i < workers.length; i++) {
            int flipLine = offsetLeftMeas + ((dispMeas + flipMergin) * i);
            workers[i].reset();
            workers[i].setLeftMeasTh(-(flipLine));
            workers[i].disposeImage();
            workers[i].makeImage();
            
            if (3 < workers.length) {
                while (workers[i].isExec()) {
                    try {
                        Thread.sleep(10);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        currentWorkerIndex = 0;
    }

    public void flipPage(int newLeftMeas, int dispMeas, int flipCount) {
        ImageWorker nextNotesThread = workers[currentWorkerIndex];
        // int[] nextCache = workers[currentWorkerIndex].getTrackCache();

        currentWorkerIndex = (currentWorkerIndex + 1 >= workers.length) ? 0 : currentWorkerIndex + 1;
        int offsetLeftMeas = Math.abs(newLeftMeas);
        int flipMergin = -(flipCount);
        int flipLine = offsetLeftMeas + ((dispMeas + flipMergin) * (workers.length - 1));
        nextNotesThread.setLeftMeasTh(-(flipLine));

        // TODO ここでキャッシュ情報を渡したいが高頻度でバグるのでやらない 要検討
        // nextNotesThread.copyTrackCacheFrom(nextCache);
        nextNotesThread.makeImage();
    }
    
    public void dispose() {
        for (int i = 0; i < workers.length; i++) {
            workers[i].dispose();
        }
    }
    
    public void forcedEnd() {
        for (int i = 0; i < workers.length; i++) {
            workers[i].forcedEnd();
        }
        
        // 全てのワーカーのレンダリングが終わるまで待つ
        waitForRenderingFin();
        
        for (int i = 0; i < workers.length; i++) {
            workers[i].clearForcedEnd();
        }
    }
}

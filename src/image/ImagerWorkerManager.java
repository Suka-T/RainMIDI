package image;

import java.awt.Image;

import plg.SystemProperties;

public class ImagerWorkerManager {

    private volatile ImageWorker[] workers = null;
    private int currentWorkerIndex = 0;

    public ImagerWorkerManager(int width, int height) {
        workers = new NotesImageWorker[SystemProperties.getInstance().getWorkerNum()];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new NotesImageWorker(width, height);
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
    
    public void firstRender(int leftMeas, int dispMeas, int flipCount) {
        reset(leftMeas, dispMeas, flipCount);
        
        // 最初のワーカーのレンダリングが終わるまで待つ
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
        
//        if (workers.length < 10) {
//            reset(leftMeas, dispMeas, flipCount);
//            return;
//        }
//        
//        int i = 0;
//        int offsetLeftMeas = Math.abs(leftMeas);
//        int flipMergin = -(flipCount);
//        int flipLine = offsetLeftMeas + ((dispMeas + flipMergin) * i);
//        workers[i].reset();
//        workers[i].setLeftMeasTh(-(flipLine));
//        workers[i].disposeImage();
//        workers[i].makeImage();
//        try {
//            // 最初のワーカーのレンダリングが終わるまで待つ
//            while (workers[i].isExec()) {
//                Thread.sleep(50);
//            }
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        
//        if (workers.length >= 2) {
//            for (i = 1; i < workers.length; i++) {
//                flipLine = offsetLeftMeas + ((dispMeas + flipMergin) * i);
//                workers[i].reset();
//                workers[i].setLeftMeasTh(-(flipLine));
//                workers[i].disposeImage();
//                workers[i].makeImage();
//            }
//        }
//        currentWorkerIndex = 0;
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
}

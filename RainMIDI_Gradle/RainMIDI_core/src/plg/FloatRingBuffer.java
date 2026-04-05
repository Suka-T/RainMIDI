package plg;

public class FloatRingBuffer {
    private final float[] buffer;
    private final float[] displaySnapshot; // 表示用の固定配列
    private int head = 0;
    private int curent = 0;
    private final int size;

    public FloatRingBuffer(int size) {
        this.size = size;
        this.buffer = new float[size];
        this.displaySnapshot = new float[size];
    }

    // データ追加
    public synchronized void add(float value) {
        buffer[head] = value;
        curent = head;
        head = (head + 1) % size;
    }

    // スナップショット作成（計算用の重い処理を分離）
    public synchronized void updateSnapshot() {
        for (int i = 0; i < size; i++) {
            displaySnapshot[i] = buffer[(head + i) % size];
        }
    }
    
    public synchronized void clear() {
        for (int i = 0; i < size; i++) {
            buffer[i] = 0.0f;
        }
    }

    public float[] getSnapshot() {
        return displaySnapshot;
    }
    
    public float getCurentValue() {
        return buffer[curent];
    }
}

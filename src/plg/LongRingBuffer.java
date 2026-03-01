package plg;

public class LongRingBuffer {
    private final long[] buffer;
    private final long[] displaySnapshot; // 表示用の固定配列
    private int head = 0;
    private int curent = 0;
    private final int size;

    public LongRingBuffer(int size) {
        this.size = size;
        this.buffer = new long[size];
        this.displaySnapshot = new long[size];
    }

    // データ追加
    public synchronized void add(long value) {
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

    public long[] getSnapshot() {
        return displaySnapshot;
    }

    public long getCurentValue() {
        return buffer[curent];
    }
}

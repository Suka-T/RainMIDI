package layout.parts;

import java.awt.Graphics;

import layout.parts.monitor.MonitorData;

public abstract class MonitorPainter {
    
    public MonitorPainter() {
    }

    public void initialize() {
        
    }

    public void formatWithCommas(long number, StringBuilder out) {

        // 負数対応（符号のみ処理）
        boolean negative = number < 0;
        if (negative) {
            number = -number;
        }

        // 数字を一時バッファに逆順で格納
        char[] buffer = new char[20];
        int index = buffer.length;

        int digitCount = 0;
        do {
            if (digitCount > 0 && digitCount % 3 == 0) {
                buffer[--index] = ',';
            }
            buffer[--index] = (char) ('0' + (number % 10));
            number /= 10;
            digitCount++;
        }
        while (number > 0);

        if (negative) {
            buffer[--index] = '-';
        }

        out.append(buffer, index, buffer.length - index);
    }
    
    public abstract void paintMonitor(Graphics g, MonitorData info);
}

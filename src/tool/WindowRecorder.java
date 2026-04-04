package tool;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class WindowRecorder {
    public static final String WINDOW_TITLE = "Rain MIDI";
    private Process process;
    
    public WindowRecorder() {
        
    }

    public void startRecording() {
        try {
            String output = "record_" + System.currentTimeMillis() + ".mkv";

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg.exe",
                    "-y",
                    
                    "-f", "gdigrab",
                    "-framerate", "60",
                    "-i", "title=" + WINDOW_TITLE,
                    
                    "-f", "wasapi",
                    "-i", "default",
                    
                    "-c:v", "libx264",
                    "-preset", "ultrafast",
                    "-c:a", "aac",
                    
                    "-f", "matroska",
                    output
            );

            //pb.inheritIO();
            pb.redirectErrorStream(true);
            process = pb.start();

            System.out.println("録画開始");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        if (process != null) {
            System.out.println("*** stopSequencer");
            try {
                process.getOutputStream().write("q\n".getBytes());
                process.getOutputStream().flush();
            } catch (Exception ignored) {}

            try {
                if (!process.waitFor(2, TimeUnit.SECONDS)) {
                    process.destroy();
                    process.waitFor();
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("録画停止");
    }
}

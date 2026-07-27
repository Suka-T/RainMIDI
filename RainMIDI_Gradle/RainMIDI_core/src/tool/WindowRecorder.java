package tool;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class WindowRecorder {
    public static final String WINDOW_TITLE = "Rain MIDI";
    private Process process;

    public WindowRecorder() {

    }

    public void startRecording() {
        if (process != null) return;
        
        try {
            String output = "record_" + System.currentTimeMillis() + ".mkv";

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg.exe", "-y",
                    
                    "-f", "gdigrab", "-framerate", "60",
                    
                    //"-i", "desktop",
                    "-i", "title=" + WINDOW_TITLE,
                    
                    //"-f", "dshow", "-i", "audio=ステレオ ミキサー (Realtek(R) Audio)",
                    
                    // 映像と音声のズレを防ぐ設定
                    //"-fflags", "+genpts", "-async", "1",
                    
                    "-c:v", "libx264", "-preset", "ultrafast",
                    //"-c:a", "aac",
                    
                    "-f", "matroska", output
                    );

            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            
            // pb.inheritIO();
            //pb.redirectErrorStream(true);
            process = pb.start();

            System.out.println("録画開始");

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        if (process != null) {
            System.out.println("*** stopSequencer");
            try {
                process.getOutputStream().write("q\n".getBytes());
                process.getOutputStream().flush();
            }
            catch (Exception ignored) {
            }

            try {
                if (!process.waitFor(2, TimeUnit.SECONDS)) {
                    process.destroy();
                    process.waitFor();
                }
                process = null;
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("録画停止");
    }
}

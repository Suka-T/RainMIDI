package plg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class VersionChecker {
    
    public static boolean existsLatestVersion() {
        String latest = getLatestVersion();
        if (latest == null) {
            return false;
        }
        
        if (latest.equalsIgnoreCase("v" + AbstractRenderPlugin.APP_VERSION)) {
            return false;
        }
        return true;
    }

    public static String getLatestVersion() {
        try {
            @SuppressWarnings("deprecation")
            URL url = new URL("https://api.github.com/repos/Suka-T/RainMIDI/releases/latest");
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(url.openStream()));

            StringBuilder json = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                json.append(line);
            }
            return extractTagName(json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static String extractTagName(String json) {
        String key = "\"tag_name\":\"";
        int start = json.indexOf(key);
        if (start == -1) return null;

        start += key.length();
        int end = json.indexOf("\"", start);

        return json.substring(start, end);
    }

}

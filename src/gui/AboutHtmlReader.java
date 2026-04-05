package gui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.text.BadLocationException;

import jlib.core.ISystemManager;
import jlib.core.JMPCoreAccessor;
import plg.AbstractRenderPlugin;
import plg.I18n;

public class AboutHtmlReader {
    
    private File htmlFileEn = null;
    private File htmlFileja = null;
    private boolean existsNewVersion = false;

    public AboutHtmlReader(boolean existsNewVersion) {
        Path folder = Paths.get(JMPCoreAccessor.getSystemManager().getSystemPath(ISystemManager.PATH_CURRENT_DIR, AbstractRenderPlugin.PluginInstance));
        //Path fullPath = folder.resolve("about").resolve("index.html");
        //Path fullPath = folder.resolve("manual.en.html");
        Path fullPath = folder.resolve("about.html");
        htmlFileEn = new File(fullPath.toString());
        
        //fullPath = folder.resolve("about").resolve("index.ja.html");
        //fullPath = folder.resolve("manual.html");
        //htmlFileja = new File(fullPath.toString());
        htmlFileja = htmlFileEn;
        
        this.existsNewVersion = existsNewVersion;
    }
    
    public URL getURL(String langCode) throws MalformedURLException {
        if (langCode.equalsIgnoreCase("ja")) {
            return htmlFileja.toURI().toURL();
        }
        return htmlFileEn.toURI().toURL();
    }
    
    public String getContent(String langCode) throws MalformedURLException, IOException, BadLocationException {
        String html = "";
        try (InputStream is = getURL(langCode).openStream()) {
            html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        
        html = html.replace("<h1>RainMIDI</h1>", "<h1>RainMIDI v" + AbstractRenderPlugin.APP_VERSION + "</h1>");
        html = html.replace("<div class=\"newver\"></div>", 
                this.existsNewVersion ? "<div class=\"newver\"><a class=\"newverlink\" href=\"NewVer\" target=\"_blank\">" + I18n.t("msg.newver") + "</a></div>" : ""
                    );
        
        if (isOnline()) {
            html = html.replace("href=\"manual.html\"", "href=\"https://suka-t.github.io/RainMIDI/\"");
            html = html.replace("href=\"manual.en.html\"", "href=\"https://suka-t.github.io/RainMIDI/index.en.html\"");
        }
        else {
            File df = new File("manual.html");
            html = html.replace("href=\"manual.html\"", "href=\"" + df.toURI() + "\"");
            df = new File("manual.en.html");
            html = html.replace("href=\"manual.en.html\"", "href=\"" + df.toURI() + "\"");
        }
        return html;
    }
    
    boolean isOnline() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("https://suka-t.github.io/RainMIDI/").openConnection();
            con.setConnectTimeout(2000);
            con.connect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
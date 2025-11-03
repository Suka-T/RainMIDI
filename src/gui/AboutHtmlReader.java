package gui;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

import jlib.core.ISystemManager;
import jlib.core.JMPCoreAccessor;
import plg.AbstractRenderPlugin;

public class AboutHtmlReader {
    
    private File htmlFileEn = null;
    private File htmlFileja = null;

    public AboutHtmlReader() {
        Path folder = Paths.get(JMPCoreAccessor.getSystemManager().getSystemPath(ISystemManager.PATH_CURRENT_DIR, AbstractRenderPlugin.PluginInstance));
        Path fullPath = folder.resolve("about").resolve("index.html");
        htmlFileEn = new File(fullPath.toString());
        
        fullPath = folder.resolve("about").resolve("index.ja.html");
        htmlFileja = new File(fullPath.toString());
    }
    
    public URL getURL(String langCode) throws MalformedURLException {
        if (langCode.equalsIgnoreCase("ja")) {
            return htmlFileja.toURI().toURL();
        }
        return htmlFileEn.toURI().toURL();
    }
    
    public String getContent(String langCode) throws MalformedURLException, IOException, BadLocationException {
        HTMLEditorKit kit = new HTMLEditorKit();
        Document doc = kit.createDefaultDocument();

        try (Reader reader = new InputStreamReader(getURL(langCode).openStream(), StandardCharsets.UTF_8)) {
            kit.read(reader, doc, 0);
        }

        StringWriter writer = new StringWriter();
        kit.write(writer, doc, 0, doc.getLength());
        String html = writer.toString();

        html = html.replace("@@APP_NAME@@", AbstractRenderPlugin.APP_NAME);
        html = html.replace("@@APP_VER@@", "v" + AbstractRenderPlugin.APP_VERSION);
        return html;
    }
}
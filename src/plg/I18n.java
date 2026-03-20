package plg;
import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {
    private static ResourceBundle bundle;

    public static void setLocale(Locale locale) {
        bundle = ResourceBundle.getBundle("messages", locale);
    }

    public static String t(String key) {
        try {
            return bundle.getString(key);
        }
        catch (Exception e) {
            return "****";
        }
    }
}

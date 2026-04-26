package plg;
import java.util.Locale;
import java.util.ResourceBundle;

import plg.SystemProperties.SyspLanguage;

public class I18n {
    private static ResourceBundle bundle;

    public static void setLocale(Locale locale) {
        bundle = ResourceBundle.getBundle("messages", locale);
    }
    
    public static void setLocale(SyspLanguage syspLang) {
    	setLocale(convertSyspLangToLocale(syspLang));
    }

    public static String t(String key) {
        try {
            return bundle.getString(key);
        }
        catch (Exception e) {
            return "****";
        }
    }
    
    public static Locale convertSyspLangToLocale(SyspLanguage syspLang) {
        switch (syspLang) {
        case ENGLISH:
        	return Locale.ENGLISH;
        case JAPANESE:
        	return Locale.JAPANESE;
        case CHINESE:
        	return Locale.CHINESE;
        case AUTO:
        default:
        	return Locale.getDefault();
	    }
    }
    
    public static String convertSyspLangToLocaleSuffix(SyspLanguage syspLang) {
        switch (syspLang) {
        case ENGLISH:
        	return "en";
        case JAPANESE:
        	return "ja";
        case CHINESE:
        	return "zh";
        case AUTO:
        default:
        	return Locale.getDefault().getLanguage();
	    }
    }
}

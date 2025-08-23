package plg;

import java.awt.Color;
import java.io.File;

public class Utility {

    /**
     * HTMLカラーコードをAWTカラーインスタンスに変換
     *
     * @param code
     *            HTMLカラーコード
     * @return カラー
     */
    public static Color convertCodeToHtmlColor(String code) throws NumberFormatException {
        String value = "";

        // "#"の有無に依存しないようにする
        String[] offsetStr = code.split("#");
        if (offsetStr.length > 0) {
            value = offsetStr[offsetStr.length - 1];
        }

        if (value.length() != 6) {
            throw new NumberFormatException();
        }

        return Color.decode("#" + value.trim());
    }
    
    /**
     * 拡張子付きのファイル名の取得
     *
     * @param path
     *            パス
     * @return 拡張子付きファイル名
     */
    public static String getFileNameAndExtension(String path) {
        File file = new File(path);
        return getFileNameAndExtension(file);
    }

    /**
     * 拡張子付きのファイル名の取得
     *
     * @param file
     *            ファイル
     * @return 拡張子付きファイル名
     */
    public static String getFileNameAndExtension(File file) {
        String name = "";
        try {
            name = file.getName();
        }
        catch (Exception e) {
            name = "";
        }
        return name;
    }

    /**
     * 拡張子無しのファイル名取得
     *
     * @param path
     *            パス
     * @return 拡張子無しファイル名
     */
    public static String getFileNameNotExtension(String path) {
        File file = new File(path);
        return getFileNameNotExtension(file);
    }

    /**
     * 拡張子無しのファイル名取得
     *
     * @param file
     *            ファイル
     * @return 拡張子無しファイル名
     */
    public static String getFileNameNotExtension(File file) {
        String name = "";
        try {
            String[] s = file.getName().split("\\.");
            if (s.length > 0) {
                name = s[0];
            }
        }
        catch (Exception e) {
            name = "";
        }
        return name;
    }

    /**
     * 拡張子を取得
     *
     * @param path
     *            パス
     * @return 拡張子
     */
    public static String getExtension(String path) {
        File file = new File(path);
        return getExtension(file);
    }

    /**
     * 拡張子を取得
     *
     * @param file
     *            ファイル
     * @return 拡張子
     */
    public static String getExtension(File file) {
        String ex = "";
        try {
            String name = getFileNameAndExtension(file);
            
            int such = name.lastIndexOf(".");
            if (such != -1) {
                String s = name.substring(such);
                if (s.length() > 1) {
                    ex = s.substring(1);
                }
            }
        }
        catch (Exception e) {
            ex = "";
        }
        return ex;
    }

    /**
     * 拡張子チェック
     *
     * @param path
     *            ファイルパス
     * @param exStr
     *            一致するか判定する拡張子(ex: checkExtension("C:/text.txt", "txt"))
     * @return 結果(一致：true)
     */
    public static boolean checkExtension(String path, String exStr) {
        String ex = getExtension(path);
        return (ex != null) ? ex.equalsIgnoreCase(exStr) : false;
    }

    /**
     * 拡張子チェック
     *
     * @param f
     *            ファイル
     * @param exStr
     *            一致するか判定する拡張子
     * @return 結果(一致：true)
     */
    public static boolean checkExtension(File f, String exStr) {
        if (f.isFile() == false) {
            return false;
        }
        return checkExtension(f.getPath(), exStr);
    }

    /**
     * 拡張子チェック(複数指定可能)
     *
     * @param f
     *            ファイル
     * @param exStr
     *            一致するか判定する拡張子
     * @return 結果(一致：true)
     */
    public static boolean checkExtensions(File f, String... exStr) {
        boolean ret = false;
        for (String ex : exStr) {
            if (checkExtension(f, ex) == true) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    /**
     * 拡張子チェック(複数指定可能)
     *
     * @param path
     *            ファイルパス
     * @param exStr
     *            一致するか判定する拡張子(ex: checkExtension("C:/text.txt", "txt", "csv",
     *            "ini"))
     * @return 結果(一致：true)
     */
    public static boolean checkExtensions(String path, String... exStr) {
        boolean ret = false;
        for (String ex : exStr) {
            if (checkExtension(path, ex) == true) {
                ret = true;
                break;
            }
        }
        return ret;
    }
}

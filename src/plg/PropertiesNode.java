package plg;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PropertiesNode {

    public static enum PropertiesNodeType {
        INT, DOUBLE, BOOLEAN, STRING, COLOR, ITEM
    };

    private Object data = null;
    private String key = "";
    
    private String defaultSVal = "";
    private String maxSVal = "";
    private String minSVal = "";
    private Map<String, Object> map = new HashMap<String, Object>();
    private Map<Object, String> v2kmap = new HashMap<Object, String>();
    private Object defaultItem = null;
    private PropertiesNodeType type;
    private String[] itemArray = null;

    private static int toInt(String str, int def) {
        try {
            return Integer.parseInt(str);
        }
        catch (Exception e) {
            return def;
        }
    }

    private static double toDouble(String str, double def) {
        try {
            return Double.parseDouble(str);
        }
        catch (Exception e) {
            return def;
        }
    }

    private static boolean toBoolean(String str, boolean def) {
        try {
            return Boolean.parseBoolean(str);
        }
        catch (Exception e) {
            return def;
        }
    }

    private static String toColorCode(String str, String def) {
        String ret = "";
        if (str == null) {
            ret = def;
        }
        else {
            ret = str;
        }
        return "#" + ret.replaceFirst("^#+", "");
    }

    public PropertiesNode(String key, PropertiesNodeType type, String defVal, String minVal, String maxVal, String[] itemName, Object[] itemObjs) {
        this.key = key;
        this.defaultSVal = defVal;
        this.minSVal = minVal;
        this.maxSVal = maxVal;
        this.type = type;

        this.map.clear();
        this.v2kmap.clear();
        for (int i = 0; i < itemName.length; i++) {
            this.map.put(itemName[i].toLowerCase(), itemObjs[i]);
            if (this.v2kmap.containsKey(itemObjs[i]) == false) {
                this.v2kmap.put(itemObjs[i], itemName[i].toLowerCase());
            }
        }
        this.itemArray = itemName;
        
        this.data = getObject(null);
    }

    public PropertiesNode(String key, PropertiesNodeType type, String defVal) {
        this.key = key;
        this.defaultSVal = defVal;
        this.type = type;
        this.map.clear();
        this.v2kmap.clear();
        this.data = getObject(null);
    }

    public PropertiesNode(String key, PropertiesNodeType type, String defVal, String minVal, String maxVal) {
        this.key = key;
        this.defaultSVal = defVal;
        this.minSVal = minVal;
        this.maxSVal = maxVal;
        this.type = type;
        this.map.clear();
        this.v2kmap.clear();
        this.data = getObject(null);
    }

    public PropertiesNode(String key, PropertiesNodeType type, Object defaultItem, String[] itemName, Object[] itemObjs) {
        this.key = key;
        this.type = type;
        this.defaultItem = defaultItem;

        this.map.clear();
        this.v2kmap.clear();
        for (int i = 0; i < itemName.length; i++) {
            this.map.put(itemName[i], itemObjs[i]);
            if (this.v2kmap.containsKey(itemObjs[i]) == false) {
                this.v2kmap.put(itemObjs[i], itemName[i].toLowerCase());
            }
        }
        this.itemArray = itemName;
        this.data = getObject(null);
    }
    
    public void reset() {
        setObject(defaultSVal);
    }
    
    public String getKey() {
        return key;
    }
    
    public PropertiesNodeType getType() {
        return this.type;
    }
    
    public Object getData() {
        return data;
    }
    
    public Set<String> getItems() {
        return map.keySet();
    }
    
    public String getDataString() {
        if (data == null) return "";
        
        if (v2kmap.containsKey(data)) {
            return (String) v2kmap.get(data);
        }
        
        if (type == PropertiesNodeType.INT) {
            return String.valueOf((int)data);
        }
        else if (type == PropertiesNodeType.BOOLEAN) {
            return String.valueOf((boolean)data);
        }
        else if (type == PropertiesNodeType.DOUBLE) {
            return String.valueOf((double)data);
        }
        else if (type == PropertiesNodeType.STRING) {
            return String.valueOf((String)data);
        }
        else if (type == PropertiesNodeType.COLOR) {
            return String.valueOf((String)data);
        }
        else if (type == PropertiesNodeType.ITEM) {
            
            return (String) v2kmap.get(data);
        }
        return "";
    }
    
    public void setObject(String s) {
        data = getObject(s);
    }

    public Object getObject(String s) {
        if (s != null) {
            if (map.isEmpty() == false && map.containsKey(s.toLowerCase())) {
                return map.get(s.toLowerCase());
            }
        }

        Object obj = 0;
        if (type == PropertiesNodeType.INT) {
            int idef = toInt(defaultSVal, 0);
            obj = (Object) idef;
            if (s != null) {
                int ival = toInt(s, idef);
                if (maxSVal.isBlank() == false) {
                    int imax = toInt(maxSVal, 0);
                    if (ival > imax) {
                        ival = imax;
                    }
                }
                if (minSVal.isBlank() == false) {
                    int imin = toInt(minSVal, 0);
                    if (ival < imin) {
                        ival = imin;
                    }
                }
                obj = (Object) ival;
            }
        }
        else if (type == PropertiesNodeType.DOUBLE) {
            double ddef = toDouble(defaultSVal, 0.0);
            obj = (Object) ddef;
            if (s != null) {
                double dval = toDouble(s, ddef);
                if (maxSVal.isBlank() == false) {
                    double dmax = toDouble(maxSVal, 0.0);
                    if (dval > dmax) {
                        dval = dmax;
                    }
                }
                if (minSVal.isBlank() == false) {
                    double dmin = toDouble(minSVal, 0.0);
                    if (dval < dmin) {
                        dval = dmin;
                    }
                }
                obj = (Object) dval;
            }
        }
        else if (type == PropertiesNodeType.BOOLEAN) {
            boolean bdef = toBoolean(defaultSVal, false);
            obj = (Object) bdef;
            if (s != null) {
                boolean bval = toBoolean(s, bdef);
                obj = (Object) bval;
            }
        }
        else if (type == PropertiesNodeType.STRING) {
            obj = (Object) (s == null ? defaultSVal : s);
        }
        else if (type == PropertiesNodeType.COLOR) {
            obj = (Object) toColorCode(s, defaultSVal);
        }
        else if (type == PropertiesNodeType.ITEM) {
            obj = (Object) defaultItem;
        }
        else {
            obj = (Object) s;
        }
        return obj;
    }

    public String[] getItemArray() {
        return itemArray;
    }

}

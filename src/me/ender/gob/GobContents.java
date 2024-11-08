package me.ender.gob;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import haven.Config;
import haven.Gob;

import java.util.HashMap;
import java.util.Map;

public class GobContents {
    private static final Map<String, GobData> DATA = new HashMap<>();
    
    static {
	final Gson gson = new Gson();
	try {
	    final Map<String, GobData> cfg = gson.fromJson(Config.loadJarFile("gob_contents.json5"), new TypeToken<Map<String, GobData>>() {
	    }.getType());
	    DATA.putAll(cfg);
	} catch (Exception ignored) {
	}
    }
    
    public static GobData getData(Gob gob) {
	String resid = gob.resid();
	if(resid == null) {return null;}
	return DATA.get(resid);
    }
    
    public static class GobData {
	public String Seed;
	public String Leaf;
	public String Bark;
	public String Bough;
    }
}

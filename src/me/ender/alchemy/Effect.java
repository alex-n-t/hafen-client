package me.ender.alchemy;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import haven.Indir;
import haven.ItemInfo;
import haven.Resource;
import haven.res.ui.tt.alch.ingr_buff.BuffAttr;
import haven.res.ui.tt.alch.ingr_heal.HealWound;
import haven.res.ui.tt.alch.ingr_time_less.LessTime;
import haven.res.ui.tt.alch.ingr_time_more.MoreTime;
import haven.res.ui.tt.attrmod.AttrMod;

import java.io.IOException;
import java.util.*;

public class Effect {
    private static final boolean INCLUDE_NUMBERS = false; //TODO: add an option for this

    public static final String BUFF = "buff";
    public static final String HEAL = "heal";
    public static final String WOUND = "wound";
    public static final String TIME = "time";

    public static final String LESS = "less";
    public static final String MORE = "more";


    public final String raw;
    public final String type;
    public final String res;
    public final String opt;

    private String name = null;

    public Effect(String raw) {
	this.raw = raw;
	String[] parts = raw.split(":");

	this.type = parts[0];
	this.res = parts[1];
	this.opt = parts.length >= 3 ? parts[2] : null;
    }

    public Effect(String type, String res, String opt) {
	this.type = type;
	this.res = res;
	this.opt = opt;

	this.raw = opt == null
	    ? String.format("%s:%s", type, res)
	    : String.format("%s:%s:%s", type, res, opt);
    }

    public Effect(String type, String res) {
	this(type, res, null);
    }

    public Effect(String type, Indir<Resource> res) {
	this(type, res, null);
    }

    public Effect(String type, Indir<Resource> res, String opt) {
	this(type, res.get(), opt);
    }

    public Effect(String type, Resource res) {
	this(type, res, null);
    }

    public Effect(String type, Resource res, String opt) {
	this(type, res.name, opt);
    }

    public boolean matches(String filter) {
	String[] parts = filter.split(":",2);
	if(parts.length < 2) {return false;}

	if(!Objects.equals(type, parts[0])) {return false;}
	if(parts[1].isEmpty()) {return true;}

	return name().toLowerCase().contains(parts[1]);
    }

    public String name() {
	if(name == null) {
	    if(TIME.equals(type)) {
		switch (res) {
		    case LESS:
			name = "Increased Duration";
			break;
		    case MORE:
			name = "Reduced Duration";
			break;
		    default:
			name = res;
		}
	    } else {
		try {
		    name = Resource.remote().loadwait(res).layer(Resource.tooltip).t;
		} catch (Exception e) {
		    name = res;
		}
	    }
	}
	return name;
    }

    public static List<ItemInfo> ingredientInfo(Collection<Effect> effects) {
	List<ItemInfo> tips = new LinkedList<>();
	for (Effect effect : effects) {
	    String res = effect.res;
	    switch (effect.type) {
		case BUFF:
		    tips.add(new BuffAttr(null, Resource.remote().load(res)));
		    break;
		case HEAL:
		    tips.add(new HealWound(null, Resource.remote().load(res), null));
		    break;
		case TIME:
		    if(res.equals(MORE)) {
			tips.add(new MoreTime(null));
		    } else if(res.equals(LESS)) {
			tips.add(new LessTime(null));
		    }
		    break;
	    }
	}
	return tips;
    }

    public static List<ItemInfo> elixirInfo(Collection<Effect> effects) {
	List<ItemInfo> tips = new LinkedList<>();
	for (Effect effect : effects) {
	    String res = effect.res;
	    int a = getValue(effect.opt);
	    switch (effect.type) {
		case BUFF:
		    tips.add(new AttrMod(null, Collections.singletonList(new AttrMod.Mod(Resource.remote().loadwait(res), 10 * a))));
		    break;
		case HEAL:
		    tips.add(new FixWound(null, Resource.remote().load(res), null, 10 * a));
		    break;
		case WOUND:
		    tips.add(new InflictWound(null, Resource.remote().load(res), a));
		    break;
		case TIME:
		    if(res.equals(MORE)) {
			tips.add(new MoreTime(null));
		    } else if(res.equals(LESS)) {
			tips.add(new LessTime(null));
		    }
		    break;
	    }
	}
	return tips;
    }

    /**
     * Returns effect string for <a href="https://yoda-magic.github.io/alchemygraph">Yoda's Alchemy Graph</a> site.
     */
    public String format() {
	int v = getValue(opt);
	String name = name();

	String prefix = "";
	switch (type) {
	    case BUFF:
		prefix = (v > 1 ? "^^" : "^");
		if(INCLUDE_NUMBERS && v > 0) {
		    prefix += 10 * v + " ";
		}
		break;
	    case HEAL:
		prefix = "+";
		break;
	    case WOUND:
		prefix = "-";
		if(INCLUDE_NUMBERS && v > 0) {
		    prefix += v + " ";
		}
		break;
	    case TIME:
		prefix = "%";
		break;
	}
	return prefix + name;
    }

    private static int getValue(String opt) {
	if(opt == null) {return 0;}
	try {
	    return Integer.parseInt(opt);
	} catch (Exception ignore) {}
	return 0;
    }

    public static class Adapter extends TypeAdapter<Effect> {
	@Override
	public void write(JsonWriter writer, Effect effect) throws IOException {
	    writer.value(effect.raw);
	}

	@Override
	public Effect read(JsonReader reader) throws IOException {
	    return new Effect(reader.nextString());
	}
    }
}

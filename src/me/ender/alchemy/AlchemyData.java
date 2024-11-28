package me.ender.alchemy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import haven.*;
import haven.res.ui.tt.alch.ingr_buff.BuffAttr;
import haven.res.ui.tt.alch.ingr_heal.HealWound;
import haven.res.ui.tt.alch.ingr_time_less.LessTime;
import haven.res.ui.tt.alch.ingr_time_more.MoreTime;
import haven.res.ui.tt.attrmod.AttrMod;
import me.ender.Reflect;

import java.util.*;

public class AlchemyData {
    public static boolean DBG = Config.get().getprop("ender.debug.alchemy", "off").equals("on");

    public static final String HERBAL_GRIND = "/herbalgrind";
    public static final String LYE_ABLUTION = "/lyeablution";
    public static final String MINERAL_CALCINATION = "/mineralcalcination";
    public static final String MEASURED_DISTILLATE = "/measureddistillate";
    public static final String FIERY_COMBUSTION = "/fierycombustion";

    private static final Gson GSON = new GsonBuilder().create();

    private static final Map<String, Ingredient> INGREDIENTS = new HashMap<>();
    private static final Set<Elixir> ELIXIRS = new HashSet<>();


    static {
	//TODO: instead of static init, call this when needed. 
	initialize();
    }

    private static void initialize() {
	loadIngredients(Config.loadFile("ingredients.json"));
	loadElixirs(Config.loadFile("elixirs.json"));
    }

    private static void loadIngredients(String json) {
	if(json != null) {
	    try {
		Map<String, Ingredient> tmp = GSON.fromJson(json, new TypeToken<Map<String, Ingredient>>() {
		}.getType());
		for (Map.Entry<String, Ingredient> entry : tmp.entrySet()) {
		    String key = entry.getKey();
		    INGREDIENTS.put(key, new Ingredient(entry.getValue().effects, INGREDIENTS.get(key)));
		}
	    } catch (Exception ignore) {}
	}
    }

    private static void loadElixirs(String json) {
	if(json != null) {
	    try {
		Set<Elixir> tmp = GSON.fromJson(json, new TypeToken<Set<Elixir>>() {
		}.getType());
		ELIXIRS.addAll(tmp);
	    } catch (Exception ignore) {}
	}
    }

    private static void saveIngredients() {
	Config.saveFile("ingredients.json", GSON.toJson(INGREDIENTS));
    }

    private static void saveElixirs() {
	Config.saveFile("elixirs.json", GSON.toJson(ELIXIRS));
    }

    public static void categorize(GItem item) {
	String res = item.resname();
	String name = item.name.get();
	List<ItemInfo> infos = item.info();
	double q = item.itemq.get().single().value;
	double qc = q > 0 ? 1d / Math.sqrt(10 * q) : 1d;

	ItemInfo.Contents contents = ItemInfo.find(ItemInfo.Contents.class, infos);
	if(contents != null) {infos = contents.sub;}

	List<String> effects = new LinkedList<>();
	boolean isElixir = false;
	Recipe recipe = null;

	for (ItemInfo info : infos) {
	    if(Reflect.is(info, "Elixir")) {
		isElixir = true;
		//noinspection unchecked
		List<ItemInfo> effs = (List<ItemInfo>) Reflect.getFieldValue(info, "effs");
		for (ItemInfo eff : effs) {
		    tryAddEffect(qc, effects, eff);
		}
		//TODO: detect less/more time effects in elixirs?
	    } else if(info instanceof haven.res.ui.tt.alch.recipe.Recipe) {
		recipe = Recipe.from(res, (haven.res.ui.tt.alch.recipe.Recipe) info);
	    } else {
		tryAddEffect(qc, effects, info);
	    }
	}

	if(isElixir && recipe != null) {
	    //TODO: option to ignore bad-only elixirs?
	    Elixir elixir = new Elixir(recipe, effects);
	    //ELIXIRS.remove(elixir); //always replace
	    ELIXIRS.add(elixir);
	    saveElixirs();
	    if(DBG) {
		String alchemyUrl = elixir.toAlchemyUrl();
		System.out.println(alchemyUrl);
		if(WebBrowser.self != null) {
		    WebBrowser.self.show(Utils.url(alchemyUrl));
		}
	    }
	} else if(!isElixir && !effects.isEmpty() && isNatural(res)) {
	    INGREDIENTS.put(res, new Ingredient(effects, INGREDIENTS.get(res)));
	    saveIngredients();
	}

	if(DBG) {
	    long wounds = effects.stream().filter(e -> e.startsWith("wound:")).count();
	    boolean dud = wounds == effects.size();
	    String sEffects = String.join(", ", effects);

	    System.out.printf("'%s' => elixir:%b, wounds:%d, dud: %b, effects: [%s], recipe:%s %n",
		name, isElixir, wounds, dud, sEffects, recipe);


	    if(recipe != null) {
		Gson gson = new GsonBuilder().create();
		String json = gson.toJson(recipe);
		System.out.println(json);

		Recipe tmp = gson.fromJson(json, Recipe.class);
		System.out.println(tmp);
		System.out.println(tmp.toAlchemy());
	    }
	}
    }

    private static void tryAddEffect(double qc, Collection<String> effects, ItemInfo info) {
	if(info instanceof BuffAttr) {
	    effects.add(String.format("buff:%s", ((BuffAttr) info).res.get().name));
	} else if(info instanceof AttrMod) {
	    for (AttrMod.Mod mod : ((AttrMod) info).mods) {
		long a = Math.round(qc * mod.mod);
		effects.add(String.format("buff:%s:%d", mod.attr.name, a));
	    }
	} else if(info instanceof HealWound) {
	    effects.add(String.format("heal:%s", ((HealWound) info).res.get().name));
	} else if(Reflect.is(info, "HealWound")) {
	    //this is from elixir, it uses different resource and has value
	    //noinspection unchecked
	    Indir<Resource> res = (Indir<Resource>) Reflect.getFieldValue(info, "res");
	    long a = Math.round(qc * Reflect.getFieldValueInt(info, "a"));
	    effects.add(String.format("heal:%s:%d", res.get().name, a));
	} else if(Reflect.is(info, "AddWound")) {
	    //this is from elixir
	    //noinspection unchecked
	    Indir<Resource> res = (Indir<Resource>) Reflect.getFieldValue(info, "res");
	    //TODO: try to find base wound magnitude
	    int a = Reflect.getFieldValueInt(info, "a");
	    effects.add(String.format("wound:%s:%d", res.get().name, a));
	} else if(info instanceof LessTime) {
	    effects.add("time:less");
	} else if(info instanceof MoreTime) {
	    effects.add("time:more");
	}
    }

    public static boolean isNatural(String res) {
	return !res.contains(HERBAL_GRIND)
	    && !res.contains(LYE_ABLUTION)
	    && !res.contains(MINERAL_CALCINATION)
	    && !res.contains(MEASURED_DISTILLATE)
	    && !res.contains(FIERY_COMBUSTION);
    }

    public static boolean isMineral(String res) {
	return GobIconCategoryList.GobCategory.isRock(res) || GobIconCategoryList.GobCategory.isOre(res);
    }
}

package haven;

import me.ender.GobInfoOpts;
import me.ender.gob.GobTimerData;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class GeneralGobInfo extends GobInfo {
    private static final int TREE_START = 10;
    private static final int BUSH_START = 30;
    private static final double TREE_MULT = 100.0 / (100.0 - TREE_START);
    private static final double BUSH_MULT = 100.0 / (100.0 - BUSH_START);
    private static final Color Q_COL = new Color(235, 252, 255, 255);
    private static final Color BARREL_COL = new Color(252, 235, 255, 255);
    private static final Color BG = new Color(0, 0, 0, 84);
    public static Pattern GOB_Q = Pattern.compile("Quality: (\\d+)");
    private static final Map<Long, Integer> gobQ = new LinkedHashMap<Long, Integer>() {
	@Override
	protected boolean removeEldestEntry(Map.Entry eldest) {
	    return size() > 50;
	}
    };
    private GobHealth health;
    private int scalePercent = -1;
    private String contents = null;
    int q;
    private static final Map<String, Integer> POS = new HashMap<>();
    
    public final GobTimerData timer;
    
    static {
	POS.put("gfx/terobjs/smelter", 5);
	POS.put("gfx/terobjs/barrel", 6);
	POS.put("gfx/terobjs/iconsign", 5);
    }

    protected GeneralGobInfo(Gob owner) {
	super(owner);
	q = gobQ.getOrDefault(gob.id, 0);
	timer = GobTimerData.from(gob);
	center = new Pair<>(0.5, 1.0);
    }
    
    
    public void setQ(int q) {
	gobQ.put(gob.id, q);
	this.q = q;
    }
    
    @Override
    protected boolean enabled() {
	return CFG.DISPLAY_GOB_INFO.get();
    }

    @Override
    protected Tex render() {
	if(gob == null || gob.getres() == null) {return null;}
	
	up(POS.getOrDefault(gob.resid(), 1));
	BufferedImage[] parts = new BufferedImage[]{
	    growth(),
	    health(),
	    content(),
	    quality(),
	    timer.img(),
	};
	
	for (BufferedImage part : parts) {
	    if(part == null) {continue;}
	    return new TexI(ItemInfo.catimgsh(UI.scale(3), 0, BG, parts));
	}
	return null;
    }
    
    @Override
    public void ctick(double dt) {
	if(enabled() && timer.update()) {dirty();}
	super.ctick(dt);
    }
    
    @Override
    public void dispose() {
	health = null;
	super.dispose();
    }

    private BufferedImage quality() {
	if(CFG.DISPLAY_GOB_INFO_DISABLED_PARTS.get().contains(GobInfoOpts.InfoPart.QUALITY)) {return null;}
	if(q != 0) {
	    String text = String.format("$img[gfx/hud/gob/quality,c]%s", RichText.color(String.valueOf(q), Q_COL));
	    return Utils.outline2(RichText.stdf.render(text).img, Color.BLACK);
	}
	return null;
    }
    
    private BufferedImage health() {
	if(CFG.DISPLAY_GOB_INFO_DISABLED_PARTS.get().contains(GobInfoOpts.InfoPart.HEALTH)) {return null;}
	health = gob.getattr(GobHealth.class);
	if(health != null) {
	    return health.text();
	}

	return null;
    }

    private BufferedImage growth() {
	Text.Line line = null;
	scalePercent = -1;
 
	if(isSpriteKind(gob, "GrowingPlant", "TrellisPlant")) {
	    if(CFG.DISPLAY_GOB_INFO_DISABLED_PARTS.get().contains(GobInfoOpts.InfoPart.PLANT_GROWTH)) {return null;}
	    int maxStage = 0;
	    for (FastMesh.MeshRes layer : gob.getres().layers(FastMesh.MeshRes.class)) {
		if(layer.id / 10 > maxStage) {
		    maxStage = layer.id / 10;
		}
	    }
	    Message data = getDrawableData(gob);
	    if(data != null) {
		int stage = data.uint8();
		if(stage > maxStage) {stage = maxStage;}
		Color c = Utils.blendcol((double) stage / maxStage, Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN);
		line = Text.std.renderstroked(String.format("%d/%d", stage, maxStage), c, Color.BLACK);
	    }
	} else if(isSpriteKind(gob, "Tree")) {
	    if(CFG.DISPLAY_GOB_INFO_DISABLED_PARTS.get().contains(GobInfoOpts.InfoPart.TREE_GROWTH)) {return null;}
	    Message data = getDrawableData(gob);
	    if(data != null && !data.eom()) {
		data.skip(1);
		scalePercent = data.eom() ? -1 : data.uint8();
		if(scalePercent < 100 && scalePercent >= 0) {
		    int growth = scalePercent;
		    if(gob.is(GobTag.TREE)) {
			growth = (int) (TREE_MULT * (growth - TREE_START));
		    } else if(gob.is(GobTag.BUSH)) {
			growth = (int) (BUSH_MULT * (growth - BUSH_START));
		    }
		    Color c = Utils.blendcol(growth / 100.0, Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN);
		    line = Text.std.renderstroked(String.format("%d%%", growth), c, Color.BLACK);
		}
	    }
	}

	if(line != null) {
	    return line.img;
	}
	return null;
    }
    
    public float growthScale() {
	int percent = scalePercent;
	return percent > 0
	    ? percent / 100f
	    : 1;
    }
    
    public String contents() {
	return contents;
    }

    private BufferedImage content() {
	this.contents = null;
	String res = gob.resid();
	if(res == null) {return null;}
	Optional<String> contents = Optional.empty();
	
	if(res.startsWith("gfx/terobjs/barrel")) {
	    if(CFG.DISPLAY_GOB_INFO_DISABLED_PARTS.get().contains(GobInfoOpts.InfoPart.BARREL)) {return null;}
	    contents = gob.ols.stream()
		.map(Gob.Overlay::name)
		.filter(name -> name.startsWith("gfx/terobjs/barrel-"))
		.map(name -> name.substring(name.lastIndexOf("-") + 1))
		.map(Utils::prettyResName)
		.findAny();
	    
	} else if(res.startsWith("gfx/terobjs/iconsign")) {
	    if(CFG.DISPLAY_GOB_INFO_DISABLED_PARTS.get().contains(GobInfoOpts.InfoPart.DISPLAY_SIGN)) {return null;}
	    Message sdt = gob.sdtm();
	    if(!sdt.eom()) {
		int resid = sdt.uint16();
		if((resid & 0x8000) != 0) {
		    resid &= ~0x8000;
		}
		
		Session session = gob.context(Session.class);
		contents = Optional.of(Utils.prettyResName(session.getres(resid)));
	    }
	}
	
	if(contents.isPresent()) {
	    this.contents = contents.get();
	    String text = this.contents;
	    if(CFG.DISPLAY_GOB_INFO_SHORT.get()) {
		text = shorten(text);
	    }
	    BufferedImage img = Text.std.renderstroked(text, BARREL_COL, Color.black).img;
	    if(img.getWidth() <= UI.scale(60)) {
		return img;
	    }
	    
	    String[] parts = text.split(" ");
	    if(parts.length <= 1) {return img;}
	    
	    return ItemInfo.catimgs(0, ItemInfo.CENTER, Arrays.stream(parts)
		.map(p -> Text.std.renderstroked(p, BARREL_COL, Color.black).img)
		.toArray(BufferedImage[]::new));
	}
	return null;
    }
    
    private static Message getDrawableData(Gob gob) {
	Drawable dr = gob.drawable;
	ResDrawable d = (dr instanceof ResDrawable) ? (ResDrawable) dr : null;
	if(d != null)
	    return d.sdt.clone();
	else
	    return null;
    }
    
    private static boolean isSpriteKind(Gob gob, String... kind) {
	List<String> kinds = Arrays.asList(kind);
	boolean result = false;
	Class spc;
	Drawable d = gob.drawable;
	Resource.CodeEntry ce = gob.getres().layer(Resource.CodeEntry.class);
	if(ce != null) {
	    spc = ce.get("spr");
	    result = spc != null && (kinds.contains(spc.getSimpleName()) || kinds.contains(spc.getSuperclass().getSimpleName()));
	}
	if(!result) {
	    if(d instanceof ResDrawable) {
		Sprite spr = ((ResDrawable) d).spr;
		if(spr == null) {throw new Loading();}
		spc = spr.getClass();
		result = kinds.contains(spc.getSimpleName()) || kinds.contains(spc.getSuperclass().getSimpleName());
	    }
	}
	return result;
    }
    
    private static String shorten(String text) {
	return text.replaceAll(" Hide|Dried |Bar of | Leaf| Leaves", "");
    }

    @Override
    public String toString() {
	Resource res = gob.getres();
	return String.format("GobInfo<%s>", res != null ? res.name : "<loading>");
    }
}
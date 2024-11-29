package me.ender.alchemy;

import haven.*;
import me.ender.ClientUtils;
import me.ender.ui.TabStrip;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ComboWdg extends Widget {
    private static final String ALL = "All";
    private static final String TESTED = "Tested";
    private static final String UNTESTED = "Untested";
    private static String LAST_SELECTED;
    private final NamesProvider namesProvider;
    private final IngredientList ingredients;
    private final ComboList combo;
    private final ACheckBox highlight;
    private boolean initialized = false;

    ComboWdg(NamesProvider namesProvider) {
	super();
	this.namesProvider = namesProvider;

	ingredients = new IngredientList(namesProvider, this::onIngredientChanged);
	Coord p = add(ingredients, AlchemyWnd.PAD, AlchemyWnd.PAD).pos("br");

	combo = new ComboList(namesProvider);
	p = add(combo, p.add(AlchemyWnd.GAP, -combo.sz.y)).pos("ul");

	TabStrip<String> strip = new TabStrip<>(this::onTabSelected);

	strip.insert(ALL, null, ALL, null);
	strip.insert(TESTED, null, TESTED, null);
	strip.insert(UNTESTED, null, UNTESTED, null);
	strip.select(0);

	add(strip, p.addy(-strip.sz.y));

	highlight = new CheckBox("Highlight").changed(this::highlight);
	add(highlight, combo.pos("ur").sub(highlight.sz).addy(-AlchemyWnd.PAD));

	pack();
    }

    @Override
    public void draw(GOut g) {
	super.draw(g);
	
	if(!initialized && ui != null) {
	    highlight.a = ui.root.getchild(Track.class) != null;
	    ingredients.change(LAST_SELECTED);
	    initialized = true;
	}
    }

    private void highlight(Boolean highlight) {
	Track tracker = ui.root.getchild(Track.class);

	if(Boolean.TRUE.equals(highlight)) {
	    if(tracker == null) {
		tracker = ui.root.add(new Track(), ClientUtils.getScreenCenter(ui));
	    }

	    String target = combo.target;
	    if(target == null) {
		tracker.highlight(null, null);
	    } else {
		tracker.highlight(target, namesProvider.text(target).tex());
	    }
	} else if(tracker != null) {
	    tracker.close();
	}
    }

    private void onTabSelected(String tab) {
	combo.filter(tab);
    }

    private void onIngredientChanged(String res) {
	LAST_SELECTED = res;
	combo.setTarget(res);
	highlight(highlight.a);
    }

    private static class Track extends WindowX {
	Tex image;
	String res = null;
	private final Coord IMG_C;

	public Track() {
	    super(Coord.of(AlchemyWnd.LIST_W, UI.scale(32)), "Ingredient Track");
	    justclose = true;
	    IMG_C = add(new Label("Highlight untested combinations for:")).pos("bl");

	    listen(AlchemyData.COMBOS_UPDATED, this::update);
	}

	private void update() {
	    if(res != null) {
		highlight(res, image);
	    }
	}

	@Override
	public void dispose() {
	    GItem.setAlchemyFilter(null);
	    super.dispose();
	}

	@Override
	public void cdraw(GOut g) {
	    if(image != null) {
		g.image(image, IMG_C);
	    }
	    super.cdraw(g);
	}

	public void highlight(String res, Tex img) {
	    image = img;
	    this.res = res;
	    if(res == null) {
		GItem.setAlchemyFilter(null);
	    } else {
		GItem.setAlchemyFilter(new ComboFilter(AlchemyData.combos(res)));
	    }
	}
    }

    private static class IngredientList extends FilteredListBox<String> {
	private final NamesProvider nameProvider;
	private final Consumer<String> onChanged;
	private boolean dirty = true;

	public IngredientList(NamesProvider nameProvider, Consumer<String> onChanged) {
	    super(AlchemyWnd.LIST_W, AlchemyWnd.ITEMS, AlchemyWnd.ITEM_H);
	    bgcolor = AlchemyWnd.BGCOLOR;

	    this.nameProvider = nameProvider;
	    this.onChanged = onChanged;

	    listen(AlchemyData.COMBOS_UPDATED, this::onComboUpdated);
	}

	private void onComboUpdated() {
	    String tmp = sel;
	    update();
	    change(tmp);
	}

	@Override
	public void changed(String item, int index) {
	    onChanged.accept(item);
	}

	private void update() {
	    if(tvisible()) {
		List<String> tmp = AlchemyData.allIngredients();
		tmp.sort(nameProvider::compare);
		setItems(tmp);
		dirty = false;
	    } else {
		dirty = true;
	    }
	}

	@Override
	public void draw(GOut g) {
	    if(dirty) {update();}
	    super.draw(g);
	}

	@Override
	protected boolean match(String item, String filter) {
	    return true;
	}

	@Override
	protected void drawitem(GOut g, String item, int i) {
	    g.image(nameProvider.text(item).tex(), Coord.z);
	}
    }

    private static class ComboList extends FilteredListBox<String> {
	private static final Coord NAME_C = Coord.of(CheckBox.smark.sz().x + AlchemyWnd.PAD, 0);
	private final NamesProvider nameProvider;
	private boolean dirty = true;
	private String target = null;
	private final Set<String> combos = new HashSet<>();

	public ComboList(NamesProvider nameProvider) {
	    super(AlchemyWnd.LIST_W + NAME_C.x + AlchemyWnd.PAD, AlchemyWnd.ITEMS - 2, AlchemyWnd.ITEM_H);
	    bgcolor = AlchemyWnd.BGCOLOR;
	    showFilterText = false;
	    this.nameProvider = nameProvider;

	    listen(AlchemyData.COMBOS_UPDATED, this::onComboUpdated);
	}

	public void setTarget(String target) {
	    this.target = target;
	    updateCombos();
	}

	private void onComboUpdated() {
	    sel = null;
	    selindex = -1;
	    update();
	}

	@Override
	public boolean keydown(KeyDownEvent ev) {
	    return false;
	}

	private void update() {
	    if(tvisible()) {
		updateCombos();
		List<String> tmp = AlchemyData.allIngredients();
		tmp.sort(nameProvider::compare);
		setItems(tmp);
		dirty = false;
	    } else {
		dirty = true;
	    }
	}

	private void updateCombos() {
	    combos.clear();
	    needfilter();
	    if(target == null) {return;}

	    combos.addAll(AlchemyData.combos(target));
	}

	@Override
	public void draw(GOut g) {
	    if(dirty) {update();}
	    super.draw(g);
	}

	@Override
	protected boolean match(String item, String filter) {
	    if(TESTED.equals(filter)) {
		return combos.contains(item);
	    } else if(UNTESTED.equals(filter)) {
		return !combos.contains(item);
	    }
	    return true;
	}

	@Override
	protected void drawitem(GOut g, String item, int i) {
	    if(combos.contains(item)) {
		g.image(CheckBox.smark, Coord.z);
	    }
	    g.image(nameProvider.text(item).tex(), NAME_C);
	}
    }
}

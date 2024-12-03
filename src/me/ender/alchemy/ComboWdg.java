package me.ender.alchemy;

import haven.*;
import me.ender.ui.CFGBox;
import me.ender.ui.TabStrip;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ComboWdg extends Widget {
    private static final String ALL = "All";
    private static final String TESTED = "Tested";
    private static final String UNTESTED = "Untested";
    private static String LAST_SELECTED_INGREDIENT;
    private static int LAST_SELECTED_TAB = 0;
    private final NamesProvider namesProvider;
    private final IngredientList ingredients;
    private final ComboList combo;
    private final TabStrip<String> strip;
    private boolean initialized = false;

    ComboWdg(NamesProvider namesProvider) {
	super();
	this.namesProvider = namesProvider;

	ingredients = new IngredientList(namesProvider, this::onIngredientChanged);
	Coord p = add(ingredients, AlchemyWnd.PAD, AlchemyWnd.PAD).pos("br");

	combo = new ComboList(namesProvider);
	p = add(combo, p.add(AlchemyWnd.GAP, -combo.sz.y)).pos("ul");

	strip = new TabStrip<>(this::onTabSelected);

	strip.insert(ALL, null, ALL, null);
	strip.insert(TESTED, null, TESTED, null);
	strip.insert(UNTESTED, null, UNTESTED, null);
	strip.select(Math.max(LAST_SELECTED_TAB, 0));

	add(strip, p.addy(-strip.sz.y));

	Widget highlight = new Button(UI.scale(90), "Track combos", false)
	    .action(this::highlight)
	    .settip("Highlight all ingredients that are not tested against selected one", CFGBox.TT_WIDTH);
	
	add(highlight, combo.pos("ur").sub(highlight.sz).addy(-AlchemyWnd.PAD));

	pack();
    }

    @Override
    public void draw(GOut g) {
	super.draw(g);

	if(!initialized && ui != null) {
	    ingredients.change(LAST_SELECTED_INGREDIENT);
	    ingredients.showsel();
	    initialized = true;
	}
    }

    private void highlight() {
	TrackWnd.track(ui, combo.target, false, false, namesProvider);
    }

    private void onTabSelected(String tab) {
	combo.filter(tab);
	LAST_SELECTED_TAB = strip.getSelectedButtonIndex();
    }

    private void onIngredientChanged(String res) {
	LAST_SELECTED_INGREDIENT = res;
	combo.setTarget(res);
	TrackWnd.track(ui, combo.target, false, true, namesProvider);
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
	public boolean mousedown(MouseDownEvent ev) {
	    parent.setfocus(this);
	    return super.mousedown(ev);
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
	    return nameProvider.name(item).toLowerCase().contains(filter.toLowerCase());
	}

	@Override
	protected void drawitem(GOut g, String item, int i) {
	    g.image(nameProvider.tex(item), Coord.z);
	}
    }

    private static class ComboList extends FilteredListBox<String> {
	private static final Coord MARK_C = Coord.of(0, UI.scale(1));
	private static final Coord NAME_C = Coord.of(AlchemyWnd.ITEM_H, 0);
	private final NamesProvider nameProvider;
	private boolean dirty = true;
	private String target = null;
	private final Set<String> combos = new HashSet<>();

	public ComboList(NamesProvider nameProvider) {
	    super(AlchemyWnd.CONTENT_W, AlchemyWnd.ITEMS - 2, AlchemyWnd.ITEM_H);
	    bgcolor = AlchemyWnd.BGCOLOR;
	    showFilterText = false;
	    this.nameProvider = nameProvider;

	    listen(AlchemyData.COMBOS_UPDATED, this::onComboUpdated);
	}

	public void setTarget(String target) {
	    this.target = target;
	    updateCombos();
	}

	@Override
	protected void itemclick(String item, int button) {
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
		g.image(CheckBox.smark, MARK_C);
	    }
	    g.image(nameProvider.tex(item), NAME_C);
	}
    }
}

package me.ender.alchemy;

import haven.*;
import me.ender.ui.TabStrip;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ComboWdg extends Widget {
    private static final String ALL = "All";
    private static final String TESTED = "Tested";
    private static final String UNTESTED = "Untested";
    private final ComboList combo;

    ComboWdg(NamesProvider namesProvider) {
	super();

	Coord p = add(new IngredientList(namesProvider, this::onIngredientChanged), AlchemyWnd.PAD, AlchemyWnd.PAD).pos("br");

	combo = new ComboList(namesProvider);
	p = add(combo, p.add(AlchemyWnd.GAP, -combo.sz.y)).pos("ul");

	TabStrip<String> strip = new TabStrip<>(this::onTabSelected);

	strip.insert(ALL, null, ALL, null);
	strip.insert(TESTED, null, TESTED, null);
	strip.insert(UNTESTED, null, UNTESTED, null);
	strip.select(0);

	add(strip, p.addy(-strip.sz.y));

	pack();
    }

    private void onTabSelected(String tab) {
	combo.filter(tab);
    }

    private void onIngredientChanged(String res) {
	combo.setTarget(res);
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
	    super(AlchemyWnd.LIST_W + NAME_C.x, AlchemyWnd.ITEMS - 2, AlchemyWnd.ITEM_H);
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

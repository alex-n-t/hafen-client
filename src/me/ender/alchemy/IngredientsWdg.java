package me.ender.alchemy;

import haven.*;
import me.ender.ui.TabStrip;

import java.util.*;
import java.util.function.Consumer;

class IngredientsWdg extends Widget {
    private static final String KNOWN = "Known";
    private static final String TESTED = "Tested";
    private static final String UNTESTED = "Untested";

    private static String LAST_SELECTED_INGREDIENT;
    private static String LAST_SELECTED_TAB = KNOWN;

    private String selectedTab = LAST_SELECTED_TAB;
    private final InfoList info;

    private Ingredient selected = null;
    private String selectedName = null;

    boolean effectsDirty = true;
    private final Set<Effect> tested = new HashSet<>();
    private final Set<Effect> untested = new HashSet<>();

    IngredientsWdg(NamesProvider nameProvider) {
	IngredientList ingredientList = new IngredientList(nameProvider, this::onSelectionChanged);
	Coord p = add(ingredientList, AlchemyWnd.PAD, AlchemyWnd.PAD).pos("br");


	info = new InfoList(nameProvider);
	p = add(info, p.add(AlchemyWnd.GAP, -info.sz.y)).pos("ul");

	TabStrip<String> tabs = new TabStrip<>(this::onTabSelected);

	tabs.insert(KNOWN, null, KNOWN, null);
	tabs.insert(TESTED, null, TESTED, null);
	tabs.insert(UNTESTED, null, UNTESTED, null);

	tabs.select(selectedTab);

	add(tabs, p.addy(-tabs.sz.y));

	listen(AlchemyData.INGREDIENTS_UPDATED, this::onDataUpdated);
	listen(AlchemyData.COMBOS_UPDATED, this::onDataUpdated);
	listen(AlchemyData.EFFECTS_UPDATED, this::onDataUpdated);

	pack();
    }

    private void onDataUpdated() {
	effectsDirty = true;
	updateInfo();
    }

    private void onTabSelected(String tab) {
	selectedTab = LAST_SELECTED_TAB = tab;
	updateInfo();
    }

    private void onSelectionChanged(String res) {
	LAST_SELECTED_INGREDIENT = selectedName = res;
	selected = AlchemyData.ingredient(res);

	effectsDirty = true;
	updateInfo();
    }

    private void updateInfo() {
	info.check = null;
	switch (selectedTab) {
	    case KNOWN:
		info.setItems(selected == null ? Collections.emptyList() : selected.effects);
		break;
	    case TESTED:
		updateEffects();
		if(selected != null) {info.check = selected.effects;}
		info.setItems(tested);
		break;
	    case UNTESTED:
		updateEffects();
		info.setItems(untested);
		break;
	    default:
		info.setItems(Collections.emptyList());
	}
    }

    private void updateEffects() {
	if(!effectsDirty) {return;}
	effectsDirty = false;

	tested.clear();
	untested.clear();
	if(selected == null || selectedName == null) {return;}

	tested.addAll(selected.effects);

	for (String combo : AlchemyData.combos(selectedName)) {
	    Ingredient ingredient = AlchemyData.ingredient(combo);
	    if(ingredient == null) {continue;}
	    tested.addAll(ingredient.effects);
	}

	for (Effect effect : AlchemyData.effects()) {
	    if(tested.contains(effect)) {continue;}
	    untested.add(effect);
	}
    }

    @Override
    public void dispose() {
	selected = null;
	selectedName = null;
	super.dispose();
    }

    private static class IngredientList extends FilteredListBox<String> {
	private final NamesProvider nameProvider;
	private final Consumer<String> onChanged;
	private boolean dirty = true;
	private boolean init = false;

	public IngredientList(NamesProvider nameProvider, Consumer<String> onChanged) {
	    super(AlchemyWnd.LIST_W, AlchemyWnd.ITEMS, AlchemyWnd.ITEM_H);
	    this.nameProvider = nameProvider;
	    this.onChanged = onChanged;
	    bgcolor = AlchemyWnd.BGCOLOR;
	    listen(AlchemyData.INGREDIENTS_UPDATED, this::update);
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
		String selected = sel;
		List<String> tmp = AlchemyData.ingredients();
		tmp.sort(nameProvider::compare);
		setItems(tmp);
		dirty = false;
		if(!init) {
		    init = true;
		    selected = LAST_SELECTED_INGREDIENT;
		}
		change(selected);
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
	protected boolean match(String item, String text) {
	    if(text == null || text.isEmpty()) {return true;}

	    final String filter = text.toLowerCase();
	    if(nameProvider.name(item).toLowerCase().contains(filter)) {
		return true;
	    }
	    Ingredient ingredient = AlchemyData.ingredient(item);
	    if(ingredient == null) {return false;}
	    return ingredient.effects.stream().anyMatch(e -> e.matches(filter));
	}

	@Override
	protected void drawitem(GOut g, String item, int i) {
	    g.image(nameProvider.tex(item), Coord.z);
	}
    }

    private static class InfoList extends Listbox<Effect> {
	private static final Tex MARK_X = Resource.loadtex("gfx/hud/mark-x");
	private static final Coord MARK_C = Coord.of(0, UI.scale(1));
	private static final Coord NAME_C = Coord.of(AlchemyWnd.ITEM_H, 0);
	public static final Comparator<Effect> COMPARATOR = Comparator.comparing(Effect::order)
	    .thenComparing(Effect::type)
	    .thenComparing(Effect::name);

	private final List<Effect> items = new ArrayList<>();
	private final NamesProvider nameProvider;

	public Collection<Effect> check = null;

	public InfoList(NamesProvider nameProvider) {
	    super(AlchemyWnd.CONTENT_W, AlchemyWnd.ITEMS - 2, AlchemyWnd.ITEM_H);
	    bgcolor = AlchemyWnd.BGCOLOR;
	    this.nameProvider = nameProvider;
	}

	private void setItems(Collection<Effect> items) {
	    this.items.clear();
	    this.items.addAll(items);
	    this.items.sort(COMPARATOR);
	}

	@Override
	protected void itemclick(Effect item, Coord c, int button) {
	    //TODO: implement ordering toggles
	}

	@Override
	protected Effect listitem(int i) {
	    return items.get(i);
	}

	@Override
	protected int listitems() {
	    return items.size();
	}

	@Override
	protected void drawitem(GOut g, Effect item, int i) {
	    if(check != null) {
		if(check.contains(item)) {
		    g.image(CheckBox.smark, MARK_C);
		} else {
		    g.image(MARK_X, MARK_C);
		}
	    }

	    g.image(nameProvider.tex(item), NAME_C);
	}
    }
}

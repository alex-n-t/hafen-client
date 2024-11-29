package me.ender.alchemy;

import haven.*;

import java.util.List;
import java.util.function.Consumer;

class IngredientsWdg extends Widget {
    private static final Coord TOOLTIP_C = Coord.of(AlchemyWnd.LIST_W + AlchemyWnd.GAP, AlchemyWnd.PAD);

    private Ingredient selected = null;
    private Tex image = null;

    IngredientsWdg(NamesProvider nameProvider) {
	Coord p = add(new IngredientList(nameProvider, this::onSelectionChanged), AlchemyWnd.PAD, AlchemyWnd.PAD).pos("br");

	sz = p.addx(AlchemyWnd.GAP + AlchemyWnd.CONTENT_W);
    }

    private void onSelectionChanged(String res) {
	selected = AlchemyData.ingredient(res);

	if(image != null) {image.dispose();}
	image = null;
    }

    @Override
    public void draw(GOut g) {
	super.draw(g);

	if(selected != null && image == null) {
	    image = AlchemyData.tex(selected.effects);
	}

	if(image != null) {
	    g.image(image, TOOLTIP_C);
	}
    }

    @Override
    public void dispose() {
	selected = null;
	if(image != null) {
	    image.dispose();
	    image = null;
	}
	super.dispose();
    }

    private static class IngredientList extends FilteredListBox<String> {
	private final NamesProvider nameProvider;
	private final Consumer<String> onChanged;
	private boolean dirty = true;

	public IngredientList(NamesProvider nameProvider, Consumer<String> onChanged) {
	    super(AlchemyWnd.LIST_W, AlchemyWnd.ITEMS, AlchemyWnd.ITEM_H);
	    this.nameProvider = nameProvider;
	    this.onChanged = onChanged;
	    bgcolor = AlchemyWnd.BGCOLOR;
	    listen(AlchemyData.INGREDIENTS_UPDATED, this::onIngredientsUpdated);
	}

	private void onIngredientsUpdated() {
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
		List<String> tmp = AlchemyData.ingredients();
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
	    g.image(nameProvider.text(item).tex(), Coord.z);
	}
    }
}

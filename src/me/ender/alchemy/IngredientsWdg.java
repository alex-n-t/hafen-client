package me.ender.alchemy;

import haven.*;
import me.ender.ClientUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

class IngredientsWdg extends Widget {
    private static final Coord TOOLTIP_C = Coord.of(AlchemyWnd.LIST_W + AlchemyWnd.GAP, AlchemyWnd.PAD);

    private Ingredient selected = null;
    private Tex image = null;

    IngredientsWdg() {
	Coord p = add(new IngredientList(this::onSelectionChanged), AlchemyWnd.PAD, AlchemyWnd.PAD).pos("br");

	sz = p.addxs(AlchemyWnd.GAP + AlchemyWnd.CONTENT_W);
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
	private final Map<String, RichText> names = new HashMap<>();
	private final Consumer<String> onChanged;
	private boolean dirty = true;

	public IngredientList(Consumer<String> onChanged) {
	    super(AlchemyWnd.LIST_W, AlchemyWnd.ITEMS, AlchemyWnd.ITEM_H);
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
		setItems(AlchemyData.ingredients());
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

	private RichText text(String res) {
	    RichText text = names.getOrDefault(res, null);
	    if(text != null) {return text;}

	    String name = ClientUtils.loadPrettyResName(res);
	    text = RichText.stdfrem.render(String.format("$img[%s,h=16,c] %s", res, name), AlchemyWnd.CONTENT_W);
	    names.put(res, text);
	    return text;
	}

	@Override
	public void dispose() {
	    names.values().forEach(Text::dispose);
	    names.clear();
	    super.dispose();
	}

	@Override
	protected boolean match(String item, String text) {
	    if(text == null || text.isEmpty()) {return true;}

	    final String filter = text.toLowerCase();
	    if(text(item).text.toLowerCase().contains(filter)) {
		return true;
	    }
	    Ingredient ingredient = AlchemyData.ingredient(item);
	    if(ingredient == null) {return false;}
	    return ingredient.effects.stream().anyMatch(e -> e.matches(filter));
	}

	@Override
	protected void drawitem(GOut g, String item, int i) {
	    g.image(text(item).tex(), Coord.z);
	}
    }
}

package me.ender.alchemy;

import haven.*;
import me.ender.ClientUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

class IngredientsWdg extends Widget {
    private static final int LIST_W = UI.scale(150);
    private static final int PAD = UI.scale(5);
    private static final int GAP = UI.scale(25);
    private static final int TOOLTIP_W = UI.scale(250);
    private static final Coord TOOLTIP_C = Coord.of(LIST_W + GAP, PAD);

    private Ingredient selected = null;
    private Tex image = null;

    IngredientsWdg() {
	Coord p = add(new IngredientList(LIST_W, 25, this::onSelectionChanged), PAD, PAD).pos("br");

	sz = p.addxs(GAP + TOOLTIP_W + PAD);
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
	private static final Color BGCOLOR = new Color(0, 0, 0, 64);
	private final Map<String, RichText> names = new HashMap<>();
	private final Consumer<String> onChanged;

	public IngredientList(int w, int h, Consumer<String> onChanged) {
	    super(w, h, UI.scale(16));
	    this.onChanged = onChanged;
	    bgcolor = BGCOLOR;
	    update();
	}

	@Override
	public void changed(String item, int index) {
	    onChanged.accept(item);
	}

	private void update() {
	    setItems(AlchemyData.ingredients());
	}

	private RichText text(String res) {
	    RichText text = names.getOrDefault(res, null);
	    if(text != null) {return text;}

	    String name = ClientUtils.prettyResName(Resource.remote().loadwait(res));
	    text = RichText.stdfrem.render(String.format("$img[%s,h=16,c] %s", res, name), TOOLTIP_W);
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

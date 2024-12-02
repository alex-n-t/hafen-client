package me.ender.alchemy;

import haven.*;
import me.ender.ClientUtils;

class TrackWnd extends WindowX implements DTarget {
    private final Label label;
    Tex image;
    String res = null;
    private boolean trackEffects;
    private final Coord IMG_C;

    private TrackWnd() {
	super(Coord.of(AlchemyWnd.LIST_W, UI.scale(32)), "Ingredient Track");
	justclose = true;
	label = new Label("");
	IMG_C = add(label).pos("bl");
	updateLabel();

	listen(AlchemyData.COMBOS_UPDATED, this::update);
	listen(AlchemyData.EFFECTS_UPDATED, this::update);
    }

    public static void track(UI ui, String target, boolean trackEffects, boolean update, NamesProvider namesProvider) {
	TrackWnd tracker = ui.gui.getchild(TrackWnd.class);

	if(tracker == null) {
	    if(update) {return;}
	    tracker = ui.gui.add(new TrackWnd(), ClientUtils.getScreenCenter(ui));
	}

	if(tracker.trackEffects != trackEffects) {
	    tracker.trackEffects = trackEffects;
	    tracker.updateLabel();
	}

	if(target == null) {
	    tracker.highlight(null, null);
	} else {
	    tracker.highlight(target, namesProvider.tex(target));
	}
    }

    private void updateLabel() {
	if(trackEffects) {
	    label.settext("Highlight untested effects for:");
	} else {
	    label.settext("Highlight untested combinations for:");
	}
    }

    @Override
    public boolean drop(Drop ev) {
	AlchemyData.process(ev.src.item, !CFG.ALCHEMY_LIMIT_RECIPE_SAVE.get());
	return true;
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
	} else if(trackEffects) {
	    GItem.setAlchemyFilter(new EffectFilter(AlchemyData.testedEffects(res), AlchemyData.combos(res)));
	} else {
	    GItem.setAlchemyFilter(new ComboFilter(AlchemyData.combos(res)));
	}
    }
}

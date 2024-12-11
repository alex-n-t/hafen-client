package me.ender.alchemy;

import haven.*;
import me.ender.ui.CFGBox;
import me.ender.ui.TabStrip;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class AlchemyWnd extends WindowX implements DTarget {
    public static final int PAD = UI.scale(5);
    static final int LIST_W = UI.scale(180);
    static final int GAP = UI.scale(25);
    static final int ITEMS = 22;
    static final int ITEM_H = UI.scale(16);
    static final int CONTENT_W = UI.scale(280);
    static final Tex MARK_X = Resource.loadtex("gfx/hud/mark-x");
    public static String LAST_SELECTED_INGREDIENT;
    public static final Coord WND_SZ = Coord.of(2 * PAD + LIST_W + GAP + CONTENT_W, ITEM_H * ITEMS);
    static final Color BGCOLOR = new Color(0, 0, 0, 96);
    private final List<AlchemyWdg> tabs = new LinkedList<>();
    private final TabStrip<AlchemyWdg> strip;

    public AlchemyWnd() {
	super(WND_SZ, "Alchemy");

	addtwdg(new IButton("gfx/hud/btn-help", "", "-d", "-h", this::showHelp).settip("Help"));

	NamesProvider namesProvider = new NamesProvider(LIST_W);
	disposables.add(namesProvider);

	strip = add(new TabStrip<>(this::onTabSelected));

	tabs.add(strip.insert(add(new IngredientsWdg(namesProvider)), null, "Effects", null).tag);
	tabs.add(strip.insert(add(new ComboWdg(namesProvider)), null, "Combos", null).tag);
	tabs.add(strip.insert(add(new RecipesWdg()), null, "Recipes", null).tag);

	Coord p = strip.pos("bl").addys(5);
	for (Widget tab : tabs) {
	    tab.c = Coord.of(p);
	}

	p = Coord.of(LIST_W + GAP + CONTENT_W + PAD, 0);
	p = adda(new CFGBox("Auto process", CFG.ALCHEMY_AUTO_PROCESS, "While Alchemy or Ingredient Track window is open all ingredients with known effects and exixirs with known recipes you see would be recorded"), p, 1,0).pos("ul");
	adda(new CFGBox("Limit recipe storing", CFG.ALCHEMY_LIMIT_RECIPE_SAVE, "Will save recipe only if elixir is dropped with Recipes tab open"), p.addx(-GAP), 1, 0);
    }

    @Override
    protected void attached() {
	super.attached();
	strip.select(CFG.ALCHEMY_LAST_TAB.get());
    }

    private void onTabSelected(Widget selected) {
	for (AlchemyWdg tab : tabs) {
	    if(tab == selected) {
		tab.select(LAST_SELECTED_INGREDIENT);
		tab.show();
	    } else {
		tab.hide();
	    }
	}
	pack();
    }

    @Override
    public Coord contentsz() {
	return super.contentsz().add(PAD, PAD);
    }

    @Override
    public boolean drop(Drop ev) {
	boolean storeRecipe = !CFG.ALCHEMY_LIMIT_RECIPE_SAVE.get() || strip.getSelectedButtonIndex() == 1;
	AlchemyData.process(ev.src.item, storeRecipe);
	return true;
    }

    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if((sender == this) && msg.equals("close")) {
	    close();
	    return;
	}
	super.wdgmsg(sender, msg, args);
    }

    @Override
    public void dispose() {
	ui.gui.alchemywnd = null;
	CFG.ALCHEMY_LAST_TAB.set(strip.getSelectedButtonIndex());
	super.dispose();
    }

    public void showHelp() {
	HelpWnd.show(ui, "halp/alchemy_wnd");
    }
}

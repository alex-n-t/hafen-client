package me.ender.alchemy;

import haven.*;
import me.ender.ui.TabStrip;

import java.util.LinkedList;
import java.util.List;

public class AlchemyWnd extends WindowX implements DTarget {
    public static final Coord WND_SZ = UI.scale(500, 600);
    private final List<Widget> tabs = new LinkedList<>();

    public AlchemyWnd() {
	super(WND_SZ, "Alchemy");

	TabStrip<Widget> strip = add(new TabStrip<>(this::onTabSelected));

	tabs.add(strip.insert(add(new IngredientsWdg()), null, "Ingredients", null).tag);
	tabs.add(strip.insert(add(new RecipesWdg()), null, "Recipes", null).tag);

	Coord p = strip.pos("bl").addys(5);
	for (Widget tab : tabs) {
	    tab.c = Coord.of(p);
	}
	strip.select(0);
    }

    private void onTabSelected(Widget selected) {
	for (Widget tab : tabs) {
	    tab.show(tab == selected);
	}
    }

    @Override
    public boolean drop(Drop ev) {
	AlchemyData.categorize(ev.src.item);
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

    public void close() {
	ui.destroy(this);
	ui.gui.alchemywnd = null;
    }

    @Override
    public void dispose() {
	super.dispose();
    }
}

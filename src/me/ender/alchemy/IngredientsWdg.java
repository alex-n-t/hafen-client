package me.ender.alchemy;

import haven.Label;
import haven.Widget;

class IngredientsWdg extends Widget {

    IngredientsWdg() {
	add(new Label("INGREDIENTS"));
	pack();
    }
    
    @Override
    public void destroy() {
	super.destroy();
    }

    @Override
    public void dispose() {
	super.dispose();
    }
}

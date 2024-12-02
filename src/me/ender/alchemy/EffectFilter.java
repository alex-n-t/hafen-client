package me.ender.alchemy;

import haven.GItem;
import haven.ItemInfo;

import java.util.HashSet;
import java.util.Set;

public class EffectFilter implements IAlchemyItemFilter {
    private final Set<Effect> testedEffects;
    private final Set<String> testedIngredients;

    public EffectFilter(Set<Effect> effects, Set<String> ingredients) {
	this.testedEffects = effects;
	this.testedIngredients = ingredients;
    }

    @Override
    public boolean matches(GItem item) {
	if(testedIngredients.contains(item.resname())) {return false;}
	
	Set<Effect> effects = new HashSet<>();

	for (ItemInfo info : item.info()) {
	    AlchemyData.tryAddIngredientEffect(effects, info);
	}

	return !testedEffects.containsAll(effects);
    }
}

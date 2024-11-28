package me.ender.alchemy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Ingredient {
    final public Collection<Effect> effects;
    //TODO: add tried combos list
    //TODO: add sorting to effects

    public Ingredient(Collection<Effect> effects) {this.effects = effects;}

    public Ingredient(Collection<Effect> effects, Ingredient base) {
	this(combine(effects, base));
    }

    private static Collection<Effect> combine(Collection<Effect> effects, Ingredient base) {
	if(base == null || base.effects == null || base.effects.isEmpty()) {return effects;}

	Set<Effect> result = new HashSet<>(effects);
	result.addAll(base.effects);

	return result;
    }
}

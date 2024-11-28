package me.ender.alchemy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Ingredient {
    final public Collection<String> effects;
    //TODO: add tried combos list
    //TODO: add sorting to effects

    public Ingredient(Collection<String> effects) {this.effects = effects;}

    public Ingredient(Collection<String> effects, Ingredient base) {
	this(combine(effects, base));
    }

    private static Collection<String> combine(Collection<String> effects, Ingredient base) {
	if(base == null || base.effects == null || base.effects.isEmpty()) {return effects;}

	Set<String> result = new HashSet<>(effects);
	result.addAll(base.effects);

	return result;
    }
}

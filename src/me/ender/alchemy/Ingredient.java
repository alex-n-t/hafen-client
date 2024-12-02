package me.ender.alchemy;

import java.util.Collection;
import java.util.HashSet;

public class Ingredient {
    final public Collection<Effect> effects;

    public Ingredient(Collection<Effect> effects, Ingredient base) {
	if(base == null) {
	    this.effects = effects;
	    return;
	}
	this.effects = new HashSet<>();

	//add new effects with position data
	for (Effect effect : effects) {
	    if(effect.opt != null) {this.effects.add(effect);}
	}

	//add effects from base that have position data
	for (Effect effect : base.effects) {
	    if(effect.opt != null && effects.contains(effect)) {this.effects.add(effect);}
	}

	//add remaining effects
	this.effects.addAll(effects);
    }
}

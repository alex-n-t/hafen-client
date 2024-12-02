package me.ender.alchemy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Ingredient {
    final public Collection<Effect> effects;

    public Ingredient(Collection<Effect> effects) {this.effects = effects;}
}

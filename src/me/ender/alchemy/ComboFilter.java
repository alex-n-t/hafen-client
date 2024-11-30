package me.ender.alchemy;

import java.util.Set;

public class ComboFilter {
    private final Set<String> tested;

    public ComboFilter(Set<String> tested) {this.tested = tested;}

    public boolean matches(String res) {
	return AlchemyData.allIngredients().contains(res) && !tested.contains(res);
    }
}

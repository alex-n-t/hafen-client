package me.ender.alchemy;

import haven.GItem;

import java.util.Set;

public class ComboFilter implements IAlchemyItemFilter {
    private final Set<String> tested;

    public ComboFilter(Set<String> tested) {this.tested = tested;}

    public boolean matches(GItem item) {
	String res = item.resname();
	return AlchemyData.allIngredients().contains(res) && !tested.contains(res);
    }
}

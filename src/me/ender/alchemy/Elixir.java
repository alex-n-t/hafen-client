package me.ender.alchemy;

import haven.Resource;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class Elixir {
    private static final boolean INCLUDE_NUMBERS = false; //TODO: add an option for this
    final public Recipe recipe;
    final public Collection<String> effects;

    public Elixir(Recipe recipe, Collection<String> effects) {
	this.recipe = recipe;
	this.effects = effects;
    }

    @Override
    public boolean equals(Object obj) {
	if(!(obj instanceof Elixir)) {return false;}
	Elixir other = (Elixir) obj;
	return Objects.equals(recipe, other.recipe);
    }

    @Override
    public int hashCode() {
	return recipe.hashCode();
    }

    private String alchemyEffects() {
	return effects.stream().map(Elixir::formatEffect).collect(Collectors.joining("\\n"));
    }

    private static String formatEffect(String effect) {
	String[] parts = effect.split(":");
	if(parts.length < 2) {return effect;}
	String res = parts[1];
	String type = parts[0];
	int v = getValue(parts);
	String name = res;
	try {
	    name = Resource.remote().loadwait(res).layer(Resource.tooltip).t;
	} catch (Exception ignore) {}

	String prefix = "";
	switch (type) {
	    case "heal":
		prefix = "+";
		break;
	    case "buff":
		prefix = (v > 1 ? "^^" : "^");
		if(INCLUDE_NUMBERS && v > 0) {
		    prefix += 10 * v + " ";
		}
		break;
	    case "time":
		prefix = "%";
		break;
	    case "wound":
		prefix = "-";
		if(INCLUDE_NUMBERS && v > 0) {prefix += v + " ";}
		break;
	}
	return prefix + name;
    }

    private static int getValue(String[] parts) {
	if(parts.length < 3) {return 0;}
	try {
	    return Integer.parseInt(parts[2]);
	} catch (Exception ignore) {}
	return 0;
    }

    /**
     * Returns formatted URL for <a href="https://yoda-magic.github.io/alchemygraph">Yoda's Alchemy Graph</a> site.
     */
    public String toAlchemyUrl() {
	//TODO: escape " symbols in the alchemy info?
	String json = String.format("{\"f\":\"%s\", \"e\":\"%s\",\"ver\":1}", recipe.toAlchemy(), alchemyEffects());
	String data = new String(Base64.getEncoder().encode(json.getBytes(StandardCharsets.UTF_16LE)));
	return String.format("https://yoda-magic.github.io/alchemygraph/#%s", data);
    }
}

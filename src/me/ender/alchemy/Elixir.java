package me.ender.alchemy;

import haven.Resource;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class Elixir {
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
		//TODO: parse magnitude to use ^^ for bigger bonuses
		prefix = "^";
		break;
	    case "time":
		prefix = "%";
		break;
	    case "wound":
		//TODO: parse wound magnitude
		prefix = "-";
		break;
	}
	return prefix + name;
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

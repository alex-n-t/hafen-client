package me.ender.alchemy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class Elixir {
    final public Recipe recipe;
    final public Collection<Effect> effects;

    public Elixir(Recipe recipe, Collection<Effect> effects) {
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
	return effects.stream().map(Effect::format).collect(Collectors.joining("\\n"));
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

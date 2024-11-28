package me.ender.alchemy;

import com.google.gson.annotations.SerializedName;
import haven.Resource;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Recipe {
    final String res;
    @SerializedName("sub")
    final List<Recipe> ingredients;

    public Recipe(haven.res.ui.tt.alch.recipe.Recipe.Spec input) {
	this(input.res.res.get().name, input.inputs.isEmpty() ? null : from(input.inputs));
    }

    public Recipe(String res, List<Recipe> ingredients) {
	this.res = res;
	this.ingredients = ingredients;
    }

    public static Recipe from(String res, haven.res.ui.tt.alch.recipe.Recipe recipe) {
	List<haven.res.ui.tt.alch.recipe.Recipe.Spec> inputs = recipe.inputs;
	if(res.contains("/jar-elixir")) {
	    if(inputs.size() == 2) {
		res = "paginae/craft/herbalswill";
	    } else if(inputs.size() == 3) {
		String last = inputs.get(2).res.res.get().name;
		if(AlchemyData.isMineral(last)) {
		    res = "paginae/craft/mercurialelixir";
		} else {
		    res = "paginae/craft/mushroomdecoction";
		}
	    }
	}

	return new Recipe(res, from(inputs));
    }

    public static List<Recipe> from(List<haven.res.ui.tt.alch.recipe.Recipe.Spec> inputs) {
	return inputs.stream().map(Recipe::new).collect(Collectors.toList());
    }

    @Override
    public String toString() {
	String name = Resource.remote().loadwait(res).layer(Resource.tooltip).t;
	if(ingredients == null || ingredients.isEmpty()) {
	    return String.format("<%s>", name);
	}

	return String.format("<%s:[%s]>", name, ingredients.stream().map(Object::toString).collect(Collectors.joining(", ")));
    }

    /**
     * Returns recipe string for <a href="https://yoda-magic.github.io/alchemygraph">Yoda's Alchemy Graph</a> site.
     */
    public String toAlchemy() {
	String name = Resource.remote().loadwait(res).layer(Resource.tooltip).t;
	if(ingredients == null || ingredients.isEmpty()) {
	    return name;
	}

	String sub = ingredients.stream().map(Recipe::toAlchemy).collect(Collectors.joining(","));

	String method = name;
	if(res.contains("/herbalswill")) {
	    method = "herb";
	} else if(res.contains("/mushroomdecoction")) {
	    method = "mush";
	} else if(res.contains("/mercurialelixir")) {
	    method = "merc";
	} else if(res.contains(AlchemyData.HERBAL_GRIND)) {
	    method = "hg";
	} else if(res.contains(AlchemyData.LYE_ABLUTION)) {
	    method = "la";
	} else if(res.contains(AlchemyData.MINERAL_CALCINATION)) {
	    method = "mc";
	} else if(res.contains(AlchemyData.MEASURED_DISTILLATE)) {
	    method = "md";
	} else if(res.contains(AlchemyData.FIERY_COMBUSTION)) {
	    method = "fc";
	}

	return String.format("%s(%s)", method, sub);
    }

    @Override
    public boolean equals(Object obj) {
	if(!(obj instanceof Recipe)) {return super.equals(obj);}

	Recipe other = (Recipe) obj;
	return Objects.equals(res, other.res)
	    && Objects.equals(ingredients, other.ingredients);
    }

    @Override
    public int hashCode() {
	return 13 * res.hashCode() + 7 * (ingredients == null ? 0 : ingredients.hashCode());
    }
}

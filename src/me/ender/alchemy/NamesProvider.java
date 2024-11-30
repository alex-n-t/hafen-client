package me.ender.alchemy;

import haven.Disposable;
import haven.RichText;
import haven.Text;
import me.ender.ClientUtils;

import java.util.HashMap;
import java.util.Map;

public class NamesProvider implements Disposable {

    private final Map<String, RichText> texts = new HashMap<>();
    private final Map<String, String> names = new HashMap<>();
    private final int width;

    public NamesProvider(int width) {
	this.width = width;
    }

    public String name(String res) {
	return names.computeIfAbsent(res, ClientUtils::loadPrettyResName);
    }

    public RichText text(String res) {
	return texts.computeIfAbsent(res, this::render);
    }

    private RichText render(String res) {
	String name = name(res);
	try {
	    return RichText.stdfrem.render(String.format("$img[%s,h=16,c] %s", res, name), width);
	} catch (Exception ignore) {
	}
	return RichText.stdfrem.render(String.format("$img[gfx/invobjs/missing,h=16,c] %s", name), width);
    }

    public int compare(String o1, String o2) {
	return name(o1).compareTo(name(o2));
    }

    @Override
    public void dispose() {
	names.clear();
	texts.values().forEach(Text::dispose);
	texts.clear();
    }
}

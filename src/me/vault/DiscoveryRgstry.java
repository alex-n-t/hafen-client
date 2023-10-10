package me.vault;

import java.util.HashSet;
import java.util.Set;

import haven.Gob;
import haven.GobTag;
import haven.OCache;
import haven.rx.Reactor;

public class DiscoveryRgstry {
	public static DiscoveryRgstry INSTANCE = new DiscoveryRgstry();
	private Set<String> discovered;
	private OCache oc;

	public DiscoveryRgstry() {
		discovered = new HashSet<String>();
		Reactor.FLOWER_CHOICE.subscribe(
			choice->{
				try {
				if (choice.target.gob!=null && choice.opt != null && choice.target.gob.getres() != null 
						&& choice.target.gob.anyOf(GobTag.TREE,GobTag.BUSH) 
						&& (choice.opt.contains("Pick") || choice.opt.contains("Clear"))) 
					discover(choice.target.gob.getres().name);
				} catch(Exception ignore) {}
			}
		);
	}
	
	public void discover(String name) {
		if (!discovered.contains(name)) {
			if (name!= null) discovered.add(name);
			updateDiscoverableGobs(name);
		}
	}
	
	public void updateDiscoverableGobs(String name) {
		oc.stream().filter(g->g.anyOf(GobTag.TREE,GobTag.BUSH) && (name == null || name.equals(g.getres().name))).forEach(Gob::infoUpdated);
	}
	
	public boolean discovered(String name) {
		return INSTANCE.discovered.contains(name);
	}
	
	public void setOC(OCache oc) {
		this.oc = oc;
	}

}

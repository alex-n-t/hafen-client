package me.vault;

import java.util.HashSet;
import java.util.Set;

import auto.GobTarget;
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
				Gob gob = null;
				if (!(choice.target instanceof GobTarget)) return;
				if ((gob = ((GobTarget)choice.target).gob) == null) return;
				try {
				if (choice.opt != null && gob.resid() != null 
						&& gob.anyOf(GobTag.TREE,GobTag.BUSH) 
						&& (choice.opt.contains("Pick") || choice.opt.contains("Clear"))) 
					discover(gob.resid());
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
	
	public void updateDiscoverableGobs(String id) {
		oc.stream().filter(g->g.anyOf(GobTag.TREE,GobTag.BUSH) && (id == null || id.equals(g.resid()))).forEach(Gob::infoUpdated);
	}
	
	public boolean discovered(String name) {
		return INSTANCE.discovered.contains(name);
	}
	
	public void setOC(OCache oc) {
		this.oc = oc;
	}

}

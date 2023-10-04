package me.vault;

import java.util.Set;

import me.vault.dao.DiscoveryRgstryDao;

public class DiscoveryRgstry {
	
	public static DiscoveryRgstry INSTANCE = new DiscoveryRgstry();
	
	private Set<String> discovered;

	public DiscoveryRgstry() {
		initialize();
	}
	
	public void discover(String name) {
		if (discovered.contains(name)) return;
		updateGobs(name);
		discovered.add(name);
	}
	
	private void initialize(){
		discovered = DiscoveryRgstryDao.INSTANCE.getDiscoveryData("");
	}
	
	private void updateGobs(String name) {
		return;
	}

}

package me.vault.dao;

import java.util.HashSet;
import java.util.Set;

public class DiscoveryRgstryDao extends VaultDao {
	private static final String SQL_SCHEMA = "create table if not exists discovery_fact("
			+ "	fact_seqno 	integer primary key autoincrement,"
			+ " pcName 		text,"
			+ " name 		text,"
			+ " unique (pcName, name));";

	public static final DiscoveryRgstryDao INSTANCE = new DiscoveryRgstryDao(SQL_SCHEMA);
	private DiscoveryRgstryDao(String ddl) {
		super(ddl);
	}
	
	public Set<String> getDiscoveryData(String pcName){
		return new HashSet<String>();
	}
	
	public void put(String pcName, String name) {
		return;
	}
}

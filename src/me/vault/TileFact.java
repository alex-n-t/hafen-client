package me.vault;

import haven.Coord;

public class TileFact {
	public long gridId;
	public Coord inGridTC;
	public String data;
	public String type;

	public TileFact(String type, long gridId, Coord inGridTC, String data){
		this.type = type;
		this.gridId = gridId;
		this.inGridTC = inGridTC;
		this.data = data;
	}

}

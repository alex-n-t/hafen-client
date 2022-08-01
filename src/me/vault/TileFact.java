package me.vault;

import java.util.Objects;

import haven.Coord;

public class TileFact {
	public String type;
	public long gridId;
	public Coord inGridTC;
	public String data;
	public String item;
	public double quality;

	public TileFact(String type, long gridId, Coord inGridTC, String data, String item, Double quality){
		this.type = type;
		this.gridId = gridId;
		this.inGridTC = inGridTC;
		this.data = data;
		this.item = item;
		this.quality = quality;
	}

	public TileFact(String type, long gridId, Coord inGridTC, String data){
		this(type,gridId,inGridTC,data,null,null);
	}
	
	
	public String toString() {
		return "{"
				+ "gridId:"		+gridId
				+ ",inGridTC:"	+inGridTC
				+ ",type:"		+type
				+ ",data:"		+data
				+ ",item:"		+item
				+ ",quality:"	+quality
				+ "}";
	}
	
	public String getMatchingKey() {
		//key to differentiate or aggregate facts of the same type within the same tile
		return item;
	}
	
}

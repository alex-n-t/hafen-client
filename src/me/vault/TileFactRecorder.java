package me.vault;

import haven.Coord;
import haven.GItem;
import haven.Gob;
import haven.MapView;
import haven.QualityList;
import me.vault.PlayerActivityInfo.ActionType;
import me.vault.dao.TileFactDao;

public class TileFactRecorder {
	private static final Coord[] offset = {Coord.right, Coord.down, Coord.left, Coord.up}; 
			
	public static void addInventoryItem(MapView map, GItem item) {
		Gob pl = map.player();
		if (pl == null) return;
		PlayerActivityInfo info = pl.getattr(PlayerActivityInfo.class);
		if(info == null) return;
		synchronized (info) {
			switch(info.lastAction.what) {
			case MINING: 	registerMiningProduct(info.lastAction, item);	break;
			case FORAGING:	
			case CHIPPING: 	
			case CHOPPING:
			case DIGGING:	
			case FISHING:	registerActionProduct(info.lastAction, item); break;
			default: info.addPendingItem(item);	break; //no productive action, but hold onto the item for a bit in case action starts later, e.g. HARVESTING
			}
		}
	}
	
	private static void registerActionProduct(PlayerActivityInfo.Action action, GItem item){
		try {
			QualityList l = item.itemq.get(); //TODO: use ItemInfo list
			Coord cc = action.where; 
			String longName = item.getres().name;
			String shortName = longName.substring(longName.lastIndexOf('/')+1);
			TileFact fact = new TileFact(action.what.name()+".Item",action.whereGridID,cc,getPlayerInfo(ActionType.MINING),shortName,l.single().value);
			TileFactDao.tileFactDao.put(fact);
		} catch(Exception e) {
			item.infoCallback = itm->{registerActionProduct(action, itm); itm.infoCallback = null;};
		}
	}
	
	private static void registerMiningProduct(PlayerActivityInfo.Action action, GItem item){
		try {
			QualityList l = item.itemq.get();
			int quadrant = (int)Math.round(action.whereAngle / Math.PI * 2) % 4;
			Coord cc = action.where.add(offset[quadrant]); //TODO: mining into adjacent grid (currently results in -1 or 101)
			String longName = item.getres().name;
			String shortName = longName.substring(longName.lastIndexOf('/')+1);
			TileFact fact = new TileFact(action.what.name()+".Item",action.whereGridID,cc,getPlayerInfo(ActionType.MINING),shortName,l.single().value);
			TileFactDao.tileFactDao.put(fact);
		} catch(Exception e) {
			item.infoCallback = itm->{registerMiningProduct(action, itm); itm.infoCallback = null;};
		}
	}

	private static String getPlayerInfo(PlayerActivityInfo.ActionType action) {
		return "playerInfo:{}";
	}

}

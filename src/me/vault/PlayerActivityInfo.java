package me.vault;

import java.util.*;
import java.util.stream.Collectors;

import haven.Composite;
import haven.Coord;
import haven.GAttrib;
import haven.GItem;
import haven.Gob;
import haven.MCache;
import haven.MCache.LoadingMap;
import me.ender.Reflect;

public class PlayerActivityInfo extends GAttrib {
	public Action lastAction;
	public GItem pendingItem;
	public long pendingItemTime;
	
	private static long msPendingItemTimeout = 500;
	
	public enum ActionType{MINING, CHOPPING, DIGGING, CHIPPING, FORAGING, FISHING, CRAFTING, IDLE, MOVING, UNKNOWN}
	
	class Action{
		ActionType what;
		Coord where;
		double whereAngle;
		long whereGridID;
		long when;
	}

	Map<ActionType,Action> actionLog = new HashMap<>();
	
	public PlayerActivityInfo(Gob gob) {
		super(gob);
		skipRender = true;
	}
	
	@Override
	public void ctick(double dt) {
		if(!(gob.drawable instanceof Composite)) return;
		List<String> poses = ((Composite)gob.drawable).getPoses();
		List<ActionType> actions = poses.stream().map(this::toActionType).filter(a -> a != ActionType.UNKNOWN).collect(Collectors.toList()); 
		if(actions.size() == 0 || lastAction != null && actions.contains(lastAction.what)) return;
		for(ActionType what : actions) {//new action! (and hopefully only one)
			System.out.println(what);
			Action action = new Action();
			action.what = what;
			action.where = gob.rc.floor(MCache.tilesz).mod(MCache.cmaps);
			action.whereAngle = gob.a;
			try{action.whereGridID = gob.glob.map.getgrid(gob.rc.floor(MCache.tilesz).div(MCache.cmaps)).id;}
			catch(LoadingMap e) {}
			action.when = System.currentTimeMillis();
			actionLog.put(action.what, action);
			lastAction = action;
			if(pendingItem != null) {
				if(action.what == ActionType.FORAGING && System.currentTimeMillis() - pendingItemTime < msPendingItemTimeout) {
					TileFactRecorder.addInventoryItem(pendingItem.ui.gui.map, pendingItem);
					pendingItem = null;
				} else if(System.currentTimeMillis() - pendingItemTime > msPendingItemTimeout) {
					pendingItem = null;
				}
			}
		}
	}
	
	public ActionType toActionType(String pose) {
		switch(pose) {
		case "gfx/borka/fishidle":
			return ActionType.FISHING;
		case "gfx/borka/treepickan":
		case "gfx/borka/bushpickan":
		case "gfx/borka/harvesting":
			return ActionType.FORAGING;
		case "gfx/borka/shoveldig":
		case "gfx/borka/dig":
			return ActionType.DIGGING;
		case "gfx/borka/pickan":	
		case "gfx/borka/choppan":
			return ActionType.MINING;
		case "gfx/borka/chipping":
			return ActionType.CHIPPING;
		case "gfx/borka/treechop":
			return ActionType.CHOPPING;
		case "gfx/borka/craftan":
			return ActionType.CRAFTING;
		case "gfx/borka/pointhome":
		case "gfx/borka/pointconfused":
		case "gfx/borka/idle":
			return ActionType.IDLE;
		case "gfx/borka/wading":
		case "gfx/borka/walking":
		case "gfx/borka/running":
			return ActionType.MOVING;
		default:
			return ActionType.UNKNOWN;
		}
	}
	
	public void addPendingItem(GItem item) {
		if(pendingItem != item) {
			pendingItem = item;
			pendingItemTime = System.currentTimeMillis();
		}
	}
}

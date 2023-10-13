package auto;

import haven.*;
import me.vault.TileFact;
import me.vault.dao.TileFactDao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

public class MineUtil {

//    public static final Set<String> ROCKS = Set.of("alabaster", "apatite", "arkose", "basalt", "breccia", "diabase", "diorite", "dolomite", "eclogite", "feldspar", "flint", "fluorospar",
//	"gabbro", "gneiss", "granite", "greenschist", "hornblende", "kyanite", "limestone", "marble", "mica", "microlite", "olivine", "orthoclase",
//	"pegmatite", "porphyry", "pumice", "quartz", "rhyolite", "sandstone", "schist", "slate", "soapstone", "sodalite", "zincspar", "jasper",
//	"serpentine", "chert", "graywacke", "quarryquartz", "catgold", "slag", "dross");
//    public static final Set<String> ORES = Set.of("argentite", "cassiterite", "chalcopyrite", "cinnabar", "galena", "hematite", "hornsilver", "ilmenite", "leadglance", "cuprite", "limonite", "magnetite", "malachite", "nagyagite", "petzite", "sylvanite");
//    public static final Set<String> AUX_ROCKS = Set.of("quarryquartz", "catgold");
    public static final Map<String, Double> SUPPORT_RADIUS = new HashMap<>();
    static {
    	SUPPORT_RADIUS.put("gfx/terobjs/ladder", 		100d);
    	SUPPORT_RADIUS.put("gfx/terobjs/minesupport", 	100d);
    	SUPPORT_RADIUS.put("gfx/terobjs/trees/towercap",100d);
    	SUPPORT_RADIUS.put("gfx/terobjs/column", 		125d);
    	SUPPORT_RADIUS.put("gfx/terobjs/minebeam", 		150d);
    };
    
//    static {
//	try {
//	    CFG.connection.createStatement().execute("CREATE TABLE IF NOT EXISTS mine_fact(grid_id INTEGER, x INTEGER, y INTEGER, map_id INTEGER DEFAULT 0, p_x INTEGER DEFAULT NULL, p_y INTEGER DEFAULT NULL, " +
//		"updated INTEGER, stone TEXT DEFAULT NULL, dust INTEGER DEFAULT 0, quality INTEGER DEFAULT NULL, capped INTEGER DEFAULT 0, mined INTEGER DEFAULT 0, PRIMARY KEY (grid_id, x, y))");
//	} catch(SQLException e) {
//	    throw new RuntimeException(e);
//	}
//    }

//    public static void storeStoneType(long gridId, Coord lc, PTile tile, String stone) {
//	try {
//	    PreparedStatement stmt = CFG.connection.prepareStatement("INSERT INTO mine_fact(grid_id,x,y,map_id,p_x,p_y,updated,stone) VALUES (?,?,?,?,?,?,unixepoch(),?) " +
//		"ON CONFLICT (grid_id,x,y) DO UPDATE SET map_id=excluded.map_id, p_x=excluded.p_x, p_y=excluded.p_y, stone=excluded.stone, updated=excluded.updated WHERE stone<>excluded.stone OR map_id<>excluded.map_id");
//	    stmt.setLong(1, gridId);
//	    stmt.setInt(2, lc.x);
//	    stmt.setInt(3, lc.y);
//	    stmt.setLong(4, tile.mapId);
//	    stmt.setInt(5, tile.coord.x);
//	    stmt.setInt(6, tile.coord.y);
//	    stmt.setString(7, stone);
//	    stmt.executeUpdate();
//	} catch(SQLException e) {
//	    throw new RuntimeException(e);
//	}
//    }
//
//    public static void storeDust(long gridId, Coord lc, int count) {
//	try {
//	    PreparedStatement stmt = CFG.connection.prepareStatement("INSERT INTO mine_fact(grid_id,x,y,updated,dust) VALUES (?,?,?,unixepoch(),?) ON CONFLICT (grid_id,x,y) DO UPDATE SET dust=excluded.dust, updated=excluded.updated");
//	    stmt.setLong(1, gridId);
//	    stmt.setInt(2, lc.x);
//	    stmt.setInt(3, lc.y);
//	    stmt.setInt(4, count);
//	    stmt.executeUpdate();
//	} catch(SQLException e) {
//	    throw new RuntimeException(e);
//	}
//    }

//    public static void storeQuality(long gridId, Coord lc, int quality, boolean capped) {
//	try {
//	    PreparedStatement stmt = CFG.connection.prepareStatement("INSERT INTO mine_fact(grid_id,x,y,updated,quality,capped) VALUES (?,?,?,unixepoch(),?,?) " +
//		"ON CONFLICT (grid_id,x,y) DO UPDATE SET quality=excluded.quality, capped=excluded.capped, updated=excluded.updated WHERE excluded.quality>quality OR quality IS NULL");
//	    stmt.setLong(1, gridId);
//	    stmt.setInt(2, lc.x);
//	    stmt.setInt(3, lc.y);
//	    stmt.setInt(4, quality);
//	    stmt.setInt(5, capped ? 1 : 0);
//	    stmt.executeUpdate();
//	} catch(SQLException e) {
//	    throw new RuntimeException(e);
//	}
//    }

//    public static void storeMinedTile(long gridId, Coord lc) {
//	try {
//	    PreparedStatement stmt = CFG.connection.prepareStatement("INSERT INTO mine_fact(grid_id,x,y,updated,mined) VALUES (?,?,?,unixepoch(),1) ON CONFLICT (grid_id,x,y) DO UPDATE SET mined=1, updated=excluded.updated");
//	    stmt.setLong(1, gridId);
//	    stmt.setInt(2, lc.x);
//	    stmt.setInt(3, lc.y);
//	    stmt.executeUpdate();
//	} catch(SQLException e) {
//	    throw new RuntimeException(e);
//	}
//    }

    private static final Coord[] GRID_9 = new Coord[]{
	Coord.of(-1, -1), Coord.of(0, -1), Coord.of(1, -1),
	Coord.of(-1, 0), Coord.of(0, 0), Coord.of(1, 0),
	Coord.of(-1, 1), Coord.of(0, 1), Coord.of(1, 1)
    };

    private static final Coord[] GRID_8 = new Coord[]{
	Coord.of(-1, -1), Coord.of(0, -1), Coord.of(1, -1),
	Coord.of(-1, 0), Coord.of(1, 0),
	Coord.of(-1, 1), Coord.of(0, 1), Coord.of(1, 1)
    };

    public static Boolean[][] calculateDangerTiles(GameUI gui, Coord ul, Coord br) {
	Coord sz = br.sub(ul).add(1, 1);
	MCache map = gui.ui.sess.glob.map;
	Map<Long, Pair<Coord, Area>> grids = new HashMap<>();
	Coord tc = new Coord();
	for(tc.x = ul.x; tc.x <= br.x; tc.x++) {
	    for(tc.y = ul.y; tc.y <= br.y; tc.y++) {
		MCache.Grid grid = map.getgridt(tc);
		if(grid != null) {
		    Coord lc = tc.sub(grid.ul);
		    Pair<Coord, Area> area = grids.computeIfAbsent(grid.id, id -> new Pair<>(grid.ul, new Area(Coord.of(lc), Coord.of(lc))));
		    area.b.ul = area.b.ul.min(lc);
		    area.b.br = area.b.br.max(lc);
		}
	    }
	}
	Byte[][] dust = new Byte[sz.x][sz.y];
	Boolean[][] danger = new Boolean[sz.x][sz.y];
	boolean empty = true;
	for(Map.Entry<Long, Pair<Coord, Area>> entry : grids.entrySet()) {
	    Coord offset = entry.getValue().a;
	    Area area = entry.getValue().b;
	    
	    List<TileFact> minedFacts = TileFactDao.INSTANCE.getGrid(entry.getKey(), area.ul, area.br, "MinedStatus");
	    List<TileFact> dustFacts = TileFactDao.INSTANCE.getGrid(entry.getKey(), area.ul, area.br, "DustCount");
	    if (minedFacts.isEmpty() && dustFacts.isEmpty()) continue;
    	
	    empty = false;
	    for(TileFact tf : minedFacts) 
	    	dust[tf.inGridTC.x - (ul.x - offset.x)][tf.inGridTC.y - (ul.y - offset.y)] = 0;
	    for(TileFact tf : dustFacts)
	    	dust[tf.inGridTC.x - (ul.x - offset.x)][tf.inGridTC.y - (ul.y - offset.y)] = Byte.parseByte(tf.data);
	}
	if(empty) {
	    return null;
	}

	TreeSet<Coord> queue = new TreeSet<>(Comparator.<Coord, Byte>comparing(c -> dust[c.x][c.y], Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(Function.identity(), Comparator.naturalOrder()));
	for(int x = 1; x < sz.x - 1; x++) {
	    for(int y = 1; y < sz.y - 1; y++) {
		if(dust[x][y] != null) {
		    queue.add(Coord.of(x, y));
		}
	    }
	}
	Coord t;
	while((t = queue.pollFirst()) != null) {
	    Byte dust_ = dust[t.x][t.y];
	    if(dust_ == null || t.x == 0 || t.x == sz.x - 1 || t.y == 0 || t.y == sz.y - 1) {
		continue;
	    }
	    int dangerTiles = 0;
	    int potentialTiles = 0;
	    for(Coord offset : GRID_8) {
		Boolean danger_ = danger[t.x + offset.x][t.y + offset.y];
		if(danger_ != null && danger_) {
		    dangerTiles++;
		}
		if(danger_ == null || danger_) {
		    potentialTiles++;
		}
	    }
	    if(dangerTiles == dust_) {
		for(Coord offset : GRID_8) {
		    if(danger[t.x + offset.x][t.y + offset.y] == null) {
			danger[t.x + offset.x][t.y + offset.y] = false;
			for(Coord off : GRID_8) {
			    Coord coord = Coord.of(t.x + offset.x + off.x, t.y + offset.y + off.y);
			    if (coord.x > 0 && coord.y > 0 && coord.x < sz.x - 1 && coord.y < sz.y - 1) {
				queue.add(coord);
			    }
			}
		    }
		}
	    } else if(potentialTiles == dust_) {
		for(Coord offset : GRID_8) {
		    if(danger[t.x + offset.x][t.y + offset.y] == null) {
			danger[t.x + offset.x][t.y + offset.y] = true;
			for(Coord off : GRID_8) {
			    Coord coord = Coord.of(t.x + offset.x + off.x, t.y + offset.y + off.y);
			    if (coord.x > 0 && coord.y > 0 && coord.x < sz.x - 1 && coord.y < sz.y - 1) {
				queue.add(coord);
			    }
			}
		    }
		}
	    }
	}
	return danger;
    }
}

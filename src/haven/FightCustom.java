package haven;

import custom.CustomUtil;

import java.awt.Color;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FightCustom {

    public static KeyBinding kb_reaggro = KeyBinding.get("fgt/reaggro", KeyMatch.forchar('R', KeyMatch.M));
    private static final Map<String, MoveInfo> MOVES;

    static {
	MOVES = Stream.of(
	    MoveInfo.restore("artevade", new HashSet<>(Arrays.asList(OpeningType.values())), true),
	    MoveInfo.restore("fdodge", new HashSet<>(Arrays.asList(OpeningType.offbalance)), false),
	    MoveInfo.restore("flex", new HashSet<>(Arrays.asList(OpeningType.dizzy, OpeningType.cornered)), false),
	    MoveInfo.restore("jump", new HashSet<>(Arrays.asList(OpeningType.reeling)), true),
	    MoveInfo.restore("qdodge", new HashSet<>(Arrays.asList(OpeningType.offbalance)), true),
	    MoveInfo.restore("regain", new HashSet<>(Arrays.asList(OpeningType.offbalance, OpeningType.reeling)), true),
	    MoveInfo.restore("sidestep", new HashSet<>(Arrays.asList(OpeningType.dizzy)), true),
	    MoveInfo.restore("watchmoves", new HashSet<>(Arrays.asList(OpeningType.dizzy)), false),
	    MoveInfo.restore("zigzag", new HashSet<>(Arrays.asList(OpeningType.reeling, OpeningType.cornered)), true)
	).collect(Collectors.toMap(move -> move.name, Function.identity()));
    }

    private final Fightsess fs;
    private Map<MoveInfo, Integer> moves;

    public FightCustom(Fightsess fs) {
	this.fs = fs;
    }

    public void resetMoves() {
	moves = null;
    }

    private Map<MoveInfo, Integer> moves() {
	if(moves == null) {
	    Map<MoveInfo, Integer> moves = new HashMap<>();
	    for(int i = 0; i < fs.actions.length; i++) {
		Fightsess.Action action = fs.actions[i];
		if(action == null) {
		    continue;
		}
		String resname = CustomUtil.resname(action.res);
		String name = resname.substring(resname.lastIndexOf('/') + 1);
		MoveInfo move = MOVES.get(name);
		if(move != null) {
		    moves.put(move, i);
		}
	    }
	    this.moves = moves;
	}
	return moves;
    }

    public int[] openings() {
	int[] openings = new int[4];
	for(Buff buff : fs.fv.buffs.children(Buff.class)) {
	    String resname = CustomUtil.resname(buff.res);
	    String name = resname.substring(resname.lastIndexOf('/') + 1);
	    try {
		OpeningType opening = OpeningType.valueOf(name);
		openings[opening.ordinal()] = buff.ameter;
	    } catch(IllegalArgumentException ignored) {
	    }
	}
	return openings;
    }

    public boolean autoRestore() {
	int[] openings = openings();
	int maxOpening = -1;
	for(int i = 0; i < 4; i++) {
	    if(openings[i] >= 5 && (maxOpening < 0 || openings[i] > openings[maxOpening])) {
		maxOpening = i;
	    }
	}
	if(maxOpening < 0) {
	    return false;
	}
	OpeningType openingType = OpeningType.values()[maxOpening];
	Map<MoveInfo, Integer> moves = moves();
	MoveInfo bestMove = moves.keySet().stream()
	    .filter(move -> move.type == MoveType.restore && move.ranged && move.restoreOpenings.contains(openingType))
	    .min(Comparator.comparing(moves::get)).orElse(null);
	if(bestMove != null) {
	    fs.wdgmsg("use", moves.get(bestMove), 1, 0);
	    return true;
	} else {
	    return false;
	}
    }

    public void reaggro() {
	Fightview.Mainrel mainrel = fs.fv.curdisp;
	if (mainrel == null || (mainrel.rel.gst & 2) == 0) {
	    return;
	}
	Gob gob = fs.ui.sess.glob.oc.getgob(mainrel.rel.gobid);
	if (gob == null) {
	    return;
	}
	mainrel.give.wdgmsg("click", 1);
	CustomUtil.cachedExecutor("Fight executor").submit(() -> {
	    CustomUtil.wait(() -> fs.fv.curdisp != mainrel, 5000);
	    fs.ui.gui.menu.wdgmsg("act", "aggro");
	    fs.ui.gui.map.wdgmsg("click", Coord.z, gob.rc.floor(OCache.posres), 1, 0, 0, (int) gob.id, gob.rc.floor(OCache.posres), 0, -1);
	    fs.ui.gui.map.wdgmsg("click", Coord.z, gob.rc.floor(OCache.posres), 3, 0);
	});
    }

    public enum MoveType {
	restore, stance, attack, move
    }

    public enum OpeningType {
	/**
	 * Striking
	 */
	offbalance(new Color(0, 161, 0)),
	/**
	 * Backhanded
	 */
	dizzy(new Color(67, 67, 255)),
	/**
	 * Sweeping
	 */
	reeling(new Color(255, 215, 0)),
	/**
	 * Oppressive
	 */
	cornered(new Color(255, 0, 0));

	public final Color color;

	OpeningType(Color color) {
	    this.color = color;
	}
    }

    public static class MoveInfo {
	public final String name;
	public final MoveType type;
	public final Set<OpeningType> restoreOpenings;
	public final boolean ranged;

	private MoveInfo(String name, MoveType type, Set<OpeningType> restoreOpenings, boolean ranged) {
	    this.name = name;
	    this.type = type;
	    this.restoreOpenings = restoreOpenings;
	    this.ranged = ranged;
	}

	private static MoveInfo restore(String name, Set<OpeningType> openings, boolean ranged) {
	    return new MoveInfo(name, MoveType.restore, openings, ranged);
	}
    }

}

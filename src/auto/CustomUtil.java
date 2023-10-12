package auto;

import haven.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class CustomUtil {

    private static final ConcurrentHashMap<Pair<Class<?>, String>, Field> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, ExecutorService> EXECUTORS = new ConcurrentHashMap<>();

    public static ExecutorService fixedExecutor(String name) {
	return EXECUTORS.computeIfAbsent(name, id -> Executors.newSingleThreadExecutor(r -> new Thread(r, name)));
    }

    public static ExecutorService cachedExecutor(String name) {
	AtomicInteger counter = new AtomicInteger(0);
	return EXECUTORS.computeIfAbsent(name, id -> Executors.newCachedThreadPool(r -> new Thread(r, name + " " + counter.incrementAndGet())));
    }

    public static void sleep(long millis) {
	try {
	    Thread.sleep(millis);
	} catch(InterruptedException ignore) {
	}
    }

//    public static void sleep(GameUI gui, long start, long millis) {
//	do {
//	    sleep(millis);
//	} while(gui.ui.sess.lastMessage <= start);
//    }
//
//    public static void sleep(GameUI gui, long millis) {
//	sleep(gui, System.currentTimeMillis(), millis);
//    }

    public static <T> T res(Supplier<T> getter) {
	long timeout = System.currentTimeMillis() + 60_000;
	while(true) {
	    try {
		return getter.get();
	    } catch(Loading l) {
		if(System.currentTimeMillis() > timeout) {
		    return null;
		}
		sleep(10);
	    }
	}
    }

    public static void waitLoaded(Runnable checker) {
	long timeout = System.currentTimeMillis() + 60_000;
	while(true) {
	    try {
		checker.run();
		return;
	    } catch(Loading l) {
		if(System.currentTimeMillis() > timeout) {
		    throw l;
		}
		sleep(10);
	    }
	}
    }

    public static String resname(Supplier<Resource> getter) {
	if(getter == null) {
	    return null;
	}
	long timeout = System.currentTimeMillis() + 60_000;
	while(true) {
	    try {
		Resource res = getter.get();
		if(res == null) {
		    return null;
		} else {
		    return res.name;
		}
	    } catch(Loading l) {
		if(System.currentTimeMillis() > timeout) {
		    return null;
		}
		sleep(10);
	    }
	}
    }

    public static String resnameOpt(Supplier<Resource> getter) {
	if(getter == null) {
	    return null;
	}
	try {
	    Resource res = getter.get();
	    if(res == null) {
		return null;
	    } else {
		return res.name;
	    }
	} catch(Loading l) {
	    return null;
	}
    }

    public static String resname(Sprite spr) {
	if(spr == null) {
	    return null;
	} else {
	    String name = spr.res.name;
	    if(name.equals("gfx/fx/eq")) {
		Sprite espr = getField(spr, "espr");
		if(espr != null) {
		    name = resname(espr);
		}
	    }
	    return name;
	}
    }

    public static String resname(Gob.Overlay ol) {
	String name = resname(ol.res);
	String sprite = resname(ol.spr);
	return sprite == null ? name : sprite;
    }

    public static String resnameOpt(Gob.Overlay ol) {
	String name = resnameOpt(ol.res);
	String sprite = resname(ol.spr);
	return sprite == null ? name : sprite;
    }

    public static Coord2d mapOffset(GameUI gui) {
	return mapTileOffset(gui).mul(MCache.tilesz);
    }

    public static Coord mapTileOffset(GameUI gui) {
	MiniMap.Location loc = gui.mmap.sessloc;
	if(loc == null) {
	    throw new Loading("Map data not loaded yet");
	}
	return loc.tc;
    }

    public static long mapId(GameUI gui) {
	if(gui.mmap != null) {
	    MiniMap.Location loc = gui.mmap.sessloc;
	    if(loc != null) {
		return loc.seg.id;
	    }
	}
	return 0;
    }

    public static PCoord persistCoord(GameUI gui, Coord2d coord) {
	Coord2d offset = mapOffset(gui);
	return new PCoord(mapId(gui), coord.add(offset));
    }
//
//    public static Coord2d localCoord(GameUI gui, PCoord coord) {
//	Coord2d offset = mapOffset(gui);
//	if(coord.mapId != mapId(gui)) {
//	    throw new IllegalArgumentException("Requested coord is on another map");
//	}
//	return coord.coord.sub(offset);
//    }
//
//    public static PTile persistTile(GameUI gui, Coord coord) {
//	Coord offset = mapTileOffset(gui);
//	return new PTile(mapId(gui), coord.add(offset));
//    }
//
//    public static PTileArea persistTileArea(GameUI gui, Coord a, Coord b) {
//	Coord offset = mapTileOffset(gui);
//	return new PTileArea(mapId(gui), a.add(offset), b.add(offset));
//    }
//
//    public static Coord localTile(GameUI gui, PTile tile) {
//	Coord offset = mapTileOffset(gui);
//	if(tile.mapId != mapId(gui)) {
//	    throw new IllegalArgumentException("Requested tile is on another map");
//	}
//	return tile.coord.sub(offset);
//    }
//
//    public static PGob persistGob(Gob gob) {
//	String resname = resname(gob::getres);
//	GameUI gui = Objects.requireNonNull(gob.glob.sess.ui.gui);
//	return new PGob(resname, persistCoord(gui, gob.rc));
//    }

    
    public static boolean wait(Supplier<Boolean> condition, long timeout) {
	long deadline = System.currentTimeMillis() + timeout;
	long sleepStart = System.currentTimeMillis();
	while(!condition.get()) {
	    sleep(100);
	    sleepStart = System.currentTimeMillis();
	    if(sleepStart > deadline) {
		break;
	    }
	}
	return condition.get();
    }

    
//    public static boolean wait(GameUI gui, Supplier<Boolean> condition, long timeout) {
//	long deadline = System.currentTimeMillis() + timeout;
//	long sleepStart = System.currentTimeMillis();
//	while(!condition.get()) {
//	    sleep(gui, sleepStart, 25);
//	    sleepStart = System.currentTimeMillis();
//	    if(sleepStart > deadline) {
//		break;
//	    }
//	}
//	return condition.get();
//    }
//
//    public static boolean waitStable(GameUI gui, long duration, Supplier<?> state, Supplier<Boolean> wait, Supplier<Boolean> abort, long timeout) {
//	if(state == null) {
//	    state = () -> null;
//	}
//	if(wait == null) {
//	    wait = () -> false;
//	}
//	if(abort == null) {
//	    abort = () -> false;
//	}
//	long period = Math.max(duration / 20, 25);
//	long sleepStart = System.currentTimeMillis();
//	Object current = state.get();
//	long deadline_stable = System.currentTimeMillis() + duration;
//	long deadline_total = System.currentTimeMillis() + timeout;
//	while(!abort.get()) {
//	    sleep(gui, sleepStart, period);
//	    sleepStart = System.currentTimeMillis();
//	    Object updated = state.get();
//	    if(!Objects.equals(current, updated) || wait.get()) {
//		current = updated;
//		deadline_stable = sleepStart + duration;
//	    } else if(sleepStart >= deadline_stable) {
//		return true;
//	    } else if(sleepStart > deadline_total) {
//		return false;
//	    }
//	}
//	return true;
//    }
//
//    public static Gob localGob(GameUI gui, PGob gob) {
//	if(gob.coord.mapId != mapId(gui)) {
//	    return null;
//	}
//	Coord2d coord = localCoord(gui, gob.coord);
//	return gui.ui.sess.glob.oc.stream()
//	    .filter(g -> g.rc.manhattan(coord) < 0.01)
//	    .filter(g -> gob.resname.equals(resname(g::getres)))
//	    .min(Comparator.comparingDouble(g -> g.rc.manhattan(coord)))
//	    .orElse(null);
//    }

    public static Field lookupField(Class<?> cls, String name) {
	return FIELD_CACHE.computeIfAbsent(new Pair<>(cls, name), pair -> {
	    Field field = null;
	    Class<?> c = pair.a;
	    while(field == null) {
		try {
		    field = c.getDeclaredField(pair.b);
		    field.setAccessible(true);
		} catch(NoSuchFieldException e) {
		    if(c.equals(Object.class)) {
			throw new RuntimeException(e);
		    } else {
			c = c.getSuperclass();
		    }
		}
	    }
	    return field;
	});
    }

    @SuppressWarnings("unchecked")
    public static <T> T getField(Object obj, String name) {
	Field field = lookupField(obj.getClass(), name);
	try {
	    return (T) field.get(obj);
	} catch(IllegalAccessException e) {
	    throw new RuntimeException(e);
	}
    }

    @SuppressWarnings("unchecked")
    public static <T> T getStaticField(Class<?> cls, String name) {
	Field field = lookupField(cls, name);
	if(!Modifier.isStatic(field.getModifiers())) {
	    throw new IllegalArgumentException();
	}
	try {
	    return (T) field.get(null);
	} catch(IllegalAccessException e) {
	    throw new RuntimeException(e);
	}
    }

    @SuppressWarnings("unchecked")
    public static <T> T getStaticField(ClassLoader loader, String cls, String name) {
	return getStaticField(getClass(loader, cls), name);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClass(ClassLoader loader, String cls) {
	try {
	    return (Class<T>) Class.forName(cls, true, loader);
	} catch(ClassNotFoundException e) {
	    throw new RuntimeException(e);
	}
    }

//    public static Gob activeVehicle(GameUI gui) {
//	Gob player = gui.map.player();
//	if(player == null) {
//	    return null;
//	}
//	Following moving = player.getattr(Following.class);
//	if(moving != null) {
//	    Gob vehicle = gui.ui.sess.glob.oc.getgob(moving.tgt);
//	    if(vehicle != null && GobTag.VEHICLE.is(vehicle)) {
//		return vehicle;
//	    }
//	}
//	return null;
//    }
//
//    public static Gob liftedGob(GameUI gui) {
//	long plgob = gui.map.plgob;
//	return gui.ui.sess.glob.oc.stream()
//	    .filter(gob -> {
//		Following moving = gob.getattr(Following.class);
//		return moving != null && moving.tgt == plgob;
//	    })
//	    .findAny().orElse(null);
//    }
//
//    public static Gob pulledGob(GameUI gui) {
//	Gob player = gui.map.player();
//	if(player == null) {
//	    return null;
//	}
//	List<Gob> gobs = gui.ui.sess.glob.oc.stream()
//	    .filter(GobTag.PULLABLE::is)
//	    .filter(GobTag.ACTIVE::is)
//	    .toList();
//	Gob pulled = gobs.stream()
//	    .filter(gob -> {
//		Homing moving = gob.getattr(Homing.class);
//		return moving != null && moving.tgt == player.id;
//	    })
//	    .findAny().orElse(null);
//	if(pulled == null) {
//	    pulled = gobs.stream()
//		.filter(gob -> gob.getattr(Moving.class) == null && gob.rc.dist(player.rc) < 40)
//		.min(Comparator.comparingDouble(gob -> gob.rc.dist(player.rc)))
//		.orElse(null);
//	}
//	return pulled;
//    }
//
//    public static Gob pushedGob(GameUI gui) {
//	Gob player = gui.map.player();
//	if(player == null) {
//	    return null;
//	}
//	Composite comp = player.getattr(Composite.class);
//	if(comp != null && comp.hasPose("/carry")) {
//	    return gui.ui.sess.glob.oc.stream()
//		.filter(GobTag.PUSHABLE::is)
//		.filter(GobTag.ACTIVE::is)
//		.min(Comparator.comparingDouble(gob -> gob.rc.dist(player.rc)))
//		.orElse(null);
//	}
//	return null;
//    }
//
//    public static String findRecentMessage(GameUI gui, long start, String regex) {
//	double nstart = start / 1e3;
//	Pattern pattern = Pattern.compile(regex);
//	List<ChatUI.Channel.RenderedMessage> rmsgs = gui.syslog.rmsgs;
//	synchronized(rmsgs) {
//	    ListIterator<ChatUI.Channel.RenderedMessage> it = rmsgs.listIterator(rmsgs.size());
//	    while(it.hasPrevious()) {
//		ChatUI.Channel.RenderedMessage msg = it.previous();
//		if(msg.msg.time <= nstart) {
//		    break;
//		} else if(pattern.matcher(msg.text().text).matches()) {
//		    return msg.text().text;
//		}
//	    }
//	}
//	return null;
//    }
}

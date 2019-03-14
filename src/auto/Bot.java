package auto;

import haven.Defer;
import haven.GameUI;
import haven.Gob;
import haven.UI;
import haven.rx.Reactor;
import rx.functions.Action1;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class Bot implements Defer.Callable<Void> {
    private static final Object lock = new Object();
    private static Bot current;
    private final List<Gob> targets;
    private final BotAction[] actions;
    private Defer.Future<Void> task;
    private boolean cancelled = false;
    
    public Bot(List<Gob> targets, BotAction... actions) {
	this.targets = targets;
	this.actions = actions;
    }
    
    @Override
    public Void call() throws InterruptedException {
	for (Gob gob : targets) {
	    for (BotAction action : actions) {
		action.call(gob);
		checkCancelled();
	    }
	}
	synchronized (lock) {
	    if(current == this) {current = null;}
	}
	return null;
    }
    
    private void run(Action1<String> callback) {
	task = Defer.later(this);
	task.callback(() -> callback.call(task.cancelled() ? "cancelled" : "complete"));
    }
    
    private void checkCancelled() throws InterruptedException {
	if(cancelled) {
	    throw new InterruptedException();
	}
    }
    
    private void markCancelled() {
	cancelled = true;
	task.cancel();
    }
    
    public static void cancel() {
	synchronized (lock) {
	    if(current != null) {
		current.markCancelled();
		current = null;
	    }
	}
    }
    
    private static void start(Bot bot, UI ui) {
	cancel();
	synchronized (lock) { current = bot; }
	bot.run((result) -> ui.message(String.format("Task is %s.", result), GameUI.MsgType.INFO));
    }
    
    public static void pickup(GameUI gui) {
	List<Gob> targets = gui.ui.sess.glob.oc.stream()
	    //.filter(startsWith("gfx/terobjs/items/branch"))
	    .filter(startsWith("gfx/terobjs/trees/"))
	    .sorted(distance)
	    //.limit(1)
	    .collect(Collectors.toList());
    
	start(new Bot(targets,
	    RClick,
	    selectFlower("Take branch"),
	    Gob::waitRemoval
	), gui.ui);
    }
    
    public static Comparator<Gob> distance = (o1, o2) -> {
	try {
	    Gob p = o1.glob.oc.getgob(o1.glob.sess.ui.gui.plid);
	    return Double.compare(p.rc.dist(o1.rc), p.rc.dist(o2.rc));
	} catch (Exception ignored) {}
	return Long.compare(o1.id, o2.id);
    };
    
    public static BotAction RClick = Gob::rclick;
    
    public static BotAction selectFlower(String option) {
	return gob -> Reactor.FLOWER.first().subscribe(flowerMenu -> flowerMenu.forceChoose(option));
    }
    
    private static Predicate<Gob> startsWith(String text) {
	return gob -> {
	    try {
		return gob.getres().name.startsWith(text);
	    } catch (Exception ignored) {}
	    return false;
	};
    }
    
    private interface BotAction {
	void call(Gob gob) throws InterruptedException;
    }
}

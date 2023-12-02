package me.ender;

import haven.*;
import haven.rx.CharterBook;
import haven.rx.Reactor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class WindowDetector {
    public static final String WND_STUDY = "Study";
    public static final String WND_CHARACTER_SHEET = "Character Sheet";
    public static final String WND_SMELTER = "Ore Smelter";
    
    private static final Object lock = new Object();
    private static final Set<Window> toDetect = new HashSet<>();
    private static final Set<Window> detected = new HashSet<>();
    
    static {
	Reactor.WINDOW.subscribe(WindowDetector::onWindowEvent);
    }
    
    public static void detect(Window window) {
	synchronized (toDetect) {
	    toDetect.add(window);
	}
    }
    
    private static void onWindowEvent(Pair<Window, String> event) {
	synchronized (lock) {
	    Window window = event.a;
	    if(toDetect.contains(window)) {
		String eventName = event.b;
		switch (eventName) {
		    case Window.ON_DESTROY:
			toDetect.remove(window);
			detected.remove(window);
			break;
		    //Detect window on 'pack' message - this is last message server sends after constructing a window
		    case Window.ON_PACK:
			if(!detected.contains(window)) {
			    detected.add(window);
			    recognize(window);
			}
			break;
		}
	    }
	}
    }
    
    private static void recognize(Window window) {
	AnimalFarm.processCattleInfo(window);
    }
    
    private static Widget.Factory convert(Widget parent, Widget.Factory f, Object[] cargs) {
	if(parent instanceof Window) {
	    Window window = (Window) parent;
	    //TODO: extract to separate class
	    String caption = window.caption();
	    if("Milestone".equals(caption) && f instanceof Label.$_) {
		String text = (String) cargs[0];
		if(!text.equals("Make new trail:")) {
		    return new Label.Untranslated.$_();
		}
	    } else if(isProspecting(caption)) {
	        if(f instanceof Label.$_) {
		    ((ProspectingWnd) parent).text((String) cargs[0]);
		} else if(f instanceof Button.$Btn) {
	            return new Button.$BtnSmall();
		}
	    }
	}
	return f;
    }
    
    public static Widget create(Widget parent, Widget.Factory f, UI ui, Object[] cargs) {
	f = convert(parent, f, cargs);
	return f.create(ui, cargs);
    }
    
    public static Widget newWindow(Coord sz, String title, boolean lg) {
	if(isPortal(title)) {
	    return new CharterBook(sz, title, lg);
	} else if(isProspecting(title)) {
	    return new ProspectingWnd(sz, title);
	}
	return (new WindowX(sz, title, lg));
    }
    
    public static String getWindowName(Widget wdg) {
	Window wnd;
	if(wdg == null) {return null;}
	if(wdg instanceof Window) {
	    wnd = (Window) wdg;
	} else {
	    wnd = wdg.getparent(Window.class);
	}
	return wnd == null ? null : wnd.caption();
    }
    
    public static boolean isWindowType(Widget wdg, String... types) {
	if(types == null || types.length == 0) {return false;}
	String wnd = getWindowName(wdg);
	if(wnd == null) {return false;}
	for (String type : types) {
	    if(Objects.equals(type, wnd)) {return true;}
	}
	
	return false;
    }
    
    public static boolean isPortal(String title) {
	return "Sublime Portico".equals(title) || "Charter Stone".equals(title);
    }
    
    public static boolean isBelt(String title) {
	return "Belt".equals(title);
    }
    
    public static boolean isProspecting(String title) {
	return "Prospecting".equals(title);
    }
}

/* Preprocessed source code */
import haven.*;
import haven.CharWnd.LoadingTextBox;
import java.util.*;
import java.awt.Color;

@FromResource(name = "ui/r-enact", version = 63)
public class Cost {
    public final double mp, ip, rp, lp;

    public Cost(double mp, double ip, double rp, double lp) {
	this.mp = mp;
	this.ip = ip;
	this.rp = rp;
	this.lp = lp;
    }
    public Cost() {this(0, 0, 0 ,0);}
}

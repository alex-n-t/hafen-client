package haven;
 
import auto.CustomUtil;
import auto.MineUtil;
import auto.PCoord;
import haven.render.*;
 
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
 
public class MineTileVisualizer implements RenderTree.Node {
    private static final int RADIUS = 30;
    private static final VertexArray.Layout LAYOUT = new VertexArray.Layout(new VertexArray.Layout.Input(Homo3D.vertex, new VectorFormat(3, NumberFormat.FLOAT32), 0, 0, 12));
    private PCoord lastCoord = new PCoord(0, Coord2d.z);
    private long flushDelay = 0;
    private final TileRectangles safeRectangles = new TileRectangles(new Color(0, 255, 128, 20));
    private final TileRectangles warnRectangles = new TileRectangles(new Color(255, 196, 0, 30));
    private final TileRectangles dangerRectangles = new TileRectangles(new Color(255, 0, 0, 30));
    private static final float SPACING = .5f;
 
    public void flush() {
    lastCoord = new PCoord(0, Coord2d.z);
    flushDelay = System.currentTimeMillis() + 300;
    }
 
    private static float[] convert(List<Coord2d> coords) {
    float[] ret = new float[coords.size() * 3];
    int i = 0;
    for(Coord2d coord : coords) {
        ret[i++] = (float) coord.x;
        ret[i++] = (float) -coord.y;
        ret[i++] = 0f;
    }
    return ret;
    }
 
    public void update(Session sess) {
    if(flushDelay > System.currentTimeMillis()) {
        return;
    }
    if(sess.ui.gui == null || sess.ui.gui.mmap == null || sess.ui.gui.mmap.sessloc == null) {
        safeRectangles.setTiles(sess, null);
        warnRectangles.setTiles(sess, null);
        dangerRectangles.setTiles(sess, null);
        return;
    }
    Gob player = sess.ui.gui.map.player();
    if(player == null) {
        safeRectangles.setTiles(sess, null);
        warnRectangles.setTiles(sess, null);
        dangerRectangles.setTiles(sess, null);
        return;
    }
    PCoord pcoord = CustomUtil.persistCoord(sess.ui.gui, player.rc);
    if(lastCoord.mapId == pcoord.mapId && lastCoord.coord.manhattan(pcoord.coord) < 50) {
        return;
    }
 
    Coord ul = player.rc.floor(MCache.tilesz).sub(RADIUS, RADIUS);
    Boolean[][] danger;
    try {
        danger = MineUtil.calculateDangerTiles(sess.ui.gui, ul, ul.add(RADIUS * 2, RADIUS * 2));
    } catch(Loading l) {
        return;
    }
    List<Gob> supports = sess.glob.oc.stream()
        .filter(gob -> {
        String res = CustomUtil.resnameOpt(gob::getres);
        return res != null && MineUtil.SUPPORT_RADIUS.containsKey(res);
        })
        .collect(Collectors.toList());
    lastCoord = pcoord;
    if(danger == null) {
        safeRectangles.setTiles(sess, null);
        warnRectangles.setTiles(sess, null);
        dangerRectangles.setTiles(sess, null);
        return;
    }
    List<Coord> dangerTiles = new ArrayList<>();
    List<Coord> warnTiles = new ArrayList<>();
    List<Coord> safeTiles = new ArrayList<>();
    for(int x = 0; x <= RADIUS * 2; x++) {
        for(int y = 0; y <= RADIUS * 2; y++) {
        Boolean danger_ = danger[x][y];
        Coord tc = Coord.of(x, y).add(ul);
        Integer tileId = CustomUtil.res(() -> sess.glob.map.gettile(tc));
        if(tileId == null) {
            continue;
        }
        String tileRes = CustomUtil.resname(() -> sess.glob.map.tilesetr(tileId));
        boolean cave = tileRes.endsWith("/cave");
        boolean rock = tileRes.startsWith("gfx/tiles/rocks/");
        if(!supports.isEmpty() && !Objects.equals(danger_, false)) {
            Coord2d rc = tc.mul(MCache.tilesz).add(5.5, 5.5);
            for(Gob support : supports) {
            double radius = MineUtil.SUPPORT_RADIUS.get(CustomUtil.resname(support::getres));
            if(rc.dist(support.rc) < radius) {
                GobHealth health = support.getattr(GobHealth.class);
                if(health == null || health.hp > 0.25) {
                danger_ = false;
                break;
                }
            }
            }
        }
        if(rock || (cave && danger_ != null)) {
            if(danger_ == null) {
            warnTiles.add(tc);
            } else if(danger_) {
            dangerTiles.add(tc);
            } else {
            safeTiles.add(tc);
            }
        }
        }
    }
    dangerRectangles.setTiles(sess, dangerTiles, TileRectangles.Style.SLASHED);
    warnRectangles.setTiles(sess, warnTiles, TileRectangles.Style.SOLID);
    safeRectangles.setTiles(sess, safeTiles, TileRectangles.Style.SOLID);
    }
 
    @Override
    public void added(RenderTree.Slot slot) {
    slot.add(dangerRectangles);
    slot.add(warnRectangles);
    slot.add(safeRectangles);
    }
 
    private static class TileRectangles extends Track1 implements Rendered {
    
    enum Style{SOLID, SLASHED}
    private final Color color;
    private Model model;
 
    private TileRectangles(Color color) {
        this.color = color;
    }
 
    public void setTiles(Session sess, List<Coord> tiles) {
    	setTiles(sess,tiles,Style.SOLID);
    }
    
    public void setTiles(Session sess, List<Coord> tiles, Style style) {
        if(tiles == null || tiles.isEmpty()) {
        model = null;
        } else {
        LinkedList<Coord2d> triangles = new LinkedList<>();
        for(Coord tile : tiles) {
            Coord2d ul = tile.mul(MCache.tilesz).add(Coord2d.of(SPACING));
            Coord2d br = tile.add(1, 1).mul(MCache.tilesz).sub(Coord2d.of(SPACING));
            switch (style) {
            case SOLID: 
            	triangles.addAll(Arrays.asList(ul, Coord2d.of(ul.x, br.y), br, ul, br, Coord2d.of(br.x, ul.y)));
            	break;
            case SLASHED:
            	triangles.addAll(Arrays.asList(ul.add(0,SPACING), Coord2d.of(ul.x, br.y), br.add(-SPACING,0), ul.add(SPACING,0), br.add(0,-SPACING), Coord2d.of(br.x, ul.y)));
            	break;
            }
        }
 
        float[] data = convert(triangles);
        VertexArray.Buffer vbo = new VertexArray.Buffer(data.length * 4, DataBuffer.Usage.STATIC, DataBuffer.Filler.of(data));
        model = new Model(Model.Mode.TRIANGLES, new VertexArray(LAYOUT, vbo), null);
        }
        if(slot != null) {
        slot.update();
        }
    }
 
    @Override
    public void added(RenderTree.Slot slot) {
        slot.ostate(Pipe.Op.compose(
        new BaseColor(color),
        new States.LineWidth(1f),
        Pipe.Op.compose(Rendered.last, States.Depthtest.none, States.maskdepth)
        ));
        super.added(slot);
    }
 
    @Override
    public void draw(Pipe context, Render out) {
        if(model != null) {
        out.draw(context, model);
        }
    }
    }
}
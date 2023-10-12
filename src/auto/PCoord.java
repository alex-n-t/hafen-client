package auto;

import haven.Coord2d;

import java.util.Objects;

public class PCoord {
    public final long mapId;
    public final Coord2d coord;

    public PCoord(long mapId, Coord2d coord) {
	this.mapId = mapId;
	this.coord = coord;
    }

    @Override
    public String toString() {
	return "(" + mapId + ": " + coord.x + ", " + coord.y + ")";
    }

    @Override
    public boolean equals(Object o) {
	if(this == o) return true;
	if(o == null || getClass() != o.getClass()) return false;
	PCoord c = (PCoord) o;
	return mapId == c.mapId && Objects.equals(coord, c.coord);
    }

    @Override
    public int hashCode() {
	return Objects.hash(mapId, coord);
    }
}

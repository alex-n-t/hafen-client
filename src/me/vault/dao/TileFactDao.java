package me.vault.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import haven.Coord;
import haven.MCache;
import me.vault.TileFact;

public class TileFactDao extends VaultDao {
	private static final String SQL_SCHEMA = "create table if not exists tile_fact("
			+ "	fact_seqno 	integer primary key autoincrement,"
			+ " type 		text,"
			+ " grid_id 	integer,"
			+ " x 			integer,"
			+ " y			integer,"
			+ " gx 			integer,"
			+ " gy			integer,"
			+ " data		text,"
			+ " item		text,"
			+ " quality_min	numeric,"
			+ " quality_max	numeric,"
			+ " quality_avg	numeric,"
			+ " counter		integer,"
			+ " tile_key    text,"
			+ " unique (grid_id,x,y,type,tile_key));";
	
	private static final String SQL_QUERY_CUT_BY_TYPE =
			"select type,grid_id,x,y,data from tile_fact"
			+ " where grid_id = ?"
			+ " and x between ? and ?"
			+ " and y between ? and ?"
			+ " and type = ?;";
	private static final String SQL_INSERT_FACT = 
			"insert into tile_fact(type,grid_id,x,y,data,item,quality_min,quality_max,quality_avg,tile_key,counter)"
			+ " values (?,?,?,?,?,?,?,?,?,?,1)"
			+ " on conflict (grid_id,x,y,type,tile_key)"
			+ " do update "
			+ " set counter = counter + 1,"
			+ " quality_min = min(quality_min,?),"
			+ " quality_max = max(quality_max,?),"
			+ " quality_avg = (quality_avg*counter+?)/(counter+1);";

	public static final TileFactDao INSTANCE = new TileFactDao(SQL_SCHEMA);
	
	private TileFactDao(String ddl) {
		super(ddl);
	}
	
	public List<TileFact> getCut(long gridId, Coord inGridCC, String type){
		List<TileFact> facts = new ArrayList<>();
		try (PreparedStatement stmt = getConnection().prepareStatement(SQL_QUERY_CUT_BY_TYPE)){
			Coord ul = inGridCC.mul(MCache.cutsz);
			Coord br = ul.add(MCache.cutsz).add(Coord.of(-1,-1));
			stmt.setLong(1, gridId);
			stmt.setInt(2, ul.x);
			stmt.setInt(3, br.x);
			stmt.setInt(4, ul.y);
			stmt.setInt(5, br.y);
			stmt.setString(6, type);
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
				facts.add(new TileFact(
						rs.getString("type"),
						rs.getLong("grid_id"),
						new Coord(rs.getInt("x"), rs.getInt("y")),
						rs.getString("data")
						));
		} catch (SQLException e) {e.printStackTrace();}
		return facts;
	}

	public List<TileFact> getGrid(long gridId, Coord ul, Coord br, String type){
		List<TileFact> facts = new ArrayList<>();
		try (PreparedStatement stmt = getConnection().prepareStatement(SQL_QUERY_CUT_BY_TYPE)){
			stmt.setLong(1, gridId);
			stmt.setInt(2, ul.x);
			stmt.setInt(3, br.x);
			stmt.setInt(4, ul.y);
			stmt.setInt(5, br.y);
			stmt.setString(6, type);
			ResultSet rs = stmt.executeQuery();
			while(rs.next())
				facts.add(new TileFact(
						rs.getString("type"),
						rs.getLong("grid_id"),
						new Coord(rs.getInt("x"), rs.getInt("y")),
						rs.getString("data")
						));
		} catch (SQLException e) {e.printStackTrace();}
		return facts;
	}

	
	
	public void put(TileFact fact) {
		try (PreparedStatement stmt = getConnection().prepareStatement(SQL_INSERT_FACT)){
			stmt.setString(1,	fact.type);
			stmt.setLong(2, 	fact.gridId);
			stmt.setInt(3, 		fact.inGridTC.x);
			stmt.setInt(4, 		fact.inGridTC.y);
			stmt.setString(5, 	fact.data);
			stmt.setString(6, 	fact.item);
			stmt.setDouble(7, 	fact.quality);
			stmt.setDouble(8, 	fact.quality);
			stmt.setDouble(9, 	fact.quality);
			stmt.setString(10, 	fact.getMatchingKey());
			stmt.setDouble(11, 	fact.quality);
			stmt.setDouble(12, 	fact.quality);
			stmt.setDouble(13, 	fact.quality);
			stmt.executeUpdate();
			System.out.println("Saved: "+fact.toString());
		} catch (SQLException e) {e.printStackTrace();}		
	}
}

package me.vault.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import haven.Coord;
import haven.MCache;
import me.vault.TileFact;

public class TileFactDao {
	private Connection connection;
	private static final String CONNECTION_STRING = "jdbc:sqlite:.\\vault.db";
	private static final String SQL_SCHEMA = "create table if not exists tile_fact("
			+ "	fact_seqno 	integer primary key autoincrement,"
			+ " type 		text,"
			+ " grid_id 	integer,"
			+ " x 			integer,"
			+ " y			integer,"
			+ " gx 			integer,"
			+ " gy			integer,"
			+ " data		text);";
	private static final String SQL_QUERY_CUT_BY_TYPE =
			"select type,grid_id,x,y,data from tile_fact "
			+ " where grid_id = ?"
			+ " and x between ? and ?"
			+ " and y between ? and ?"
			+ " and type = ?;";
	private static final String SQL_INSERT_FACT = "insert into tile_fact(type,grid_id,x,y,data) values (?,?,?,?,?);";

	public static final TileFactDao tileFactDao = new TileFactDao();

	private TileFactDao(){
		this.connection = getConnection();
		makeSchema();
	}

	Connection getConnection() {
		if(connection != null) return connection;
		else try {
			connection = DriverManager.getConnection(CONNECTION_STRING);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return connection;
	}

	private void makeSchema() {
		try(Statement stmt = getConnection().createStatement()){
			stmt.execute(SQL_SCHEMA);
		} catch (SQLException e) {e.printStackTrace();}
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

	public void put(TileFact fact) {
		try (PreparedStatement stmt = getConnection().prepareStatement(SQL_INSERT_FACT)){
			stmt.setString(1,fact.type);
			stmt.setLong(2, fact.gridId);
			stmt.setInt(3, fact.inGridTC.x);
			stmt.setInt(4, fact.inGridTC.y);
			stmt.setString(5, fact.data);
			stmt.executeUpdate();
		} catch (SQLException e) {e.printStackTrace();}		
	}
}

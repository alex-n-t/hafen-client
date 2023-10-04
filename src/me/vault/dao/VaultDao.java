package me.vault.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

class VaultDao {

	private static final String CONNECTION_STRING = "jdbc:sqlite:.\\vault.db";
	
	Connection connection;

	VaultDao(String ddl) {
		getConnection();
		makeSchema(ddl);
	}

	String getSchema() {
		return null;
	};
	
	protected Connection getConnection() {
		if(connection != null) return connection;
		else try {
			connection = DriverManager.getConnection(CONNECTION_STRING);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return connection;
	}

	protected void makeSchema(String schemaDDL) {
		try(Statement stmt = getConnection().createStatement()){
			stmt.execute(schemaDDL);
		} catch (SQLException e) {e.printStackTrace();}
	}

}
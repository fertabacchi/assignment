package com.crypto.portfolio.app.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;

/**
 * initialise the database tables
 */
public class DbSchemaBuilder {

	private final Supplier<Connection> connectionSupplier;

	public DbSchemaBuilder(Supplier<Connection> connectionSupplier) {
		this.connectionSupplier = connectionSupplier;
	}

	/**
	 * creates the tables for reference data (equities and options)
	 * @throws SQLException
	 */
	public void createSchema() throws SQLException {
		try (Connection connection = this.connectionSupplier.get()) {

			Statement statement = connection.createStatement();
			statement.execute("" +
					"CREATE table Equity(" +
					"ticker VARCHAR(20) NOT NULL, " +
					"expectedReturn DOUBLE NOT NULL," +
					"standardDeviation DOUBLE NOT NULL," +
					"PRIMARY KEY (ticker)" +
					")"
			);

			statement.execute("" +
					"CREATE table Option(" +
					"ticker VARCHAR(20), " +
					"equityTicker VARCHAR(20)," +
					"strike NUMERIC(10,4)," +
					"maturity DATE," +
					"type CHAR(1)," +
					"PRIMARY KEY (ticker), " +
					"FOREIGN KEY (equityTicker) REFERENCES Equity(ticker)" +
					")"
			);

		}
	}

}

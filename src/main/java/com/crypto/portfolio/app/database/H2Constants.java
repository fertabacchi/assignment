package com.crypto.portfolio.app.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Supplier;

public final class H2Constants {

	private H2Constants() {
	}

	public static final String H2_MEM_URL = "jdbc:h2:mem:refData;DB_CLOSE_DELAY=-1";

	public static Supplier<Connection> connectionSupplier(String url){
		return () -> {
			try {
				return DriverManager.getConnection(url);
			} catch (SQLException e) {
				throw new IllegalStateException(e);
			}
		};
	}

}

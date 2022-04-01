package com.crypto.portfolio.app.database;

import com.crypto.portfolio.api.Equity;
import com.crypto.portfolio.api.EuOption;
import com.crypto.portfolio.api.Instrument;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * it performs a bulky, fail-fast setup of instruments upon an empty database.
 * Not suitable for business-as-usual periodic insertions since it doesn't
 */
public class DbDataPreparer {

	private final Supplier<Connection> connectionSupplier;

	private static final String INSERT_EQUITY =
			"insert into Equity(ticker, expectedReturn, standardDeviation) " +
					"values (?,?,?)";

	private static final String INSERT_OPTION =
			"insert into Option(ticker, equityTicker, strike, maturity, type) " +
					"values (?,?,?,?,?)";

	public DbDataPreparer(Supplier<Connection> connectionSupplier) {
		this.connectionSupplier = connectionSupplier;
	}

	/**
	 * creates the set of instruments (equities and options) in the database.
	 * Assumes the input is correct and doesn't deal with duplicates
	 * @param instruments
	 * @throws SQLException
	 */
	public void insertInstruments( Instrument... instruments) throws SQLException {
		Set<String> setupEquities = new HashSet<>();
		for (Instrument instrument : instruments) {
			Equity equityOrUnderlying;

			if (instrument instanceof Equity)
				equityOrUnderlying = (Equity) instrument;
			else if (instrument instanceof EuOption){
				equityOrUnderlying = ((EuOption)instrument).getUnderlying();
			}
			else throw new IllegalArgumentException(instrument.getClass().getName() +" not expected");

			if (setupEquities.add(equityOrUnderlying.getTicker()))
				this.storeEquity(equityOrUnderlying);

			if (instrument instanceof EuOption){
				this.storeOption((EuOption) instrument);
			}
		}
	}


	private void storeEquity(Equity equity) throws SQLException {
		try (Connection connection = this.connectionSupplier.get()) {
			PreparedStatement preparedStatement = connection.prepareStatement(INSERT_EQUITY);
			preparedStatement.setString(1, equity.getTicker());
			preparedStatement.setDouble(2, equity.getExpectedReturn());
			preparedStatement.setDouble(3, equity.getAnnualizedStandardDeviation());
			preparedStatement.execute();
		}
	}


	/**
	 * it requires to have already stored the underlying equity
	 * @param option
	 * @throws SQLException
	 */
	private void storeOption(EuOption option) throws SQLException {
		try (Connection connection = this.connectionSupplier.get()) {
			PreparedStatement preparedStatement = connection.prepareStatement(INSERT_OPTION);
			preparedStatement.setString(1, option.getTicker());
			preparedStatement.setString(2, option.getUnderlying().getTicker());
			preparedStatement.setBigDecimal(3, option.getStrike());
			preparedStatement.setDate(4, Date.valueOf(option.getMaturity()));
			preparedStatement.setString(5, option.getOptionType().symbol);
			preparedStatement.execute();
		}
	}
}

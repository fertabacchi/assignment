package com.crypto.portfolio.app.database;

import com.crypto.portfolio.api.Equity;
import com.crypto.portfolio.api.EuOption;
import com.crypto.portfolio.api.Instrument;
import com.crypto.portfolio.api.interfaces.ReferenceDataService;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.function.Supplier;

/**
 * uses two tables (created in {@link DbSchemaBuilder}) to fetch option and equity data.
 * There's no central "instrument" table, like the java object hierarchy.
 *
 * We rather figure out whether an instrument is an equity or an option by checking the ticker symbol
 */
public class ReferenceDataInDbService implements ReferenceDataService {

	private final Supplier<Connection> connectionSupplier;

	private static final String EQUITY_TICKER_PATTERN = "[A-Z]+";
	private static final String OPTION_TICKER_PATTERN = "[A-Z]+\\-[A-Z]{3}\\-[0-9]{4}\\-[0-9]+-[CP]{1}";

	private static final String SELECT_EQUITY = "select * from Equity where ticker = ?";
	private static final String SELECT_OPTION = "select * from Option where ticker = ?";

	public ReferenceDataInDbService(Supplier<Connection> connectionSupplier) {
		this.connectionSupplier = connectionSupplier;
	}

	@Override
	public Instrument findInstrument(String ticker) {
		try (Connection connection = this.connectionSupplier.get()) {
			if (ticker.matches(EQUITY_TICKER_PATTERN))
				return this.findEquity(ticker, connection);
			else if (ticker.matches(OPTION_TICKER_PATTERN))
				return this.findOption(ticker, connection);
			else throw new IllegalArgumentException(ticker+" doesn't have a standard format");
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	private EuOption findOption(String ticker, Connection connection) throws SQLException {

		PreparedStatement preparedStatement = connection.prepareStatement(SELECT_OPTION);
		preparedStatement.setString(1, ticker);
		ResultSet resultSet = preparedStatement.executeQuery();
		if (!resultSet.next())
			throw new IllegalArgumentException("can't find options with ticker "+ticker);

		BigDecimal strike = resultSet.getBigDecimal("strike");
		String eqTicker = resultSet.getString("equityTicker");
		LocalDate maturity = resultSet.getDate("maturity").toLocalDate();
		EuOption.OptionType optionType = EuOption
				.fromSymbol(resultSet.getString("type"));
		resultSet.close();

		// let's load the underlying equity first
		Equity underlying = this.findEquity(eqTicker, connection);

		return new EuOption(
				ticker, underlying, strike, maturity, optionType
		);
	}

	private Equity findEquity(String ticker, Connection connection) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(SELECT_EQUITY);
		preparedStatement.setString(1, ticker);
		ResultSet resultSet = preparedStatement.executeQuery();
		if (!resultSet.next())
			throw new IllegalArgumentException("can't find equities with ticker "+ticker);
		return new Equity(
				resultSet.getString("ticker"),
				resultSet.getDouble("expectedReturn"),
				resultSet.getDouble("standardDeviation")
		);
	}
}

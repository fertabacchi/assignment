package com.crypto.portfolio.app;

import com.crypto.portfolio.api.Equity;
import com.crypto.portfolio.api.EuOption;
import com.crypto.portfolio.api.Instrument;
import com.crypto.portfolio.app.database.DbDataPreparer;
import com.crypto.portfolio.app.database.DbSchemaBuilder;
import com.crypto.portfolio.app.database.ReferenceDataInDbService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.function.Supplier;

import static com.crypto.portfolio.app.database.H2Constants.connectionSupplier;

public class DatabaseTest {

	public static final String H2_MEM_URL_TEST = "jdbc:h2:mem:refDataTest;DB_CLOSE_DELAY=-1";

	@Test
	public void testEquity() throws SQLException {
		Supplier<Connection> connector = connectionSupplier(H2_MEM_URL_TEST);

		DbSchemaBuilder schemaBuilder = new DbSchemaBuilder(connector);
		schemaBuilder.createSchema();

		// [A-Z]+\-[A-Z]{3}\-[0-9]{4}}\-[0-9]+-[CP]{1}

		Equity APPLE = new Equity("APPLE", 0, 1);
		EuOption APPLE_C = new EuOption("APPLE-MAR-2022-100-C", APPLE, new BigDecimal("100"), LocalDate.of(2022,1,1), EuOption.OptionType.CALL);
		Equity TESLA = new Equity("TESLA", 0, 1);
		EuOption TESLA_P = new EuOption("TESLA-MAR-2022-100-P", TESLA, new BigDecimal("100"), LocalDate.of(2022,1,1), EuOption.OptionType.PUT);

		DbDataPreparer dbDataPreparer = new DbDataPreparer(connector);
		dbDataPreparer.insertInstruments(
			APPLE_C, APPLE, TESLA_P
		);

		ReferenceDataInDbService dbService = new ReferenceDataInDbService(connector);

		Assertions.assertEquals(APPLE, dbService.findInstrument(APPLE.getTicker()));
		assertOptionsAreSame(APPLE_C, (EuOption) dbService.findInstrument(APPLE_C.getTicker()));
		assertOptionsAreSame(TESLA_P, (EuOption) dbService.findInstrument(TESLA_P.getTicker()));
	}

	private static void assertOptionsAreSame(EuOption expected, EuOption actual){
		Assertions.assertEquals(expected.getUnderlying(), actual.getUnderlying());
		Assertions.assertEquals(expected.getTicker(), actual.getTicker());
		Assertions.assertEquals(expected.getOptionType(), actual.getOptionType());
		Assertions.assertEquals(expected.getMaturity(), actual.getMaturity());
		Assertions.assertTrue(expected.getStrike().compareTo( actual.getStrike() ) == 0);
	}

}

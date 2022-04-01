package com.crypto.portfolio.app;

import com.crypto.portfolio.api.Equity;
import com.crypto.portfolio.api.EuOption;
import com.crypto.portfolio.api.interfaces.MarketService;
import com.crypto.portfolio.api.interfaces.OptionPriceService;
import com.crypto.portfolio.api.subscriber.PortfolioUpdate;
import com.crypto.portfolio.api.subscriber.PortfolioUpdateSubscriberQueue;
import com.crypto.portfolio.app.database.DbDataPreparer;
import com.crypto.portfolio.app.database.DbSchemaBuilder;
import com.crypto.portfolio.app.database.H2Constants;
import com.crypto.portfolio.app.database.ReferenceDataInDbService;
import com.crypto.portfolio.app.implementations.RandomisedBrownianMarketService;
import com.crypto.portfolio.app.implementations.StaticPositionService;
import com.crypto.portfolio.app.utils.OptionCalcUtils;
import com.crypto.portfolio.app.utils.PositionsUtils;
import com.crypto.portfolio.app.utils.StandardisedDistributionSampler;
import com.crypto.portfolio.core.PortfolioService;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Supplier;


/**
 * provides an implementation for the interfaces used by {@link PortfolioService}, setting up a handful of
 * instruments.
 * It then starts the service and registers a subscriber which prints on the console a summary of the {@link PortfolioUpdate}
 */
public class Runner {

	/**
	 * this is the emulated startup time of the service
	 */
	private final static LocalDateTime BEGIN_TIMESTAMP = LocalDateTime.of(2022,1,1,0,0);

	/**
	 * how many milliseconds will pass according the system between the iterations of the portfolio service.
	 * It affects how options are priced and the brownian motion as well
	 */
	private final static long EMULATED_INTERVAL_MILLIS = 2000;

	/**
	 * the real interval in milliseconds between the iterations of the portfolio service.
	 * This is the time experienced by the user and drives the frequency of PortfolioUpdates being delivered
	 */
	private final static long USER_INTERVAL_MILLIS = 2000;


	/**
	 * the more samples are used, the more precise the option pricing is
	 */
	private final static StandardisedDistributionSampler SAMPLER = new StandardisedDistributionSampler(10000);


	public static void main(String[] args) throws IOException {
		/*
		let's define a few equities and options that we will use in our services.
		The instruments must be consistently defined in our interfaces: each equity / option that shows
		up in the position-service must also have the stock priced by the market-service and the reference-data available.

		Last but not least, the H2-based reference data service expects options and equities with a well formed
		ticker, otherwise it would fail to recognise what they are.

		P.S. Also, the pricing service would lunch an exception if we try to price an option already matured.
		So all options here should expire after the BEGIN_TIMESTAMP defined above
		*/

		Equity aapl = new Equity("AAPL", 0.4, 0.2);
		Equity tsla = new Equity("TSLA", 0.2, 0.5);
		Equity rklb = new Equity("RKLB", 0.1, 0.8);
		Equity amzn = new Equity("AMZN", 0.5, 0.2);

		EuOption amznC = new EuOption("AMZN-MAR-2022-3000-C", amzn, BigDecimal.valueOf(3000), LocalDate.of(2022,3,1), EuOption.OptionType.CALL);
		EuOption amznP = new EuOption("AMZN-JUN-2022-3400-P", amzn, BigDecimal.valueOf(3400), LocalDate.of(2022,6,1), EuOption.OptionType.PUT);
		EuOption rklbP = new EuOption("RKLB-JUN-2022-16-C", rklb, BigDecimal.valueOf(16), LocalDate.of(2022,6,1), EuOption.OptionType.PUT);


		// static never-changing positions, as defined in the csv file
		StaticPositionService positionService = new StaticPositionService(PositionsUtils.loadPositionsFromCsv(
				new InputStreamReader(ClassLoader.getSystemResourceAsStream("positions.csv"))
		));

		// we initialise the price of the equities at BEGIN_TIMESTAMP
		// From there on, they'll move with a brownian motion
		MarketService marketService = new RandomisedBrownianMarketService(ImmutableMap.<Equity, BigDecimal>builder()
				.put( aapl, new BigDecimal("180") )
				.put( tsla, new BigDecimal("900") )
				.put( amzn, new BigDecimal("3200") )
				.put( rklb, new BigDecimal("12") )
				.build(),
				BEGIN_TIMESTAMP
		);

		// we define the option-pricing-service on the spot, by relying on the utility calculation library
		OptionPriceService optionPriceService = new OptionPriceService() {
			@Override
			public BigDecimal price(EuOption option, LocalDate localDate, BigDecimal equityPrice) {
				return OptionCalcUtils.priceOption(SAMPLER, option, equityPrice, localDate);
			}
		};


		// now let's prepare the database from scratch
		Supplier<Connection> connectionSupplier = H2Constants.connectionSupplier(H2Constants.H2_MEM_URL);
		try {
			// create schema
			new DbSchemaBuilder(connectionSupplier).createSchema();
			// insert data
			new DbDataPreparer(connectionSupplier).insertInstruments(aapl, tsla, rklb, amzn, rklbP, amznP, amznC);
		} catch (SQLException e) {
			throw new RuntimeException("issues while preparing the database", e);
		}
		//.. and define the reference data service using it
		ReferenceDataInDbService referenceDataInDbService = new ReferenceDataInDbService(connectionSupplier);


		// finally, we can initialise the service
		PortfolioService service = new PortfolioService(
				EMULATED_INTERVAL_MILLIS,
				USER_INTERVAL_MILLIS,
				10, // max subscription queue capacity
				positionService, optionPriceService, marketService, referenceDataInDbService
		);

		// before stating the server, we register our listener, in charge of printing the updates on the console
		PortfolioUpdateSubscriberQueue subscriberQueue = service.addSubscriber("printer");
		PortfolioUpdatePrinter portfolioUpdatePrinter = new PortfolioUpdatePrinter(subscriberQueue);

		// one thread will be created to process the portfolio iterations
		service.startService(BEGIN_TIMESTAMP);

		// while this thread will wait for updates indefinitely
		portfolioUpdatePrinter.printWheneverAvailable();

	}

}

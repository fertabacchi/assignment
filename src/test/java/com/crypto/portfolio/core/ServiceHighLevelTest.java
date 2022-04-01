package com.crypto.portfolio.core;

import com.crypto.portfolio.api.Equity;
import com.crypto.portfolio.api.EuOption;
import com.crypto.portfolio.api.Instrument;
import com.crypto.portfolio.api.interfaces.MarketService;
import com.crypto.portfolio.api.interfaces.OptionPriceService;
import com.crypto.portfolio.api.interfaces.Position;
import com.crypto.portfolio.api.interfaces.ReferenceDataService;
import com.crypto.portfolio.api.subscriber.EquityPriceUpdate;
import com.crypto.portfolio.api.subscriber.PortfolioEntry;
import com.crypto.portfolio.api.subscriber.PortfolioUpdate;
import com.crypto.portfolio.api.subscriber.PortfolioUpdateSubscriberQueue;
import com.crypto.portfolio.app.implementations.StaticPositionService;
import com.crypto.portfolio.app.utils.OptionCalcUtils;
import com.crypto.portfolio.app.utils.StandardisedDistributionSampler;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * we run the whole portfolio service with mock interfaces, and verify the consistency of market updates
 * sent to the subscribers
 */
public class ServiceHighLevelTest {

	private static final LocalDateTime BEGIN_TS = LocalDateTime.of(2022,1,1,0,0);

	private static StandardisedDistributionSampler SAMPLER = new StandardisedDistributionSampler(1000);

	private static Equity APPLE = new Equity("AAPL", 0.4, 0.2);
	private static Equity TESLA = new Equity("TSLA", 0.2, 0.3);
	private static Equity RKLB = new Equity("RKLB", 0.3, 0.2);
	private static EuOption RKLB_C = new EuOption("RKLB-2023-C", RKLB, new BigDecimal(14),
			LocalDate.of(2023,1,1), EuOption.OptionType.CALL);


	@Test
	public void testService() throws InterruptedException {
		final int APPLE_QTY = -100;
		final int TESLA_QTY = 15;
		final int RKLB_C_QTY = 10;
		final int RKLB_PRICE = 10;
		final int APPLE_PRICE = 100;
		final int TESLA_PRICE = 1000;

		StaticPositionService positionService = new StaticPositionService(
				ImmutableList.of(
						new Position(BigInteger.valueOf(APPLE_QTY), APPLE.getTicker()),
						new Position(BigInteger.valueOf(TESLA_QTY), TESLA.getTicker()),
						new Position(BigInteger.valueOf(RKLB_C_QTY), RKLB_C.getTicker())
				)
		);



		MarketService marketService = new MarketService() {
			@Override
			public BigDecimal getPrice(String ticker, LocalDateTime timestamp) throws IllegalStateException {
				if (ticker.equals(APPLE.getTicker()))
					return new BigDecimal(APPLE_PRICE);
				else if (ticker.equals(TESLA.getTicker()))
					return new BigDecimal(TESLA_PRICE);
				else if (ticker.equals(RKLB.getTicker()))
					return new BigDecimal(RKLB_PRICE);
				else throw new IllegalArgumentException();
			}
		};

		OptionPriceService optionPriceService = new OptionPriceService() {
			@Override
			public BigDecimal price(EuOption option, LocalDate localDate, BigDecimal equityPrice) {
				return OptionCalcUtils.priceOption(SAMPLER, option, equityPrice, localDate);
			}
		};

		ReferenceDataService referenceDataService = new ReferenceDataService() {
			@Override
			public Instrument findInstrument(String ticker) {
				return ImmutableList.of(RKLB, APPLE, TESLA, RKLB_C).stream().filter(
						instrument -> instrument.getTicker().equals(ticker)
				).findAny().orElseThrow(() -> new IllegalArgumentException()) ;
			}
		};

		PortfolioService service = new PortfolioService(
				2000,
				30,
				10,
				positionService, optionPriceService, marketService, referenceDataService
		);

		PortfolioUpdateSubscriberQueue id1 = service.addSubscriber("id1");
		PortfolioUpdateSubscriberQueue id2 = service.addSubscriber("id1");

		service.startService(BEGIN_TS);

		PortfolioUpdate portfolioUpdate11 = id1.waitForNextUpdate();
		PortfolioUpdate portfolioUpdate12 = id1.waitForNextUpdate();
		PortfolioUpdate portfolioUpdate21 = id2.waitForNextUpdate();
		PortfolioUpdate portfolioUpdate22 = id2.waitForNextUpdate();

		service.kill();

		// we verify the updates equivalence. The portfolio updates generated at the same instant
		// are the same in-memory instance
		Assertions.assertEquals(portfolioUpdate11, portfolioUpdate21);
		Assertions.assertEquals(portfolioUpdate12, portfolioUpdate22);

		// verify the instruments in the portfolio
		Assertions.assertEquals(
				ImmutableSet.of(
						APPLE.getTicker(),
						TESLA.getTicker(),
						RKLB_C.getTicker()
				),
				portfolioUpdate11.getEntries().stream().map(PortfolioEntry::getPosition)
						.map(Position::getTicker).collect(Collectors.toSet())
		);

		// verify that the market updates also contain RKLB (which is not in my portfolio as equity, but as option)
		Assertions.assertEquals(
				ImmutableSet.of(
						APPLE.getTicker(),
						TESLA.getTicker(),
						RKLB.getTicker()
				),
				portfolioUpdate11.getEquityPriceUpdates().stream().map(EquityPriceUpdate::getEquityTicker)
						.collect(Collectors.toSet())
		);


	}

}

package com.crypto.portfolio.app.implementations;


import com.crypto.portfolio.api.Equity;
import com.crypto.portfolio.api.interfaces.MarketService;
import com.crypto.portfolio.app.utils.BrownianUtils;

import javax.annotation.concurrent.NotThreadSafe;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * given a set of predefined equities and their price, at each call the requested price is updated as a Brownian motion.
 * Equities not belonging to the initial set cannot be enquired.
 *
 * The implementation keeps track of the last timestamp the price was requested; the new price will depend on the
 * time "virtually" passed since then.
 *
 * Please note that this implementation does not accept backwards prices; once the price for the timestamp T has been
 * requested, you cannot ask for T1 where T1 < T
 */
@NotThreadSafe
public class RandomisedBrownianMarketService implements MarketService {

	private final Map<String, Equity> referenceData;
	private final Map<String, LastPrice> currentPrices;

	/**
	 * initialise the random market service
	 * @param initialPrices		the full equities reference data instances mapped to their their initial prices
	 * @param beginTimestamp	it's the "initial" timestamp for which the initialised prices apply
	 */
	public RandomisedBrownianMarketService(Map<Equity, BigDecimal> initialPrices, LocalDateTime beginTimestamp) {
		// cache the equity reference data
		this.referenceData = initialPrices.keySet().stream().collect(Collectors.toMap(Equity::getTicker, equity -> equity));
		this.currentPrices = new HashMap<>();
		initialPrices.forEach(
				(equity, price) -> this.currentPrices.put(equity.getTicker(), new LastPrice(beginTimestamp, price))
		);
	}

	@Override
	public BigDecimal getPrice(String ticker, LocalDateTime timestamp) {

		Equity equity = Objects.requireNonNull(this.referenceData.get(ticker));
		LastPrice lastPrice = this.currentPrices.get(ticker);

		long deltaMillis = ChronoUnit.MILLIS.between(lastPrice.time, timestamp);

		BigDecimal newPrice = BrownianUtils.brownianMotionPrice(lastPrice.price, deltaMillis,
				equity.getExpectedReturn(), equity.getAnnualizedStandardDeviation());

		// update price / timestamp in the local map
		this.currentPrices.put(ticker, new LastPrice(timestamp, newPrice));
		return newPrice;
	}

	private static class LastPrice{
		private final LocalDateTime time;
		private final BigDecimal price;

		public LastPrice(LocalDateTime time, BigDecimal price) {
			this.time = time;
			this.price = price;
		}
	}

}

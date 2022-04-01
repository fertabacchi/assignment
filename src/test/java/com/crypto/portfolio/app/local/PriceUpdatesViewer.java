package com.crypto.portfolio.app.local;

import com.crypto.portfolio.app.implementations.RandomisedBrownianMarketService;
import com.crypto.portfolio.api.Equity;
import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * test execution showing how equity prices will change with the brownian motion.
 * The standard deviation expectedly drives the differences of the outputs throughout executions
 */
public class PriceUpdatesViewer {

	public static void main(String[] args) throws InterruptedException {

		LocalDateTime START_TIME = LocalDateTime.of(2000, 1, 1, 0, 0);

		RandomisedBrownianMarketService priceSrv = new RandomisedBrownianMarketService(
				ImmutableMap.of(
						new Equity("TSLA", 0.2, 0.9),
						new BigDecimal("300"),
						new Equity("APPL", 0.3, 0.01),
						new BigDecimal("40")
				), START_TIME
		);

		System.out.println( priceSrv.getPrice("TSLA", START_TIME) );
		System.out.println( priceSrv.getPrice("APPL", START_TIME) );

		System.out.println("after six months");

		System.out.println( priceSrv.getPrice("TSLA", START_TIME.plusMonths(6)) );
		System.out.println( priceSrv.getPrice("APPL", START_TIME.plusMonths(6)) );

		System.out.println("after another six months");

		System.out.println( priceSrv.getPrice("TSLA", START_TIME.plusYears(1)) );
		System.out.println( priceSrv.getPrice("APPL", START_TIME.plusYears(1)) );

	}

}

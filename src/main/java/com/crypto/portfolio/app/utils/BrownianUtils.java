package com.crypto.portfolio.app.utils;

import java.math.BigDecimal;
import java.util.Random;

/**
 * the static method simulates the price of an equity according a brownian motion
 */
public final class BrownianUtils {

	private BrownianUtils() {}

	private static final long TWELVE_WEEKS_IN_SECONDS = 3600*24*7*12;
	private static final Random RANDOM = new Random();

	/**
	 * produces the next price of an equity
	 * @param previousPrice		previous known price of the equity
	 * @param millisInterval	number of millis which have passed in our scenario before simulating the next price.
	 *                          It can't be a negative number
	 * @param expectedReturn	equity's expected return
	 * @param annualizedStandardDeviation	equity's standard deviation
	 * @return
	 */
	public static BigDecimal brownianMotionPrice(
			BigDecimal previousPrice,
			long millisInterval,
			double expectedReturn,
			double annualizedStandardDeviation
	){
		if (millisInterval<0)
			throw new IllegalArgumentException("the time interval must be non negative");

		double timeDeltaSeconds = (double) millisInterval / 1000;

		/*
		calculated according the appendix notes.
		If the "nextGaussian" is a big-negative number and lots of seconds have passed,
		in theory the price could become negative. In case that happens, we re-do the simulation
		 */
		double deltaMultiplier;
		do{
			deltaMultiplier = // all doubles sub-expressions here
					// first operand (deterministic using expected-return and time)
					expectedReturn * timeDeltaSeconds / TWELVE_WEEKS_IN_SECONDS
							// second operand (random factor and standard-deviation). Positive or negative
							+ annualizedStandardDeviation * RANDOM.nextGaussian() *
							Math.sqrt(timeDeltaSeconds/ TWELVE_WEEKS_IN_SECONDS);
		}while(deltaMultiplier < -1);

		return previousPrice
				.multiply(new BigDecimal(deltaMultiplier, Constants.INTERNAL_PRICE_CONTEXT))
				.add(previousPrice, Constants.INTERNAL_PRICE_CONTEXT);
	}
}

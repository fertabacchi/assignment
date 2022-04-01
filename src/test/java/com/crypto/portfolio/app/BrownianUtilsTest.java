package com.crypto.portfolio.app;

import com.crypto.portfolio.app.utils.BrownianUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import java.math.BigDecimal;

/**
 * we run some statistical-based tests for the brownian generator.
 * e.g. if I toss a coin many times at some point I will have similar number of heads/crosses.
 *
 * In case of issues these tests won't traditionally fail. So there's a manual check to
 * kill the test in case we had way too many iterations
 */
public class BrownianUtilsTest {

	private static final int MAX_ITERATIONS = 10000;

	/**
	 * the instrument with a better annualised return will eventually out-price the other
	 */
	@RepeatedTest(10)
	public void annualisedReturnTest(){
		final double HIGH_RETURN = 0.7;
		final double LOW_RETURN = 0.3;
		final long INT_MILLIS = 10000l;
		final double DEVIATION = 0.5d;

		BigDecimal lowReturnPrice = BigDecimal.valueOf(100);
		BigDecimal highReturnPrice = BigDecimal.valueOf(100);

		int iterations = 0;

		do{
			lowReturnPrice = BrownianUtils.brownianMotionPrice(lowReturnPrice, INT_MILLIS, LOW_RETURN, DEVIATION);
			highReturnPrice = BrownianUtils.brownianMotionPrice(highReturnPrice, INT_MILLIS, HIGH_RETURN, DEVIATION);
			if (iterations > MAX_ITERATIONS)
				Assertions.fail();
		}
		// repeat at least 10 times, till the high-return equity overtakes the low-return one
		while (iterations++ < 10 || lowReturnPrice.compareTo(highReturnPrice) == 1 );
	}


	/**
	 * by letting more time pass, the price will increase more (because the annualised return is positive).
	 * We set the random deviation (which could be negative) very low, to facilitate the test.
	 */
	@RepeatedTest(10)
	public void intervalTest(){
		final double RETURN = 0.9;
		final long SMALL_INT = 10l;
		final long BIG_MILLIS = 10000l;
		final double DEVIATION = 0.01d;

		BigDecimal smallIntReturnPrice = BigDecimal.valueOf(100);
		BigDecimal bigIntReturnPrice = BigDecimal.valueOf(100);

		int iterations = 0;

		do{
			smallIntReturnPrice = BrownianUtils.brownianMotionPrice(smallIntReturnPrice, SMALL_INT, RETURN, DEVIATION);
			bigIntReturnPrice = BrownianUtils.brownianMotionPrice(bigIntReturnPrice, BIG_MILLIS, RETURN, DEVIATION);
			if (iterations > MAX_ITERATIONS)
				Assertions.fail();
		}
		// repeat at least 10 times, till the high-return equity overtakes the low-return one
		while (iterations++ < 10 || smallIntReturnPrice.compareTo(bigIntReturnPrice) == 1 );
	}

}

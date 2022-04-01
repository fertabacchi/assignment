package com.crypto.portfolio.app;

import com.crypto.portfolio.api.Equity;
import com.crypto.portfolio.api.EuOption;
import com.crypto.portfolio.app.utils.OptionCalcUtils;
import com.crypto.portfolio.app.utils.StandardisedDistributionSampler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * a bit hard to do bullet-proof tests here considering my lack of technical knowledge,
 * So I'd focus on some intuitive behaviours of the option pricing; by moving only one variable of the
 * equation I'd expect the price to increase or decrease.
 *
 * All tests are defined here. Subclasses (one for put, one for call) just initialise the constructor
 */
public abstract class CalcUtilsForOptionAbstractTest {

	// we static-initialise the sampler used for calculations.
	// The "sampler" can be considered high-level tested within here
	private static final StandardisedDistributionSampler SAMPLER =
			new StandardisedDistributionSampler(1000);


	protected static final double DEFAULT_EXCEPTED_RETURN = 0.5d;
	protected static final double DEFAULT_STANDARD_DEVIATION = 0.5d;
	protected static final BigDecimal OPTION_STRIKE = BigDecimal.valueOf(100);
	protected static final LocalDate OPTION_MATURITY = LocalDate.of(1999,12,31);

	private final EuOption.OptionType optionType;
	private final boolean expectedLinearity;

	/**
	 *
	 * @param optionType			the option type to be passed to the calculation library
	 * @param expectedLinearity		true if you expect the option to appreciate itself
	 *                              when the underlying does; false when they'd go in opposite directions
	 */
	public CalcUtilsForOptionAbstractTest(EuOption.OptionType optionType, boolean expectedLinearity) {
		this.optionType = optionType;
		this.expectedLinearity = expectedLinearity;
	}


	/**
	 * begin closer to maturity should decrease the value of an option.
	 * This is indifferently true for both option types
	 */
	@Test
	public void verifyDecay(){
		BigDecimal price10daysBeforeMat = OptionCalcUtils.priceOption(
				SAMPLER,
				this.buildSampleOption(null, null),
				OPTION_STRIKE, // same current price than the strike
				OPTION_MATURITY.minusDays(10)
		);
		BigDecimal price5daysBeforeMat = OptionCalcUtils.priceOption(
				SAMPLER,
				this.buildSampleOption(null, null),
				OPTION_STRIKE, // same current price than the strike
				OPTION_MATURITY.minusDays(5)
		);
		Assertions.assertTrue( price5daysBeforeMat.compareTo(price10daysBeforeMat) == -1 );
	}


	/**
	 * the higher the volatility, the higher the price
	 */
	@Test
	public void verifyVolatility(){
		BigDecimal priceHighVol = OptionCalcUtils.priceOption(
				SAMPLER,
				this.buildSampleOption(null, 0.9d),
				OPTION_STRIKE, // same current price than the strike
				OPTION_MATURITY.minusDays(10)
		);
		BigDecimal priceLowVol = OptionCalcUtils.priceOption(
				SAMPLER,
				this.buildSampleOption(null, 0.1d),
				OPTION_STRIKE, // same current price than the strike
				OPTION_MATURITY.minusDays(10)
		);
		Assertions.assertTrue( priceLowVol.compareTo(priceHighVol) == -1 );
	}

	/**
	 * with underlying price going up, call options should appreciate; and vice-versa for put
	 */
	@Test
	public void verifyMargin(){
		BigDecimal priceStockUp = OptionCalcUtils.priceOption(
				SAMPLER,
				this.buildSampleOption(null, null),
				OPTION_STRIKE.add(BigDecimal.ONE),
				OPTION_MATURITY.minusDays(10)
		);
		BigDecimal priceStockDown = OptionCalcUtils.priceOption(
				SAMPLER,
				this.buildSampleOption(null, null),
				OPTION_STRIKE.add(BigDecimal.ONE.negate()),
				OPTION_MATURITY.minusDays(10)
		);
		Assertions.assertTrue(  ( priceStockDown.compareTo(priceStockUp) == -1 )  ==  this.expectedLinearity  );
	}


	private EuOption buildSampleOption(
			Double nullableExpectedReturn,
			Double nullableStandardDeviation
	){
		return new EuOption("optionTicker",
				new Equity(
						"ticker etc..",
						(nullableExpectedReturn == null)? DEFAULT_EXCEPTED_RETURN : nullableExpectedReturn,
						(nullableStandardDeviation == null)? DEFAULT_STANDARD_DEVIATION : nullableStandardDeviation
				),
				OPTION_STRIKE,
				OPTION_MATURITY,
				this.optionType
		);
	}


}
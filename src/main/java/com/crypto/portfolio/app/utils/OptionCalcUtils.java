package com.crypto.portfolio.app.utils;


import com.crypto.portfolio.api.EuOption;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * utility for pricing european options
 */
public final class OptionCalcUtils {

	private OptionCalcUtils() {}

	private static final double IR = 0.02;

	/**
	 * shortcut for {@link #priceOption(StandardisedDistributionSampler, BigDecimal, BigDecimal, double, double, EuOption.OptionType)}
	 * @return
	 */
	public static BigDecimal priceOption(
			StandardisedDistributionSampler distributionSampler,
			EuOption option,
			BigDecimal stockPrice,
			LocalDate priceDate
			){
		long daysToMaturity = ChronoUnit.DAYS.between(priceDate, option.getMaturity());
		double yearsToMaturity = (double) daysToMaturity / 365;

		return priceOption(
				distributionSampler,
				stockPrice,
				option.getStrike(),
				option.getUnderlying().getAnnualizedStandardDeviation(),
				yearsToMaturity,
				option.getOptionType()
		);

	}


	/**
	 * returns the price of an option using a gaussian sampler.
	 * The more samples have been taken, the more precise the calculation is
	 * @param distributionSampler
	 * @param stockPrice
	 * @param strikePrice
	 * @param annualizedStandardDeviation
	 * @param yearsToMaturity
	 * @param optionType
	 * @return
	 */
	public static BigDecimal priceOption(
			StandardisedDistributionSampler distributionSampler,
			BigDecimal stockPrice, // S
			BigDecimal strikePrice, // K
			double annualizedStandardDeviation, // o¬
			double yearsToMaturity,
			EuOption.OptionType optionType
	){
		if (yearsToMaturity<=0)
			throw new IllegalArgumentException("the option is matured");

		double stockPriceDb = stockPrice.doubleValue();
		double strikePriceDb = strikePrice.doubleValue();

		double d1 = (
				Math.log(stockPriceDb/strikePriceDb) +
				( IR + annualizedStandardDeviation * annualizedStandardDeviation / 2 ) * yearsToMaturity
		) /
		( annualizedStandardDeviation * Math.sqrt(yearsToMaturity) );

		double d2 = d1 - annualizedStandardDeviation * Math.sqrt(yearsToMaturity);

		switch (optionType){
			case CALL:
					return stockPrice.multiply( asBD(distributionSampler.oddsLessThan( d1 )), Constants.INTERNAL_PRICE_CONTEXT)
							.add(
									strikePrice.negate().multiply( asBD(
											Math.pow(Math.E, - IR * yearsToMaturity)
											*
											distributionSampler.oddsLessThan(d2)
									), Constants.EXTERNAL_PRICE_CONTEXT)
							);
			case PUT:
					return strikePrice.multiply(asBD(Math.pow(Math.E, - IR * yearsToMaturity) * distributionSampler.oddsLessThan (-d2)), Constants.INTERNAL_PRICE_CONTEXT)
							.add(stockPrice.negate().multiply(asBD(distributionSampler.oddsLessThan(-d1)), Constants.EXTERNAL_PRICE_CONTEXT));
			default: throw new Error("not implemented");
		}
	}

	private static BigDecimal asBD(double d){
		return new BigDecimal(d, Constants.INTERNAL_PRICE_CONTEXT);
	}

}

package com.crypto.portfolio.api;

import java.util.Objects;

public class Equity extends Instrument {

	/**
	 * between 0 (0%) and 1 (+100%).
	 * So we assume overall economy is growing
	 */
	protected final double expectedReturn;
	/**
	 * between 0 and 1
	 */
	protected final double annualizedStandardDeviation;

	public Equity(String ticker, double expectedReturn, double annualizedStandardDeviation) {
		super(ticker);
		if (expectedReturn<0 || expectedReturn > 1)
			throw new IllegalArgumentException("expected return should be in the interval [0,1]");
		if (annualizedStandardDeviation<0 || annualizedStandardDeviation > 1)
			throw new IllegalArgumentException("annualizedStandardDeviation return should be in the interval [0,1]");
		this.expectedReturn = expectedReturn;
		this.annualizedStandardDeviation = annualizedStandardDeviation;
	}

	public double getExpectedReturn() {
		return expectedReturn;
	}

	public double getAnnualizedStandardDeviation() {
		return annualizedStandardDeviation;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Equity equity = (Equity) o;
		return Double.compare(equity.expectedReturn, expectedReturn) == 0 && Double.compare(equity.annualizedStandardDeviation, annualizedStandardDeviation) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(expectedReturn, annualizedStandardDeviation);
	}
}

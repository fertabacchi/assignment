package com.crypto.portfolio.api.interfaces;


import java.math.BigInteger;
import java.util.Objects;

/**
 * an open position in a portfolio
 * @see PositionService
 */
public final class Position {
	// we assume it's an integer (as non-decimal) number
	private final BigInteger positionSize;
	private final String ticker;

	public Position(BigInteger positionSize, String ticker) {
		this.positionSize = Objects.requireNonNull(positionSize);
		this.ticker = Objects.requireNonNull(ticker);
	}

	public BigInteger getPositionSize() {
		return positionSize;
	}

	public String getTicker() {
		return ticker;
	}

	@Override
	public String toString() {
		return "Position{" +
				"positionSize=" + positionSize +
				", ticker='" + ticker + '\'' +
				'}';
	}
}

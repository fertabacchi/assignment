package com.crypto.portfolio.api;

import java.util.Objects;

public class Instrument {

	protected final String ticker;

	public Instrument(String ticker) {
		this.ticker = Objects.requireNonNull(ticker);
	}

	public String getTicker() {
		return ticker;
	}
}

package com.crypto.portfolio.api.subscriber;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.util.Objects;

@Immutable
public class EquityPriceUpdate {

	private final String equityTicker;
	private final BigDecimal price;

	public EquityPriceUpdate(String equityTicker, BigDecimal price) {
		this.equityTicker = Objects.requireNonNull(equityTicker);
		this.price = Objects.requireNonNull(price);
	}

	public String getEquityTicker() {
		return equityTicker;
	}

	public BigDecimal getPrice() {
		return price;
	}

	@Override
	public String toString() {
		return "EquityPriceUpdate{" +
				"equityTicker='" + equityTicker + '\'' +
				", price=" + price +
				'}';
	}
}

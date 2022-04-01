package com.crypto.portfolio.api.subscriber;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * offers a snapshot of a portfolio.
 *
 * For each position:
 * - ticker and quantity
 * - unit market value
 * - value of the whole position
 *
 * Plus:
 * - price of all equities (either directly owned or by options)
 * - timestamp of this update
 * - total portfolio value
 */
@Immutable
public class PortfolioUpdate {

	private final LocalDateTime timestamp;
	private final List<PortfolioEntry> entries;
	private final List<EquityPriceUpdate> equityPriceUpdates;
	private final BigDecimal totalValue;

	public PortfolioUpdate(LocalDateTime timestamp, List<PortfolioEntry> entries, List<EquityPriceUpdate> equityPriceUpdates, BigDecimal totalValue) {
		this.timestamp = Objects.requireNonNull(timestamp);
		// important to ensure immutability
		// In our architecture there will be only one physical instance
		// of this object in memory, for each portfolio update. I mean all subscribers will read the same update
		this.entries = Collections.unmodifiableList( entries );
		this.equityPriceUpdates = Collections.unmodifiableList( equityPriceUpdates );
		this.totalValue = Objects.requireNonNull(totalValue);
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public List<PortfolioEntry> getEntries() {
		return entries;
	}

	public List<EquityPriceUpdate> getEquityPriceUpdates() {
		return equityPriceUpdates;
	}

	public BigDecimal getTotalValue() {
		return totalValue;
	}

	@Override
	public String toString() {
		return "PortfolioUpdate{" +
				"timestamp=" + timestamp +
				", entries=" + entries +
				", equityPriceUpdates=" + equityPriceUpdates +
				", totalValue=" + totalValue +
				'}';
	}
}

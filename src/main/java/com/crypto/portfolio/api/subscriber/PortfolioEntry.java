package com.crypto.portfolio.api.subscriber;

import com.crypto.portfolio.api.interfaces.Position;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.util.Objects;

@Immutable
public final class PortfolioEntry {

	private final Position position;
	private final BigDecimal unitValue;
	private final BigDecimal value;

	public PortfolioEntry(Position position, BigDecimal unitValue, BigDecimal value) {
		this.position = Objects.requireNonNull(position);
		this.unitValue = Objects.requireNonNull(unitValue);
		this.value = Objects.requireNonNull(value);
	}

	public Position getPosition() {
		return position;
	}

	public BigDecimal getUnitValue() {
		return unitValue;
	}

	public BigDecimal getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "PortfolioEntry{" +
				"position=" + position +
				", unitValue=" + unitValue +
				", value=" + value +
				'}';
	}
}

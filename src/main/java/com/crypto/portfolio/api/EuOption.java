package com.crypto.portfolio.api;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EuOption extends Instrument {

	protected final Equity underlying;
	protected final BigDecimal strike;
	protected final LocalDate maturity;
	protected final OptionType optionType;


	public EuOption(String ticker, Equity underlying, BigDecimal strike, LocalDate maturity, OptionType optionType) {
		super(ticker);
		this.underlying = Objects.requireNonNull(underlying);
		this.strike = Objects.requireNonNull(strike);
		this.maturity = Objects.requireNonNull(maturity);
		this.optionType = Objects.requireNonNull(optionType);
	}

	public Equity getUnderlying() {
		return underlying;
	}

	public BigDecimal getStrike() {
		return strike;
	}

	public LocalDate getMaturity() {
		return maturity;
	}

	public OptionType getOptionType() {
		return optionType;
	}

	static Map<String, OptionType> reverseSymbolLookup = new HashMap<>();

	public enum OptionType{

		CALL("C"), PUT("P");

		public final String symbol;

		OptionType(String symbol) {
			this.symbol = symbol;
			reverseSymbolLookup.put(this.symbol, this);
		}
	}

	public static EuOption.OptionType fromSymbol(String symbol){
		return Objects.requireNonNull(reverseSymbolLookup.get(symbol));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		EuOption euOption = (EuOption) o;
		return underlying.equals(euOption.underlying) && strike.equals(euOption.strike) && maturity.equals(euOption.maturity) && optionType == euOption.optionType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(underlying, strike, maturity, optionType);
	}
}

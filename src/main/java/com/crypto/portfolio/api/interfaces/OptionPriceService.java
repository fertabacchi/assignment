package com.crypto.portfolio.api.interfaces;


import com.crypto.portfolio.api.EuOption;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * calculates the price of an option
 */
public interface OptionPriceService {

	BigDecimal price(EuOption option, LocalDate currentDate, BigDecimal equityPrice);

}

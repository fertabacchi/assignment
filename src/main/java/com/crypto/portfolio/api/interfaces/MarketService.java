package com.crypto.portfolio.api.interfaces;

import javax.annotation.concurrent.ThreadSafe;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * "real-time" price of quoted equities, by ticker. It's up to the user to specify the desired timestamp
 * at which equities should be priced
 *
 * Assumptions:
 * - no bid/ask. Just a straight number
 * - the market is always open
 */
public interface MarketService {

	/**
	 * retrieve price for a single equity instrument.
 	 * @param ticker
 	 * @return	null if the equity is not found or the equity is not available
	 * @throws IllegalStateException	if the equity is not found
	 */
	BigDecimal getPrice(String ticker, LocalDateTime timestamp) throws IllegalStateException;

}

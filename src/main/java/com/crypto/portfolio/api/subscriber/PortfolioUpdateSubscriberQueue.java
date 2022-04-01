package com.crypto.portfolio.api.subscriber;

/**
 * receives updates about the valuation of the portfolio
 */
public interface PortfolioUpdateSubscriberQueue {

	/**
	 * blocks the current thread till an update is available
	 * @return	the portfolio update, with positions evaluation and equity market updates
	 */
	PortfolioUpdate waitForNextUpdate() throws InterruptedException;

}

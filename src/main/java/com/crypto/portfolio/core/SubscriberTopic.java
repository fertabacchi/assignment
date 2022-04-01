package com.crypto.portfolio.core;

import com.crypto.portfolio.api.subscriber.PortfolioUpdateSubscriberQueue;
import com.crypto.portfolio.api.subscriber.PortfolioUpdate;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * this implementation should not be visible to the user as it contains critical
 * configuration of the topic which should only be managed by the PortfolioService.
 *
 * Keeps a queue of the portfolio updates not consumed yet by a subscriber.
 * If the queue is full, new updates will be discarded
 */
class SubscriberTopic implements PortfolioUpdateSubscriberQueue {

	private final BlockingQueue<PortfolioUpdate> queue;
	private final String description;

	SubscriberTopic(String description, int maxCapacity) {
		this.description = description;
		this.queue = new LinkedBlockingQueue<>(maxCapacity);
	}

	@Override
	public PortfolioUpdate waitForNextUpdate() throws InterruptedException {
		return queue.take();
	}

	/**
	 * adds an update to the subscriber's topic.
	 * @param portfolioUpdate
	 * @return	true if all good. false if the queue is full and the update will be lost
	 */
	boolean add(PortfolioUpdate portfolioUpdate){
		return this.queue.offer(portfolioUpdate);
	}

	public String getDescription() {
		return description;
	}
}

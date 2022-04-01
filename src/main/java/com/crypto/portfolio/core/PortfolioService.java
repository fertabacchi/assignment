package com.crypto.portfolio.core;


import com.crypto.portfolio.api.Equity;
import com.crypto.portfolio.api.EuOption;
import com.crypto.portfolio.api.Instrument;
import com.crypto.portfolio.api.Logger;
import com.crypto.portfolio.api.interfaces.*;
import com.crypto.portfolio.api.subscriber.EquityPriceUpdate;
import com.crypto.portfolio.api.subscriber.PortfolioEntry;
import com.crypto.portfolio.api.subscriber.PortfolioUpdate;
import com.crypto.portfolio.api.subscriber.PortfolioUpdateSubscriberQueue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * prepares the portfolio service.
 * Invoke {@link #startService(LocalDateTime)} to kick off the process in a separate thread
 */
public class PortfolioService {

	private final long emulatedUpdatesInterval;
	private final long updatesInterval;
	private final int maxQueueCapacity;

	private final PositionService positionService;
	private final OptionPriceService priceService;
	private final MarketService marketDataService;
	private final ReferenceDataService referenceDataService;

	private volatile boolean running = true;

	private final CachingReferenceData cachingReferenceData;

	// list of topics interested in portfolio updates
	public Collection<SubscriberTopic> subscriberTopics = new LinkedBlockingQueue<>();

	/**
	 *
	 * @param emulatedUpdatesInterval		the virtual time between portfolio updates, in millis
	 * @param updatesInterval				the actual time between updates, in millis
	 * @param maxQueueCapacity				maximum number of portfolio updates which will be stored, per subscriber
	 * @param positionService
	 * @param priceService
	 * @param marketDataService
	 * @param referenceDataService
	 */
	public PortfolioService(
			long emulatedUpdatesInterval,
			long updatesInterval,
			int maxQueueCapacity, PositionService positionService,
			OptionPriceService priceService,
			MarketService marketDataService,
			ReferenceDataService referenceDataService
	) {
		this.emulatedUpdatesInterval = emulatedUpdatesInterval;
		this.updatesInterval = updatesInterval;
		this.maxQueueCapacity = maxQueueCapacity;
		this.positionService = Objects.requireNonNull(positionService);
		this.priceService = Objects.requireNonNull(priceService);
		this.marketDataService = Objects.requireNonNull(marketDataService);
		this.referenceDataService = Objects.requireNonNull(referenceDataService);
		this.cachingReferenceData = new CachingReferenceData(this.referenceDataService);
	}


	/**
	 * registers interest in portfolio updates.
	 * @param 	id		only for log purposes. There's no actual requirement to keep this id different among listeners
	 * @return	the queue from which portfolio updates will be retrieved
	 */
	public PortfolioUpdateSubscriberQueue addSubscriber(String id){
		SubscriberTopic observer = new SubscriberTopic(id, this.maxQueueCapacity);
		this.subscriberTopics.add(observer);
		return observer;
	}


	/**
	 * de-register the subscriber.
	 * Leftover portfolio updates are not automatically erased
	 * @param subscriber
	 * @return	true if the lister was successfully removed
	 */
	public boolean removeSubscriber(PortfolioUpdateSubscriberQueue subscriber){
		// sequential lookup, using memory address. Makes sense as long as we don't have too many listeners
		return this.subscriberTopics.remove(subscriber);
	}

	/**
	 * non-blocking. Starts the service with another thread
	 */
	public void startService(LocalDateTime timestamp){
		new Thread( () -> this.runner(timestamp), "portfolioService").start();
	}

	public void kill(){
		this.running = true;
	}


	private void runner(LocalDateTime timestamp){
		LocalDateTime currentTimestamp = timestamp;
		while(this.running){

			if (! this.subscriberTopics.isEmpty()){

				// we won't call the
				List<Position> positions = this.positionService.getPositions(currentTimestamp);

				// collect all traded instruments (no duplicates in this set)
				final Set<String> positionTickers = positions.stream().map(
						Position::getTicker
				).collect(Collectors.toSet());

				// .. and be sure they're loaded into the cache
				Map<String, Instrument> openPositionsWithRef =
						positionTickers.stream().collect(Collectors.toMap(
								ticker -> ticker, ticker -> this.cachingReferenceData.getInstrument(ticker)
						));

				// get equity prices, including the ones non directly traded but underlying of options
				Map<String, BigDecimal> equityPrices = extractEquityPricesFromPositions(currentTimestamp, openPositionsWithRef);

				Map<String, BigDecimal> optionPrices = retrieveOptionPrices(currentTimestamp, openPositionsWithRef, equityPrices);

				PortfolioUpdate update = this.buildPortfolioUpdate(positions, equityPrices, optionPrices, currentTimestamp);

				// push the update to all subscribers
				for (SubscriberTopic subscriberTopic : this.subscriberTopics) {
					if (!subscriberTopic.add(update))
						Logger.log("the queue for the subscriber "+subscriberTopic.getDescription()+" is full");
				}
			}
			else
				Logger.log("no subscribers found yet");


			try {
				Thread.sleep(this.updatesInterval);
			} catch (InterruptedException e) {
				throw new Error("unexpected termination", e);
			}

			currentTimestamp = currentTimestamp.plus(this.emulatedUpdatesInterval, ChronoUnit.MILLIS);
		}
		Logger.log("service terminated");
	}

	/**
	 * create the user-side object which will be consumed by subscribers
	 * @param positions
	 * @param equityPrices
	 * @param optionPrices
	 * @param currentTimestamp
	 * @return
	 */
	private PortfolioUpdate buildPortfolioUpdate(
			List<Position> positions,
			Map<String, BigDecimal> equityPrices,
			Map<String, BigDecimal> optionPrices,
			LocalDateTime currentTimestamp) {

		List<PortfolioEntry> entries = new LinkedList<>();
		BigDecimal nav = BigDecimal.ZERO;

		for (Position position : positions) {
			BigDecimal price = equityPrices.get(position.getTicker());
			if (price == null) // it must be an option then
				price = optionPrices.get(position.getTicker());
			if (price == null)
				throw new IllegalStateException("can't price "+position.getTicker());

			// new BigDecimal(bigInteger) has scale 0.
			// multiplying our price BigDecimal (whose scale is X) by one whose scale is 0, the result has scale X
			// so no loss of precision
			BigDecimal entryValue = price.multiply(new BigDecimal(position.getPositionSize()));
			nav = nav.add(entryValue);
			entries.add(new PortfolioEntry(position, price, entryValue));
		}

		return new PortfolioUpdate(
				currentTimestamp,
				entries,
				// equity updates are already made. just transform the map into a list
				equityPrices.entrySet().stream()
						.map(entry -> new EquityPriceUpdate(entry.getKey(), entry.getValue()))
						.collect(Collectors.toList()),
				nav
		);
	}

	/**
	 * using the pricing service, obtain the price of all options among the open position
	 * @param currentTimestamp
	 * @param openPositionsWithRef	open positions, which includes options to price
	 * @param equityPrices			it must contain prices for all equities which are necessary to compute option prices
	 * @return
	 */
	private Map<String, BigDecimal> retrieveOptionPrices(
			LocalDateTime currentTimestamp,
			Map<String, Instrument> openPositionsWithRef,
			Map<String, BigDecimal> equityPrices
	) {
		return openPositionsWithRef.entrySet()
				.stream().filter(entry -> entry.getValue() instanceof EuOption)
				.collect(Collectors.toMap(
						entry -> entry.getKey(),
						entry -> this.priceService.price(
								(EuOption) entry.getValue(),
								currentTimestamp.toLocalDate(),
								equityPrices.get(((EuOption) entry.getValue()).getUnderlying().getTicker())
						)
				));
	}

	/**
	 * uses the market data to price all equities among the "open positions".
	 * It also prices equities which are underlying of the options
	 * @param currentTimestamp
	 * @param openPositions
	 * @return	equity ticker -> price
	 */
	private Map<String, BigDecimal> extractEquityPricesFromPositions(
			LocalDateTime currentTimestamp,
			Map<String, Instrument> openPositions
	) {
		Map<String, BigDecimal> equityPrices = new HashMap<>();
		for (Map.Entry<String, Instrument> position : openPositions.entrySet()) {
			String equityTicker;
			if (position.getValue() instanceof Equity)
				equityTicker = position.getKey();
			else if (position.getValue() instanceof EuOption)
				equityTicker = ((EuOption) position.getValue()).getUnderlying().getTicker();
			else
				throw new Error("derivative not expected");
			equityPrices.computeIfAbsent(equityTicker, ticker -> this.marketDataService.getPrice(ticker, currentTimestamp));
		}
		return equityPrices;
	}


}

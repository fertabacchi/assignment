package com.crypto.portfolio.app;

import com.crypto.portfolio.api.Logger;
import com.crypto.portfolio.api.subscriber.EquityPriceUpdate;
import com.crypto.portfolio.api.subscriber.PortfolioEntry;
import com.crypto.portfolio.api.subscriber.PortfolioUpdate;
import com.crypto.portfolio.api.subscriber.PortfolioUpdateSubscriberQueue;
import com.google.common.base.Strings;

public class PortfolioUpdatePrinter {

	private final PortfolioUpdateSubscriberQueue listener;

	public PortfolioUpdatePrinter(PortfolioUpdateSubscriberQueue listener) {
		this.listener = listener;
	}


	public void printWheneverAvailable()  {
		while(true){
			PortfolioUpdate portfolioUpdate;
			try {
				portfolioUpdate = this.listener.waitForNextUpdate();
			} catch (InterruptedException e) {
				Logger.log("terminating listener: "+e.getMessage());
				break;
			}
			StringBuilder sb = new StringBuilder();
			sb.append("---- new update at ");
			sb.append(portfolioUpdate.getTimestamp().toString());
			sb.append("\n\n");

			// equity updates
			sb.append("(equity) market updates relevant for the portfolio:\n");
			for (EquityPriceUpdate equityPriceUpdate : portfolioUpdate.getEquityPriceUpdates()) {
				sb.append( padTicker(equityPriceUpdate.getEquityTicker()) );
				sb.append(": ");
				sb.append(equityPriceUpdate.getPrice().toPlainString());
				sb.append("\n");
			}

			sb.append("\n");

			// portfolio value
			sb.append("open positions:\n");
			sb.append( padTicker("ticker") );
			sb.append( padNumbers("price") );
			sb.append( padNumbers("qty") );
			sb.append( padNumbers("value\n") );
			for (PortfolioEntry entry : portfolioUpdate.getEntries()) {
				sb.append( padTicker(entry.getPosition().getTicker()) );
				sb.append( padNumbers(entry.getUnitValue().toPlainString()) );
				sb.append( padNumbers(entry.getPosition().getPositionSize().toString()) );
				sb.append( padNumbers(entry.getValue().toPlainString()) );
				sb.append("\n");
			}
			sb.append("\ntotal value: ");
			sb.append(portfolioUpdate.getTotalValue().toPlainString());
			sb.append("\n----------------\n\n");

			System.out.println(sb.toString());
		}

	}

	private static String padTicker(String ticker){
		return Strings.padEnd(ticker, 25, ' ');
	}

	private static String padNumbers(String value){
		return Strings.padStart(value, 25, ' ');
	}
}

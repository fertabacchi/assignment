package com.crypto.portfolio.core;

import com.crypto.portfolio.api.Instrument;
import com.crypto.portfolio.api.interfaces.ReferenceDataService;

import java.util.HashMap;
import java.util.Map;

/**
 * we assume reference data is immutable (and the number of instruments throughout the life of the service is small),
 * so we can keep it all in memory
 */
public class CachingReferenceData {

	private final ReferenceDataService referenceDataService;

	private final Map<String, Instrument> cache = new HashMap<>();

	public CachingReferenceData(ReferenceDataService referenceDataService) {
		this.referenceDataService = referenceDataService;
	}

	public Instrument getInstrument(String ticker){
		return this.cache.computeIfAbsent(ticker, referenceDataService::findInstrument);
	}

}

package com.crypto.portfolio.api.interfaces;


import com.crypto.portfolio.api.Instrument;

/**
 * loads the definition of equities or options
 */
public interface ReferenceDataService {

	/**
	 *
	 * @param ticker
	 * @return
	 * @throws IllegalArgumentException	 if the ticker is not found
	 */
	Instrument findInstrument(String ticker) throws IllegalArgumentException;

}

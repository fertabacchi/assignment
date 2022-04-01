package com.crypto.portfolio.app.utils;


import com.crypto.portfolio.api.interfaces.Position;

import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class PositionsUtils {

	private static final String CSV_SYMBOL_HEADER = "symbol" , CSV_POSITION_SIZE_HEADER = "positionSize";

	private PositionsUtils() {
	}

	/**
	 * Loads positions from a CSV content whose headers
	 * are {@value  #CSV_POSITION_SIZE_HEADER} and {@value  #CSV_SYMBOL_HEADER}
	 * It also closes the reader.
	 * @param csvFileReader
	 * @return
	 */
	public static List<Position> loadPositionsFromCsv(Reader csvFileReader) throws IOException {
		CsvReader csvReader = new CsvReader(csvFileReader);
		// the CSV file will be read only once
		List<Position> allPositions = new LinkedList<>();
		csvReader.forEach(
				(Map<String,String> csvRow) -> allPositions.add(
						new Position(
								new BigInteger(csvRow.get(CSV_POSITION_SIZE_HEADER)),
								csvRow.get(CSV_SYMBOL_HEADER)
								)
				)

		);
		csvReader.close();
		return allPositions;
	}
}

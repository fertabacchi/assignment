package com.crypto.portfolio.app.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * since we can't use other third parties libraries, here's a basic csv-reader with mandatory header, wrapping
 * an io.Reader. It returns an iteration of maps of strings for each line: header -> value
 * No quotes or other decorators supported.
 */
public class CsvReader implements Iterable<Map<String,String>>, Closeable {

	private final BufferedReader rawReader;
	private final String[] headerNames;

	private static final String CSV_SEPARATOR = Pattern.quote(",");

	public CsvReader(Reader rawReader) throws IOException {
		this.rawReader = new BufferedReader(rawReader);
		this.headerNames = this.buildHeader();
	}

	private Map<String, String> computeNext() throws IOException {
	 	String nextLine = this.rawReader.readLine();
		if (nextLine == null)
			return null;

		String values[] = nextLine.split(CSV_SEPARATOR);
		if (values.length != this.headerNames.length)
			throw new IOException("inconsistent csv data. Header size is " + this.headerNames.length +
					" and found raw with " + values.length + " entries: " + nextLine);

		Map<String,String> returningMap = new HashMap<>();
		for (int i = 0; i < headerNames.length; i++) {
			returningMap.put(headerNames[i], values[i]);
		}
		return returningMap;
	}

	private String[] buildHeader() throws IOException {
		String headerLine = this.rawReader.readLine();
		if (headerLine==null)
			throw new IOException("the CSV doesn't seem to contain a header");
		return headerLine.split(CSV_SEPARATOR);
	}


	@Override
	public void close() throws IOException {
		this.rawReader.close(); // cascade-close all encapsulated Readers
	}

	@Override
	public Iterator<Map<String, String>> iterator() {
		return new Iterator<Map<String, String>>() {

			/*
			these two variables are the state of the iterator
			*/
			private Map<String, String> next; // next item to return. if null it might have to be computed
			private boolean done = false; // if true, we reached the end of the iterator

			private void fetchNextIfNecessary(){
				if (this.next == null && !this.done){
					try {
						this.next = CsvReader.this.computeNext();
					} catch (IOException e) {
						throw new IllegalStateException(e);
					}
					if (this.next == null)
						this.done = true;
				}
			}

			@Override
			public boolean hasNext() {
				this.fetchNextIfNecessary();
				return ! this.done;
			}

			@Override
			public Map<String, String> next() {
				this.fetchNextIfNecessary();
				if (this.done)
					throw new NoSuchElementException();
				Map<String, String> toReturn = this.next;
				this.next = null;
				return toReturn;
			}
		};
	}
}

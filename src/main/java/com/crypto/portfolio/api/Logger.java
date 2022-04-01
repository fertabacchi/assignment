package com.crypto.portfolio.api;

/**
 * static logger utility
 */
public final class Logger {

	private Logger() {
	}

	public static void log(String s){
		System.out.println(Thread.currentThread()+": "+s);
	}

}

package com.crypto.portfolio.app.utils;

import java.util.*;

/**
 * Utility that (approximately) calculates the odds that a variable following a normal standardised distribution
 * is less than a given value.
 *
 * Rather than applying some formula online that I little understand, I use the brute force of the CPU to pre-arrange
 * and order many samples from the same distribution, then I will see my variable fits each time with cost log(samples)
 */
public final class StandardisedDistributionSampler {

	private final double[] sortedSamples;

	private final NavigableSet<Double> samples;


	/**
	 * pre-calculate the given number of samples in a normal standardised distribution and sorts them
	 * in a tree.
	 * @param sampleNumber
	 */
	public StandardisedDistributionSampler(int sampleNumber) {
		this.sortedSamples = new double[sampleNumber];
		Random random = new Random();
		// fill the
		for (int i = 0; i < sampleNumber; i++)
			sortedSamples[i] = random.nextGaussian();
		Arrays.sort(this.sortedSamples);

		this.samples = new TreeSet<>(); // using Double's natural order
		for (int i = 0; i < sampleNumber ; i++)
			samples.add(random.nextGaussian());
	}


	public double oddsLessThan(double number){
		/*
		looking into JDK, the operations below should not take more than O(logN).
		The size is already assigned by the time the sub-SortedSet is initialised.
		Same approach of a binary, search without equivalence requirement.
		 */
		SortedSet<Double> doubles = this.samples.headSet(number);
		return (double) doubles.size() / this.samples.size();
	}

}

package com.crypto.portfolio.app.utils;

import java.math.MathContext;
import java.math.RoundingMode;

public class Constants {

	static final MathContext INTERNAL_PRICE_CONTEXT = new MathContext(13, RoundingMode.HALF_UP);
	static final MathContext EXTERNAL_PRICE_CONTEXT = new MathContext(10, RoundingMode.HALF_UP);
}

package com.crypto.portfolio.app;

import com.crypto.portfolio.api.EuOption;


public class CalcUtilsForCallOptionTest extends CalcUtilsForOptionAbstractTest{

	public CalcUtilsForCallOptionTest() {
		super(EuOption.OptionType.CALL, true);
	}
}
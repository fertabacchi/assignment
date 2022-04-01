package com.crypto.portfolio.app;


import com.crypto.portfolio.api.EuOption;

public class CalcUtilsForPutOptionTest extends CalcUtilsForOptionAbstractTest{

	public CalcUtilsForPutOptionTest() {
		super(EuOption.OptionType.PUT, false);
	}
}
package com.schneenet.android.common;

public class Utils
{

	/**
	 * Round a float to the next highest int
	 * 
	 * ceil(0.65) = 1
	 * ceil(658.7) = 659
	 * ceil(3.14159) = 4
	 * 
	 * How it works:
	 * Add 0.5 to the float value and call Math.round
	 * 
	 * 0.65 + 0.5 = 1.15
	 * round(1.15) = 1
	 * 
	 * 658.7 + 0.5 = 659.2
	 * round(659.2) = 659
	 * 
	 * 3.14159 + 0.5 = 3.65159
	 * round(3.65159) = 4
	 * 
	 * @param n Number to ceil
	 * @return integer 
	 */
	public static int ceil(float n)
	{
		return Math.round(n + 0.5f);
	}
	
}

package com.cordys.coe.test;

import java.util.Date;

/**
 * @author pgussow
 *
 */
public class TestClass
{
	/**
	 * Main method.
	 *
	 * @param saArguments The commandline arguments.
	 */
	public static void main(String[] saArguments)
	{
		try
		{
			System.out.println(new Date(1229101025601L));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}

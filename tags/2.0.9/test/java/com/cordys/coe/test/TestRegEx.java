package com.cordys.coe.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author pgussow
 *
 */
public class TestRegEx
{
	/**
	 * Main method.
	 *
	 * @param saArguments Commandline arguments.
	 */
	public static void main(String[] saArguments)
	{
		try
		{
			String sSource = "\nLine 1\nLine2\n\nFinal";
			
			Pattern p = Pattern.compile("(?<!\r)\n");
			
			Matcher m = p.matcher(sSource);
			while (m.find())
			{
				System.out.println("Found at: " + m.start() + ": " + m.group());
			}
			
			m = p.matcher(sSource);
			String sReplaced = m.replaceAll("\r\n");
			System.out.println(sReplaced);
			
			sReplaced = sSource.replaceAll("(?<!\r)\n", "\r\n");
			System.out.println(sReplaced);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}

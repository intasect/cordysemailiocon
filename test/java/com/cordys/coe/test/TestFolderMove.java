package com.cordys.coe.test;

import java.io.File;

/**
 * @author pgussow
 *
 */
public class TestFolderMove
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
			File fTemp = new File("d:/temp");
			File fBackup = new File(fTemp, "backup");
			fBackup.mkdirs();
			
			File fTest = new File(fTemp, "dddd");
			fTest.mkdirs();
			new File(fTest, "iets.txt").createNewFile();
			
			fTest.renameTo(new File(fBackup, fTest.getName()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}

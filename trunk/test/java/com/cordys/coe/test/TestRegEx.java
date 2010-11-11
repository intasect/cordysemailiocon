
 /**
 * Copyright 2007 Cordys R&D B.V. 
 * 
 * This file is part of the Cordys Email IO Connector. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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


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

/**
 * This class produces and OutOfMemory Error.
 *
 * @author  pgussow
 */
public class TestStringBuffer
{
    /**
     * DOCUMENTME.
     *
     * @param   args  DOCUMENTME
     *
     * @throws  Exception  DOCUMENTME
     */
    public static void main(String[] args)
                     throws Exception
    {
        StringBuffer sb = new StringBuffer();
        String s = "foobar";
        String which = args[1];

        for (int i = 0; i < Integer.parseInt(args[0]); i++)
        {
            if (which.equals("sb"))
            {
                sb.append(s);
            }
            else
            {
                s += s;
            }

            System.out.println(System.currentTimeMillis() + ": " + i);
			Thread.sleep(500);
        }
    }
}

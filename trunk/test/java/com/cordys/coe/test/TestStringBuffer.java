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

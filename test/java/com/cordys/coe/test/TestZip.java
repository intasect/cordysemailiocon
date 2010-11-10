package com.cordys.coe.test;

import com.cordys.coe.util.FileUtils;

import com.eibus.util.system.Native;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import java.util.Random;

import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * DOCUMENTME .
 *
 * @author  pgussow
 */
public class TestZip
{
    /**
     * DOCUMENTME.
     */
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * Main method.
     *
     * @param  saArguments  The commandline arguments.
     */
    public static void main(String[] saArguments)
    {
        try
        {
            // Create the big data stream.
            int iMaxCount = 1024 * 200;
            StringBuilder sbBuilder = new StringBuilder(iMaxCount);
            Random r = new Random(System.currentTimeMillis());

            for (int iCount = 0; iCount < iMaxCount; iCount++)
            {
                sbBuilder.append(CHARACTERS.charAt(r.nextInt(CHARACTERS.length())));
            }

            String sData = sbBuilder.toString();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(baos);
            gzip.write(sData.getBytes());
            gzip.finish();

            byte[] baData = baos.toByteArray();
            String sBase64 = new String(Native.encodeBinBase64(baData, baData.length));

            FileOutputStream fos = new FileOutputStream("build/test.txt");
            fos.write(sBase64.getBytes());
            fos.close();

            String sContent = new String(FileUtils.readFileContents(new File("build/test.txt")));
            byte[] baDecoded = Native.decodeBinBase64(sContent.getBytes(), sContent.length());
            ByteArrayInputStream bais = new ByteArrayInputStream(baDecoded);

            GZIPInputStream zis = new GZIPInputStream(bais);
            zis.available();

            byte[] baBuffer = new byte[1024 * 64];
            baos = new ByteArrayOutputStream();

            int iRead = zis.read(baBuffer);

            while (iRead > -1)
            {
                baos.write(baBuffer, 0, iRead);
                iRead = zis.read(baBuffer);
            }

            String sOther = baos.toString();
            System.out.println("The same: " + sOther.equals(sData));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

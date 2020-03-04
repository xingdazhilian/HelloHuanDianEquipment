package client.NewElectric.app10000;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Author:      Lee Yeung
 * Create Date: 2020-02-24
 * Description:
 */
public class FileUtil
{
    private static final int BYTE_BUF_SIZE = 1024;

    public static void copy(InputStream inputStream, String targetName) throws IOException
    {
        FileOutputStream outputStream = null;

        try
        {
            File targetFile = new File(targetName);
            if (!targetFile.getParentFile().exists())
            {
                targetFile.getParentFile().mkdirs();
            }

            if (targetFile.exists())
            {
                targetFile.delete();
            }

            if (!targetFile.exists())
            {
                targetFile.createNewFile();
            }
            outputStream = new FileOutputStream(targetFile, false);
            copy(inputStream, outputStream);
        }
        finally
        {
            if (outputStream != null)
            {
                outputStream.close();
            }
            if (inputStream != null)
            {
                inputStream.close();
            }
        }
    }

    private static void copy(InputStream from, OutputStream to) throws IOException
    {
        byte[] buf = new byte[BYTE_BUF_SIZE];
        while (true)
        {
            int r = from.read(buf);
            if (r == -1)
            {
                break;
            }
            to.write(buf, 0, r);
        }
    }
}

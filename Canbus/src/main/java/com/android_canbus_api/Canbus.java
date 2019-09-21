package com.android_canbus_api;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-18
 * Description:
 */
class Canbus
{
    static
    {
        System.loadLibrary("Canbus");
    }

    private final byte[] emptyData = new byte[0];
    // TODO: 2019-08-21 协议规定16长度
    private final byte[] serialPortBuffer = new byte[16];

    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    final void openCan()
    {
        try
        {
            mFd = open();
        }
        catch (Exception e)
        {
            throw e;
        }
        if (mFd == null)
        {
            throw new NullPointerException("文件描述符为null");
        } else
        {
            mFileInputStream = new FileInputStream(mFd);
            mFileOutputStream = new FileOutputStream(mFd);
        }
    }

    private native static FileDescriptor open();

    private native static void close();

    byte[] read() throws IOException
    {
        if (mFileInputStream != null)
        {
            mFileInputStream.read(serialPortBuffer);
            return serialPortBuffer;
        }

        return emptyData;
    }

    void write(byte[] data) throws IOException
    {
        if (mFileOutputStream != null)
        {
            mFileOutputStream.write(data);
            mFileOutputStream.flush();
        }
    }


    public void closeCan()
    {
        if (mFileInputStream != null)
        {
            try
            {
                mFileInputStream.close();
                mFileInputStream = null;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        if (mFileOutputStream != null)
        {
            try
            {
                mFileOutputStream.close();
                mFileOutputStream = null;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        mFd = null;
    }
}

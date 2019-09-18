package android_serialport_api;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Author:      Lee Yeung
 * Create Date: 2018/11/5
 * Description: 串口设备
 */
class SerialPort
{
    static
    {
        System.loadLibrary("serial_port");
    }

    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    private final byte[] emptyData = new byte[0];

    public SerialPort(String serialPortPath, int serialPortRate) throws Exception
    {
        File device = new File(serialPortPath);

        if (device != null && device.exists())
        {
            if (!device.canRead() || !device.canWrite())
            {
                try
                {
                    Process su;
                    su = Runtime.getRuntime().exec("su");
                    String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
                    su.getOutputStream().write(cmd.getBytes());
                    if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite())
                    {
                        throw new SecurityException();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    throw e;
                }
            }

            try
            {
                mFd = open(device.getAbsolutePath(), serialPortRate, 0);
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
        } else
        {
            throw new RuntimeException("串口设备不存在！");
        }
    }

    byte[] read() throws IOException
    {
        if (mFileInputStream != null)
        {
            // TODO: 2019-08-21 available方法陷阱： 一部分InputStream的实现类会返回整个流的字节数，而一部分并不是。
            final int size = mFileInputStream.available();
            if (size > 0)
            {
                byte[] serialPortBuffer = new byte[size];
                mFileInputStream.read(serialPortBuffer);
                return serialPortBuffer;
            }
        }

        return emptyData;
    }

    void write(byte[] data) throws IOException
    {
        if (mFileOutputStream != null)
        {
            mFileOutputStream.write(data);
        }
    }

    private native static FileDescriptor open(String path, int baudrate, int flags);

    private native void close();

    public void closeSerial()
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

        close();
        mFd = null;
    }
}

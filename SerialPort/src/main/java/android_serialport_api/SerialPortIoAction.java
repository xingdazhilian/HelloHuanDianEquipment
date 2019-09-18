package android_serialport_api;

import java.io.IOException;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-10
 * Description:
 */
public interface SerialPortIoAction
{
    void write(byte[] data) throws IOException;

    byte[] read() throws IOException;
}

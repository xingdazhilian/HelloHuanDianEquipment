package com.hellohuandian.apps.controllerlibrary;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-10
 * Description:
 */
public interface DeviceIoAction
{
    int SERIAL_PORT = 100;
    int CANBUS = 200;

    @IntDef({SERIAL_PORT, CANBUS})
    @Retention(RetentionPolicy.SOURCE)
    @interface Protocol
    {

    }

    void write(byte[] data) throws IOException;

    byte[] read() throws IOException;

    /**
     * io通讯协议，串口或者canbus
     *
     * @return
     */
    @Protocol
    int ioProtocol();
}

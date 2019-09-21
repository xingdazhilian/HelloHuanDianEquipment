package com.android_canbus_api;

import java.io.IOException;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-18
 * Description:
 */
public interface CanIoAction
{
    void write(byte[] data) throws IOException;

    byte[] read() throws IOException;
}

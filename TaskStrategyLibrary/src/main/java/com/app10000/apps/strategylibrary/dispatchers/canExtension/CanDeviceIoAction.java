package com.app10000.apps.strategylibrary.dispatchers.canExtension;

import com.app10000.apps.controllerlibrary.DeviceIoAction;

import androidx.core.util.Consumer;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-23
 * Description:
 */
public interface CanDeviceIoAction extends DeviceIoAction
{
    void registerTimeOut(int id, long timeOutValue);

    void register(int id, Consumer<byte[]> consumer);

    void unRegister(final int id);
}

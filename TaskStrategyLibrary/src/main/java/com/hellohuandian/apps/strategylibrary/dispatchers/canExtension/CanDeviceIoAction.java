package com.hellohuandian.apps.strategylibrary.dispatchers.canExtension;

import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;

import androidx.core.util.Consumer;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-23
 * Description:
 */
public interface CanDeviceIoAction extends DeviceIoAction
{
    void register(final int id, Consumer<byte[]> consumer);

    void unRegister(final int id);
}

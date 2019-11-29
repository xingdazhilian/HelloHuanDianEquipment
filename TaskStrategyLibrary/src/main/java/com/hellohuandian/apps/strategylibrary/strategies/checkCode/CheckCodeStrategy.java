package com.hellohuandian.apps.strategylibrary.strategies.checkCode;

import android.text.TextUtils;

import com.hellohuandian.apps.controllerlibrary.DeviceIoAction;
import com.hellohuandian.apps.strategylibrary.dispatchers.canExtension.CanDeviceIoAction;
import com.hellohuandian.apps.strategylibrary.strategies._base.ProtocolStrategy;
import com.hellohuandian.apps.utillibrary.StringFormatHelper;
import com.orhanobut.logger.Logger;

import java.io.IOException;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-11-28
 * Description: 写电池校验码策略
 * <p>
 * /*
 * 10 05 05 00 0D 00 01 FF
 * 20 FF FF FF FF FF FF FF
 * 21 crc crc
 * <p>
 * 格式：can(长度8)+(10/20/21)+xxxxxxx(长度7)    总长度是16必须。
 */
public class CheckCodeStrategy extends ProtocolStrategy
{
    private String checkCode;

    public CheckCodeStrategy(byte address)
    {
        super(address);
    }

    public void setCheckCode(String checkCode)
    {
        this.checkCode = checkCode;
    }

    @Override
    protected void execute_sp(DeviceIoAction deviceIoAction)
    {

    }

    @Override
    protected void execute_can(final CanDeviceIoAction deviceIoAction)
    {
        if (!TextUtils.isEmpty(checkCode) && checkCode.trim().length() == 8)
        {
            String upperCaseCheckCode = checkCode.trim().toUpperCase();
            System.out.println("校验码：" + upperCaseCheckCode);

            // TODO: 2019-11-29 定义校验码原始数据
            byte[] CHECK_CODE_BYTES = new byte[]{address, 0x05, 0x00, 0x0D, 0x00, 0x01
                    , 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00//校验码数据
                    , 0x00, 0x00 /*crc校验位*/
            };
            for (int i = 6, j = 0, len = 6 + 8; i < len; i++, j++)
            {
                CHECK_CODE_BYTES[i] = (byte) upperCaseCheckCode.charAt(j);
            }
            short crc = crc16(CHECK_CODE_BYTES, 0, CHECK_CODE_BYTES.length - 2);
            CHECK_CODE_BYTES[CHECK_CODE_BYTES.length - 2] = (byte) (crc & 0xFF);
            CHECK_CODE_BYTES[CHECK_CODE_BYTES.length - 1] = (byte) (crc >> 8 & 0xFF);
            System.out.println("校验码元数据：" + StringFormatHelper.getInstance().toHexString(CHECK_CODE_BYTES));
            // TODO: 2019-11-29 can通讯指令
            int index = 6;
            final byte[] CHECK_CODE = {address, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00,
                    0x10, address, 0x05, 0x00, 0x0D, 0x00, 0x01, CHECK_CODE_BYTES[index++]};

            // TODO: 2019-11-28 1：第1帧数据
            System.out.println("校验码第1帧：" + StringFormatHelper.getInstance().toHexString(CHECK_CODE));
            try
            {
                deviceIoAction.write(CHECK_CODE);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            // TODO: 2019-11-28 2：第2帧数据
            CHECK_CODE[8] = 0x20;
            for (int i = 9, len = CHECK_CODE.length; i < len; i++)
            {
                CHECK_CODE[i] = CHECK_CODE_BYTES[index++];
            }
            System.out.println("校验码第2帧：" + StringFormatHelper.getInstance().toHexString(CHECK_CODE));
            try
            {
                deviceIoAction.write(CHECK_CODE);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            // TODO: 2019-11-28 3：第3帧数据
            CHECK_CODE[8] = 0x21;
            CHECK_CODE[9] = CHECK_CODE_BYTES[index++];
            CHECK_CODE[10] = CHECK_CODE_BYTES[index];
            for (int i = 11, len = CHECK_CODE.length; i < len; i++)
            {
                CHECK_CODE[i] = 0x00;
            }
            System.out.println("校验码第3帧：" + StringFormatHelper.getInstance().toHexString(CHECK_CODE));
            try
            {
                deviceIoAction.write(CHECK_CODE);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            //            Logger.i("写入" + checkCode);
        }
    }
}


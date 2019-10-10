package com.hellohuandian.apps.strategylibrary.strategies._base;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-09-16
 * Description:
 */
public abstract class BaseStrategy extends TaskStrategy
{
    public BaseStrategy(byte address)
    {
        super(address);
    }

    protected final void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 计算校验和
     *
     * @param data
     * @param start
     * @param end
     *
     * @return
     */
    protected final int calculateSum(byte[] data, int start, int end)
    {
        int sum = 0;
        if (data != null && start >= 0 && end >= start && end < data.length)
        {
            for (int s = start; s <= end; sum += data[s] & 0xFF, s++) ;
        }
        return sum;
    }

    /**
     * CRC-32/MPEG-2
     *
     * @param data
     *
     * @return
     */
    protected final int crc32(byte[] data)
    {
        int crcVal = 0xFFFFFFFF;
        int k;
        for (byte item : data)
        {
            crcVal ^= item << 24;
            for (k = 0; k < 8; k++, crcVal = ((crcVal & 0x80000000) != 0) ? ((crcVal << 1) ^ 0x04C11DB7) : (crcVal << 1)) ;
        }
        return crcVal;
    }

    /**
     * CRC-16/MODBUS
     *
     * @param data
     * @param offset
     * @param len
     *
     * @return
     */
    protected final short crc16(byte[] data, int offset, int len)
    {
        int crc = 0xFFFF;
        int j;
        for (int i = offset; i < len; i++)
        {
            crc = ((crc & 0xFF00) | (crc & 0x00FF) ^ (data[i] & 0xFF));
            for (j = 0; j < 8; j++, crc = ((crc & 0x0001) > 0) ? (crc >> 1) ^ 0xA001 : (crc >> 1)) ;
        }

        return (short) (crc & 0xFFFF);
    }
}

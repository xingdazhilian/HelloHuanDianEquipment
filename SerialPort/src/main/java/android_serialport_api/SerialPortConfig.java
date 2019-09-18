package android_serialport_api;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-07-24
 * Description:
 */
public class SerialPortConfig
{
    private String serialPortPath = "/dev/ttyS4";
    private int serialPortRate = 9600;

    public SerialPortConfig()
    {
    }

    public SerialPortConfig(String serialPortPath, int serialPortRate)
    {
        this.serialPortPath = serialPortPath;
        this.serialPortRate = serialPortRate;
    }

    public String getSerialPortPath()
    {
        return serialPortPath;
    }

    public int getSerialPortRate()
    {
        return serialPortRate;
    }

}

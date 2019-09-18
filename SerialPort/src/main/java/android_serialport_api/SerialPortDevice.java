package android_serialport_api;

import java.io.IOException;

import androidx.core.util.Consumer;

/**
 * Author:      Lee Yeung
 * Create Date: 2019-07-25
 * Description:
 */
public class SerialPortDevice extends SerialPort
{
    private final SerialPortIoAction ioAction = new SerialPortIoAction()
    {
        @Override
        public void write(byte[] data) throws IOException
        {
            SerialPortDevice.this.write(data);
        }

        @Override
        public byte[] read() throws IOException
        {
            return SerialPortDevice.this.read();
        }
    };

    public SerialPortDevice(SerialPortConfig serialPortConfig) throws Exception
    {
        super(serialPortConfig.getSerialPortPath(), serialPortConfig.getSerialPortRate());
    }

    public final <T extends Consumer<SerialPortIoAction>> void doIoAction(T ioConsumer)
    {
        if (ioConsumer != null)
        {
            ioConsumer.accept(ioAction);
        }
    }
}







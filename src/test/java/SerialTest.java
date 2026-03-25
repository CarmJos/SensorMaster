import cc.carm.app.sensormaster.data.SerialData;
import com.fazecast.jSerialComm.SerialPort;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialTest {

//    @Test
    public void test() throws InterruptedException {
//        01 06 00 3E 00 54 E9 F9
        SerialData data = SerialData.of(0x5A, 0x03, 0x00, 0x00, 0x00, 0x02);
        System.out.println("Test SerialData: " + data);

        SerialPort port = SerialPort.getCommPort("COM3");

        if (!port.openPort()) {
            return;
        }

        port.setBaudRate(9600);
        port.setNumDataBits(8);
        port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        port.setParity(SerialPort.NO_PARITY);

        // 发送  并等待回复
        try {
            // 发送设备控制指令
            OutputStream outputStream = port.getOutputStream();
            outputStream.write(data.raw());
            outputStream.flush();
            System.out.println("控制指令已发送");

            // 等待并读取设备反馈
            Thread.sleep(100);  // 等待设备处理
            InputStream inputStream = port.getInputStream();
            byte[] responseBuffer = new byte[256];
            int bytesRead = inputStream.read(responseBuffer);

            if (bytesRead > 0) {
                SerialData response = SerialData.of(responseBuffer);
                System.out.println("设备反馈: " + response);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("数据传输错误: " + e.getMessage());
        } finally {
            // 及时关闭串口资源
            port.closePort();
            System.out.println("串口已关闭");
        }
        System.out.println("Test completed");
    }

}

package cc.carm.app.sensormaster.serial;

import cc.carm.app.sensormaster.Main;
import cc.carm.app.sensormaster.data.SerialData;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_AVAILABLE;

public abstract class IntervalSerialListener implements SerialPortDataListener {

    protected final long interval; // 当接受到首条消息后等待的读取时间

    private volatile byte[] buffer = new byte[0]; // 数据缓冲区
    private volatile ScheduledFuture<?> timeoutTask = null; // 超时任务
    private final Object lock = new Object(); // 同步锁
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public IntervalSerialListener(long interval) {
        this.interval = interval;
    }

    @Override
    public int getListeningEvents() {
        return LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        SerialPort serialPort = event.getSerialPort();
        byte[] newData = new byte[serialPort.bytesAvailable()];
        int numRead = serialPort.readBytes(newData, newData.length);
        if (numRead == 0) return;

        synchronized (lock) {
            // 将新数据追加到 buffer
            byte[] oldBuffer = buffer;
            buffer = new byte[oldBuffer.length + numRead];
            System.arraycopy(oldBuffer, 0, buffer, 0, oldBuffer.length);
            System.arraycopy(newData, 0, buffer, oldBuffer.length, numRead);

            Main.LOGGER.info("Buffered data, total buffer size: {} bytes", buffer.length);

            // 取消现有的超时任务
            if (timeoutTask != null) {
                timeoutTask.cancel(false);
                Main.LOGGER.debug("Cancelled previous timeout task, resetting timer...");
            }

            // 创建新的超时任务
            timeoutTask = executor.schedule(() -> {
                synchronized (lock) {
                    if (buffer.length > 0) {       // 在超时时间到达时处理缓冲区的数据
                        Main.LOGGER.info("Interval timeout reached, processing buffered data ({} bytes)", buffer.length);

                        // 去除buffer末尾的0数据
                        int validLength = buffer.length;
                        while (validLength > 0 && buffer[validLength - 1] == 0) {
                            validLength--;
                        }

                        if (validLength > 0) {
                            // 创建不包含末尾0的新数组
                            byte[] trimmedBuffer = new byte[validLength];
                            System.arraycopy(buffer, 0, trimmedBuffer, 0, validLength);
                            SerialData response = SerialData.of(trimmedBuffer);
                            if (response != null) handle(response);
                        }
                        buffer = new byte[0]; // 清空缓冲区
                    }
                    timeoutTask = null;
                }
            }, interval, TimeUnit.MILLISECONDS);
        }
    }

    public abstract void handle(@NotNull SerialData data);

    /**
     * 清理资源，取消任何待处理的超时任务
     */
    public void close() {
        synchronized (lock) {
            if (timeoutTask != null) {
                timeoutTask.cancel(true);
                timeoutTask = null;
                Main.LOGGER.debug("Cancelled timeout task in IntervalSerialListener");
            }
            buffer = new byte[0]; // 清空缓冲区
        }
    }

}

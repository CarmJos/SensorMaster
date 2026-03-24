package cc.carm.app.sensormaster.controller;

import cc.carm.app.sensormaster.data.SerialData;
import cc.carm.app.sensormaster.type.SensorType;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.concurrent.*;
import java.util.function.BiConsumer;

public abstract class SerialController<DATA> {

    private static final Logger LOGGER = LogManager.getLogger(SerialController.class);
    protected static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    public static <DATA> SerialController<DATA> create(@NotNull SerialPort serialPort, @NotNull SensorType<DATA> sensorType,
                                                       @NotNull BiConsumer<DATA, String> dataConsumer) {
        return new SerialController<>(serialPort, sensorType) {
            @Override
            public void handleData(@NotNull DATA data, @NotNull String dataText) {
                dataConsumer.accept(data, dataText);
            }
        };
    }

    protected final @NotNull SerialPort serialPort;
    protected final @NotNull SensorType<DATA> sensorType;

    protected @Nullable Integer address;
    protected @Nullable ScheduledFuture<?> refreshTask; // 自动刷新任务

    public SerialController(@NotNull SerialPort serialPort, @NotNull SensorType<DATA> sensorType) {
        this.serialPort = serialPort;
        this.sensorType = sensorType;
        this.refreshTask = null;
    }

    public abstract void handleData(@NotNull DATA data, @NotNull String text);

    public boolean connect() {
        serialPort.setBaudRate(SerialSettings.BAUD_RATE);
        serialPort.setNumDataBits(SerialSettings.DATA_BITS);
        serialPort.setNumStopBits(SerialSettings.STOP_BITS);
        serialPort.setParity(SerialSettings.PARITY);

        LOGGER.info("Try to connect [{}] ...", serialPort.getSystemPortPath());
        if (!serialPort.openPort()) return false;

        LOGGER.info("Connected to [{}]", serialPort.getSystemPortPath());
        serialPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;
                byte[] newData = new byte[serialPort.bytesAvailable()];
                int numRead = serialPort.readBytes(newData, newData.length);
                if (numRead == 0) return;
                SerialData response = SerialData.of(newData);
                if (response == null) return;

                try {
                    DATA parsed = sensorType.handleResponse(response);
                    if (parsed != null) {
                        address = response.unsignedAddress();
                        handleData(parsed, sensorType.formatData(parsed));
                    }
                } catch (Exception ex) {
                    LOGGER.error("Failed to parse response [{}]: {}", response, ex.getMessage());
                }
            }
        });

        fetchAddress();
        return true;
    }

    public @Nullable Integer currentAddress() {
        return address;
    }

    public CompletableFuture<Void> fetchAddress() {
        LOGGER.info("Fetching address for sensor type [{}] ...", sensorType.name());
        return CompletableFuture.runAsync(() -> {
            // 从 0 到 255 发送请求，并等待首次返回数据
            for (int i = 0; i < 255; i++) {
                send(sensorType.generateRequest(i));
            }
        });
    }

    public CompletableFuture<Void> refresh() {
        if (address == null) {
            LOGGER.warn("Address is unknown, cannot refresh data. Please fetch address first.");
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            if (address == null) return;
            LOGGER.info("Requesting a data refresh for address [{}] ...", address);
            send(sensorType.generateRequest(address));
        });
    }

    public CompletableFuture<Void> updateAddress(@Range(from = 1, to = 255) int newAddress) {
        if (address == null) {
            LOGGER.warn("Address is unknown, cannot update address. Please fetch address first.");
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            LOGGER.info("Updating address from [{}] to [{}] ...", address, newAddress);
            send(sensorType.modifyAddress(address, newAddress));
        });
    }

    public void autoRefresh(long interval) {
        // 先取消现有的刷新任务
        if (refreshTask != null) {
            refreshTask.cancel(true);
            refreshTask = null;
            LOGGER.info("Stopped current refresh task.");
        }

        // 如果 interval > 0，创建新的定时任务
        if (interval > 0) {
            refreshTask = SCHEDULER.scheduleAtFixedRate(() -> {
                if (address == null) return;
                send(sensorType.generateRequest(address));
            }, interval, interval, TimeUnit.MILLISECONDS);
            LOGGER.info("Started refresh task with interval: {} ms", interval);
        }
    }

    public void close() {
        // 先停止刷新任务
        if (this.refreshTask != null) {
            this.refreshTask.cancel(false);
            this.refreshTask = null;
            LOGGER.info("Stopped refresh task.");
        }

        // 关闭串口
        if (serialPort.isOpen()) {
            serialPort.closePort();
            LOGGER.info("Closed serial port [{}]", serialPort.getSystemPortPath());
        }
    }

    protected boolean send(SerialData data) {
        if (!serialPort.isOpen()) return false;
        byte[] raw = data.raw();
        int bytesWritten = serialPort.writeBytes(raw, raw.length);
        if (bytesWritten == raw.length) {
            LOGGER.info("Sent data {{}}", data);
            return true;
        } else {
            LOGGER.error("Failed to send all data.");
            return false;
        }
    }

}


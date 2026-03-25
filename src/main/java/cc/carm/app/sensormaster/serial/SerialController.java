package cc.carm.app.sensormaster.serial;

import cc.carm.app.sensormaster.data.SerialData;
import cc.carm.app.sensormaster.type.SensorType;
import com.fazecast.jSerialComm.SerialPort;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

public abstract class SerialController<DATA> {

    private static final Logger LOGGER = LogManager.getLogger(SerialController.class);

    public static <DATA> SerialController<DATA> create(@NotNull SerialPort serialPort, @NotNull SensorType<DATA> sensorType,
                                                       @NotNull BiConsumer<DATA, String> dataConsumer) {
        return new SerialController<DATA>(serialPort, sensorType) {
            @Override
            public void handleData(@NotNull DATA data, @NotNull String dataText) {
                dataConsumer.accept(data, dataText);
            }
        };
    }

    protected final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

    protected final @NotNull SerialPort serialPort;
    protected final @NotNull SensorType<DATA> sensorType;

    protected @Nullable Integer address;
    protected @Nullable ScheduledFuture<?> refreshTask; // 自动刷新任务

    public SerialController(@NotNull SerialPort serialPort, @NotNull SensorType<DATA> sensorType) {
        this.serialPort = serialPort;
        this.sensorType = sensorType;
        this.refreshTask = null;
    }

    public @NotNull SerialPort getSerialPort() {
        return this.serialPort;
    }

    public @NotNull SensorType<DATA> getSensorType() {
        return this.sensorType;
    }

    public abstract void handleData(@NotNull DATA data, @NotNull String text);

    public boolean connect() {

        LOGGER.info("Try to connect [{}] ...", serialPort.getSystemPortPath());
        if (!serialPort.openPort()) return false;
        LOGGER.info("Connected to [{}]", serialPort.getSystemPortPath());
        serialPort.setComPortParameters(
                9600, 8,
                SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY
        );
        serialPort.flushDataListener();
        serialPort.addDataListener(new IntervalSerialListener(100) {
            @Override
            public void handle(@NotNull SerialData response) {
                LOGGER.info("Received raw data [{}].", response);
                try {
                    DATA parsed = sensorType.handleResponse(response);
                    if (parsed != null) {
                        if (address == null || address != response.unsignedAddress()) {
                            address = response.unsignedAddress();
                            LOGGER.info("Detected address [{}] for sensor type [{}]", address, sensorType.name());
                        }
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
            int defaultAddress = sensorType.defaultAddress();
            // 先尝试默认地址
            send(sensorType.generateRequest(defaultAddress));
            try {
                Thread.sleep(250);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (this.address != null) return; // 已找到地址，结束。
            for (int i = 1; i < 256; i++) {
                if (i == defaultAddress) continue;
                send(sensorType.generateRequest(i));
                try {
                    Thread.sleep(250);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (this.address != null) return; // 已找到地址，结束。
            }
        }, executor);
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
            LOGGER.info(
                    "Updating address from [{}] to [{}] ...",
                    String.format("%02X", currentAddress()),
                    String.format("%02X", newAddress)
            );
            send(sensorType.modifyAddress(address, newAddress));
            this.address = newAddress;
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
            refreshTask = executor.scheduleAtFixedRate(() -> {
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

        // 关闭线程池
        if (!executor.isShutdown()) {
            executor.shutdownNow();
            LOGGER.info("Shutdown executor service.");
        }

        // 关闭串口
        if (serialPort.isOpen()) {
            serialPort.removeDataListener();
            serialPort.closePort();
            LOGGER.info("Closed serial port [{}]", serialPort.getSystemPortPath());
        }

        this.address = null;
    }

    protected boolean send(@NotNull SerialData data) {
        if (!serialPort.isOpen()) return false;
        try (OutputStream out = serialPort.getOutputStream()) {
            out.write(data.raw());
            out.flush();
            LOGGER.info("Sent data {{}}", data);
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to send data [{}]: {}", data, e.getMessage());
            return false;
        }
    }

}


package cc.carm.app.sensormaster.type.impl;

import cc.carm.app.sensormaster.data.SerialData;
import cc.carm.app.sensormaster.type.SensorType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.function.BiFunction;
import java.util.function.Function;

public class NumericSensorType implements SensorType<Double> {

    public static Builder create(@NotNull String name) {
        return new Builder(name);
    }

    protected final @NotNull String name;
    protected final @NotNull String unit;

    protected final int dataStartIndex; // 数据位开始index（含）
    protected final int dataEndIndex;// 数据位结束index（含）

    protected final int[] requestCommand;
    protected final BiFunction<SerialData, Double, Double> dataHandler;

    public NumericSensorType(@NotNull String name, @NotNull String unit,
                             int dataStartIndex, int dataEndIndex,
                             int[] requestCommand,
                             BiFunction<SerialData, Double, Double> dataHandler) {
        this.name = name;
        this.unit = unit;
        this.dataStartIndex = dataStartIndex;
        this.dataEndIndex = dataEndIndex;
        this.requestCommand = requestCommand;
        this.dataHandler = dataHandler;
    }

    @Override
    public @NotNull String name() {
        return name;
    }

    @Override
    public @NotNull SerialData generateRequest(@Range(from = 0, to = 255) int address) {
        return SerialData.of(address, requestCommand);
    }

    @Override
    public @Nullable Double handleResponse(SerialData response) {
        if (response == null || response.length() < dataEndIndex) return null;
        int dataRaw = 0;
        // 从 dataStartIndex 到 dataEndIndex（含）依次读取字节，组合成整数
        for (int i = dataStartIndex; i <= dataEndIndex; i++) {
            int byteValue = response.read(i);
            dataRaw = (dataRaw << 8) | byteValue;
        }
        return dataHandler.apply(response, (double) dataRaw);
    }

    @Override
    public @NotNull String formatData(@NotNull Double data) {
        return String.format("%.1f %s", data, unit);
    }


    @Override
    public String toString() {
        return name;
    }

    public static class Builder {

        protected final String name;
        protected String unit = ".";
        protected int start = 2;
        protected int end = 3;
        protected int[] requestCommand = new int[0];
        protected BiFunction<SerialData, Double, Double> dataHandler = (data, raw) -> raw;

        public Builder(String name) {
            this.name = name;
        }

        public Builder unit(@NotNull String unit) {
            this.unit = unit;
            return this;
        }

        public Builder dataIndices(int start, int end) {
            this.start = start;
            this.end = end;
            return this;
        }

        public Builder requestCommand(int... command) {
            this.requestCommand = command;
            return this;
        }

        public Builder handleData(BiFunction<SerialData, Double, Double> handler) {
            this.dataHandler = handler;
            return this;
        }

        public Builder handleData(Function<Double, Double> handler) {
            return handleData((data, raw) -> handler.apply(raw));
        }

        public NumericSensorType build() {
            return new NumericSensorType(name, unit, start, end, requestCommand, dataHandler);
        }

    }

}

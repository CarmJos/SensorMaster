package cc.carm.app.sensormaster.type.impl;

import cc.carm.app.sensormaster.data.SerialData;
import cc.carm.app.sensormaster.type.SensorType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;

public class Thermohygrometer implements SensorType<Thermohygrometer.Data> {

    public static Thermohygrometer create(@NotNull String name, int... requestCommand) {
        return new Thermohygrometer(name, requestCommand);
    }

    protected final @NotNull String name;
    protected final int[] requestCommand;

    public Thermohygrometer(@NotNull String name, int[] requestCommand) {
        this.name = name;
        this.requestCommand = requestCommand;
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
    public @Nullable Data handleResponse(SerialData response) {
        if (response == null || response.length() < 6) return null;
        if (!response.validate(0, 3) || !response.validate(1, 4)) return null;
        int humidityRaw = (response.read(2) << 8) | response.read(3);
        int tempRaw = (response.read(4) << 8) | response.read(5);
        return new Data(tempRaw / 10.0, humidityRaw / 10.0);
    }

    @Override
    public @NotNull String formatData(@NotNull Thermohygrometer.Data data) {
        return data.temperature + " ℃ / " + data.humidity + " %RH";
    }

    @Override
    public String toString() {
        return name;
    }

    public static final class Data {
        private final double temperature;
        private final double humidity;

        public Data(double temperature, double humidity) {
            this.temperature = temperature;
            this.humidity = humidity;
        }

        public double temperature() {
            return temperature;
        }

        public double humidity() {
            return humidity;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            Data that = (Data) obj;
            return Double.doubleToLongBits(this.temperature) == Double.doubleToLongBits(that.temperature) &&
                    Double.doubleToLongBits(this.humidity) == Double.doubleToLongBits(that.humidity);
        }

        @Override
        public int hashCode() {
            return Objects.hash(temperature, humidity);
        }

        @Override
        public String toString() {
            return "Data[" +
                    "temperature=" + temperature + ", " +
                    "humidity=" + humidity + ']';
        }

    }

}

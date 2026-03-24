package cc.carm.app.sensormaster.type.impl;

import cc.carm.app.sensormaster.data.SerialData;
import cc.carm.app.sensormaster.type.SensorType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

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

    public record Data(double temperature, double humidity) {
    }

}

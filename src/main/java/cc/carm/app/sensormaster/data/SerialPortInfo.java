package cc.carm.app.sensormaster.data;

import com.fazecast.jSerialComm.SerialPort;
import org.jetbrains.annotations.NotNull;

public record SerialPortInfo(SerialPort port) {

    public static SerialPortInfo of(SerialPort port) {
        return new SerialPortInfo(port);
    }

    public String id() {
        return port.getSystemPortName();
    }

    public String description() {
        return port.getPortDescription();
    }

    @Override
    public @NotNull String toString() {
        return "[" + id() + "] " + description();
    }
}

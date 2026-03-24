package cc.carm.app.sensormaster.data;

import com.fazecast.jSerialComm.SerialPort;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class SerialPortInfo {
    private final SerialPort port;

    public SerialPortInfo(SerialPort port) {
        this.port = port;
    }

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

    public SerialPort port() {
        return port;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        SerialPortInfo that = (SerialPortInfo) obj;
        return Objects.equals(this.port, that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(port);
    }

}

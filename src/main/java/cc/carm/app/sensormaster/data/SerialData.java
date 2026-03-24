package cc.carm.app.sensormaster.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public record SerialData(byte address, byte[] data, byte[] check) {

    public static @NotNull SerialData of(int address, int... data) {
        byte addressByte = (byte) (address & 0xFF);
        byte[] dataBytes = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            dataBytes[i] = (byte) (data[i] & 0xFF);
        }
        return of(addressByte, dataBytes);
    }

    public static @Nullable SerialData of(byte[] raw) {
        if (raw.length == 0) return null;
        byte address = raw[0];
        byte[] data = new byte[raw.length - 1];
        System.arraycopy(raw, 1, data, 0, data.length);
        return of(address, data);
    }

    public static @NotNull SerialData of(byte address, byte[] data) {
        return new SerialData(address, data, generateCheckBit(address, data));
    }

    public static @NotNull SerialData of(byte address, byte[] data, byte[] check) {
        return new SerialData(address, data, check);
    }

    public @Range(from = 0, to = 255) int unsignedAddress() {
        return address & 0xFF;
    }

    public byte[] raw() {
        // 组合完整的数据 包括地址、数据和附加位
        return combineBytes(new byte[]{address}, data, check);
    }

    public int length() {
        return 1 + (data != null ? data.length : 0);
    }

    public boolean validate(int index, @Range(from = 0, to = 255) int data) {
        if (index < 0) {
            throw new IllegalArgumentException("Index and data must be non-negative and data must be in the range 0-255");
        }
        if (this.data == null || index >= this.data.length) return false;
        return this.data[index] == (byte) data;
    }

    public @Range(from = 0, to = 255) int read(int index) {
        if (this.data == null || index >= this.data.length) return 0;
        return data[index] & 0xFF; // 转换为无符号整数
    }

    @Override
    public @NotNull String toString() {
        // 转换为形如 01 05 EE FF 的形式
        StringBuilder sb = new StringBuilder();

        // 添加地址字节
        sb.append(String.format("%02X", address));

        if (data != null) {    // 添加数据字节
            for (byte b : data) {
                sb.append(" ").append(String.format("%02X", b));
            }
        }

        if (check != null) {   // 添加附加位
            for (byte b : check) {
                sb.append(" ").append(String.format("%02X", b));
            }
        }

        return sb.toString();
    }

    static byte[] generateCheckBit(byte address, byte[] data) {
        // CRC-16/MODBUS 算法
        int crc = 0xFFFF; // 初始值
        crc = calculateCRC(crc, address);
        if (data != null) {
            for (byte b : data) {
                crc = calculateCRC(crc, b);
            }
        }
        return new byte[]{(byte) (crc & 0xFF), (byte) ((crc >> 8) & 0xFF)};
    }

    private static int calculateCRC(int crc, byte byte_) {
        crc ^= (byte_ & 0xFF);

        for (int i = 0; i < 8; i++) {
            if ((crc & 0x0001) != 0) {
                crc = (crc >> 1) ^ 0xA001; // 0xA001 是 0x8005 的反向
            } else {
                crc = crc >> 1;
            }
        }
        return crc;
    }

    private static byte[] combineBytes(byte[]... bytes) {
        int totalLength = 0;
        for (byte[] array : bytes) {
            if (array != null) {
                totalLength += array.length;
            }
        }
        byte[] result = new byte[totalLength];
        int offset = 0;
        for (byte[] array : bytes) {
            if (array != null) {
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
        }
        return result;
    }

}

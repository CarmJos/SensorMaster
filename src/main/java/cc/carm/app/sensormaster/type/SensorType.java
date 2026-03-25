package cc.carm.app.sensormaster.type;

import cc.carm.app.sensormaster.data.SerialData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public interface SensorType<DATA> {

    /**
     *
     * @return 传感器名称
     */
    @NotNull String name();

    @Range(from = 1, to = 255)
    int defaultAddress();

    @NotNull
    default SerialData modifyAddress(
            @Range(from = 0, to = 255) int currentAddress,
            @Range(from = 0, to = 255) int newAddress
    ) {   // 修改地址命令： <当前地址> 06 00 3E 00 <目标地址>
        return SerialData.of(currentAddress, 0X06, 0X00, 0X3E, 0X00, newAddress);
    }

    @NotNull SerialData generateRequest(@Range(from = 0, to = 255) int address);

    /**
     * 将串口源数据解析为实际数据体
     *
     * @param response 串口响应数据
     * @return 实际数据内容
     */
    @Nullable DATA handleResponse(SerialData response);

    @NotNull String formatData(@NotNull DATA data);
}

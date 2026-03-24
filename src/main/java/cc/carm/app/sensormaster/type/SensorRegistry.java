package cc.carm.app.sensormaster.type;

import cc.carm.app.sensormaster.type.impl.NumericSensorType;
import cc.carm.app.sensormaster.type.impl.Thermohygrometer;

public interface SensorRegistry {

    NumericSensorType WIND_SPEED = NumericSensorType.create("风速")
            .dataIndices(2, 3)
            .requestCommand(0x03, 0x00, 0x09, 0x00, 0x01)
            .handleData(raw -> raw / 10)
            .build();

    NumericSensorType WIND_DIRECTION = NumericSensorType.create("风向")
            .dataIndices(3, 4)
            .requestCommand(0x03, 0x00, 0x0A, 0x00, 0x01)
            .handleData(raw -> raw / 10)
            .build();

    Thermohygrometer SOIL_MOISTURE = Thermohygrometer.create(
            "土壤墒情",
            0x03, 0x00, 0x04, 0x00, 0x02
    );

    Thermohygrometer AIR_TEMPERATURE_HUMIDITY = Thermohygrometer.create(
            "空气温湿度",
            0x03, 0x00, 0x00, 0x00, 0x02
    );

    NumericSensorType CO2_CONCENTRATION = NumericSensorType.create("二氧化碳浓度")
            .dataIndices(3, 4)
            .requestCommand(0x03, 0x00, 0x07, 0x00, 0x01)
            .build();

    NumericSensorType ATMOSPHERIC_PRESSURE = NumericSensorType.create("大气压力")
            .dataIndices(3, 4)
            .requestCommand(0x03, 0x00, 0x0B, 0x00, 0x01)
            .handleData(raw -> raw / 10)
            .build();

    NumericSensorType LIGHT_INTENSITY = NumericSensorType.create("光照度")
            .dataIndices(3, 6)
            .requestCommand(0x03, 0x00, 0x02, 0x00, 0x02)
            .build();

    NumericSensorType SOIL_PH = NumericSensorType.create("土壤pH值")
            .dataIndices(3, 4)
            .requestCommand(0x03, 0x00, 0x08, 0x00, 0x01)
            .handleData(raw -> raw / 10)
            .build();

    NumericSensorType SOIL_EC = NumericSensorType.create("土壤EC值")
            .dataIndices(3, 4)
            .requestCommand(0x03, 0x00, 0x06, 0x00, 0x01)
            .build();

    NumericSensorType LIQUID_LEVEL = NumericSensorType.create("液位")
            .dataIndices(3, 4)
            .requestCommand(0x03, 0x00, 0x00, 0x00, 0x01)
            .handleData(raw -> raw / 100)
            .build();

    SensorType<?>[] VALUES = new SensorType[]{
            WIND_SPEED, WIND_DIRECTION, SOIL_MOISTURE, AIR_TEMPERATURE_HUMIDITY,
            CO2_CONCENTRATION, ATMOSPHERIC_PRESSURE, LIGHT_INTENSITY,
            SOIL_PH, SOIL_EC, LIQUID_LEVEL
    };

    static SensorType<?>[] values() {
        return VALUES;
    }

    static SensorType<?> getByName(String name) {
        for (SensorType<?> type : VALUES) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }

}

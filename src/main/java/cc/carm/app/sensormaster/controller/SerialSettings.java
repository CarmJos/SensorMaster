package cc.carm.app.sensormaster.controller;

public interface SerialSettings {

    int BAUD_RATE = 9600;
    int DATA_BITS = 8;
    int STOP_BITS = com.fazecast.jSerialComm.SerialPort.ONE_STOP_BIT;
    int PARITY = com.fazecast.jSerialComm.SerialPort.NO_PARITY;

}

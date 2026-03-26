package cc.carm.app.sensormaster.ui;

import cc.carm.app.sensormaster.serial.SerialPortInfo;
import cc.carm.app.sensormaster.sensor.SensorRegistry;
import cc.carm.app.sensormaster.sensor.SensorType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

public abstract class ControlPanel extends JPanel {

    protected JComboBox<SerialPortInfo> comSelector;
    protected JComboBox<SensorType<?>> sensorSelector;

    protected final JButton connectButton;
    protected final JButton refreshButton;
    protected boolean connected = false;

    public ControlPanel() {
        super(new BorderLayout());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));

        left.add(new JLabel("使用串口"));
        comSelector = new JComboBox<>(refreshComPorts().toArray(new SerialPortInfo[0]));
        comSelector.addActionListener(e -> updateConnectButton());
        left.add(comSelector);

        refreshButton = new JButton("刷新串口信息");
        refreshButton.addActionListener(e -> {
            List<SerialPortInfo> ports = refreshComPorts();
            comSelector.removeAllItems();
            ports.forEach(port -> comSelector.addItem(port));
            updateConnectButton();
        });
        left.add(refreshButton);
        left.add(Box.createHorizontalStrut(10));

        left.add(new JLabel("传感器"));
        sensorSelector = new JComboBox<>(SensorRegistry.values());
        sensorSelector.addActionListener(e -> updateConnectButton());

        left.add(sensorSelector);

        connectButton = new JButton();
        connectButton.setPreferredSize(new Dimension(120, 30));
        connectButton.addActionListener(e -> setStarted(!this.started(), true));
        updateConnectButton();

        add(left, BorderLayout.WEST);
        add(connectButton, BorderLayout.EAST);
        setStarted(false, false);
        sensorSelector.setSelectedItem(null);
        comSelector.setSelectedItem(null);
    }

    public boolean started() {
        return this.connected;
    }

    public void updateConnectButton() {
        if (started()) {
            connectButton.setEnabled(true);
            return;
        }
        connectButton.setEnabled(comSelector.getSelectedItem() != null && sensorSelector.getSelectedItem() != null);
    }

    public void setStarted(boolean status, boolean triggerEvent) {
        if (status) {
            comSelector.setEnabled(false);
            sensorSelector.setEnabled(false);
            refreshButton.setEnabled(false);
            connectButton.setText(" 终止连接 ");
            connectButton.setBackground(new Color(255, 102, 102));
            if (triggerEvent) {
                whenStart(
                        (SerialPortInfo) Objects.requireNonNull(comSelector.getSelectedItem()),
                        (SensorType<?>) Objects.requireNonNull(sensorSelector.getSelectedItem())
                );
            }
            connected = true;
        } else {
            comSelector.setEnabled(true);
            sensorSelector.setEnabled(true);
            refreshButton.setEnabled(true);
            connectButton.setText(" 开启连接 ");
            connectButton.setBackground(new Color(170, 255, 195));
            if (triggerEvent) {
                whenStop(
                        (SerialPortInfo) Objects.requireNonNull(comSelector.getSelectedItem()),
                        (SensorType<?>) Objects.requireNonNull(sensorSelector.getSelectedItem())
                );
            }
            connected = false;
        }
    }

    public abstract void whenStart(@NotNull SerialPortInfo comPort, @NotNull SensorType<?> type);

    public abstract void whenStop(@NotNull SerialPortInfo comPort, @NotNull SensorType<?> type);

    public abstract List<SerialPortInfo> refreshComPorts();
}

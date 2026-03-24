package cc.carm.app.sensormaster;

import cc.carm.app.sensormaster.controller.SerialController;
import cc.carm.app.sensormaster.data.SerialPortInfo;
import cc.carm.app.sensormaster.type.SensorType;
import cc.carm.app.sensormaster.ui.ConsolePanel;
import cc.carm.app.sensormaster.ui.ControlPanel;
import cc.carm.app.sensormaster.ui.DisplayPanel;
import cc.carm.app.sensormaster.ui.FooterPanel;
import com.fazecast.jSerialComm.SerialPort;
import com.formdev.flatlaf.FlatLightLaf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Main {

    static {
        FlatLightLaf.setup();
    }

    public static final Logger LOGGER = LogManager.getLogger(Main.class);
    public static @Nullable SerialController<?> CONTROLLER = null;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            JFrame dashboard = new JFrame();
            dashboard.setTitle("SensorMaster 传感器调试工具");
            dashboard.setSize(900, 700);
            dashboard.setMinimumSize(new Dimension(900, 700));
            dashboard.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            dashboard.setLocationRelativeTo(null);

            Container contentPane = dashboard.getContentPane();
            contentPane.setLayout(new BorderLayout(15, 15));
            ((JPanel) contentPane).setBorder(new EmptyBorder(20, 20, 20, 20));

            contentPane.add(controlPanel, BorderLayout.NORTH);
            contentPane.add(displayPanel, BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel(new BorderLayout(0, 15));
            bottomPanel.add(consolePanel, BorderLayout.CENTER);
            bottomPanel.add(new FooterPanel(), BorderLayout.SOUTH);
            contentPane.add(bottomPanel, BorderLayout.SOUTH);


            Image icon = readIcon();
            if (icon != null) {
                dashboard.setIconImage(icon);
            }

            dashboard.setVisible(true);
        });
    }

    protected static final ConsolePanel consolePanel = new ConsolePanel();

    protected static final ControlPanel controlPanel = new ControlPanel() {
        @Override
        public void whenStart(@NotNull SerialPortInfo comPort, @NotNull SensorType<?> sensor) {
            consolePanel.appendLine("> 正在连接串口 " + comPort + " ，传感器类型 " + sensor.name());
            displayPanel.updateContent("尝试连接中", Color.ORANGE);

            if (Main.CONTROLLER != null) {
                Main.CONTROLLER.close();
            }

            Main.CONTROLLER = SerialController.create(comPort.port(), sensor, (data, text) -> {
                displayPanel.updateContent(text);
                displayPanel.updateStatus(true);
                if (Main.CONTROLLER != null) {
                    Main.CONTROLLER.autoRefresh(displayPanel.getUpdateInterval());
                    displayPanel.updateAddress(Optional.ofNullable(Main.CONTROLLER.currentAddress()).orElse(1));
                }
            });

            if (!Main.CONTROLLER.connect()) {
                consolePanel.appendLine("> 无法连接至 " + comPort + " ，请检查连接状态和串口权限。");
                displayPanel.updateContent("连接失败", Color.RED);
                displayPanel.updateStatus(false);
            }

        }

        @Override
        public void whenStop(@NotNull SerialPortInfo comPort, @NotNull SensorType<?> type) {
            consolePanel.appendLine("> 终止连接");
            displayPanel.updateContent("未连接", Color.DARK_GRAY);
            displayPanel.updateStatus(false);

            if (Main.CONTROLLER != null) {
                Main.CONTROLLER.close();
                Main.CONTROLLER = null;
            }
        }

        @Override
        public List<SerialPortInfo> refreshComPorts() {
            LOGGER.info("Refreshing COM ports...");
            return Arrays.stream(SerialPort.getCommPorts())
                    .peek(port -> LOGGER.info(
                            "Found serial port ({}) {} @[{}] - {}",
                            port.getPortLocation(),
                            port.getSystemPortName(),
                            port.getSystemPortPath(),
                            port.getPortDescription()
                    )).map(SerialPortInfo::of).collect(Collectors.toList());
        }
    };

    protected static final DisplayPanel displayPanel = new DisplayPanel() {

        @Override
        public void whenApplyUpdateInterval(long interval) {
            if (interval > 0) {
                if (Main.CONTROLLER != null) {
                    Main.CONTROLLER.autoRefresh(displayPanel.getUpdateInterval());
                }
                consolePanel.appendLine("> 更新自动刷新时间为 " + interval + " 毫秒。");
            } else {
                consolePanel.appendLine("> 已关闭自动刷新数据。");
            }
        }

        @Override
        public void whenRefresh() {
            if (Main.CONTROLLER == null) return;
            Main.CONTROLLER.refresh();
        }

        @Override
        public void whenApplyAddress(int address) {
            if (Main.CONTROLLER == null) return;
            Main.CONTROLLER.updateAddress(address);
        }

    };

    private static @Nullable Image readIcon() {
        Image image = null;
        try {
            image = ImageIO.read(Main.class.getResourceAsStream("/icon.png"));
        } catch (Exception e) {
        }
        return image;
    }

    public static String getVersion() {
        // 从 resources/PROJECT_VERSION 文件获取项目版本号
        try (InputStream inputStream = Main.class.getResourceAsStream("/PROJECT_VERSION")) {
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String version = reader.readLine();
                if (version != null && !version.trim().isEmpty()) {
                    return version.trim();
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to read PROJECT_VERSION file", e);
        }
        return "Unknown";
    }

}

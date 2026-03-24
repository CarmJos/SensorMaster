package cc.carm.app.sensormaster.ui;

import cc.carm.app.sensormaster.type.SensorRegistry;
import cc.carm.app.sensormaster.type.SensorType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultFormatterFactory;
import java.awt.*;
import java.util.Arrays;

public class Dashboard extends JFrame {

    private JLabel dataDisplay; // 大字号数据展示
    private JSpinner addressDisplay;    // HEX 地址框

    public Dashboard() {
        setTitle("传感器操作终端");
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(15, 15));
        ((JPanel) contentPane).setBorder(new EmptyBorder(20, 20, 20, 20));

        contentPane.add(createTopConnectionPanel(), BorderLayout.NORTH);
        contentPane.add(createMainDisplayArea(), BorderLayout.CENTER);
        contentPane.add(createConsolePanel(), BorderLayout.SOUTH);
    }

    // --- 1. 顶部连接面板 ---
    private JPanel createTopConnectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));

        left.add(new JLabel("串口列表"));
        left.add(new JComboBox<>(new String[]{"COM1", "COM2", "COM3"}));

        left.add(Box.createHorizontalStrut(10));

        left.add(new JLabel("传感器"));
        left.add(new JComboBox<>(Arrays.stream(SensorRegistry.values()).map(SensorType::name).toArray()));

        JButton openBtn = new JButton(" 开启连接 ");
        openBtn.setPreferredSize(new Dimension(100, 30));

        panel.add(left, BorderLayout.WEST);
        panel.add(openBtn, BorderLayout.EAST);
        return panel;
    }

    // --- 2. 核心大屏显示区 ---
    private JPanel createMainDisplayArea() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));

        // 2.1 大型数值展示卡片
        JPanel dataCard = new JPanel(new GridBagLayout());
        dataCard.setBackground(Color.WHITE);
        dataCard.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 2));

        dataDisplay = new JLabel("未连接");
        dataDisplay.setFont(new Font("SimHei", Font.BOLD, 72));
        dataDisplay.setForeground(new Color(0, 102, 204));

        JLabel unitLabel = new JLabel("实时数据");
        unitLabel.setFont(new Font("SimHei", Font.PLAIN, 14));
        unitLabel.setForeground(Color.GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        dataCard.add(dataDisplay, gbc);
        gbc.gridy = 1;
        dataCard.add(unitLabel, gbc);

        // 2.2 参数调节区（规整的网格布局）
        JPanel paramPanel = new JPanel(new GridLayout(1, 2, 20, 0));

        // 左侧：地址修改
        JPanel addrGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        addrGroup.setBorder(BorderFactory.createTitledBorder("设备地址"));
        addrGroup.add(new JLabel("传感器地址"));
        addressDisplay = new JSpinner(new SpinnerNumberModel(1, 1, 255, 1));
        addressDisplay.setEditor(new HexEditor(addressDisplay));
        addressDisplay.setPreferredSize(new Dimension(80, 30));
        addrGroup.add(addressDisplay);
        addrGroup.add(new JButton("修改"));

        // 右侧：刷新频率
        JPanel freqGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        freqGroup.setBorder(BorderFactory.createTitledBorder("监控频率"));
        freqGroup.add(new JLabel("自动刷新周期 "));
        freqGroup.add(new JComboBox<>(new String[]{"200ms", "500ms", "1.0s", "5.0s"}));
        freqGroup.add(new JButton("立即刷新"));

        paramPanel.add(addrGroup);
        paramPanel.add(freqGroup);

        mainPanel.add(dataCard, BorderLayout.CENTER);
        mainPanel.add(paramPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    // --- 3. 底部控制台 ---
    private JPanel createConsolePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("串口实时日志"));

        JTextArea console = new JTextArea(6, 50);
        console.setBackground(new Color(245, 245, 245));
        console.setEditable(false);
        console.setFont(new Font("Monospaced", Font.PLAIN, 12));
        console.append("> 系统就绪，等待串口连接...\n");

        panel.add(new JScrollPane(console), BorderLayout.CENTER);
        return panel;
    }

    static class HexEditor extends JSpinner.DefaultEditor {
        public HexEditor(JSpinner spinner) {
            super(spinner);
            JFormattedTextField ftf = getTextField();
            ftf.setFormatterFactory(new DefaultFormatterFactory(new JFormattedTextField.AbstractFormatter() {
                @Override
                public Object stringToValue(String t) {
                    return Integer.parseInt(t, 16);
                }

                @Override
                public String valueToString(Object v) {
                    return (v instanceof Integer) ? String.format("%02X", (Integer) v) : "01";
                }
            }));
        }
    }

    public static void main(String[] args) {

    }

}
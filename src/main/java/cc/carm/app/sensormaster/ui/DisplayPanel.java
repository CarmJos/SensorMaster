package cc.carm.app.sensormaster.ui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import java.awt.*;

public abstract class DisplayPanel extends JPanel {

    protected JLabel dataDisplay; // 数据展示框
    protected JLabel subtitleDisplay;

    protected JSpinner addressDisplay;  // 数据地址框
    protected JButton addressApply; // 地址应用按钮
    protected JButton refreshButton; // 刷新按钮
    protected JComboBox<TimeInterval> intervalSelector; // 刷新频率选择框

    public DisplayPanel() {
        super(new BorderLayout(0, 20));

        JPanel dataCard = new JPanel(new GridBagLayout());
        dataCard.setBackground(Color.WHITE);
        dataCard.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 2));

        dataDisplay = new JLabel();
        dataDisplay.setFont(new Font("SimHei", Font.BOLD, 72));

        subtitleDisplay = new JLabel("最新数据");
        subtitleDisplay.setFont(new Font("SimHei", Font.PLAIN, 14));
        subtitleDisplay.setForeground(Color.GRAY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        dataCard.add(dataDisplay, gbc);
        gbc.gridy = 1;
        dataCard.add(subtitleDisplay, gbc);

        JPanel paramPanel = new JPanel(new GridLayout(1, 2, 20, 0));

        // 左侧：地址修改
        JPanel addrGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        addrGroup.setBorder(BorderFactory.createTitledBorder("设备地址"));
        addrGroup.add(new JLabel("传感器地址"));
        addressDisplay = new JSpinner(new SpinnerNumberModel(1, 1, 255, 1));
        addressDisplay.setEditor(new HexEditor(addressDisplay));
        addrGroup.add(addressDisplay);

        addressApply = new JButton("应用新地址");
        addrGroup.add(addressApply);
        addressApply.addActionListener(e -> {
            if (!addressApply.isEnabled()) return;
            whenApplyAddress((Integer) addressDisplay.getValue());
        });

        // 右侧：刷新频率
        JPanel freqGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        freqGroup.setBorder(BorderFactory.createTitledBorder("监控频率"));
        freqGroup.add(new JLabel("自动刷新周期 "));

        intervalSelector = new JComboBox<>(new TimeInterval[]{
                new TimeInterval("关闭", 0),
                new TimeInterval("200毫秒", 200),
                new TimeInterval("500毫秒", 500),
                new TimeInterval("1秒", 1000),
                new TimeInterval("5秒", 5000)
        });
        intervalSelector.addActionListener(e -> {
            if (!intervalSelector.isEnabled()) return;
            TimeInterval selected = (TimeInterval) intervalSelector.getSelectedItem();
            if (selected != null) {
                whenApplyUpdateInterval(selected.interval);
            }
        });
        freqGroup.add(intervalSelector);

        refreshButton = new JButton("立即刷新");
        refreshButton.addActionListener(e -> {
            if (!refreshButton.isEnabled()) return;
            whenRefresh();
        });
        freqGroup.add(refreshButton);

        paramPanel.add(addrGroup);
        paramPanel.add(freqGroup);

        add(dataCard, BorderLayout.CENTER);
        add(paramPanel, BorderLayout.SOUTH);

        updateStatus(false);
        updateContent("未连接", "等待串口连接", Color.DARK_GRAY);
    }

    public void updateAddress(int address) {
        addressDisplay.setValue(address);
    }

    public void updateContent(@NotNull String content) {
        updateContent(content, "最新数据", new Color(0, 102, 204));
    }

    public void updateContent(@NotNull String content, @NotNull String subtitle, @Nullable Color color) {
        dataDisplay.setText(content);
        dataDisplay.setForeground(color);
        subtitleDisplay.setText(subtitle);
    }

    public void updateStatus(boolean status) {
        addressApply.setEnabled(status);
        addressDisplay.setEnabled(status);
        refreshButton.setEnabled(status);
    }

    public long getUpdateInterval() {
        Object selected = intervalSelector.getSelectedItem();
        if (selected instanceof TimeInterval) {
            return ((TimeInterval) selected).interval;
        }
        return 500;
    }

    public abstract void whenApplyUpdateInterval(long interval);

    public abstract void whenRefresh();

    public abstract void whenApplyAddress(int address);


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

    public static class TimeInterval {
        private final String display;
        private final long interval;

        public TimeInterval(String display, long interval) {
            this.display = display;
            this.interval = interval;
        }

        public String display() {
            return display;
        }

        public long interval() {
            return interval;
        }

        @Override
        public @NotNull String toString() {
            return display;
        }
    }

}

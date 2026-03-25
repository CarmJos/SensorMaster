package cc.carm.app.sensormaster.ui;

import cc.carm.app.sensormaster.Main;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ConsolePanel extends JPanel {

    protected final JTextArea consoleArea; // 串口输出
    protected final JScrollPane scrollPane;

    public ConsolePanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("串口实时日志"));

        // --- 1. 创建顶部工具栏容器 ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // 判断点击位置是否在顶部标题栏区域（大约上方 20 像素以内）
                if (e.getClickCount() == 2 && e.getY() < 25) {
                    clear();
                }
            }
        });
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. 配置日志显示区 ---
        consoleArea = new JTextArea(6, 50);
        consoleArea.setBackground(new Color(245, 245, 245));
        consoleArea.setEditable(false);
        consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        consoleArea.append("> 系统就绪，等待串口连接...\n");

        scrollPane = new JScrollPane(consoleArea);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * 清空日志内容
     */
    public void clear() {
        Main.LOGGER.info("Cleared console lines.");
        consoleArea.setText("");
    }

    public void appendLine(@Nullable String text) {
        // 检查是否在底部
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        boolean isAtBottom = verticalBar.getValue() + verticalBar.getVisibleAmount() >= verticalBar.getMaximum();

        if (text != null) {
            consoleArea.append(text);
        }
        consoleArea.append("\n");

        if (isAtBottom) {
            SwingUtilities.invokeLater(() -> {
                Rectangle rect = new Rectangle(0, consoleArea.getHeight() - 1, 1, 1);
                consoleArea.scrollRectToVisible(rect);
            });
        }
    }

}
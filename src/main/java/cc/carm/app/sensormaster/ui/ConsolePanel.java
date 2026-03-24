package cc.carm.app.sensormaster.ui;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ConsolePanel extends JPanel {

    protected final JTextArea consoleArea; // 串口输出

    public ConsolePanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("串口实时日志"));

        consoleArea = new JTextArea(6, 50);
        consoleArea.setBackground(new Color(245, 245, 245));
        consoleArea.setEditable(false);
        consoleArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        consoleArea.append("> 系统就绪，等待串口连接...\n");
        add(new JScrollPane(consoleArea), BorderLayout.CENTER);
    }

    public JTextArea getConsoleArea() {
        return this.consoleArea;
    }

    public void clear() {
        consoleArea.removeAll();
    }

    public void appendLine(@Nullable String text) {
        if (text != null) {
            consoleArea.append(text);
        }
        consoleArea.append("\n");
    }

}

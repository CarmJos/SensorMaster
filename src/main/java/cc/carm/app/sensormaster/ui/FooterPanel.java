package cc.carm.app.sensormaster.ui;

import cc.carm.app.sensormaster.Main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class FooterPanel extends JPanel {

    public FooterPanel() {
        super(new BorderLayout(20, 0));
        setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftPanel.setOpaque(false);
        JLabel title = new JLabel("SensorMaster");
        title.setFont(new Font("Consolas", Font.BOLD, 18));
        leftPanel.add(title);


        JLabel version = new JLabel(Main.getVersion());
        version.setFont(new Font("Consolas", Font.PLAIN, 12));
        leftPanel.add(version);

        leftPanel.add(createDash('|'));

        JLabel descriptionLabel = new JLabel("适用于\"物联网工程\"实训实验的传感器快速调试工具。");
        descriptionLabel.setForeground(new Color(80, 80, 80));
        leftPanel.add(descriptionLabel);
        add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightPanel.setOpaque(false);

        JLabel projectLink = createHyperlink("开源地址", "https://github.com/CarmJos/SensorMaster");
        rightPanel.add(projectLink);
        rightPanel.add(createDash('/'));
        rightPanel.add(new JLabel("作者"));
        JLabel authorLink = createHyperlink("@CarmJos", "https://github.com/CarmJos/");
        rightPanel.add(authorLink);

        add(rightPanel, BorderLayout.EAST);
    }

    private JLabel createDash(char c) {
        JLabel label = new JLabel(" " + c + " ");
        label.setForeground(Color.LIGHT_GRAY);
        return label;
    }

    private JLabel createHyperlink(String text, String url) {
        JLabel link = new JLabel(text);
        link.setForeground(new Color(0, 102, 204));
        link.setCursor(new Cursor(Cursor.HAND_CURSOR));

        link.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openBrowser(url);
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                link.setText("<html><u>" + text + "</u></html>");
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                link.setText(text);
            }
        });

        return link;
    }

    private void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

}


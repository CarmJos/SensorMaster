package cc.carm.app.sensormaster;

import cc.carm.app.sensormaster.ui.Dashboard;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new Dashboard().setVisible(true));
    }


}

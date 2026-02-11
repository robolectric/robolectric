package org.robolectric.simulator;

import javax.swing.JFrame;

/** The top-level {@link JFrame} for the Robolectric Simulator. */
public class SimulatorFrame extends JFrame {

  private final SimulatorPanel simulatorPanel;

  public SimulatorFrame(
      SimulatorPanel simulatorPanel, int displayWidth, int displayHeight, int sdkLevel) {
    this.simulatorPanel = simulatorPanel;
    setTitle("Robolectric SDK " + sdkLevel + " Simulator");

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationByPlatform(true);
    setAlwaysOnTop(true);
    setResizable(false);
    add(simulatorPanel);
    simulatorPanel.requestFocus();
    pack();
  }

  public SimulatorPanel getSimulatorPanel() {
    return simulatorPanel;
  }
}

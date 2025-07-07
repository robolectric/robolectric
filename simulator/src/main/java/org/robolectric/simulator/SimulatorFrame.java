package org.robolectric.simulator;

import javax.swing.JFrame;

/** The top-level {@link JFrame} for the Robolectric Simulator. */
public class SimulatorFrame extends JFrame {

  private final SimulatorCanvas simulatorCanvas;

  public SimulatorFrame(int displayWidth, int displayHeight, int sdkLevel) {
    setTitle("Robolectric SDK " + sdkLevel + " Simulator");

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationByPlatform(true);
    setAlwaysOnTop(true);
    setResizable(false);

    simulatorCanvas = new SimulatorCanvas();
    simulatorCanvas.setSize(displayWidth, displayHeight);
    add(simulatorCanvas);
    simulatorCanvas.requestFocus();
    pack();
  }

  public SimulatorCanvas getCanvas() {
    return simulatorCanvas;
  }
}

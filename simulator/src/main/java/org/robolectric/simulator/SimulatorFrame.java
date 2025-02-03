package org.robolectric.simulator;

import javax.swing.JFrame;

/** The top-level {@link JFrame} for the Robolectric Simulator. */
public class SimulatorFrame extends JFrame {

  private final SimulatorCanvas simulatorCanvas;

  public SimulatorFrame(int displayWidth, int displayHeight) {
    setTitle("Robolectric Simulator");

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationByPlatform(true);
    setAlwaysOnTop(true);
    setResizable(false);
    addKeyListener(new KeyboardHandler());

    simulatorCanvas = new SimulatorCanvas();
    simulatorCanvas.setSize(displayWidth, displayHeight);
    add(simulatorCanvas);
    pack();
  }

  public SimulatorCanvas getCanvas() {
    return simulatorCanvas;
  }
}

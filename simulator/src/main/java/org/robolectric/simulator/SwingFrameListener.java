package org.robolectric.simulator;

import android.graphics.Bitmap;
import android.util.Log;
import java.awt.Dimension;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.robolectric.util.PerfStatsCollector;

public class SwingFrameListener implements FrameListener {

  private SimulatorFrame simulatorFrame;
  private SimulatorPanel simulatorPanel;
  private final AtomicReference<Bitmap> pendingFrame = new AtomicReference<>();
  private final AtomicBoolean isCallbackPending = new AtomicBoolean(false);
  private final int apiLevel;
  private final int width;
  private final int height;

  private final boolean isHeadless;

  public SwingFrameListener(boolean isHeadless, int apiLevel, int width, int height) {
    this.isHeadless = isHeadless;
    this.apiLevel = apiLevel;
    this.width = width;
    this.height = height;
  }

  @Override
  public void onInitialize() {
    SwingUtilities.invokeLater(
        () -> {
          this.simulatorPanel = new SimulatorPanel();
          simulatorPanel.setPreferredSize(new Dimension(width, height));

          if (!isHeadless) {
            try {
              UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
            simulatorFrame = new SimulatorFrame(simulatorPanel, width, height, apiLevel);
            simulatorFrame.setVisible(true);
            simulatorFrame.toFront();
          }
        });
  }

  @Override
  public void onFrame(Bitmap bitmap) {
    Bitmap oldFrame = pendingFrame.getAndSet(bitmap);
    if (oldFrame != null) {
      // frames are being sent faster than swing can process them, drop the old frame.
      Log.w("SwingFrameListener", "dropping frame");
      PerfStatsCollector.getInstance().incrementCount("SwingFrameListener-droppedFrame");
      oldFrame.recycle();
    }

    // don't schedule multiple callbacks if there is one already pending
    if (isCallbackPending.compareAndSet(false, true)) {
      SwingUtilities.invokeLater(this::renderLatestFrame);
    }
  }

  private void renderLatestFrame() {
    isCallbackPending.set(false);
    Bitmap b = pendingFrame.getAndSet(null);
    if (b != null) {
      sendFrameToSwing(b);
      b.recycle();
    }
  }

  private void sendFrameToSwing(Bitmap bitmap) {
    PerfStatsCollector.getInstance()
        .measure("SwingFrameListener-sendFrameToSwing", () -> simulatorPanel.drawBitmap(bitmap));
    SimulatorPanelRegistry.register(simulatorPanel);
  }
}

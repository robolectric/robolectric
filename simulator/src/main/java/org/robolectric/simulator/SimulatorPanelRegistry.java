package org.robolectric.simulator;

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JPanel;

/** A registry of active {@link SimulatorPanel} instances. Should only be used for tests. */
@VisibleForTesting
public final class SimulatorPanelRegistry {
  private static final AtomicReference<JPanel> activePanel = new AtomicReference<>();

  public static void register(JPanel panel) {
    activePanel.set(panel);
  }

  public static JPanel getActivePanel() {
    return activePanel.get();
  }

  private SimulatorPanelRegistry() {}
}

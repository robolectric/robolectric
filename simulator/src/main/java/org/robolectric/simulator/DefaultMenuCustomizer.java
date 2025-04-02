package org.robolectric.simulator;

import com.google.auto.service.AutoService;
import com.google.common.annotations.Beta;
import javax.annotation.Priority;
import javax.swing.JPopupMenu;
import org.robolectric.simulator.pluginapi.MenuCustomizer;

/** Default implementation of {@link MenuCustomizer}. */
@Priority(Integer.MIN_VALUE)
@Beta
@AutoService(MenuCustomizer.class)
public class DefaultMenuCustomizer implements MenuCustomizer {
  @Override
  public void customizePopupMenu(JPopupMenu menu) {
    // no-op
  }
}

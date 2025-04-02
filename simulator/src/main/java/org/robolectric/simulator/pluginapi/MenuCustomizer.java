package org.robolectric.simulator.pluginapi;

import com.google.common.annotations.Beta;
import javax.swing.JPopupMenu;

/** A plugin that makes it possible to customize the right click menu in the simulator. */
@Beta
public interface MenuCustomizer {
  void customizePopupMenu(JPopupMenu menu);
}

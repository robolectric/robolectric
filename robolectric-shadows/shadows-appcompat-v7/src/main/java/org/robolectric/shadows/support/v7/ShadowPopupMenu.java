package org.robolectric.shadows.support.v7;

import android.view.View;
import android.content.Context;
import android.support.v7.widget.PopupMenu;
import org.robolectric.annotation.Implements;

/**
 * Shadow for {@link android.support.v7.widget.PopupMenu}.
 */
@Implements(PopupMenu.class)
public class ShadowPopupMenu {

  public void __constructor__(Context context, View anchor) {
  }
}

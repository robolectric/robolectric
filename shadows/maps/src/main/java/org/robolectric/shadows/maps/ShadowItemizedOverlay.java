package org.robolectric.shadows.maps;

import android.graphics.drawable.Drawable;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.ShadowItemizedOverlayBridge;
import java.util.ArrayList;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(ItemizedOverlay.class)
public class ShadowItemizedOverlay<Item extends OverlayItem> {
  private boolean isPopulated;
  private boolean shouldHit;
  private boolean lastFocusedIndexWasReset;
  private ArrayList<Item> items = new ArrayList<>();

  @RealObject
  private ItemizedOverlay<Item> realObject;

  @Implementation
  public final void populate() {
    isPopulated = true;

    items.clear();
    for (int i = 0; i < realObject.size(); i++) {
      items.add(getBridge().createItem(i));
    }
  }

  @Implementation
  public final Item getItem(int position) {
    return items.get(position);
  }

  @Implementation
  public boolean hitTest(OverlayItem item, android.graphics.drawable.Drawable drawable, int i, int i1) {
    return shouldHit;
  }

  @Implementation
  public void setLastFocusedIndex(int i) {
    lastFocusedIndexWasReset = (i == -1);
  }

  @Implementation
  public static Drawable boundCenterBottom(Drawable drawable) {
    return drawable;
  }

  /**
   * Indicates whether {@link #setLastFocusedIndex(int)} has been called with a value other
   * than -1.
   *
   * @return whether {@link #setLastFocusedIndex(int)} has been called with a value other
   *         than -1
   */
  public boolean lastFocusedIndexWasReset() {
    return lastFocusedIndexWasReset;
  }

  public void setIsPopulated(boolean isPopulated) {
    this.isPopulated = isPopulated;
  }

  public boolean isPopulated() {
    return isPopulated;
  }

  /**
   * Sets up the return value for
   * {@link #hitTest(com.google.android.maps.OverlayItem, android.graphics.drawable.Drawable, int, int)}
   *
   * @param shouldHit the value that
   *                  {@link #hitTest(com.google.android.maps.OverlayItem, android.graphics.drawable.Drawable, int, int)}
   *                  should return
   */
  public void setShouldHit(boolean shouldHit) {
    this.shouldHit = shouldHit;
  }

  private ShadowItemizedOverlayBridge<Item> getBridge() {
    return new ShadowItemizedOverlayBridge<>(realObject);
  }
}

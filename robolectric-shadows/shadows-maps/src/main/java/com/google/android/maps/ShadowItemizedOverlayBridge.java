package com.google.android.maps;

import org.robolectric.annotation.internal.DoNotInstrument;

/**
 * Bridge between Robolectric and {@link com.google.android.maps.OverlayItem}.
 */
@DoNotInstrument
public class ShadowItemizedOverlayBridge<Item extends OverlayItem> {
  private ItemizedOverlay<Item> itemizedObject;

  public ShadowItemizedOverlayBridge(ItemizedOverlay<Item> itemizedObject) {
    this.itemizedObject = itemizedObject;
  }

  public Item createItem(int i) {
    return itemizedObject.createItem(i);
  }
}

package com.google.android.maps;

import com.xtremelabs.robolectric.internal.DoNotInstrument;

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

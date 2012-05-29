package com.xtremelabs.robolectric.shadows;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class ItemizedOverlayTest {

    class TestItemizedOverlay extends ItemizedOverlay<OverlayItem> {
        public OverlayItem firstOverlayItem = new OverlayItem(new GeoPoint(0, 0), "title1", "snippet1");
        public OverlayItem secondOverlayItem = new OverlayItem(new GeoPoint(5, 5), "title2", "snippet2");

        public TestItemizedOverlay() {
            super(null);
        }

        @Override
        protected OverlayItem createItem(int index) {
            if (index == 0) {
                return firstOverlayItem;
            } else if (index == 1) {
                return secondOverlayItem;
            }
            return null;
        }

        @Override
        public int size() {
            return 2;
        }

        public void callPopulate() {
            populate();
        }
    }

    @Test
    public void populateShouldCreateItems() {
        TestItemizedOverlay itemizedOverlay = new TestItemizedOverlay();
        itemizedOverlay.callPopulate();

        assertEquals(itemizedOverlay.firstOverlayItem, itemizedOverlay.getItem(0));
        assertEquals(itemizedOverlay.secondOverlayItem, itemizedOverlay.getItem(1));
    }

    @Test
    public void callingPopulateTwoTimesShouldNotAddAdditionalItems() {
        TestItemizedOverlay itemizedOverlay = new TestItemizedOverlay();
        itemizedOverlay.callPopulate();
        itemizedOverlay.callPopulate();

        assertEquals(itemizedOverlay.firstOverlayItem, itemizedOverlay.getItem(0));
        assertEquals(itemizedOverlay.secondOverlayItem, itemizedOverlay.getItem(1));

        boolean indexOutOfBoundsExceptionCatched = false;
        try {
            itemizedOverlay.getItem(2);
        } catch (IndexOutOfBoundsException e) {
            indexOutOfBoundsExceptionCatched = true;
        }
        assertTrue(indexOutOfBoundsExceptionCatched);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void getItemWithoutPopulateShouldThrowIndexOutOfBoundException() {
        TestItemizedOverlay itemizedOverlay = new TestItemizedOverlay();

        assertNull(itemizedOverlay.getItem(0));
    }
}

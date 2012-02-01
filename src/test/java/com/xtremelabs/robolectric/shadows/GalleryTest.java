package com.xtremelabs.robolectric.shadows;

import android.view.KeyEvent;
import android.view.View;
import android.widget.Gallery;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class GalleryTest {
    private Gallery gallery;
    private TestOnKeyListener listener;
    private KeyEvent event;

    @Before
    public void setUp() throws Exception {
        gallery = new Gallery(null);
        listener = new TestOnKeyListener();
        gallery.setOnKeyListener(listener);
        event = new KeyEvent(1, 2);
    }

    @Test
    public void onKeyDown_dPadRightShouldTriggerKeyEventDPadRight() throws Exception {
        assertTrue(gallery.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, event));
        assertThat(listener.keyCode, equalTo(KeyEvent.KEYCODE_DPAD_RIGHT));
        assertThat((Gallery) listener.view, sameInstance(gallery));
        assertThat(listener.event, sameInstance(event));
    }

    @Test
    public void onKeyDown_dPadLeftShouldTriggerKeyEventListener() throws Exception {
        assertTrue(gallery.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, event));
        assertThat(listener.keyCode, equalTo(KeyEvent.KEYCODE_DPAD_RIGHT));
        assertThat((Gallery)listener.view, sameInstance(gallery));
        assertThat(listener.event, sameInstance(event));
    }

    private static class TestOnKeyListener implements View.OnKeyListener {
        View view;
        int keyCode;
        KeyEvent event;

        @Override
        public boolean onKey(View view, int keyCode, KeyEvent event) {
            this.view = view;
            this.keyCode = keyCode;
            this.event = event;
            return false;
        }
    }
}

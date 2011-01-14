package com.xtremelabs.robolectric.shadows;


import android.hardware.Camera;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class CameraSizeTest {

    private Camera.Size cameraSize;

    @Before
    public void setUp() throws Exception {
        cameraSize = Robolectric.newInstanceOf(Camera.class).new Size(480, 320);
    }

    @Test
    public void testConstructor() throws Exception {
        assertThat(cameraSize.width, equalTo(480));
        assertThat(cameraSize.height, equalTo(320));
    }

    @Test
    public void testSetWidth() throws Exception {
        assertThat(cameraSize.width, not(equalTo(640)));
        cameraSize.width = 640;
        assertThat(cameraSize.width, equalTo(640));
    }

    @Test
    public void testSetHeight() throws Exception {
        assertThat(cameraSize.height, not(equalTo(480)));
        cameraSize.height = 480;
        assertThat(cameraSize.height, equalTo(480));
    }

}

package com.xtremelabs.robolectric.shadows;


import android.graphics.ImageFormat;
import android.hardware.Camera;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class CameraParametersTest {

    private Camera.Parameters parameters;
    private ShadowCameraParameters shadowParameters;

    @Before
    public void setUp() throws Exception {
        parameters = Robolectric.newInstanceOf(Camera.Parameters.class);
        shadowParameters = Robolectric.shadowOf(parameters);
    }

    @Test
    public void testSetPreviewSize() throws Exception {
        assertThat(shadowParameters.getPreviewWidth(), not(equalTo(320)));
        assertThat(shadowParameters.getPreviewHeight(), not(equalTo(240)));
        parameters.setPreviewSize(320, 240);
        assertThat(shadowParameters.getPreviewWidth(), equalTo(320));
        assertThat(shadowParameters.getPreviewHeight(), equalTo(240));
    }

    @Test
    public void testPreviewFormat() throws Exception {
        assertThat(shadowParameters.getPreviewFormat(), equalTo(ImageFormat.NV21));
        parameters.setPreviewFormat(ImageFormat.JPEG);
        assertThat(shadowParameters.getPreviewFormat(), equalTo(ImageFormat.JPEG));
    }

    @Test
    public void testGetSupportedPreviewFormats() throws Exception {
        List<Integer> supportedFormats = parameters.getSupportedPreviewFormats();
        assertThat(supportedFormats, notNullValue());
        assertThat(supportedFormats.size(), greaterThan(0));
        assertThat(supportedFormats, hasItem(new Integer(ImageFormat.NV21)));
    }
}

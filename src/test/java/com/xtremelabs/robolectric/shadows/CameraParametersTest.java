package com.xtremelabs.robolectric.shadows;


import android.graphics.ImageFormat;
import android.hardware.Camera;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
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
    public void testPictureSize() throws Exception {
        assertThat(shadowParameters.getPictureHeight(), not(equalTo(600)));
        assertThat(shadowParameters.getPictureWidth(), not(equalTo(800)));
        parameters.setPictureSize(800, 600);
        Camera.Size pictureSize = parameters.getPictureSize();
        assertThat(pictureSize.width, equalTo(800));
        assertThat(pictureSize.height, equalTo(600));
        assertThat(shadowParameters.getPictureHeight(), equalTo(600));
        assertThat(shadowParameters.getPictureWidth(), equalTo(800));
    }

    @Test
    public void testPreviewFpsRange() throws Exception {
        int[] fpsRange = new int[2];
        parameters.getPreviewFpsRange(fpsRange);
        assertThat(fpsRange[1], not(equalTo(15)));
        assertThat(fpsRange[0], not(equalTo(25)));
        parameters.setPreviewFpsRange(15, 25);
        parameters.getPreviewFpsRange(fpsRange);
        assertThat(fpsRange[1], equalTo(25));
        assertThat(fpsRange[0], equalTo(15));
    }

    @Test
    public void testPreviewFrameRate() throws Exception {
        assertThat(parameters.getPreviewFrameRate(), not(equalTo(15)));
        parameters.setPreviewFrameRate(15);
        assertThat(parameters.getPreviewFrameRate(), equalTo(15));
    }

    @Test
    public void testPreviewSize() throws Exception {
        assertThat(shadowParameters.getPreviewWidth(), not(equalTo(320)));
        assertThat(shadowParameters.getPreviewHeight(), not(equalTo(240)));
        parameters.setPreviewSize(320, 240);
        Camera.Size size = parameters.getPreviewSize();
        assertThat(size.width, equalTo(320));
        assertThat(size.height, equalTo(240));
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
        assertThat(supportedFormats.size(), not(equalTo(0)));
        assertThat(supportedFormats, hasItem(ImageFormat.NV21));
    }

    @Test
    public void testGetSupportedPictureFormats() throws Exception {
        List<Integer> supportedFormats = parameters.getSupportedPictureFormats();
        assertThat(supportedFormats, notNullValue());
        assertThat(supportedFormats.size(), equalTo(2));
        assertThat(supportedFormats, hasItem(new Integer(ImageFormat.NV21)));
    }

    @Test
    public void testGetSupportedPictureSizes() throws Exception {
        List<Camera.Size> supportedSizes = parameters.getSupportedPictureSizes();
        assertThat(supportedSizes, notNullValue());
        assertThat(supportedSizes.size(), equalTo(3));
        assertThat(supportedSizes.get(0).width, equalTo(320));
        assertThat(supportedSizes.get(0).height, equalTo(240));
    }

    @Test
    public void testGetSupportedPreviewSizes() throws Exception {
        List<Camera.Size> supportedSizes = parameters.getSupportedPreviewSizes();
        assertThat(supportedSizes, notNullValue());
        assertThat(supportedSizes.size(), equalTo(2));
        assertThat(supportedSizes.get(0).width, equalTo(320));
        assertThat(supportedSizes.get(0).height, equalTo(240));
    }

    @Test
    public void testGetSupportedPreviewFpsRange() throws Exception {
        List<int[]> supportedRanges = parameters.getSupportedPreviewFpsRange();
        assertThat(supportedRanges, notNullValue());
        assertThat(supportedRanges.size(), equalTo(2));
        assertThat(supportedRanges.get(0)[0], equalTo(15000));
        assertThat(supportedRanges.get(0)[1], equalTo(15000));
        assertThat(supportedRanges.get(1)[0], equalTo(10000));
        assertThat(supportedRanges.get(1)[1], equalTo(30000));
    }

    @Test
    public void testGetSupportedPreviewFrameRates() throws Exception {
        List<Integer> supportedRates = parameters.getSupportedPreviewFrameRates();
        assertThat(supportedRates, notNullValue());
        assertThat(supportedRates.size(), equalTo(3));
        assertThat(supportedRates.get(0), equalTo(10));
    }

}

package com.xtremelabs.robolectric.shadows;


import java.util.List;

import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import android.graphics.ImageFormat;
import android.hardware.Camera;

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
		assertThat( shadowParameters.getPreviewWidth(), not(equalTo(640)));
		assertThat( shadowParameters.getPreviewHeight(), not(equalTo(480)));
		parameters.setPreviewSize(640, 480);
		assertThat( shadowParameters.getPreviewWidth(), equalTo(640));
		assertThat( shadowParameters.getPreviewHeight(), equalTo(480));
	}
	
	@Test
	public void testPreviewFormat() throws Exception {
		assertThat( shadowParameters.getPreviewFormat(), equalTo(ImageFormat.NV21) );
		parameters.setPreviewFormat(ImageFormat.JPEG);
		assertThat( shadowParameters.getPreviewFormat(), equalTo(ImageFormat.JPEG) );
	}
	
	@Test
	public void testGetSupportedPreviewFormats() throws Exception {
		List<Integer> supportedFormats = parameters.getSupportedPreviewFormats();
		assertThat(supportedFormats, notNullValue());
		assertThat(supportedFormats.size(), greaterThan(0));
		assertThat(supportedFormats, hasItem(new Integer(ImageFormat.NV21)));
	}
	
}

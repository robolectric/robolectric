package com.xtremelabs.robolectric.shadows;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import android.view.View;
import android.view.View.MeasureSpec;

@RunWith(WithTestDefaultsRunner.class)
public class ViewMeasureSpecTest {

	@Test
	public void testMode() throws Exception {
		assertEquals(View.MeasureSpec.UNSPECIFIED,
				MeasureSpec.getMode(MeasureSpec.makeMeasureSpec(512, View.MeasureSpec.UNSPECIFIED)));
		assertEquals(View.MeasureSpec.EXACTLY, 
				MeasureSpec.getMode(MeasureSpec.makeMeasureSpec(600, View.MeasureSpec.EXACTLY)));
		assertEquals(View.MeasureSpec.AT_MOST, 
				MeasureSpec.getMode(MeasureSpec.makeMeasureSpec(320, View.MeasureSpec.AT_MOST)));
	}
	
	@Test
	public void testSize() throws Exception {
		assertEquals(512, 
				MeasureSpec.getSize(MeasureSpec.makeMeasureSpec(512, View.MeasureSpec.UNSPECIFIED)));
		assertEquals(800,
				MeasureSpec.getSize(MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY)));
		assertEquals(1280,
				MeasureSpec.getSize(MeasureSpec.makeMeasureSpec(1280, View.MeasureSpec.AT_MOST)));
	}
	
	@Test
	public void testToString() throws Exception {
		assertEquals("MeasureSpec: UNSPECIFIED 512",
				MeasureSpec.toString(MeasureSpec.makeMeasureSpec(512, View.MeasureSpec.UNSPECIFIED)));
		assertEquals("MeasureSpec: EXACTLY 480",
				MeasureSpec.toString(MeasureSpec.makeMeasureSpec(480, View.MeasureSpec.EXACTLY)));
		assertEquals("MeasureSpec: AT_MOST 960",
				MeasureSpec.toString(MeasureSpec.makeMeasureSpec(960, View.MeasureSpec.AT_MOST)));
	}

}

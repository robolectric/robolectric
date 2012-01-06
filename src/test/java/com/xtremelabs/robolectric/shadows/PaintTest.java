package com.xtremelabs.robolectric.shadows;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.graphics.Paint;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.*;


@RunWith(WithTestDefaultsRunner.class)
public class PaintTest {

	@Test
	public void shouldGetIsDitherInfo() {
		Paint paint = Robolectric.newInstanceOf(Paint.class);
		assertFalse(paint.isAntiAlias());
		ShadowPaint shadowPaint = shadowOf(paint);
		shadowPaint.setAntiAlias(true);
		assertTrue(paint.isAntiAlias());		
	}
	
	@Test
	public void shouldGetIsAntiAlias() {
		Paint paint = Robolectric.newInstanceOf(Paint.class);
		assertFalse(paint.isAntiAlias());
		ShadowPaint shadowPaint = shadowOf(paint);
		shadowPaint.setAntiAlias(true);
		assertTrue(paint.isAntiAlias());				
	}
	
	@Test
	public void testCtor() {
		Paint paint = Robolectric.newInstanceOf(Paint.class);
		assertFalse(paint.isAntiAlias());
		ShadowPaint shadowPaint = shadowOf(paint);
		shadowPaint.__constructor__( Paint.ANTI_ALIAS_FLAG );
		assertTrue(paint.isAntiAlias());		
	}
}

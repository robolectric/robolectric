package com.xtremelabs.robolectric.shadows;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.graphics.Path;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertEquals;


@RunWith(WithTestDefaultsRunner.class)
public class PathTest {
	
	@Test
	public void testGradTo() {
		Path path = Robolectric.newInstanceOf(Path.class);
		path.quadTo(0, 5, 10, 15);
		ShadowPath shadowPath = shadowOf(path);
		assertEquals(shadowPath.getQuadDescription(), "Add a quadratic bezier from last point, approaching (0.0,5.0), ending at (10.0,15.0)");
	}
}

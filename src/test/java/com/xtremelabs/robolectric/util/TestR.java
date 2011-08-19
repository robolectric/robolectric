package com.xtremelabs.robolectric.util;

/**
 * Test class used as a substitute for com.xtremelabs.robolectric.res.ResourceLoader's
 * reference to the application R class
 */
public final class TestR {
	
	private static int base = 1025;
	
	public static final class anim {
		public static final int test_anim_1 = ++base;
	}
	
	public static class color {
		public static final int test_color_1 = ++base;
	}
	
	public static class drawable {
		public static final int test_drawable_1 = ++base;		
	}
}
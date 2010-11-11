package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Display.class)
public class ShadowDisplay {
	
	private int displayId;
	private int width = 800;
	private int height = 480;
	private float density = 1.5f;
	private int densityDpi = DisplayMetrics.DENSITY_HIGH;
	private int xdpi = 240;
	private int ydpi = 240;
	private float scaledDensity = 1.0f;
	private float refreshRate = 60.0f;
	private int rotation = Surface.ROTATION_0;
	private int pixelFormat = PixelFormat.RGBA_4444;

	@Implementation
	public int getHeight() {
		return height;
	}

	@Implementation
	public void getMetrics(DisplayMetrics outMetrics) {
		outMetrics.density = density;
		outMetrics.densityDpi = densityDpi;
		outMetrics.scaledDensity = scaledDensity;
		outMetrics.widthPixels = width;
		outMetrics.heightPixels = height;
		outMetrics.xdpi = xdpi;
		outMetrics.ydpi = ydpi;
	}

	@Implementation
	public int getWidth() {
		return width;
	}

	@Implementation
	public int getDisplayId() {
		return displayId;
	}

	@Implementation
	public float getRefreshRate() {
		return refreshRate;
	}

	@Implementation
	public int getRotation() {
		return rotation;
	}

	@Implementation
	public int getPixelFormat() {
		return pixelFormat;
	}

	public float getDensity() {
		return density;
	}

	public void setDensity(float density) {
		this.density = density;
	}

	public int getDensityDpi() {
		return densityDpi;
	}

	public void setDensityDpi(int densityDpi) {
		this.densityDpi = densityDpi;
	}

	public int getXdpi() {
		return xdpi;
	}

	public void setXdpi(int xdpi) {
		this.xdpi = xdpi;
	}

	public int getYdpi() {
		return ydpi;
	}

	public void setYdpi(int ydpi) {
		this.ydpi = ydpi;
	}

	public float getScaledDensity() {
		return scaledDensity;
	}

	public void setScaledDensity(float scaledDensity) {
		this.scaledDensity = scaledDensity;
	}

	public void setDisplayId(int displayId) {
		this.displayId = displayId;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setRefreshRate(float refreshRate) {
		this.refreshRate = refreshRate;
	}

	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	public void setPixelFormat(int pixelFormat) {
		this.pixelFormat = pixelFormat;
	}

}

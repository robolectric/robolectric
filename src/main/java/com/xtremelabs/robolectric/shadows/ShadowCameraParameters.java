package com.xtremelabs.robolectric.shadows;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.List;

/**
 * Shadows the Android {@code Camera.Parameters} class.
 */
@Implements(Camera.Parameters.class)
public class ShadowCameraParameters {

    private int pictureWidth = 1280;
    private int pictureHeight = 960;
    private int previewWidth = 640;
    private int previewHeight = 480;
    private int previewFormat = ImageFormat.NV21;
    private int previewFpsMin = 10;
    private int previewFpsMax = 30;
    private int previewFps = 30;

    @Implementation
    public Camera.Size getPictureSize() {
        Camera.Size pictureSize = Robolectric.newInstanceOf(Camera.class).new Size(0, 0);
        pictureSize.width = pictureWidth;
        pictureSize.height = pictureHeight;
        return pictureSize;
    }

    @Implementation
    public int getPreviewFormat() {
        return previewFormat;
    }

    @Implementation
    public void getPreviewFpsRange(int[] range) {
        range[0] = previewFpsMin;
        range[1] = previewFpsMax;
    }

    @Implementation
    public int getPreviewFrameRate() {
        return previewFps;
    }

    @Implementation
    public Camera.Size getPreviewSize() {
        Camera.Size previewSize = Robolectric.newInstanceOf(Camera.class).new Size(0, 0);
        previewSize.width = previewWidth;
        previewSize.height = previewHeight;
        return previewSize;
    }

    @Implementation
    public List<Camera.Size> getSupportedPictureSizes() {
        List<Camera.Size> supportedSizes = new ArrayList<Camera.Size>();
        addSize(supportedSizes, 320, 240);
        addSize(supportedSizes, 640, 480);
        addSize(supportedSizes, 800, 600);
        return supportedSizes;
    }

    @Implementation
    public List<Integer> getSupportedPictureFormats() {
        List<Integer> formats = new ArrayList<Integer>();
        formats.add(ImageFormat.NV21);
        formats.add(ImageFormat.JPEG);
        return formats;
    }

    @Implementation
    public List<Integer> getSupportedPreviewFormats() {
        List<Integer> formats = new ArrayList<Integer>();
        formats.add(ImageFormat.NV21);
        formats.add(ImageFormat.JPEG);
        return formats;
    }

    @Implementation
    public List<int[]> getSupportedPreviewFpsRange() {
        List<int[]> supportedRanges = new ArrayList<int[]>();
        addRange(supportedRanges, 15000, 15000);
        addRange(supportedRanges, 10000, 30000);
        return supportedRanges;
    }

    @Implementation
    public List<Integer> getSupportedPreviewFrameRates() {
        List<Integer> supportedRates = new ArrayList<Integer>();
        supportedRates.add(10);
        supportedRates.add(15);
        supportedRates.add(30);
        return supportedRates;
    }

    @Implementation
    public List<Camera.Size> getSupportedPreviewSizes() {
        List<Camera.Size> supportedSizes = new ArrayList<Camera.Size>();
        addSize(supportedSizes, 320, 240);
        addSize(supportedSizes, 640, 480);
        return supportedSizes;
    }

    @Implementation
    public void setPictureSize(int width, int height) {
        pictureWidth = width;
        pictureHeight = height;
    }

    @Implementation
    public void setPreviewFormat(int pixel_format) {
        previewFormat = pixel_format;
    }

    @Implementation
    public void setPreviewFpsRange(int min, int max) {
        previewFpsMin = min;
        previewFpsMax = max;
    }

    @Implementation
    public void setPreviewFrameRate(int fps) {
        previewFps = fps;
    }

    @Implementation
    public void setPreviewSize(int width, int height) {
        previewWidth = width;
        previewHeight = height;
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    public int getPictureWidth() {
        return pictureWidth;
    }

    public int getPictureHeight() {
        return pictureHeight;
    }

    private void addSize(List<Camera.Size> sizes, int width, int height) {
        Camera.Size newSize = Robolectric.newInstanceOf(Camera.class).new Size(0, 0);
        newSize.width = width;
        newSize.height = height;
        sizes.add(newSize);
    }

    private void addRange(List<int[]> ranges, int min, int max) {
        int[] range = new int[2];
        range[0] = min;
        range[1] = max;
        ranges.add(range);
    }

}

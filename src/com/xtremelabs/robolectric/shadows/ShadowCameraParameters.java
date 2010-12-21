package com.xtremelabs.robolectric.shadows;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.List;

/**
 * Shadows the Android {@code Camera.Parameters} class.
 */
@Implements(Camera.Parameters.class)
public class ShadowCameraParameters {

    private int previewWidth;
    private int previewHeight;
    private int previewFormat = ImageFormat.NV21;

    @Implementation
    public void setPreviewSize(int width, int height) {
        this.previewWidth = width;
        this.previewHeight = height;
    }

    @Implementation
    public void setPreviewFormat(int pixel_format) {
        previewFormat = pixel_format;
    }

    @Implementation
    public int getPreviewFormat() {
        return previewFormat;
    }

    @Implementation
    public List<Integer> getSupportedPreviewFormats() {
        List<Integer> formats = new ArrayList<Integer>();
        formats.add(ImageFormat.NV21);
        formats.add(ImageFormat.JPEG);
        return formats;
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }
}

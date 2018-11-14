package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowCameraParametersTest {

  private Camera.Parameters parameters;

  @Before
  public void setUp() throws Exception {
    parameters = Shadow.newInstanceOf(Camera.Parameters.class);
  }

  @Test
  public void testPictureSize() throws Exception {
    assertThat(Shadows.shadowOf(parameters).getPictureHeight()).isNotEqualTo(600);
    assertThat(Shadows.shadowOf(parameters).getPictureWidth()).isNotEqualTo(800);
    parameters.setPictureSize(800, 600);
    Camera.Size pictureSize = parameters.getPictureSize();
    assertThat(pictureSize.width).isEqualTo(800);
    assertThat(pictureSize.height).isEqualTo(600);
    assertThat(Shadows.shadowOf(parameters).getPictureHeight()).isEqualTo(600);
    assertThat(Shadows.shadowOf(parameters).getPictureWidth()).isEqualTo(800);
  }

  @Test
  public void testPreviewFpsRange() throws Exception {
    int[] fpsRange = new int[2];
    parameters.getPreviewFpsRange(fpsRange);
    assertThat(fpsRange[1]).isNotEqualTo((15));
    assertThat(fpsRange[0]).isNotEqualTo((25));
    parameters.setPreviewFpsRange(15, 25);
    parameters.getPreviewFpsRange(fpsRange);
    assertThat(fpsRange[1]).isEqualTo(25);
    assertThat(fpsRange[0]).isEqualTo(15);
  }

  @Test
  public void testPreviewFrameRate() throws Exception {
    assertThat(parameters.getPreviewFrameRate()).isNotEqualTo((15));
    parameters.setPreviewFrameRate(15);
    assertThat(parameters.getPreviewFrameRate()).isEqualTo(15);
  }

  @Test
  public void testPreviewSize() throws Exception {
    assertThat(Shadows.shadowOf(parameters).getPreviewWidth()).isNotEqualTo(320);
    assertThat(Shadows.shadowOf(parameters).getPreviewHeight()).isNotEqualTo(240);
    parameters.setPreviewSize(320, 240);
    Camera.Size size = parameters.getPreviewSize();
    assertThat(size.width).isEqualTo(320);
    assertThat(size.height).isEqualTo(240);
    assertThat(Shadows.shadowOf(parameters).getPreviewWidth()).isEqualTo(320);
    assertThat(Shadows.shadowOf(parameters).getPreviewHeight()).isEqualTo(240);
  }

  @Test
  public void testPreviewFormat() throws Exception {
    assertThat(parameters.getPreviewFormat()).isEqualTo(ImageFormat.NV21);
    parameters.setPreviewFormat(ImageFormat.JPEG);
    assertThat(parameters.getPreviewFormat()).isEqualTo(ImageFormat.JPEG);
  }

  @Test
  public void testGetSupportedPreviewFormats() throws Exception {
    List<Integer> supportedFormats = parameters.getSupportedPreviewFormats();
    assertThat(supportedFormats).isNotNull();
    assertThat(supportedFormats.size()).isNotEqualTo((0));
    assertThat(supportedFormats).contains(ImageFormat.NV21);
  }

  @Test
  public void testGetSupportedPictureFormats() throws Exception {
    List<Integer> supportedFormats = parameters.getSupportedPictureFormats();
    assertThat(supportedFormats).isNotNull();
    assertThat(supportedFormats.size()).isEqualTo(2);
    assertThat(supportedFormats).contains(ImageFormat.NV21);
  }

  @Test
  public void testGetSupportedPictureSizes() throws Exception {
    List<Camera.Size> supportedSizes = parameters.getSupportedPictureSizes();
    assertThat(supportedSizes).isNotNull();
    assertThat(supportedSizes.size()).isEqualTo(3);
    assertThat(supportedSizes.get(0).width).isEqualTo(320);
    assertThat(supportedSizes.get(0).height).isEqualTo(240);
  }

  @Test
  public void testGetSupportedPreviewSizes() throws Exception {
    List<Camera.Size> supportedSizes = parameters.getSupportedPreviewSizes();
    assertThat(supportedSizes).isNotNull();
    assertThat(supportedSizes.size()).isEqualTo(2);
    assertThat(supportedSizes.get(0).width).isEqualTo(320);
    assertThat(supportedSizes.get(0).height).isEqualTo(240);
  }

  @Test
  public void testInitSupportedPreviewSizes() throws Exception {
    Shadows.shadowOf(parameters).initSupportedPreviewSizes();
    assertThat(parameters.getSupportedPreviewSizes()).isNotNull();
    assertThat(parameters.getSupportedPreviewSizes()).isEmpty();
  }

  @Test
  public void testAddSupportedPreviewSizes() throws Exception {
    Shadows.shadowOf(parameters).initSupportedPreviewSizes();
    Shadows.shadowOf(parameters).addSupportedPreviewSize(320, 240);
    List<Camera.Size> supportedSizes = parameters.getSupportedPreviewSizes();
    assertThat(supportedSizes).isNotNull();
    assertThat(supportedSizes).hasSize(1);
    assertThat(supportedSizes.get(0).width).isEqualTo(320);
    assertThat(supportedSizes.get(0).height).isEqualTo(240);
  }

  @Test
  public void testGetSupportedPreviewFpsRange() throws Exception {
    List<int[]> supportedRanges = parameters.getSupportedPreviewFpsRange();
    assertThat(supportedRanges).isNotNull();
    assertThat(supportedRanges.size()).isEqualTo(2);
    assertThat(supportedRanges.get(0)[0]).isEqualTo(15000);
    assertThat(supportedRanges.get(0)[1]).isEqualTo(15000);
    assertThat(supportedRanges.get(1)[0]).isEqualTo(10000);
    assertThat(supportedRanges.get(1)[1]).isEqualTo(30000);
  }

  @Test
  public void testGetSupportedPreviewFrameRates() throws Exception {
    List<Integer> supportedRates = parameters.getSupportedPreviewFrameRates();
    assertThat(supportedRates).isNotNull();
    assertThat(supportedRates.size()).isEqualTo(3);
    assertThat(supportedRates.get(0)).isEqualTo(10);
  }

  @Test
  public void testExposureCompensationLimits() {
    assertThat(parameters.getMinExposureCompensation()).isEqualTo(-6);
    assertThat(parameters.getMaxExposureCompensation()).isEqualTo(6);
    assertThat(parameters.getExposureCompensationStep()).isEqualTo(0.5f);
  }

  @Test
  public void testExposureCompensationSetting() {
    assertThat(parameters.getExposureCompensation()).isEqualTo(0);
    parameters.setExposureCompensation(5);
    assertThat(parameters.getExposureCompensation()).isEqualTo(5);
  }

  @Test
  public void testGetSupportedFocusModesDefaultValue() {
    List<String> supportedFocusModes = parameters.getSupportedFocusModes();
    assertThat(supportedFocusModes).isEmpty();
  }

  @Test
  public void testSetSupportedFocusModes() {
    Shadows.shadowOf(parameters).setSupportedFocusModes("foo", "bar");
    assertThat(parameters.getSupportedFocusModes()).isEqualTo(Lists.newArrayList("foo", "bar"));
    Shadows.shadowOf(parameters).setSupportedFocusModes("baz");
    assertThat(parameters.getSupportedFocusModes()).isEqualTo(Lists.newArrayList("baz"));
  }

  @Test
  public void testSetAndGetFocusMode() {
    parameters.setFocusMode("foo");
    assertThat(parameters.getFocusMode()).isEqualTo("foo");
  }

  @Test
  public void testGetSupportedFlashModesDefaultValue() {
    List<String> supportedFlashModes = parameters.getSupportedFlashModes();
    assertThat(supportedFlashModes).isEmpty();
  }

  @Test
  public void testSetSupportedFlashModes() {
    Shadows.shadowOf(parameters).setSupportedFlashModes("foo", "bar");
    assertThat(parameters.getSupportedFlashModes()).containsExactly("foo", "bar").inOrder();
    Shadows.shadowOf(parameters).setSupportedFlashModes("baz");
    assertThat(parameters.getSupportedFlashModes()).containsExactly("baz");
  }

  @Test
  public void testSetAndGetFlashMode() {
    parameters.setFlashMode("foo");
    assertThat(parameters.getFlashMode()).isEqualTo("foo");
  }
}

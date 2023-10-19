package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.Gainmap;
import android.os.Parcel;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit test for {@link ShadowGainmap}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = UPSIDE_DOWN_CAKE)
public class ShadowGainmapTest {

  @Test
  public void testGainmap_getSetRatioMin() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Gainmap gainmap = new Gainmap(bitmap);
    gainmap.setRatioMin(1, 2, 3);
    assertThat(gainmap.getRatioMin()).isEqualTo(new float[] {1, 2, 3});
  }

  @Test
  public void testGainmap_getSetRatioMax() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Gainmap gainmap = new Gainmap(bitmap);
    gainmap.setRatioMax(1, 2, 3);
    assertThat(gainmap.getRatioMax()).isEqualTo(new float[] {1, 2, 3});
  }

  @Test
  public void testGainmap_getSetGamma() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Gainmap gainmap = new Gainmap(bitmap);
    gainmap.setGamma(1, 2, 3);
    assertThat(gainmap.getGamma()).isEqualTo(new float[] {1, 2, 3});
  }

  @Test
  public void testGainmap_getSetEpsilonSdr() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Gainmap gainmap = new Gainmap(bitmap);
    gainmap.setEpsilonSdr(1, 2, 3);
    assertThat(gainmap.getEpsilonSdr()).isEqualTo(new float[] {1, 2, 3});
  }

  @Test
  public void testGainmap_getSetEpsilonHdr() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Gainmap gainmap = new Gainmap(bitmap);
    gainmap.setEpsilonHdr(1, 2, 3);
    assertThat(gainmap.getEpsilonHdr()).isEqualTo(new float[] {1, 2, 3});
  }

  @Test
  public void testGainmap_getSetDisplayRatioForFullHdr() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Gainmap gainmap = new Gainmap(bitmap);
    gainmap.setDisplayRatioForFullHdr(5.0f);
    assertThat(gainmap.getDisplayRatioForFullHdr()).isEqualTo(5.0f);
  }

  @Test
  public void testGainmap_getSetMinDisplayRatioForHdrTransition() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Gainmap gainmap = new Gainmap(bitmap);
    gainmap.setDisplayRatioForFullHdr(5.0f);
    assertThat(gainmap.getDisplayRatioForFullHdr()).isEqualTo(5.0f);
  }

  @Test
  public void testGainmap_writeToParcel() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Gainmap gainmap = new Gainmap(bitmap);

    Parcel parcel = Parcel.obtain();
    gainmap.writeToParcel(parcel, 0);

    parcel.setDataPosition(0);
    Gainmap parcelGainmap = Gainmap.CREATOR.createFromParcel(parcel);
    assertTrue(bitmap.sameAs(parcelGainmap.getGainmapContents()));
  }

  // TODO: move this to ShadowBitmapTest once U is supported there
  @Test
  public void testBitmap_writeToParcel_with_Gainmap() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Bitmap bitmapGainmapContents = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Gainmap gainmap = new Gainmap(bitmapGainmapContents);
    bitmap.setGainmap(gainmap);
    assertThat(bitmap.hasGainmap()).isTrue();

    Parcel parcel = Parcel.obtain();
    bitmap.writeToParcel(parcel, 0);

    parcel.setDataPosition(0);
    Bitmap parcelBitmap = Bitmap.CREATOR.createFromParcel(parcel);
    assertThat(parcelBitmap.getGainmap()).isNotNull();
  }

  @Test
  public void setGainmap_recycledBitmap() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Gainmap gainmap = new Gainmap(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888));
    bitmap.recycle();
    assertThrows(IllegalStateException.class, () -> bitmap.setGainmap(gainmap));
  }

  @Test
  public void hasGainmap_recycledBitmap() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Gainmap gainmap = new Gainmap(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888));
    bitmap.setGainmap(gainmap);
    bitmap.recycle();
    assertThrows(IllegalStateException.class, bitmap::hasGainmap);
  }
}

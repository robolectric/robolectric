package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.content.ComponentName;
import android.content.LocusId;
import android.os.ParcelFileDescriptor;
import android.view.contentcapture.ContentCaptureCondition;
import android.view.contentcapture.ContentCaptureManager;
import android.view.contentcapture.DataShareWriteAdapter;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Test for {@link ShadowContentCaptureManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = R)
public final class ShadowContentCaptureManagerTest {

  private ShadowContentCaptureManager instance;

  @Before
  public void setUp() throws Exception {
    ContentCaptureManager contentCaptureManagerShadow =
        Shadow.newInstanceOf(ContentCaptureManager.class);
    instance = Shadow.extract(contentCaptureManagerShadow);

    assertThat(instance).isNotNull();
  }

  @Test
  public void getContentCaptureConditions() {
    assertThat(instance.getContentCaptureConditions()).isNull();
  }

  @Test
  public void getContentCaptureConditions_withContentCaptureConditions() {
    Set<ContentCaptureCondition> contentCaptureConditions = new HashSet<>();
    contentCaptureConditions.add(new ContentCaptureCondition(new LocusId("fake locusId"), 0));

    instance.setContentCaptureConditions(contentCaptureConditions);
    assertThat(instance.getContentCaptureConditions()).isEqualTo(contentCaptureConditions);
  }

  @Test
  public void getServiceComponentName() {
    assertThat(instance.getServiceComponentName()).isNull();
  }

  @Test
  public void getServiceComponentName_nonNull() {
    ComponentName componentName = new ComponentName("fake pkg", "fake cls");

    instance.setServiceComponentName(componentName);
    assertThat(instance.getServiceComponentName()).isEqualTo(componentName);
  }

  @Test
  public void isContentCaptureEnabled() {
    assertThat(instance.isContentCaptureEnabled()).isFalse();
  }

  @Test
  public void isContentCaptureEnabled_setToTrue() {
    instance.setContentCaptureEnabled(true);
    assertThat(instance.isContentCaptureEnabled()).isTrue();
  }

  @Test
  public void shareData() {
    DataShareWriteAdapter adapter = mock(DataShareWriteAdapter.class);

    instance.shareData(null, null, adapter);

    verify(adapter).onWrite(null);
  }

  @Test
  public void shareData_parcelFileDescriptorSpecified() {
    DataShareWriteAdapter adapter = mock(DataShareWriteAdapter.class);
    ParcelFileDescriptor parcelFileDescriptor = mock(ParcelFileDescriptor.class);

    instance.setShareDataParcelFileDescriptor(parcelFileDescriptor);
    instance.shareData(null, null, adapter);

    verify(adapter).onWrite(parcelFileDescriptor);
  }

  @Test
  public void shareData_withRejection() {
    DataShareWriteAdapter adapter = mock(DataShareWriteAdapter.class);

    instance.setShouldRejectRequest(true);
    instance.shareData(null, null, adapter);

    verify(adapter).onRejected();
  }

  @Test
  public void shareData_withDataShareErrorCode() {
    DataShareWriteAdapter adapter = mock(DataShareWriteAdapter.class);

    instance.setDataShareErrorCode(ContentCaptureManager.DATA_SHARE_ERROR_CONCURRENT_REQUEST);
    instance.shareData(null, null, adapter);

    verify(adapter).onError(ContentCaptureManager.DATA_SHARE_ERROR_CONCURRENT_REQUEST);
  }
}

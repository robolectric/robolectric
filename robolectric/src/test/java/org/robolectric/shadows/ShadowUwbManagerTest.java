package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.os.PersistableBundle;
import android.uwb.RangingSession;
import android.uwb.UwbManager;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Unit tests for {@link ShadowUwbManager}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = S)
public class ShadowUwbManagerTest {
  private /* RangingSession.Callback */ Object callbackObject;
  private /* UwbManager */ Object uwbManagerObject;
  private ShadowRangingSession.Adapter adapter;

  @Before
  public void setUp() {
    callbackObject = mock(RangingSession.Callback.class);
    adapter = mock(ShadowRangingSession.Adapter.class);
    uwbManagerObject = getApplicationContext().getSystemService(UwbManager.class);
  }

  @Test
  public void openRangingSession_openAdapter() {
    UwbManager manager = (UwbManager) uwbManagerObject;
    RangingSession.Callback callback = (RangingSession.Callback) callbackObject;
    Shadow.<ShadowUwbManager>extract(manager).setUwbAdapter(adapter);
    manager.openRangingSession(genParams("openRangingSession"), directExecutor(), callback);
    verify(adapter)
        .onOpen(
            any(RangingSession.class), eq(callback), argThat(checkParams("openRangingSession")));
  }

  @Test
  public void getSpecificationInfo_expectedValue() {
    UwbManager manager = (UwbManager) uwbManagerObject;
    Shadow.<ShadowUwbManager>extract(manager)
        .setSpecificationInfo(genParams("getSpecificationInfo"));
    assertThat(getName(manager.getSpecificationInfo())).isEqualTo("getSpecificationInfo");
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void openRangingSessionWithChipId_openAdapter() {
    UwbManager manager = (UwbManager) uwbManagerObject;
    RangingSession.Callback callback = (RangingSession.Callback) callbackObject;
    Shadow.<ShadowUwbManager>extract(manager).setUwbAdapter(adapter);
    manager.openRangingSession(
        genParams("openRangingSession"), directExecutor(), callback, "chipId");
    verify(adapter)
        .onOpen(
            any(RangingSession.class), eq(callback), argThat(checkParams("openRangingSession")));
  }

  @Config(minSdk = TIRAMISU)
  @Test
  public void getChipInfos_expectedValue() {
    UwbManager manager = (UwbManager) uwbManagerObject;
    Shadow.<ShadowUwbManager>extract(manager).setChipInfos(ImmutableList.of(genParams("chipInfo")));

    List<PersistableBundle> chipInfos = manager.getChipInfos();
    assertThat(chipInfos).hasSize(1);
    assertThat(getName(chipInfos.get(0))).isEqualTo("chipInfo");
  }

  private static PersistableBundle genParams(String name) {
    PersistableBundle params = new PersistableBundle();
    params.putString("test", name);
    return params;
  }

  private static String getName(PersistableBundle params) {
    return params.getString("test");
  }

  private static ArgumentMatcher<PersistableBundle> checkParams(String name) {
    return params -> getName(params).equals(name);
  }
}

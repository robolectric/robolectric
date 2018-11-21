package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.pm.ResolveInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowResolveInfoTest {

  private ResolveInfo mResolveInfo;

  @Before
  public void setup() {
    mResolveInfo = ShadowResolveInfo.newResolveInfo("name", "package", "fragmentActivity");
  }

  @Test
  public void testNewResolveInfoWithActivity() {
    assertThat(mResolveInfo.loadLabel(null).toString()).isEqualTo("name");
    assertThat(mResolveInfo.activityInfo.packageName).isEqualTo("package");
    assertThat(mResolveInfo.activityInfo.applicationInfo.packageName).isEqualTo("package");
    assertThat(mResolveInfo.activityInfo.name).isEqualTo("fragmentActivity");
  }
}

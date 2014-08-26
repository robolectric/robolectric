package org.robolectric.shadows;

import android.content.pm.ResolveInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ResolveInfoTest {

  private ResolveInfo mResolveInfo;
  private ShadowResolveInfo mShadowInfo;

  @Before
  public void setup() {
    mResolveInfo = ShadowResolveInfo.newResolveInfo("name", "package", "fragmentActivity");
    mShadowInfo = Robolectric.shadowOf(mResolveInfo);
  }

  @Test
  public void testLoadLabel() {
    mShadowInfo.setLabel("test");
    assertThat((CharSequence) "test").isEqualTo(mResolveInfo.loadLabel(null));
  }

  @Test
  public void testNewResolveInfoWithActivity() {
    assertThat(mResolveInfo.loadLabel(null).toString()).isEqualTo("name");
    assertThat(mResolveInfo.activityInfo.packageName).isEqualTo("package");
    assertThat(mResolveInfo.activityInfo.applicationInfo.packageName).isEqualTo("package");
    assertThat(mResolveInfo.activityInfo.name).isEqualTo("fragmentActivity");
  }
}

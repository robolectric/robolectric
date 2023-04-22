package org.robolectric.android;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;
import static org.robolectric.shadows.ShadowAssetManager.useLegacy;

import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.res.ResName;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.LOLLIPOP)
public class ResourceTableFactoryIntegrationTest {
  @Test
  public void shouldIncludeStyleableAttributesThatDoNotHaveACorrespondingEntryInAttrClass() throws Exception {
    assume().that(useLegacy()).isTrue();
    // This covers a corner case in Framework resources where an attribute is mentioned in a styleable array, e.g: R.styleable.Toolbar_buttonGravity but there is no corresponding R.attr.buttonGravity
    assertThat(RuntimeEnvironment.getSystemResourceTable()
          .getResourceId(new ResName("android", "attr", "buttonGravity"))).isGreaterThan(0);
  }
}

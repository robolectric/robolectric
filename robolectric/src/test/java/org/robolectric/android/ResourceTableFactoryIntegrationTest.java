package org.robolectric.android;

import static org.assertj.core.api.Assertions.assertThat;

import android.os.Build;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.res.ResName;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = Build.VERSION_CODES.L)
public class ResourceTableFactoryIntegrationTest {
  @Test
  public void shouldIncludeStyleableAttributesThatDoNotHaveACorrespondingEntryInAttrClass() throws Exception {
    // This covers a corner case in Framework resources where an attribute is mentioned in a styleable array, e.g: R.styleable.Toolbar_buttonGravity but there is no corresponding R.attr.buttonGravity
    assertThat(RuntimeEnvironment.getSystemResourceTable().getResourceId(new ResName("android", "attr", "buttonGravity"))).isGreaterThan(0);
  }
}

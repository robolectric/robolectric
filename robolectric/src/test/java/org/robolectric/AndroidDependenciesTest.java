package org.robolectric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import android.os.Bundle;
import android.util.Size;
import java.io.StringReader;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Simple test to sanity check that the loaded android-all jars include the right classes.
 */
@RunWith(RobolectricTestRunner.class)
public class AndroidDependenciesTest {
  @Test
  public void frameworkClass() {
    assertThat(new Bundle()).isNotNull();
  }

  @Test
  public void androidUtilClass() {
    assertThat(new Size(0, 0)).isNotNull();
  }
}

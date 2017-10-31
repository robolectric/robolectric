package org.robolectric.android;

import static org.assertj.core.api.Assertions.assertThat;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceTable;

@RunWith(RobolectricTestRunner.class)
public class ResourceLoaderTest {

  @Test
  @Config(qualifiers = "doesnotexist-land-xlarge")
  public void testChoosesLayoutBasedOnSearchPath_respectsOrderOfPath() throws Exception {
    View view = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.different_screen_sizes, null);
    TextView textView = (TextView) view.findViewById(android.R.id.text1);
    assertThat(textView.getText().toString()).isEqualTo("land");
  }

  @Test
  @Config(qualifiers="w0dp")
  public void checkDefaultBooleanValue() throws Exception {
	  assertThat(RuntimeEnvironment.application.getResources().getBoolean(R.bool.different_resource_boolean)).isEqualTo(false);
  }

  @Test
  @Config(qualifiers="w820dp")
  public void checkQualifiedBooleanValue() throws Exception {
	  assertThat(RuntimeEnvironment.application.getResources().getBoolean(R.bool.different_resource_boolean)).isEqualTo(true);
  }
  
  @Test
  public void checkForPollution1() throws Exception {
    checkForPollutionHelper();
  }

  @Test
  public void checkForPollution2() throws Exception {
    checkForPollutionHelper();
  }

  private void checkForPollutionHelper() {
    View view = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.different_screen_sizes, null);
    TextView textView = (TextView) view.findViewById(android.R.id.text1);
    assertThat(textView.getText().toString()).isEqualTo("default");
    RuntimeEnvironment.setQualifiers("land"); // testing if this pollutes the other test
  }

  @Test
  public void shouldMakeInternalResourcesAvailable() throws Exception {
    ResourceTable resourceProvider = RuntimeEnvironment.getSystemResourceTable();
    ResName internalResource = new ResName("android", "string", "badPin");
    Integer resId = resourceProvider.getResourceId(internalResource);
    assertThat(resId).isNotNull();
    assertThat(resourceProvider.getResName(resId)).isEqualTo(internalResource);

    Class<?> internalRIdClass = Robolectric.class.getClassLoader().loadClass("com.android.internal.R$" + internalResource.type);
    int internalResourceId;
    internalResourceId = (Integer) internalRIdClass.getDeclaredField(internalResource.name).get(null);
    assertThat(resId).isEqualTo(internalResourceId);

    assertThat(RuntimeEnvironment.application.getResources().getString(resId)).isEqualTo("The old PIN you typed isn't correct.");
  }
}

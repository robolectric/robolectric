package org.robolectric.shadows;

import android.content.IntentFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class IntentFilterTest {
  @Test
  public void addDataScheme_shouldAddTheDataScheme() throws Exception {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addDataScheme("http");
    intentFilter.addDataScheme("ftp");

    assertThat(intentFilter.getDataScheme(0)).isEqualTo("http");
    assertThat(intentFilter.getDataScheme(1)).isEqualTo("ftp");
  }

  @Test
  public void addDataAuthority_shouldAddTheDataAuthority() throws Exception {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addDataAuthority("test.com", "8080");
    intentFilter.addDataAuthority("example.com", "42");

    assertThat(intentFilter.getDataAuthority(0).getHost()).isEqualTo("test.com");
    assertThat(intentFilter.getDataAuthority(0).getPort()).isEqualTo(8080);
    assertThat(intentFilter.getDataAuthority(1).getHost()).isEqualTo("example.com");
    assertThat(intentFilter.getDataAuthority(1).getPort()).isEqualTo(42);
  }

  @Test
  public void hasAction() {
    IntentFilter intentFilter = new IntentFilter();
    assertThat(intentFilter.hasAction("test")).isFalse();
    intentFilter.addAction("test");

    assertThat(intentFilter.hasAction("test")).isTrue();
  }
  
  @Test
  public void hasDataScheme() {
    IntentFilter intentFilter = new IntentFilter();
    assertThat(intentFilter.hasDataScheme("test")).isFalse();
    intentFilter.addDataScheme("test");
  
    assertThat(intentFilter.hasDataScheme("test")).isTrue();
  }
}

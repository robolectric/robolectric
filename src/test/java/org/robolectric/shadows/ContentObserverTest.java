package org.robolectric.shadows;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ContentObserverTest {

  private TestContentObserver observer;

  @Before
  public void setUp() throws Exception {
    observer = new TestContentObserver(null);
  }

  @Test
  public void testDispatchChangeBooleanUri() {
    assertThat(observer.changed).isFalse();
    assertThat(observer.selfChange).isFalse();
    assertThat(observer.uri).isNull();

    Uri uri = Uri.parse("http://www.somewhere.com");
    observer.dispatchChange(true, uri);

    assertThat(observer.changed).isTrue();
    assertThat(observer.selfChange).isTrue();
    assertThat(observer.uri).isSameAs(uri);
  }

  @Test
  public void testDispatchChangeBoolean() {
    assertThat(observer.changed).isFalse();
    assertThat(observer.selfChange).isFalse();

    Uri uri = Uri.parse("http://www.somewhere.com");
    observer.dispatchChange(true);

    assertThat(observer.changed).isTrue();
    assertThat(observer.selfChange).isTrue();
  }

  private class TestContentObserver extends ContentObserver {

    public TestContentObserver(Handler handler) {
      super(handler);
    }

    public boolean changed = false;
    public boolean selfChange = false;
    public Uri uri = null;

    @Override
    public void onChange(boolean selfChange) {
      changed = true;
      this.selfChange = selfChange;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
      changed = true;
      this.selfChange = selfChange;
      this.uri = uri;
    }
  }

}

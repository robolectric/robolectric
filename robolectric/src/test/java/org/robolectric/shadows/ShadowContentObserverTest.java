package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowContentObserverTest {

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
    assertThat(observer.uri).isSameInstanceAs(uri);
  }

  @Test
  public void testDispatchChangeBoolean() {
    assertThat(observer.changed).isFalse();
    assertThat(observer.selfChange).isFalse();

    observer.dispatchChange(true);

    assertThat(observer.changed).isTrue();
    assertThat(observer.selfChange).isTrue();
  }

  private static class TestContentObserver extends ContentObserver {

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

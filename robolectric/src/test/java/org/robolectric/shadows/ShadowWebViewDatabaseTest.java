package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.webkit.WebViewDatabase;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ShadowWebViewDatabase} */
@RunWith(AndroidJUnit4.class)
public final class ShadowWebViewDatabaseTest {
  private WebViewDatabase subject;

  @Before
  public void setUp() {
    subject = WebViewDatabase.getInstance(ApplicationProvider.getApplicationContext());
  }

  @After
  public void tearDown() {
    shadowOf(subject).resetDatabase();
  }

  @Test
  public void getInstance_returnsSameInstance() {
    assertThat(subject)
        .isSameInstanceAs(WebViewDatabase.getInstance(ApplicationProvider.getApplicationContext()));
  }

  @Test
  public void wasClearFormDataCalled_falseIfClearFormDataIsNotInvoked() {
    assertThat(shadowOf(subject).wasClearFormDataCalled()).isFalse();
  }

  @Test
  public void wasClearFormDataCalled_trueAfterClearFormFataInvocation() {
    subject.clearFormData();

    assertThat(shadowOf(subject).wasClearFormDataCalled()).isTrue();
  }

  @Test
  public void resetClearFormData_resetsWasClearFormDataCalledState() {
    subject.clearFormData();

    shadowOf(subject).resetClearFormData();

    assertThat(shadowOf(subject).wasClearFormDataCalled()).isFalse();
  }
}

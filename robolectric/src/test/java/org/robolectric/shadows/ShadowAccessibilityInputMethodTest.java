package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.InputMethod;
import android.view.inputmethod.EditorInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.testing.TestAccessibilityService;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = TIRAMISU)
public final class ShadowAccessibilityInputMethodTest {
  private InputMethod inputMethod;
  private ShadowAccessibilityService shadowService;

  @Before
  public void setUp() {
    TestAccessibilityService service = Robolectric.setupService(TestAccessibilityService.class);
    shadowService = shadowOf(service);
    AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
    serviceInfo.flags |= AccessibilityServiceInfo.FLAG_INPUT_METHOD_EDITOR;
    service.setServiceInfo(serviceInfo);
    inputMethod = service.getInputMethod();
  }

  @Test
  public void getCurrentInputConnection_returnsNullIfNotConnected() {
    assertThat(inputMethod.getCurrentInputConnection()).isNull();
  }

  @Test
  public void getCurrentInputConnection_returnsConnectionIfConnected() {
    shadowService.startInput(new EditorInfo());
    InputMethod.AccessibilityInputConnection connection = inputMethod.getCurrentInputConnection();
    assertThat(connection).isNotNull();
  }

  @Test
  public void getCurrentEditorInfo_returnsNullIfNotSet() {
    shadowService.startInput(null);
    assertThat(inputMethod.getCurrentInputEditorInfo()).isNull();
  }

  @Test
  public void getCurrentEditorInfo_returnsNullIfNotConnected() {
    assertThat(inputMethod.getCurrentInputEditorInfo()).isNull();
  }

  @Test
  public void getCurrentEditorInfo_returnsEditorInfoIfSetAndConnected() {
    EditorInfo editorInfo = new EditorInfo();
    editorInfo.hintText = "Watermelon";
    shadowService.startInput(editorInfo);
    assertThat(inputMethod.getCurrentInputEditorInfo()).isEqualTo(editorInfo);
  }
}

package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.accessibilityservice.InputMethod;
import android.view.inputmethod.EditorInfo;
import com.android.internal.inputmethod.CancellationGroup;
import com.android.internal.inputmethod.IRemoteAccessibilityInputConnection;
import com.android.internal.inputmethod.RemoteAccessibilityInputConnection;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.ForType;

/**
 * Shadow of Accessibility's InputMethod that provides a mechanism to get an accessibility input
 * connection and editor info.
 */
@Implements(value = InputMethod.class, minSdk = TIRAMISU, isInAndroidSdk = false)
public class ShadowAccessibilityInputMethod {

  @RealObject private InputMethod realInputMethod;

  void startInput(EditorInfo editorInfo) {
    RemoteAccessibilityInputConnection cxn =
        new RemoteAccessibilityInputConnection(
            ReflectionHelpers.createNullProxy(IRemoteAccessibilityInputConnection.class),
            new CancellationGroup());
    reflector(InputMethodReflector.class, realInputMethod).startInput(cxn, editorInfo);
  }

  @ForType(InputMethod.class)
  interface InputMethodReflector {
    void startInput(RemoteAccessibilityInputConnection ic, EditorInfo attribute);
  }
}

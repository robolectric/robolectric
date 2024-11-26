package org.robolectric.shadows;

import android.annotation.SystemApi;
import android.content.Context;
import android.os.Build;
import android.service.textclassifier.TextClassifierService;
import android.view.textclassifier.TextClassifier;
import java.util.concurrent.atomic.AtomicReference;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link TextClassifierService} */
@SystemApi
@Implements(
    value = TextClassifierService.class,
    minSdk = Build.VERSION_CODES.Q,
    isInAndroidSdk = false)
public class ShadowTextClassifierService {
  private static final AtomicReference<TextClassifier> defaultTextClassifier =
      new AtomicReference<>(TextClassifier.NO_OP);

  @Resetter
  public static void reset() {
    defaultTextClassifier.set(TextClassifier.NO_OP);
  }

  @Implementation
  protected static TextClassifier getDefaultTextClassifierImplementation(Context context) {
    return defaultTextClassifier.get();
  }

  /** Sets the default text classifier implementation for the test. */
  public static void setDefaultTextClassifierImplementation(TextClassifier textClassifier) {
    defaultTextClassifier.set(textClassifier);
  }

  private ShadowTextClassifierService() {}
}

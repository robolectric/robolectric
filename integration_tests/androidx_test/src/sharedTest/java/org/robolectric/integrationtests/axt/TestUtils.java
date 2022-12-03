package org.robolectric.integrationtests.axt;

import android.content.Context;
import android.content.Intent;
import androidx.test.platform.app.InstrumentationRegistry;

public class TestUtils {
  public static void dismissSystemDialog() {
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    context.sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
  }
}

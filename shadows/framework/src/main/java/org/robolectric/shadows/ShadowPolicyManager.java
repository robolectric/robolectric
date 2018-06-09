package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;

import android.content.Context;
import android.view.LayoutInflater;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(
  className = "com.android.internal.policy.PolicyManager",
  isInAndroidSdk = false,
  maxSdk = LOLLIPOP_MR1
)
public class ShadowPolicyManager {

  @Implementation
  protected static LayoutInflater makeNewLayoutInflater(Context context) {
    Class<LayoutInflater> phoneLayoutInflaterClass =
        (Class<LayoutInflater>)
            ReflectionHelpers.loadClass(
                ShadowPolicyManager.class.getClassLoader(),
                "com.android.internal.policy.impl.PhoneLayoutInflater");
    return ReflectionHelpers.callConstructor(
        phoneLayoutInflaterClass, ClassParameter.from(Context.class, context));
  }
}

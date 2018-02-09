package org.robolectric.shadows;

import android.content.Context;
import android.view.LayoutInflater;

import com.android.internal.policy.PhoneLayoutInflater;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;

@Implements(className = "com.android.internal.policy.PolicyManager", isInAndroidSdk = false, maxSdk = LOLLIPOP_MR1)
public class ShadowPolicyManager {

  @Implementation
  protected static LayoutInflater makeNewLayoutInflater(Context context) {
    return new PhoneLayoutInflater(context);
  }
}

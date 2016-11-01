package org.robolectric.shadows;

import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.Collections;
import java.util.List;

import static android.os.Build.VERSION_CODES;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

@Implements(value = UserManager.class, minSdk = JELLY_BEAN_MR1)
public class ShadowUserManager {

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public Bundle getApplicationRestrictions(String packageName) {
    return null;
  }

  @Implementation(minSdk = LOLLIPOP)
  public List<UserHandle> getUserProfiles(){
    return Collections.emptyList();
  }

}
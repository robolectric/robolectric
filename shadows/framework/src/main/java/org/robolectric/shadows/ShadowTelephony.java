package org.robolectric.shadows;

import android.annotation.Nullable;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.provider.Telephony;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Intents;
import android.telephony.SmsMessage;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(value = Telephony.class, minSdk = VERSION_CODES.KITKAT)
public class ShadowTelephony {
  @Implements(value = Sms.class, minSdk = VERSION_CODES.KITKAT)
  public static class ShadowSms {
    @Nullable private static String defaultSmsPackage;

    @Implementation
    protected static String getDefaultSmsPackage(Context context) {
      return defaultSmsPackage;
    }

    /**
     * Override the package name returned from calling {@link Sms#getDefaultSmsPackage(Context)}.
     *
     * <p>This will be reset for the next test.
     */
    public static void setDefaultSmsPackage(String defaultSmsPackage) {
      ShadowSms.defaultSmsPackage = defaultSmsPackage;
    }

    @Resetter
    public static synchronized void reset() {
      defaultSmsPackage = null;
    }

    /** Shadow implementation for {@link Telephony.Sms.Intents}. */
    @Implements(value = Intents.class, minSdk = VERSION_CODES.KITKAT)
    public static class ShadowIntents {
      @Nullable private static SmsMessage[] smsMessage;

      @Implementation
      protected static SmsMessage[] getMessagesFromIntent(Intent intent) {
        return ShadowIntents.smsMessage;
      }

      /**
       * Override the messages returned from calling {@link Intents#getMessagesFromIntent(Intent)}.
       */
      public static void setMessageFromIntent(SmsMessage[] smsMessage) {
        ShadowIntents.smsMessage = smsMessage;
      }
    }
  }
}

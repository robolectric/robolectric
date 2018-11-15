package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.ComponentName;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcel;
import android.telecom.PhoneAccountHandle;
import android.telephony.VisualVoicemailSms;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Tests for {@link ShadowVisualVoicemailSms} */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.O)
public class ShadowVisualVoicemailSmsTest {

  private final Context appContext = ApplicationProvider.getApplicationContext();
  private final PhoneAccountHandle phoneAccountHandle =
      new PhoneAccountHandle(new ComponentName(appContext, Object.class), "foo");

  private VisualVoicemailSms sms;
  private ShadowVisualVoicemailSms shadowSms;

  @Before
  public void setup() {
    sms = Shadow.newInstanceOf(VisualVoicemailSms.class);
    shadowSms = Shadow.extract(sms);
  }

  @Test
  public void setPhoneAccountHandle_setsPhoneAccountHandle() {
    shadowSms.setPhoneAccountHandle(phoneAccountHandle);

    assertThat(sms.getPhoneAccountHandle()).isEqualTo(phoneAccountHandle);
  }

  @Test
  public void setPrefix_setsPrefix() {
    shadowSms.setPrefix("prefix");

    assertThat(sms.getPrefix()).isEqualTo("prefix");
  }

  @Test
  public void setFields_setsFields() {
    Bundle bundle = new Bundle();
    bundle.putString("key", "value");
    shadowSms.setFields(bundle);

    assertThat(sms.getFields()).isEqualTo(bundle);
  }

  @Test
  public void setMessageBody_setsMessageBody() {
    shadowSms.setMessageBody("messageBody");

    assertThat(sms.getMessageBody()).isEqualTo("messageBody");
  }

  @Test
  public void parcelable_unparcelable() {
    Bundle bundle = new Bundle();
    bundle.putString("key", "value");
    shadowSms
        .setPhoneAccountHandle(phoneAccountHandle)
        .setPrefix("prefix")
        .setFields(bundle)
        .setMessageBody("messageBody");

    Parcel parcel = Parcel.obtain();
    sms.writeToParcel(parcel, 0);
    parcel.setDataPosition(0);
    VisualVoicemailSms newSms = VisualVoicemailSms.CREATOR.createFromParcel(parcel);

    assertThat(newSms.getPhoneAccountHandle()).isEqualTo(phoneAccountHandle);
    assertThat(newSms.getPrefix()).isEqualTo("prefix");
    assertThat(newSms.getFields().getString("key")).isEqualTo("value");
    assertThat(newSms.getMessageBody()).isEqualTo("messageBody");
  }
}

package org.robolectric.shadows;

import android.annotation.Nullable;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.telecom.PhoneAccountHandle;
import android.telephony.VisualVoicemailSms;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = VisualVoicemailSms.class, minSdk = VERSION_CODES.O)
public class ShadowVisualVoicemailSms {
  private PhoneAccountHandle phoneAccountHandle;

  @Nullable private String prefix;

  @Nullable private Bundle fields;

  private String messageBody;

  @Implementation
  protected static void __staticInitializer__() {
    ReflectionHelpers.setStaticField(
        VisualVoicemailSms.class, "CREATOR", ShadowVisualVoicemailSms.CREATOR);
  }

  @Implementation
  protected PhoneAccountHandle getPhoneAccountHandle() {
    return phoneAccountHandle;
  }

  public ShadowVisualVoicemailSms setPhoneAccountHandle(PhoneAccountHandle phoneAccountHandle) {
    this.phoneAccountHandle = phoneAccountHandle;
    return this;
  }

  @Implementation
  protected String getPrefix() {
    return prefix;
  }

  public ShadowVisualVoicemailSms setPrefix(String prefix) {
    this.prefix = prefix;
    return this;
  }

  @Implementation
  protected Bundle getFields() {
    return fields;
  }

  public ShadowVisualVoicemailSms setFields(Bundle fields) {
    this.fields = fields;
    return this;
  }

  @Implementation
  protected String getMessageBody() {
    return messageBody;
  }

  public ShadowVisualVoicemailSms setMessageBody(String messageBody) {
    this.messageBody = messageBody;
    return this;
  }

  public static final Creator<VisualVoicemailSms> CREATOR =
      new Creator<VisualVoicemailSms>() {
        @Override
        public VisualVoicemailSms createFromParcel(Parcel in) {
          VisualVoicemailSms sms = Shadow.newInstanceOf(VisualVoicemailSms.class);
          ShadowVisualVoicemailSms shadowSms = Shadow.extract(sms);
          shadowSms
              .setPhoneAccountHandle(in.readParcelable(PhoneAccountHandle.class.getClassLoader()))
              .setPrefix(in.readString())
              .setFields(in.readBundle())
              .setMessageBody(in.readString());
          return sms;
        }

        @Override
        public VisualVoicemailSms[] newArray(int size) {
          return new VisualVoicemailSms[size];
        }
      };

  @Implementation
  protected int describeContents() {
    return 0;
  }

  @Implementation
  protected void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(getPhoneAccountHandle(), flags);
    dest.writeString(getPrefix());
    dest.writeBundle(getFields());
    dest.writeString(getMessageBody());
  }
}

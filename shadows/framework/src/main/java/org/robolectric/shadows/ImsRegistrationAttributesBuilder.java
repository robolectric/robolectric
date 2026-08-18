package org.robolectric.shadows;

import android.annotation.RequiresApi;
import android.os.Build;
import android.telephony.ims.ImsRegistrationAttributes;
import android.telephony.ims.SipDetails;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Set;
import javax.annotation.Nullable;

/** Builder for {@link android.telephony.ims.ImsRegistrationAttributes} */
@RequiresApi(Build.VERSION_CODES.S)
public class ImsRegistrationAttributesBuilder {

  private ImsRegistrationAttributes.Builder builder;

  private ImsRegistrationAttributesBuilder(int registrationTech) {
    this.builder = new ImsRegistrationAttributes.Builder(registrationTech);
  }

  public static ImsRegistrationAttributesBuilder newBuilder(int registrationTech) {
    return new ImsRegistrationAttributesBuilder(registrationTech);
  }

  @CanIgnoreReturnValue
  public ImsRegistrationAttributesBuilder setFeatureTags(Set<String> featureTags) {
    this.builder = this.builder.setFeatureTags(featureTags);
    return this;
  }

  @CanIgnoreReturnValue
  @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public ImsRegistrationAttributesBuilder setSipDetails(@Nullable SipDetails sipDetails) {
    this.builder = this.builder.setSipDetails(sipDetails);
    return this;
  }

  @CanIgnoreReturnValue
  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  public ImsRegistrationAttributesBuilder setFlagRegistrationTypeEmergency() {
    this.builder = this.builder.setFlagRegistrationTypeEmergency();
    return this;
  }

  @CanIgnoreReturnValue
  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  public ImsRegistrationAttributesBuilder setFlagVirtualRegistrationForEmergencyCall() {
    this.builder = this.builder.setFlagVirtualRegistrationForEmergencyCall();
    return this;
  }

  public ImsRegistrationAttributes build() {
    return this.builder.build();
  }
}

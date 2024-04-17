package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;

import android.annotation.RequiresApi;
import android.telephony.emergency.EmergencyNumber;
import android.telephony.emergency.EmergencyNumber.EmergencyCallRouting;
import android.telephony.emergency.EmergencyNumber.EmergencyNumberSources;
import android.telephony.emergency.EmergencyNumber.EmergencyServiceCategories;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Builder for {@link android.telephony.emergency.EmergencyNumber}. */
@RequiresApi(Q)
public class EmergencyNumberBuilder {

  private final String number;
  private final String countryIso;
  private final String mnc;
  private final List<String> emergencyUrns = new ArrayList<String>();
  private int emergencyServiceCategories = EmergencyNumber.EMERGENCY_SERVICE_CATEGORY_UNSPECIFIED;
  private int emergencyNumberSources = EmergencyNumber.EMERGENCY_NUMBER_SOURCE_DEFAULT;
  private int emergencyCallRouting = EmergencyNumber.EMERGENCY_CALL_ROUTING_UNKNOWN;

  private EmergencyNumberBuilder(String number, String countryIso, String mnc) {
    this.number = number;
    this.countryIso = countryIso;
    this.mnc = mnc;
  }

  public static EmergencyNumberBuilder newBuilder(String number, String countryIso, String mnc) {
    return new EmergencyNumberBuilder(number, countryIso, mnc);
  }

  public EmergencyNumberBuilder setEmergencyServiceCategories(
      @EmergencyServiceCategories int emergencyServiceCategories) {
    this.emergencyServiceCategories = emergencyServiceCategories;
    return this;
  }

  public EmergencyNumberBuilder addEmergencyUrn(String emergencyUrn) {
    emergencyUrns.add(emergencyUrn);
    return this;
  }

  public EmergencyNumberBuilder setEmergencyNumberSources(
      @EmergencyNumberSources int emergencyNumberSources) {
    this.emergencyNumberSources = emergencyNumberSources;
    return this;
  }

  public EmergencyNumberBuilder setEmergencyCallRouting(
      @EmergencyCallRouting int emergencyCallRouting) {
    this.emergencyCallRouting = emergencyCallRouting;
    return this;
  }

  public EmergencyNumber build() {
    return ReflectionHelpers.callConstructor(
        EmergencyNumber.class,
        ClassParameter.from(String.class, number),
        ClassParameter.from(String.class, countryIso),
        ClassParameter.from(String.class, mnc),
        ClassParameter.from(int.class, emergencyServiceCategories),
        ClassParameter.from(List.class, emergencyUrns),
        ClassParameter.from(int.class, emergencyNumberSources),
        ClassParameter.from(int.class, emergencyCallRouting));
  }
}

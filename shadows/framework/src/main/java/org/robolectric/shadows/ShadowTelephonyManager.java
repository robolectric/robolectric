package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.telephony.PhoneStateListener.LISTEN_CALL_STATE;
import static android.telephony.PhoneStateListener.LISTEN_CELL_INFO;
import static android.telephony.PhoneStateListener.LISTEN_CELL_LOCATION;
import static android.telephony.PhoneStateListener.LISTEN_NONE;
import static android.telephony.TelephonyManager.CALL_STATE_IDLE;
import static android.telephony.TelephonyManager.CALL_STATE_RINGING;

import android.os.Build.VERSION;
import android.os.PersistableBundle;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(TelephonyManager.class)
public class ShadowTelephonyManager {

  private final Map<PhoneStateListener, Integer> phoneStateRegistrations = new HashMap<>();
  private PhoneStateListener lastListener;
  private int lastEventFlags;

  private String deviceId;
  private String imei;
  private String meid;
  private String groupIdLevel1;
  private String networkOperatorName = "";
  private String networkCountryIso;
  private String networkOperator = "";
  private String simOperator;
  private String simOperatorName;
  private boolean readPhoneStatePermission = true;
  private int phoneType = TelephonyManager.PHONE_TYPE_GSM;
  private String simCountryIso = "";
  private int simState = TelephonyManager.SIM_STATE_READY;
  private String line1Number;
  private int networkType;
  private List<CellInfo> allCellInfo = Collections.emptyList();
  private CellLocation cellLocation = null;
  private int callState = CALL_STATE_IDLE;
  private String incomingPhoneNumber = null;
  private boolean isSmsCapable = true;

  @Implementation
  protected void listen(PhoneStateListener listener, int flags) {
    lastListener = listener;
    lastEventFlags = flags;

    if (flags == LISTEN_NONE) {
      phoneStateRegistrations.remove(listener);
    } else {
      initListener(listener, flags);
      phoneStateRegistrations.put(listener, flags);
    }
  }

  /**
   * Returns the most recent listener passed to #listen().
   *
   * @return Phone state listener.
   * @deprecated Avoid using.
   */
  @Deprecated
  public PhoneStateListener getListener() {
    return lastListener;
  }

  /**
   * Returns the most recent flags passed to #listen().
   *
   * @return Event flags.
   * @deprecated Avoid using.
   */
  @Deprecated
  public int getEventFlags() {
    return lastEventFlags;
  }

  /** Call state may be specified via {@link #setCallState(int)}. */
  @Implementation
  protected int getCallState() {
    return callState;
  }

  /** Sets the current call state to the desired state and updates any listeners. */
  public void setCallState(int callState) {
    setCallState(callState, null);
  }

  /**
   * Sets the current call state with the option to specify an incoming phone number for the
   * CALL_STATE_RINGING state. The incoming phone number will be ignored for all other cases.
   */
  public void setCallState(int callState, String incomingPhoneNumber) {
    if (callState != CALL_STATE_RINGING) {
      incomingPhoneNumber = null;
    }

    this.callState = callState;
    this.incomingPhoneNumber = incomingPhoneNumber;

    for (PhoneStateListener listener : getListenersForFlags(LISTEN_CALL_STATE)) {
      listener.onCallStateChanged(callState, incomingPhoneNumber);
    }
  }

  @Implementation
  protected String getDeviceId() {
    checkReadPhoneStatePermission();
    return deviceId;
  }

  public void setDeviceId(String newDeviceId) {
    deviceId = newDeviceId;
  }

  public void setNetworkOperatorName(String networkOperatorName) {
    this.networkOperatorName = networkOperatorName;
  }

  @Implementation
  protected String getImei() {
    checkReadPhoneStatePermission();
    return imei;
  }

  /** Set the IMEI returned by getImei(). */
  public void setImei(String imei) {
    this.imei = imei;
  }

  @Implementation
  protected String getMeid() {
    checkReadPhoneStatePermission();
    return meid;
  }

  /** Set the MEID returned by getMeid(). */
  public void setMeid(String meid) {
    this.meid = meid;
  }

  @Implementation
  protected String getNetworkOperatorName() {
    return networkOperatorName;
  }

  public void setNetworkCountryIso(String networkCountryIso) {
    this.networkCountryIso = networkCountryIso;
  }

  @Implementation
  protected String getNetworkCountryIso() {
    return networkCountryIso;
  }

  public void setNetworkOperator(String networkOperator) {
    this.networkOperator = networkOperator;
  }

  @Implementation
  protected String getNetworkOperator() {
    return networkOperator;
  }

  @Implementation
  protected String getSimOperator() {
    return simOperator;
  }

  public void setSimOperator(String simOperator) {
    this.simOperator = simOperator;
  }

  @Implementation
  protected String getSimOperatorName() {
    return simOperatorName;
  }

  public void setSimOperatorName(String simOperatorName) {
    this.simOperatorName = simOperatorName;
  }

  @Implementation
  protected String getSimCountryIso() {
    return simCountryIso;
  }

  public void setSimCountryIso(String simCountryIso) {
    this.simCountryIso = simCountryIso;
  }

  @Implementation
  protected int getSimState() {
    return simState;
  }

  public void setSimState(int simState) {
    this.simState = simState;
  }

  public void setReadPhoneStatePermission(boolean readPhoneStatePermission) {
    this.readPhoneStatePermission = readPhoneStatePermission;
  }

  private void checkReadPhoneStatePermission() {
    if (!readPhoneStatePermission) {
      throw new SecurityException();
    }
  }

  @Implementation
  protected int getPhoneType() {
    return phoneType;
  }

  public void setPhoneType(int phoneType) {
    this.phoneType = phoneType;
  }

  @Implementation
  protected String getLine1Number() {
    return line1Number;
  }

  public void setLine1Number(String line1Number) {
    this.line1Number = line1Number;
  }

  @Implementation
  protected int getNetworkType() {
    return networkType;
  }

  public void setNetworkType(int networkType) {
    this.networkType = networkType;
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected List<CellInfo> getAllCellInfo() {
    return allCellInfo;
  }

  public void setAllCellInfo(List<CellInfo> allCellInfo) {
    this.allCellInfo = allCellInfo;

    if (VERSION.SDK_INT >= JELLY_BEAN_MR1) {
      for (PhoneStateListener listener : getListenersForFlags(LISTEN_CELL_INFO)) {
        listener.onCellInfoChanged(allCellInfo);
      }
    }
  }

  @Implementation
  protected CellLocation getCellLocation() {
    return this.cellLocation;
  }

  public void setCellLocation(CellLocation cellLocation) {
    this.cellLocation = cellLocation;

    for (PhoneStateListener listener : getListenersForFlags(LISTEN_CELL_LOCATION)) {
      listener.onCellLocationChanged(cellLocation);
    }
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected String getGroupIdLevel1() {
    return this.groupIdLevel1;
  }

  public void setGroupIdLevel1(String groupIdLevel1) {
    this.groupIdLevel1 = groupIdLevel1;
  }

  private void initListener(PhoneStateListener listener, int flags) {
    if ((flags & LISTEN_CALL_STATE) != 0) {
      listener.onCallStateChanged(callState, incomingPhoneNumber);
    }
    if ((flags & LISTEN_CELL_INFO) != 0) {
      if (VERSION.SDK_INT >= JELLY_BEAN_MR1) {
        listener.onCellInfoChanged(allCellInfo);
      }
    }
    if ((flags & LISTEN_CELL_LOCATION) != 0) {
      listener.onCellLocationChanged(cellLocation);
    }
  }

  private Iterable<PhoneStateListener> getListenersForFlags(int flags) {
    return Iterables.filter(
        phoneStateRegistrations.keySet(),
        new Predicate<PhoneStateListener>() {
          @Override
          public boolean apply(PhoneStateListener input) {
            // only select PhoneStateListeners with matching flags
            return (phoneStateRegistrations.get(input) & flags) != 0;
          }
        });
  }

  /**
   * @return `true` by default, or the value specified via {@link #setIsSmsCapable(boolean)}
   */
  @Implementation
  protected boolean isSmsCapable() {
    return isSmsCapable;
  }

  /** Sets the value returned by {@link TelephonyManager#isSmsCapable()}. */
  public void setIsSmsCapable(boolean isSmsCapable) {
    this.isSmsCapable = isSmsCapable;
  }

  @Implementation
  protected PersistableBundle getCarrierConfig() {
    // Avoid NPE - no testing APIS yet.
    return new PersistableBundle();
  }
}

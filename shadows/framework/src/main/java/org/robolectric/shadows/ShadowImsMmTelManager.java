package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.Manifest;
import android.annotation.CallbackExecutor;
import android.annotation.RequiresPermission;
import android.annotation.SystemApi;
import android.os.Build.VERSION_CODES;
import android.telephony.SubscriptionManager;
import android.telephony.ims.ImsException;
import android.telephony.ims.ImsMmTelManager;
import android.telephony.ims.ImsMmTelManager.CapabilityCallback;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsRegistrationAttributes;
import android.telephony.ims.RegistrationManager;
import android.telephony.ims.feature.MmTelFeature.MmTelCapabilities;
import android.telephony.ims.stub.ImsRegistrationImplBase;
import android.util.ArrayMap;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import java.util.Map;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/**
 * Supports IMS by default. IMS unregistered by default.
 *
 * @see #setImsAvailableOnDevice(boolean)
 * @see #setImsRegistered(int)
 */
@Implements(
    value = ImsMmTelManager.class,
    minSdk = VERSION_CODES.Q,
    looseSignatures = true,
    isInAndroidSdk = false)
@SystemApi
public class ShadowImsMmTelManager {

  protected static final Map<Integer, ImsMmTelManager> existingInstances = new ArrayMap<>();

  private final Map<ImsMmTelManager.RegistrationCallback, Executor>
      registrationCallbackExecutorMap = new ArrayMap<>();
  private final Map<RegistrationManager.RegistrationCallback, Executor>
      registrationManagerCallbackExecutorMap = new ArrayMap<>();
  private final Map<CapabilityCallback, Executor> capabilityCallbackExecutorMap = new ArrayMap<>();
  private boolean imsAvailableOnDevice = true;
  private MmTelCapabilities mmTelCapabilitiesAvailable =
      new MmTelCapabilities(); // start with empty
  private int imsRegistrationTech = ImsRegistrationImplBase.REGISTRATION_TECH_NONE;
  private int subId;

  @Implementation(maxSdk = VERSION_CODES.R)
  protected void __constructor__(int subId) {
    this.subId = subId;
  }

  /**
   * Sets whether IMS is available on the device. Setting this to false will cause {@link
   * ImsException} to be thrown whenever methods requiring IMS support are invoked including {@link
   * #registerImsRegistrationCallback(Executor, RegistrationCallback)} and {@link
   * #registerMmTelCapabilityCallback(Executor, CapabilityCallback)}.
   */
  public void setImsAvailableOnDevice(boolean imsAvailableOnDevice) {
    this.imsAvailableOnDevice = imsAvailableOnDevice;
  }

  @RequiresPermission(Manifest.permission.READ_PRIVILEGED_PHONE_STATE)
  @Implementation
  protected void registerImsRegistrationCallback(
      @NonNull @CallbackExecutor Executor executor, @NonNull ImsMmTelManager.RegistrationCallback c)
      throws ImsException {
    if (!imsAvailableOnDevice) {
      throw new ImsException(
          "IMS not available on device.", ImsException.CODE_ERROR_UNSUPPORTED_OPERATION);
    }
    registrationCallbackExecutorMap.put(c, executor);
  }

  @RequiresPermission(
      anyOf = {
        android.Manifest.permission.READ_PRIVILEGED_PHONE_STATE,
        android.Manifest.permission.READ_PRECISE_PHONE_STATE
      })
  @Implementation(minSdk = VERSION_CODES.R)
  protected void registerImsRegistrationCallback(
      @NonNull @CallbackExecutor Executor executor,
      @NonNull RegistrationManager.RegistrationCallback c)
      throws ImsException {
    if (!imsAvailableOnDevice) {
      throw new ImsException(
          "IMS not available on device.", ImsException.CODE_ERROR_UNSUPPORTED_OPERATION);
    }
    registrationManagerCallbackExecutorMap.put(c, executor);
  }

  @RequiresPermission(Manifest.permission.READ_PRIVILEGED_PHONE_STATE)
  @Implementation
  protected void unregisterImsRegistrationCallback(
      @NonNull ImsMmTelManager.RegistrationCallback c) {
    registrationCallbackExecutorMap.remove(c);
  }

  @RequiresPermission(
      anyOf = {
        android.Manifest.permission.READ_PRIVILEGED_PHONE_STATE,
        android.Manifest.permission.READ_PRECISE_PHONE_STATE
      })
  @Implementation(minSdk = VERSION_CODES.R)
  protected void unregisterImsRegistrationCallback(
      @NonNull RegistrationManager.RegistrationCallback c) {
    registrationManagerCallbackExecutorMap.remove(c);
  }

  /**
   * Triggers {@link RegistrationCallback#onRegistering(int)} for all registered {@link
   * RegistrationCallback} callbacks.
   *
   * @see #registerImsRegistrationCallback(Executor, RegistrationCallback)
   */
  public void setImsRegistering(int imsRegistrationTech) {
    for (Map.Entry<ImsMmTelManager.RegistrationCallback, Executor> entry :
        registrationCallbackExecutorMap.entrySet()) {
      entry.getValue().execute(() -> entry.getKey().onRegistering(imsRegistrationTech));
    }

    for (Map.Entry<RegistrationManager.RegistrationCallback, Executor> entry :
        registrationManagerCallbackExecutorMap.entrySet()) {
      entry.getValue().execute(() -> entry.getKey().onRegistering(imsRegistrationTech));
    }
  }

  @RequiresApi(api = VERSION_CODES.S)
  public void setImsRegistering(@NonNull ImsRegistrationAttributes attrs) {
    for (Map.Entry<RegistrationManager.RegistrationCallback, Executor> entry :
        registrationManagerCallbackExecutorMap.entrySet()) {
      entry.getValue().execute(() -> entry.getKey().onRegistering(attrs));
    }
  }

  /**
   * Triggers {@link RegistrationCallback#onRegistered(int)} for all registered {@link
   * RegistrationCallback} callbacks.
   *
   * @see #registerImsRegistrationCallback(Executor, RegistrationCallback)
   */
  public void setImsRegistered(int imsRegistrationTech) {
    this.imsRegistrationTech = imsRegistrationTech;
    for (Map.Entry<ImsMmTelManager.RegistrationCallback, Executor> entry :
        registrationCallbackExecutorMap.entrySet()) {
      entry.getValue().execute(() -> entry.getKey().onRegistered(imsRegistrationTech));
    }

    for (Map.Entry<RegistrationManager.RegistrationCallback, Executor> entry :
        registrationManagerCallbackExecutorMap.entrySet()) {
      entry.getValue().execute(() -> entry.getKey().onRegistered(imsRegistrationTech));
    }
  }

  @RequiresApi(api = VERSION_CODES.S)
  public void setImsRegistered(@NonNull ImsRegistrationAttributes attrs) {
    for (Map.Entry<RegistrationManager.RegistrationCallback, Executor> entry :
        registrationManagerCallbackExecutorMap.entrySet()) {
      entry.getValue().execute(() -> entry.getKey().onRegistered(attrs));
    }
  }

  /**
   * Triggers {@link RegistrationCallback#onUnregistered(ImsReasonInfo)} for all registered {@link
   * RegistrationCallback} callbacks.
   *
   * @see #registerImsRegistrationCallback(Executor, RegistrationCallback)
   */
  public void setImsUnregistered(@NonNull ImsReasonInfo imsReasonInfo) {
    this.imsRegistrationTech = ImsRegistrationImplBase.REGISTRATION_TECH_NONE;
    for (Map.Entry<ImsMmTelManager.RegistrationCallback, Executor> entry :
        registrationCallbackExecutorMap.entrySet()) {
      entry.getValue().execute(() -> entry.getKey().onUnregistered(imsReasonInfo));
    }

    for (Map.Entry<RegistrationManager.RegistrationCallback, Executor> entry :
        registrationManagerCallbackExecutorMap.entrySet()) {
      entry.getValue().execute(() -> entry.getKey().onUnregistered(imsReasonInfo));
    }
  }

  /**
   * Triggers {@link RegistrationCallback#onTechnologyChangeFailed(int, ImsReasonInfo)} for all
   * registered {@link RegistrationCallback} callbacks.
   *
   * @see #registerImsRegistrationCallback(Executor, RegistrationCallback)
   */
  public void setOnTechnologyChangeFailed(int imsRadioTech, @NonNull ImsReasonInfo imsReasonInfo) {
    for (Map.Entry<RegistrationManager.RegistrationCallback, Executor> entry :
        registrationManagerCallbackExecutorMap.entrySet()) {
      entry
          .getValue()
          .execute(() -> entry.getKey().onTechnologyChangeFailed(imsRadioTech, imsReasonInfo));
    }
  }

  @RequiresPermission(Manifest.permission.READ_PRIVILEGED_PHONE_STATE)
  @Implementation
  protected void registerMmTelCapabilityCallback(
      @NonNull @CallbackExecutor Executor executor, @NonNull CapabilityCallback c)
      throws ImsException {
    if (!imsAvailableOnDevice) {
      throw new ImsException(
          "IMS not available on device.", ImsException.CODE_ERROR_UNSUPPORTED_OPERATION);
    }
    capabilityCallbackExecutorMap.put(c, executor);
  }

  @RequiresPermission(Manifest.permission.READ_PRIVILEGED_PHONE_STATE)
  @Implementation
  protected void unregisterMmTelCapabilityCallback(@NonNull CapabilityCallback c) {
    capabilityCallbackExecutorMap.remove(c);
  }

  @RequiresPermission(Manifest.permission.READ_PRIVILEGED_PHONE_STATE)
  @Implementation
  protected boolean isAvailable(
      @MmTelCapabilities.MmTelCapability int capability,
      @ImsRegistrationImplBase.ImsRegistrationTech int imsRegTech) {
    // Available if MmTelCapability enabled and IMS registered under same tech
    return mmTelCapabilitiesAvailable.isCapable(capability) && imsRegTech == imsRegistrationTech;
  }

  /**
   * Sets the available {@link MmTelCapabilities}. Only invokes {@link
   * CapabilityCallback#onCapabilitiesStatusChanged(MmTelCapabilities)} if IMS has been registered
   * using {@link #setImsUnregistered(ImsReasonInfo)}.
   */
  public void setMmTelCapabilitiesAvailable(@NonNull MmTelCapabilities capabilities) {
    this.mmTelCapabilitiesAvailable = capabilities;
    if (imsRegistrationTech != ImsRegistrationImplBase.REGISTRATION_TECH_NONE) {
      for (Map.Entry<CapabilityCallback, Executor> entry :
          capabilityCallbackExecutorMap.entrySet()) {
        entry.getValue().execute(() -> entry.getKey().onCapabilitiesStatusChanged(capabilities));
      }
    }
  }

  /** Get subscription id */
  public int getSubscriptionId() {
    return subId;
  }

  /** Returns only one instance per subscription id. */
  @Implementation
  protected static ImsMmTelManager createForSubscriptionId(int subId) {
    if (!SubscriptionManager.isValidSubscriptionId(subId)) {
      throw new IllegalArgumentException("Invalid subscription ID");
    }

    if (existingInstances.containsKey(subId)) {
      return existingInstances.get(subId);
    }
    ImsMmTelManager imsMmTelManager =
        reflector(ImsMmTelManagerReflector.class).createForSubscriptionId(subId);
    existingInstances.put(subId, imsMmTelManager);
    return imsMmTelManager;
  }

  @Resetter
  public static void clearExistingInstances() {
    existingInstances.clear();
  }

  @ForType(ImsMmTelManager.class)
  interface ImsMmTelManagerReflector {

    @Static
    @Direct
    ImsMmTelManager createForSubscriptionId(int subId);
  }
}

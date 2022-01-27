package org.robolectric.shadows;

import android.companion.AssociationRequest;
import android.companion.CompanionDeviceManager;
import android.content.ComponentName;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for CompanionDeviceManager. */
@Implements(value = CompanionDeviceManager.class, minSdk = VERSION_CODES.O)
public class ShadowCompanionDeviceManager {

  private final Set<String> associations = new HashSet<>();
  private final Set<ComponentName> hasNotificationAccess = new HashSet<>();
  private ComponentName lastRequestedNotificationAccess;
  private AssociationRequest lastAssociationRequest;
  private CompanionDeviceManager.Callback lastAssociationCallback;

  @Implementation
  protected List<String> getAssociations() {
    return ImmutableList.copyOf(associations);
  }

  public void addAssociation(String newAssociation) {
    associations.add(newAssociation);
  }

  @Implementation
  protected void disassociate(String deviceMacAddress) {
    if (!associations.remove(deviceMacAddress)) {
      throw new IllegalArgumentException("Association does not exist");
    }
  }

  @Implementation
  protected boolean hasNotificationAccess(ComponentName component) {
    checkHasAssociation();
    return hasNotificationAccess.contains(component);
  }

  public void setNotificationAccess(ComponentName component, boolean hasAccess) {
    if (hasAccess) {
      hasNotificationAccess.add(component);
    } else {
      hasNotificationAccess.remove(component);
    }
  }

  @Implementation
  protected void requestNotificationAccess(ComponentName component) {
    checkHasAssociation();
    lastRequestedNotificationAccess = component;
  }

  @Implementation
  protected void associate(
      AssociationRequest request, CompanionDeviceManager.Callback callback, Handler handler) {
    lastAssociationRequest = request;
    lastAssociationCallback = callback;
  }

  public AssociationRequest getLastAssociationRequest() {
    return lastAssociationRequest;
  }

  public CompanionDeviceManager.Callback getLastAssociationCallback() {
    return lastAssociationCallback;
  }

  public ComponentName getLastRequestedNotificationAccess() {
    return lastRequestedNotificationAccess;
  }

  private void checkHasAssociation() {
    if (associations.isEmpty()) {
      throw new IllegalStateException("App must have an association before calling this API");
    }
  }
}

package org.robolectric.shadows;

import android.content.ContextWrapper;
import android.content.Intent;

import org.robolectric.annotation.Implements;

import java.util.List;

/**
 * Shadow for {@link android.content.ContextWrapper}.
 */
@Implements(ContextWrapper.class)
public class ShadowContextWrapper {

  public List<Intent> getBroadcastIntents() {
    return ShadowApplication.getInstance().getBroadcastIntents();
  }

  /**
   * Non-Android accessor that delegates to the application to consume and return the next {@code Intent} on the
   * started activities stack.
   *
   * @return the next started {@code Intent} for an activity
   */
  public Intent getNextStartedActivity() {
    return ShadowApplication.getInstance().getNextStartedActivity();
  }

  /**
   * Non-Android accessor that delegates to the application to return (without consuming) the next {@code Intent} on
   * the started activities stack.
   *
   * @return the next started {@code Intent} for an activity
   */
  public Intent peekNextStartedActivity() {
    return ShadowApplication.getInstance().peekNextStartedActivity();
  }

  /**
   * Non-Android accessor that delegates to the application to consume and return the next {@code Intent} on the
   * started services stack.
   *
   * @return the next started {@code Intent} for a service
   */
  public Intent getNextStartedService() {
    return ShadowApplication.getInstance().getNextStartedService();
  }

  /**
   * Non-android accessor that delefates to the application to clear the stack of started
   * service intents.
   */
  public void clearStartedServices() {
    ShadowApplication.getInstance().clearStartedServices();
  }

  /**
   * Return (without consuming) the next {@code Intent} on the started services stack.
   *
   * @return the next started {@code Intent} for a service
   */
  public Intent peekNextStartedService() {
    return ShadowApplication.getInstance().peekNextStartedService();
  }

  /**
   * Non-Android accessor that delegates to the application to return the next {@code Intent} to stop
   * a service (irrespective of if the service was running)
   *
   * @return {@code Intent} for the next service requested to be stopped
   */
  public Intent getNextStoppedService() {
    return ShadowApplication.getInstance().getNextStoppedService();
  }

  public void grantPermissions(String... permissionNames) {
    ShadowApplication.getInstance().grantPermissions(permissionNames);
  }

  public void denyPermissions(String... permissionNames) {
    ShadowApplication.getInstance().denyPermissions(permissionNames);
  }
}

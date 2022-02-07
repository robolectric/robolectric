package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.companion.AssociationRequest;
import android.companion.CompanionDeviceManager;
import android.content.ComponentName;
import android.content.IntentSender;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit test for ShadowCompanionDeviceManager. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowCompanionDeviceManagerTest {

  private static final String MAC_ADDRESS = "AA:BB:CC:DD:FF:EE";

  private CompanionDeviceManager companionDeviceManager;
  private ShadowCompanionDeviceManager shadowCompanionDeviceManager;
  private ComponentName componentName;

  @Before
  public void setUp() throws Exception {
    companionDeviceManager = getApplicationContext().getSystemService(CompanionDeviceManager.class);
    shadowCompanionDeviceManager = shadowOf(companionDeviceManager);
    componentName = new ComponentName(getApplicationContext(), Application.class);
  }

  @Test
  public void testAddAssociation() {
    assertThat(companionDeviceManager.getAssociations()).isEmpty();
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);
    assertThat(companionDeviceManager.getAssociations()).contains(MAC_ADDRESS);
  }

  @Test
  public void testDisassociate() {
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);
    companionDeviceManager.disassociate(MAC_ADDRESS);
    assertThat(companionDeviceManager.getAssociations()).isEmpty();
  }

  @Test
  public void testDisassociate_throwsIfNotFound() {
    assertThrows(Exception.class, () -> companionDeviceManager.disassociate(MAC_ADDRESS));
  }

  @Test
  public void testHasNotificationAccess() {
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);

    assertThat(companionDeviceManager.hasNotificationAccess(componentName)).isFalse();
    shadowCompanionDeviceManager.setNotificationAccess(componentName, true);
    assertThat(companionDeviceManager.hasNotificationAccess(componentName)).isTrue();

    shadowCompanionDeviceManager.setNotificationAccess(componentName, false);
    assertThat(companionDeviceManager.hasNotificationAccess(componentName)).isFalse();
  }

  @Test
  public void testHasNotificationAccess_throwsIfNotAssociated() {
    assertThrows(
        Exception.class, () -> companionDeviceManager.hasNotificationAccess(componentName));
  }

  @Test
  public void testRequestNotificationAccess() {
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);

    companionDeviceManager.requestNotificationAccess(componentName);
    assertThat(shadowCompanionDeviceManager.getLastRequestedNotificationAccess())
        .isEqualTo(componentName);
  }

  @Test
  public void testRequestNotificationAccess_throwsIfNotAssociated() {
    assertThrows(
        Exception.class, () -> companionDeviceManager.requestNotificationAccess(componentName));
  }

  @Test
  public void testAssociate() {
    AssociationRequest request = new AssociationRequest.Builder().build();
    CompanionDeviceManager.Callback callback =
        new CompanionDeviceManager.Callback() {
          @Override
          public void onDeviceFound(IntentSender chooserLauncher) {}

          @Override
          public void onFailure(CharSequence error) {}
        };
    companionDeviceManager.associate(request, callback, null);

    assertThat(shadowCompanionDeviceManager.getLastAssociationRequest()).isSameInstanceAs(request);
    assertThat(shadowCompanionDeviceManager.getLastAssociationCallback())
        .isSameInstanceAs(callback);
  }
}

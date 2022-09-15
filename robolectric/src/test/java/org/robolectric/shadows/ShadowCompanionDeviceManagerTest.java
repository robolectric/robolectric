package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.companion.AssociationInfo;
import android.companion.AssociationRequest;
import android.companion.CompanionDeviceManager;
import android.content.ComponentName;
import android.content.IntentSender;
import android.net.MacAddress;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.Executors;
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

  @Test
  public void testAddAssociation_byMacAddress() {
    assertThat(companionDeviceManager.getAssociations()).isEmpty();
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);
    assertThat(companionDeviceManager.getAssociations()).contains(MAC_ADDRESS);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testAddAssociation_byAssociationInfo() {
    AssociationInfo info =
        new AssociationInfo(
            /* id= */ 1,
            /* userId= */ 1,
            "packageName",
            MacAddress.fromString(MAC_ADDRESS),
            "displayName",
            "deviceProfile",
            /* selfManaged= */ false,
            /* notifyOnDeviceNearby= */ false,
            /* timeApprovedMs= */ 0,
            /* lastTimeConnectedMs= */ 0);
    assertThat(companionDeviceManager.getAssociations()).isEmpty();
    shadowCompanionDeviceManager.addAssociation(info);
    assertThat(companionDeviceManager.getMyAssociations()).contains(info);
  }

  @Test
  public void testDisassociate_byMacAddress() {
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);
    companionDeviceManager.disassociate(MAC_ADDRESS);
    assertThat(companionDeviceManager.getAssociations()).isEmpty();
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testDisassociate_byId() {
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);
    // default ID is 1
    companionDeviceManager.disassociate(1);
    assertThat(companionDeviceManager.getAssociations()).isEmpty();
    assertThat(companionDeviceManager.getMyAssociations()).isEmpty();
  }

  @Test
  public void testDisassociate_byMacAddress_throwsIfNotFound() {
    assertThrows(Exception.class, () -> companionDeviceManager.disassociate(MAC_ADDRESS));
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testDisassociate_byId_throwsIfNotFound() {
    assertThrows(Exception.class, () -> companionDeviceManager.disassociate(0));
  }

  @Test
  public void testAssociate_handlerVariant_updatesShadow() {
    AssociationRequest request = new AssociationRequest.Builder().build();
    CompanionDeviceManager.Callback callback = createCallback();

    companionDeviceManager.associate(request, callback, null);

    assertThat(shadowCompanionDeviceManager.getLastAssociationRequest()).isSameInstanceAs(request);
    assertThat(shadowCompanionDeviceManager.getLastAssociationCallback())
        .isSameInstanceAs(callback);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testAssociate_executorVariant_updatesShadow() {
    AssociationRequest request = new AssociationRequest.Builder().build();
    CompanionDeviceManager.Callback callback = createCallback();
    companionDeviceManager.associate(request, Executors.newSingleThreadExecutor(), callback);

    assertThat(shadowCompanionDeviceManager.getLastAssociationRequest()).isSameInstanceAs(request);
    assertThat(shadowCompanionDeviceManager.getLastAssociationCallback())
        .isSameInstanceAs(callback);
  }

  private CompanionDeviceManager.Callback createCallback() {
    return new CompanionDeviceManager.Callback() {
      @Override
      public void onDeviceFound(IntentSender chooserLauncher) {}

      @Override
      public void onFailure(CharSequence error) {}
    };
  }
}

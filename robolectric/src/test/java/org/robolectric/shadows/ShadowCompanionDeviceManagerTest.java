package org.robolectric.shadows;

import static android.Manifest.permission.ASSOCIATE_COMPANION_DEVICES;
import static android.os.Build.VERSION_CODES.O;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.app.Application;
import android.companion.AssociationInfo;
import android.companion.AssociationRequest;
import android.companion.CompanionDeviceManager;
import android.companion.DeviceNotAssociatedException;
import android.content.ComponentName;
import android.content.IntentSender;
import android.net.MacAddress;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.base.Ascii;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Unit test for ShadowCompanionDeviceManager. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowCompanionDeviceManagerTest {

  private static final String MAC_ADDRESS = "AA:BB:CC:DD:FF:EE";
  private static final String PACKAGE_NAME = "org.robolectric";

  private final Application application = getApplicationContext();
  private CompanionDeviceManager companionDeviceManager;
  private ShadowCompanionDeviceManager shadowCompanionDeviceManager;
  private ComponentName componentName;

  @Before
  public void setUp() throws Exception {
    companionDeviceManager = application.getSystemService(CompanionDeviceManager.class);
    shadowCompanionDeviceManager = shadowOf(companionDeviceManager);
    componentName = new ComponentName(application, Application.class);
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
        AssociationInfoBuilder.newBuilder()
            .setId(1)
            .setUserId(1)
            .setPackageName("packageName")
            .setDeviceMacAddress(MAC_ADDRESS)
            .setDisplayName("displayName")
            .setSelfManaged(false)
            .setNotifyOnDeviceNearby(false)
            .setApprovedMs(0)
            .setLastTimeConnectedMs(0)
            .build();
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

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void notifyDeviceAppeared() {
    ReflectionHelpers.callInstanceMethod(
        companionDeviceManager, "notifyDeviceAppeared", ClassParameter.from(int.class, 1));
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testStartObservingDevicePresence_deviceNotAssociated() {
    assertThrows(
        DeviceNotAssociatedException.class,
        () -> companionDeviceManager.startObservingDevicePresence(MAC_ADDRESS));
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testStartObservingDevicePresence_deviceAssociated() {
    shadowCompanionDeviceManager.addAssociation(MAC_ADDRESS);

    companionDeviceManager.startObservingDevicePresence(MAC_ADDRESS);
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testAssociate_systemApi() {
    shadowOf(application).grantPermissions(ASSOCIATE_COMPANION_DEVICES);

    companionDeviceManager.associate(
        PACKAGE_NAME, MacAddress.fromString(MAC_ADDRESS), new byte[] {0x01});
    assertThat(companionDeviceManager.getAssociations()).contains(Ascii.toLowerCase(MAC_ADDRESS));
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testAssociate_systemApi_permissionDenied() {
    shadowOf(application).denyPermissions(ASSOCIATE_COMPANION_DEVICES);

    assertThrows(
        SecurityException.class,
        () ->
            companionDeviceManager.associate(
                PACKAGE_NAME, MacAddress.fromString(MAC_ADDRESS), new byte[] {0x01}));
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testAssociate_systemApi_badPackageName() {
    shadowOf(application).grantPermissions(ASSOCIATE_COMPANION_DEVICES);

    assertThrows(
        SecurityException.class,
        () ->
            companionDeviceManager.associate(
                "some.package", MacAddress.fromString(MAC_ADDRESS), new byte[] {0x01}));
  }

  @Test
  @Config(minSdk = VERSION_CODES.TIRAMISU)
  public void testAssociate_systemApi_badCertificate() {
    shadowOf(application).grantPermissions(ASSOCIATE_COMPANION_DEVICES);

    assertThrows(
        SecurityException.class,
        () ->
            companionDeviceManager.associate(
                PACKAGE_NAME, MacAddress.fromString(MAC_ADDRESS), null));
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

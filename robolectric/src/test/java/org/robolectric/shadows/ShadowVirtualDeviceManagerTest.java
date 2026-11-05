package org.robolectric.shadows;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.input.VirtualKeyEvent.ACTION_DOWN;
import static android.media.AudioRecord.READ_BLOCKING;
import static android.media.AudioRecord.READ_NON_BLOCKING;
import static android.media.AudioRecord.RECORDSTATE_RECORDING;
import static android.media.AudioRecord.RECORDSTATE_STOPPED;
import static android.media.AudioTrack.WRITE_BLOCKING;
import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.companion.virtual.IVirtualDevice;
import android.companion.virtual.VirtualDeviceManager;
import android.companion.virtual.VirtualDeviceManager.VirtualDevice;
import android.companion.virtual.VirtualDeviceManager.VirtualDeviceListener;
import android.companion.virtual.VirtualDeviceParams;
import android.companion.virtual.audio.AudioCapture;
import android.companion.virtual.audio.AudioInjection;
import android.companion.virtual.audio.VirtualAudioDevice;
import android.companion.virtual.camera.VirtualCamera;
import android.companion.virtual.camera.VirtualCameraCallback;
import android.companion.virtual.camera.VirtualCameraConfig;
import android.companion.virtual.sensor.VirtualSensorCallback;
import android.companion.virtual.sensor.VirtualSensorConfig;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraMetadata;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.hardware.display.VirtualDisplay;
import android.hardware.display.VirtualDisplayConfig;
import android.hardware.input.VirtualKeyEvent;
import android.hardware.input.VirtualKeyboard;
import android.hardware.input.VirtualKeyboardConfig;
import android.hardware.input.VirtualMouse;
import android.hardware.input.VirtualMouseButtonEvent;
import android.hardware.input.VirtualMouseConfig;
import android.hardware.input.VirtualMouseRelativeEvent;
import android.hardware.input.VirtualMouseScrollEvent;
import android.hardware.input.VirtualTouchEvent;
import android.hardware.input.VirtualTouchscreen;
import android.hardware.input.VirtualTouchscreenConfig;
import android.media.AudioFormat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import com.google.common.util.concurrent.MoreExecutors;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntConsumer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.junit.rules.SetSystemPropertyRule;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowAudioRecord.AudioRecordSource;
import org.robolectric.shadows.ShadowVirtualDeviceManager.ShadowVirtualDevice;
import org.robolectric.util.ReflectionHelpers;

/** Unit test for ShadowVirtualDeviceManager and ShadowVirtualDevice. */
@Config(minSdk = UPSIDE_DOWN_CAKE, maxSdk = BAKLAVA)
@RunWith(RobolectricTestRunner.class)
public class ShadowVirtualDeviceManagerTest {
  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  private Context context;
  private VirtualDeviceManager virtualDeviceManager;
  @Mock private IntConsumer mockCallback;
  @Mock private VirtualDisplay.Callback mockDisplayCallback;

  private static final int DISPLAY_WIDTH = 720;
  private static final int DISPLAY_HEIGHT = 1280;
  private static final int DISPLAY_DPI = 160;

  @Before
  public void setUp() throws Exception {
    virtualDeviceManager =
        (VirtualDeviceManager)
            getApplicationContext().getSystemService(Context.VIRTUAL_DEVICE_SERVICE);
    context = getApplicationContext();
  }

  @Test
  public void testCreateVirtualDevice() {
    assertThat(virtualDeviceManager.getVirtualDevices()).isEmpty();
    try (VirtualDevice unused =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName("foo").build())) {
      assertThat(virtualDeviceManager.getVirtualDevices()).hasSize(1);
      assertThat(virtualDeviceManager.getVirtualDevices().get(0).getName()).isEqualTo("foo");
    }
    // When device is closed, it's removed from the list.
    assertThat(virtualDeviceManager.getVirtualDevices()).isEmpty();
  }

  @Test
  public void testCreateVirtualDevice_setsVirtualDeviceProxy() {
    try (VirtualDevice device =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName("foo").build())) {
      Object internalDevice = ReflectionHelpers.getField(device, "mVirtualDeviceInternal");
      assertThat(internalDevice).isNotNull();
      IVirtualDevice virtualDevice = ReflectionHelpers.getField(internalDevice, "mVirtualDevice");
      assertThat(virtualDevice).isNotNull();
    }
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void testRegisterVirtualDeviceListener() {
    VirtualDeviceListener listener = mock(VirtualDeviceListener.class);
    virtualDeviceManager.registerVirtualDeviceListener(MoreExecutors.directExecutor(), listener);

    try (VirtualDevice unused =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName("foo").build())) {
      verify(listener).onVirtualDeviceCreated(1);
    }
    // When device is closed, listener is notified.
    verify(listener).onVirtualDeviceClosed(1);
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void testRegisterVirtualDeviceListener_acrossDeviceContexts() {
    Context deviceContext = context.createDeviceContext(Context.DEVICE_ID_DEFAULT);
    VirtualDeviceManager deviceContextVirtualDeviceManager =
        deviceContext.getSystemService(VirtualDeviceManager.class);
    VirtualDeviceListener listener = mock(VirtualDeviceListener.class);
    deviceContextVirtualDeviceManager.registerVirtualDeviceListener(
        MoreExecutors.directExecutor(), listener);

    try (VirtualDevice unused =
        virtualDeviceManager.createVirtualDevice(
            /* associationId= */ 0, new VirtualDeviceParams.Builder().setName("foo").build())) {
      verify(listener).onVirtualDeviceCreated(/* deviceId= */ 1);
    }
    verify(listener).onVirtualDeviceClosed(/* deviceId= */ 1);
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void
      testRegisterVirtualDeviceListener_unregisterDuringCallback_doesNotThrowConcurrentModificationException() {
    VirtualDeviceListener secondListener = mock(VirtualDeviceListener.class);
    VirtualDeviceListener selfUnregisteringListener =
        new VirtualDeviceListener() {
          @Override
          public void onVirtualDeviceCreated(int deviceId) {
            virtualDeviceManager.unregisterVirtualDeviceListener(this);
          }

          @Override
          public void onVirtualDeviceClosed(int deviceId) {}
        };
    virtualDeviceManager.registerVirtualDeviceListener(
        MoreExecutors.directExecutor(), selfUnregisteringListener);
    virtualDeviceManager.registerVirtualDeviceListener(
        MoreExecutors.directExecutor(), secondListener);

    try (VirtualDevice unused =
        virtualDeviceManager.createVirtualDevice(
            /* associationId= */ 0, new VirtualDeviceParams.Builder().setName("foo").build())) {
      verify(secondListener).onVirtualDeviceCreated(/* deviceId= */ 1);
    }
    verify(secondListener).onVirtualDeviceClosed(/* deviceId= */ 1);
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void testUnregisterVirtualDeviceListener() {
    VirtualDeviceListener listener = mock(VirtualDeviceListener.class);
    virtualDeviceManager.registerVirtualDeviceListener(MoreExecutors.directExecutor(), listener);
    virtualDeviceManager.unregisterVirtualDeviceListener(listener);

    try (VirtualDevice unused =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName("foo").build())) {}

    verifyNoInteractions(listener);
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void testRegisterVirtualDeviceListener_nullExecutor_throwsNullPointerException() {
    VirtualDeviceListener listener = mock(VirtualDeviceListener.class);
    assertThrows(
        NullPointerException.class,
        () -> virtualDeviceManager.registerVirtualDeviceListener(null, listener));
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void testRegisterVirtualDeviceListener_nullListener_throwsNullPointerException() {
    Executor executor = MoreExecutors.directExecutor();
    assertThrows(
        NullPointerException.class,
        () -> virtualDeviceManager.registerVirtualDeviceListener(executor, null));
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void testUnregisterVirtualDeviceListener_nullListener_throwsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () -> virtualDeviceManager.unregisterVirtualDeviceListener(null));
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void testGetVirtualDevice_deviceAvailable_returnsVirtualDevice() {
    String deviceName = "foo";
    try (VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName(deviceName).build())) {

      android.companion.virtual.VirtualDevice retrievedVirtualDevice =
          virtualDeviceManager.getVirtualDevice(virtualDevice.getDeviceId());
      assertThat(retrievedVirtualDevice).isNotNull();
      assertThat(retrievedVirtualDevice.getName()).isEqualTo(deviceName);
    }
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void testGetVirtualDevice_deviceNotAvailable_returnsNull() {
    int testDeviceId = 0;

    assertThat(virtualDeviceManager.getVirtualDevice(testDeviceId)).isNull();
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void testGetVirtualDevice_deviceAvailable_incorrectId_returnsNull() {
    try (VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName("foo").build())) {

      int incorrectId = virtualDevice.getDeviceId() + 1;

      assertThat(virtualDeviceManager.getVirtualDevice(incorrectId)).isNull();
    }
  }

  @Test
  public void testIsClosed() {
    VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName("foo").build());
    ShadowVirtualDevice shadowDevice = Shadow.extract(virtualDevice);

    assertThat(shadowDevice.isClosed()).isFalse();

    virtualDevice.close();

    assertThat(shadowDevice.isClosed()).isTrue();
  }

  @Test
  public void testIsValidVirtualDeviceId() {
    try (VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName("foo").build())) {

      assertThat(virtualDeviceManager.isValidVirtualDeviceId(virtualDevice.getDeviceId())).isTrue();

      // Random virtual device id should be false
      assertThat(virtualDeviceManager.isValidVirtualDeviceId(999)).isFalse();
    }
  }

  @Test
  public void testGetDevicePolicy() {
    try (VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0,
            new VirtualDeviceParams.Builder()
                .setDevicePolicy(
                    VirtualDeviceParams.POLICY_TYPE_SENSORS,
                    VirtualDeviceParams.DEVICE_POLICY_CUSTOM)
                .build())) {

      assertThat(
              virtualDeviceManager.getDevicePolicy(
                  virtualDevice.getDeviceId(), VirtualDeviceParams.POLICY_TYPE_SENSORS))
          .isEqualTo(VirtualDeviceParams.DEVICE_POLICY_CUSTOM);
    }
  }

  @Test
  public void testLaunchIntentOnDevice() {
    PendingIntent pendingIntent =
        PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_IMMUTABLE);
    try (VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(0, new VirtualDeviceParams.Builder().build())) {
      ShadowVirtualDevice shadowVirtualDevice = Shadow.extract(virtualDevice);
      shadowVirtualDevice.setPendingIntentCallbackResultCode(
          VirtualDeviceManager.LAUNCH_FAILURE_NO_ACTIVITY);

      virtualDevice.launchPendingIntent(0, pendingIntent, context.getMainExecutor(), mockCallback);
      ShadowLooper.idleMainLooper();

      verify(mockCallback).accept(VirtualDeviceManager.LAUNCH_FAILURE_NO_ACTIVITY);
      assertThat(shadowVirtualDevice.getLastLaunchedPendingIntent()).isEqualTo(pendingIntent);
    }
  }

  @Test
  public void testGetVirtualSensorList() {
    try (VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0,
            new VirtualDeviceParams.Builder()
                .setDevicePolicy(
                    VirtualDeviceParams.POLICY_TYPE_SENSORS,
                    VirtualDeviceParams.DEVICE_POLICY_CUSTOM)
                .addVirtualSensorConfig(
                    new VirtualSensorConfig.Builder(TYPE_ACCELEROMETER, "accel").build())
                .setVirtualSensorCallback(
                    context.getMainExecutor(), mock(VirtualSensorCallback.class))
                .build())) {

      assertThat(virtualDevice.getVirtualSensorList()).hasSize(1);
      assertThat(virtualDevice.getVirtualSensorList().get(0).getName()).isEqualTo("accel");
      assertThat(virtualDevice.getVirtualSensorList().get(0).getType())
          .isEqualTo(TYPE_ACCELEROMETER);
      assertThat(virtualDevice.getVirtualSensorList().get(0).getDeviceId())
          .isEqualTo(virtualDevice.getDeviceId());
    }
  }

  @Test
  public void testGetSensorCallbacks() {
    VirtualSensorCallback mockVirtualSensorCallback = mock(VirtualSensorCallback.class);
    try (VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0,
            new VirtualDeviceParams.Builder()
                .setDevicePolicy(
                    VirtualDeviceParams.POLICY_TYPE_SENSORS,
                    VirtualDeviceParams.DEVICE_POLICY_CUSTOM)
                .addVirtualSensorConfig(
                    new VirtualSensorConfig.Builder(TYPE_ACCELEROMETER, "accel").build())
                .setVirtualSensorCallback(context.getMainExecutor(), mockVirtualSensorCallback)
                .build())) {

      ShadowVirtualDevice shadowDevice = Shadow.extract(virtualDevice);
      VirtualSensorCallback retrievedCallback = shadowDevice.getVirtualSensorCallback();

      retrievedCallback.onConfigurationChanged(
          virtualDevice.getVirtualSensorList().get(0), true, Duration.ZERO, Duration.ZERO);

      assertThat(retrievedCallback).isNotNull();
      verify(mockVirtualSensorCallback).onConfigurationChanged(any(), eq(true), any(), any());
    }
  }

  @Test
  public void testCreateVirtualMouse() {
    try (VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName("foo").build())) {
      VirtualMouseButtonEvent buttonDownEvent =
          new VirtualMouseButtonEvent.Builder()
              .setButtonCode(VirtualMouseButtonEvent.BUTTON_PRIMARY)
              .setAction(VirtualMouseButtonEvent.ACTION_BUTTON_PRESS)
              .build();
      VirtualMouseButtonEvent buttonUpEvent =
          new VirtualMouseButtonEvent.Builder()
              .setButtonCode(VirtualMouseButtonEvent.BUTTON_PRIMARY)
              .setAction(VirtualMouseButtonEvent.ACTION_BUTTON_RELEASE)
              .build();
      VirtualMouseScrollEvent scrollEvent =
          new VirtualMouseScrollEvent.Builder()
              .setXAxisMovement(0.5f)
              .setYAxisMovement(0.5f)
              .build();
      VirtualMouseRelativeEvent relativeEvent =
          new VirtualMouseRelativeEvent.Builder().setRelativeX(0.1f).setRelativeY(0.1f).build();

      VirtualMouse virtualMouse =
          virtualDevice.createVirtualMouse(
              new VirtualMouseConfig.Builder()
                  .setAssociatedDisplayId(0)
                  .setInputDeviceName("mouse")
                  .build());
      virtualMouse.sendButtonEvent(buttonDownEvent);
      virtualMouse.sendButtonEvent(buttonUpEvent);
      virtualMouse.sendScrollEvent(scrollEvent);
      virtualMouse.sendRelativeEvent(relativeEvent);

      assertThat(virtualMouse).isNotNull();
      ShadowVirtualMouse shadowVirtualMouse = Shadow.extract(virtualMouse);
      assertThat(shadowVirtualMouse.getSentButtonEvents())
          .containsExactly(buttonDownEvent, buttonUpEvent);
      assertThat(shadowVirtualMouse.getSentScrollEvents()).containsExactly(scrollEvent);
      assertThat(shadowVirtualMouse.getSentRelativeEvents()).containsExactly(relativeEvent);
    }
  }

  @Test
  public void testCreateVirtualTouchscreen() {
    try (VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName("foo").build())) {
      VirtualTouchEvent virtualTouchEvent =
          new VirtualTouchEvent.Builder()
              .setToolType(MotionEvent.TOOL_TYPE_FINGER)
              .setAction(MotionEvent.ACTION_DOWN)
              .setPointerId(1)
              .setX(1.0f)
              .setY(1.0f)
              .build();

      VirtualTouchscreen virtualTouchscreen =
          virtualDevice.createVirtualTouchscreen(
              new VirtualTouchscreenConfig.Builder(DISPLAY_WIDTH, DISPLAY_HEIGHT)
                  .setAssociatedDisplayId(0)
                  .setInputDeviceName("touchscreen")
                  .build());
      virtualTouchscreen.sendTouchEvent(virtualTouchEvent);

      assertThat(virtualTouchscreen).isNotNull();
      ShadowVirtualTouchscreen shadowVirtualTouchscreen = Shadow.extract(virtualTouchscreen);
      assertThat(shadowVirtualTouchscreen.getSentEvents()).containsExactly(virtualTouchEvent);
    }
  }

  @Test
  public void testCreateVirtualKeyboard() {
    try (VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName("foo").build())) {
      VirtualKeyEvent keyEvent1 =
          new VirtualKeyEvent.Builder()
              .setAction(ACTION_DOWN)
              .setKeyCode(KeyEvent.KEYCODE_A)
              .build();
      VirtualKeyEvent keyEvent2 =
          new VirtualKeyEvent.Builder()
              .setAction(ACTION_DOWN)
              .setKeyCode(KeyEvent.KEYCODE_ENTER)
              .build();

      VirtualKeyboard virtualKeyboard =
          virtualDevice.createVirtualKeyboard(
              new VirtualKeyboardConfig.Builder()
                  .setAssociatedDisplayId(0)
                  .setInputDeviceName("keyboard")
                  .build());
      virtualKeyboard.sendKeyEvent(keyEvent1);
      virtualKeyboard.sendKeyEvent(keyEvent2);

      assertThat(virtualKeyboard).isNotNull();
      ShadowVirtualKeyboard shadowVirtualKeyboard = Shadow.extract(virtualKeyboard);
      assertThat(shadowVirtualKeyboard.getSentEvents()).containsExactly(keyEvent1, keyEvent2);
    }
  }

  @Test
  public void testCloseVirtualInputDevices() {
    VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName("foo").build());
    VirtualKeyboard virtualKeyboard =
        virtualDevice.createVirtualKeyboard(
            new VirtualKeyboardConfig.Builder()
                .setAssociatedDisplayId(0)
                .setInputDeviceName("keyboard")
                .build());
    VirtualTouchscreen virtualTouchscreen =
        virtualDevice.createVirtualTouchscreen(
            new VirtualTouchscreenConfig.Builder(DISPLAY_WIDTH, DISPLAY_HEIGHT)
                .setAssociatedDisplayId(1)
                .setInputDeviceName("touchscreen")
                .build());
    VirtualMouse virtualMouse =
        virtualDevice.createVirtualMouse(
            new VirtualMouseConfig.Builder()
                .setAssociatedDisplayId(2)
                .setInputDeviceName("mouse")
                .build());

    virtualKeyboard.close();
    virtualTouchscreen.close();
    virtualMouse.close();

    ShadowVirtualKeyboard shadowVirtualKeyboard = Shadow.extract(virtualKeyboard);
    ShadowVirtualTouchscreen shadowVirtualTouchscreen = Shadow.extract(virtualTouchscreen);
    ShadowVirtualMouse shadowVirtualMouse = Shadow.extract(virtualMouse);
    assertThat(shadowVirtualKeyboard.isClosed()).isTrue();
    assertThat(shadowVirtualTouchscreen.isClosed()).isTrue();
    assertThat(shadowVirtualMouse.isClosed()).isTrue();
  }

  @Test
  public void testCreateVirtualDisplay() {
    try (VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName("foo").build())) {

      Surface surface = new Surface(new SurfaceTexture(0));

      VirtualDisplay virtualDisplay =
          virtualDevice.createVirtualDisplay(
              getVirtualDisplayConfig("name", surface, /* flags= */ 123),
              MoreExecutors.directExecutor(),
              mockDisplayCallback);

      Rect size = new Rect();
      assertThat(virtualDisplay).isNotNull();
      virtualDisplay.getDisplay().getRectSize(size);
      DisplayMetrics displayMetrics = new DisplayMetrics();
      virtualDisplay.getDisplay().getMetrics(displayMetrics);

      assertThat(displayMetrics.densityDpi).isEqualTo(DISPLAY_DPI);
      assertThat(displayMetrics.heightPixels).isEqualTo(DISPLAY_HEIGHT);
      assertThat(displayMetrics.widthPixels).isEqualTo(DISPLAY_WIDTH);
      assertThat(size).isEqualTo(new Rect(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT));

      assertThat(virtualDisplay.getSurface()).isEqualTo(surface);
      assertThat(virtualDisplay.getDisplay().getDisplayId()).isNotEqualTo(Display.DEFAULT_DISPLAY);
      assertThat(virtualDisplay.getDisplay().getName()).isEqualTo("name");
      assertThat(virtualDisplay.getDisplay().getFlags()).isEqualTo(Display.FLAG_PRESENTATION);
    }
  }

  @Test
  public void testCreateVirtualDisplay_deprecatedSignature() {
    try (VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName("foo").build())) {

      Surface surface = new Surface(new SurfaceTexture(0));

      // Call the deprecated signature via reflector to ensure it works even if hidden in SDK
      VirtualDisplay virtualDisplay =
          reflector(
                  ShadowVirtualDeviceManager.DeviceManagerVirtualDeviceReflector.class,
                  virtualDevice)
              .createVirtualDisplay(
                  DISPLAY_WIDTH,
                  DISPLAY_HEIGHT,
                  DISPLAY_DPI,
                  surface,
                  123,
                  MoreExecutors.directExecutor(),
                  mockDisplayCallback);

      Rect size = new Rect();
      assertThat(virtualDisplay).isNotNull();
      virtualDisplay.getDisplay().getRectSize(size);
      DisplayMetrics displayMetrics = new DisplayMetrics();
      virtualDisplay.getDisplay().getMetrics(displayMetrics);

      assertThat(displayMetrics.densityDpi).isEqualTo(DISPLAY_DPI);
      assertThat(displayMetrics.heightPixels).isEqualTo(DISPLAY_HEIGHT);
      assertThat(displayMetrics.widthPixels).isEqualTo(DISPLAY_WIDTH);
      assertThat(size).isEqualTo(new Rect(0, 0, DISPLAY_WIDTH, DISPLAY_HEIGHT));

      assertThat(virtualDisplay.getSurface()).isEqualTo(surface);
      assertThat(virtualDisplay.getDisplay().getDisplayId()).isNotEqualTo(Display.DEFAULT_DISPLAY);
      assertThat(virtualDisplay.getDisplay().getName())
          .isEqualTo("VirtualDisplay-1"); // Default name format
      assertThat(virtualDisplay.getDisplay().getFlags()).isEqualTo(Display.FLAG_PRESENTATION);
    }
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void testShadowConstructor_coversInternalDeviceSetup() {
    try (VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName("foo").build())) {
      Object internalDevice =
          reflector(
                  ShadowVirtualDeviceManager.DeviceManagerVirtualDeviceReflector.class,
                  virtualDevice)
              .getVirtualDeviceInternal();
      assertThat(internalDevice).isNotNull();
      IVirtualDevice proxy =
          reflector(ShadowVirtualDeviceManager.VirtualDeviceInternalReflector.class, internalDevice)
              .getVirtualDevice();
      assertThat(proxy).isNotNull();
    }
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void testShadowConstructor_withNonNullInternalDevice() throws Exception {
    ShadowVirtualDevice shadowDevice = new ShadowVirtualDevice();

    VirtualDevice mockRealDevice = mock(VirtualDevice.class);
    ReflectionHelpers.setField(shadowDevice, "realVirtualDevice", mockRealDevice);

    Class<?> internalDeviceClass;
    try {
      internalDeviceClass = Class.forName("android.companion.virtual.VirtualDeviceInternal");
    } catch (ClassNotFoundException e) {
      return;
    }
    Object mockInternalDevice = mock(internalDeviceClass);
    ReflectionHelpers.setField(mockRealDevice, "mVirtualDeviceInternal", mockInternalDevice);

    shadowDevice.__constructor__(
        /* service= */ null,
        /* context= */ context,
        /* associationId= */ 1,
        /* params= */ new VirtualDeviceParams.Builder().setName("foo").build());

    IVirtualDevice proxy =
        reflector(
                ShadowVirtualDeviceManager.VirtualDeviceInternalReflector.class, mockInternalDevice)
            .getVirtualDevice();
    assertThat(proxy).isNotNull();
  }

  @Test
  @Config(minSdk = UPSIDE_DOWN_CAKE)
  public void testShadowConstructor_withNullInternalDevice() throws Exception {
    ShadowVirtualDevice shadowDevice = new ShadowVirtualDevice();

    VirtualDevice mockRealDevice = mock(VirtualDevice.class);
    ReflectionHelpers.setField(shadowDevice, "realVirtualDevice", mockRealDevice);

    shadowDevice.__constructor__(
        /* service= */ null,
        /* context= */ context,
        /* associationId= */ 1,
        /* params= */ new VirtualDeviceParams.Builder().setName("foo").build());

    // Should complete successfully without throwing
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void testGetVirtualDisplayIds() {
    String virtualDeviceName = "foo";
    try (VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName(virtualDeviceName).build())) {

      Surface surface = new Surface(new SurfaceTexture(0));

      VirtualDisplay firstVirtualDisplay =
          virtualDevice.createVirtualDisplay(
              getVirtualDisplayConfig("firstName", surface, /* flags= */ 123),
              MoreExecutors.directExecutor(),
              mockDisplayCallback);

      VirtualDisplay secondVirtualDisplay =
          virtualDevice.createVirtualDisplay(
              getVirtualDisplayConfig("secondName", surface, /* flags= */ 123),
              MoreExecutors.directExecutor(),
              mockDisplayCallback);

      android.companion.virtual.VirtualDevice filteredVirtualDevice =
          virtualDeviceManager.getVirtualDevices().stream()
              .filter(device -> virtualDeviceName.equals(device.getName()))
              .findFirst()
              .orElseThrow(() ->
                  new AssertionError(
                      "Virtual device with name '" + virtualDeviceName + "' not found"));

      assertThat(secondVirtualDisplay).isNotNull();
      assertThat(firstVirtualDisplay).isNotNull();
      assertThat(filteredVirtualDevice.getDisplayIds())
          .asList()
          .containsExactly(
              firstVirtualDisplay.getDisplay().getDisplayId(),
              secondVirtualDisplay.getDisplay().getDisplayId());
      assertThat(firstVirtualDisplay.getDisplay().getDisplayId())
          .isNotEqualTo(secondVirtualDisplay.getDisplay().getDisplayId());
    }
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void testCreateVirtualDisplay_displayIdAvailableDuringOnDisplayAddedCallback() {
    DisplayManager displayManager = context.getSystemService(DisplayManager.class);
    AtomicReference<int[]> displayIdsDuringCallback = new AtomicReference<>();
    DisplayListener displayListener =
        new DisplayListener() {
          @Override
          public void onDisplayAdded(int displayId) {
            List<android.companion.virtual.VirtualDevice> devices =
                virtualDeviceManager.getVirtualDevices();
            if (!devices.isEmpty()) {
              displayIdsDuringCallback.set(devices.get(0).getDisplayIds());
            }
          }

          @Override
          public void onDisplayRemoved(int displayId) {}

          @Override
          public void onDisplayChanged(int displayId) {}
        };
    displayManager.registerDisplayListener(displayListener, /* handler= */ null);

    try {
      Surface surface = new Surface(new SurfaceTexture(/* texName= */ 0));
      try (VirtualDevice firstDevice =
          virtualDeviceManager.createVirtualDevice(
              /* associationId= */ 0, new VirtualDeviceParams.Builder().setName("foo").build())) {
        VirtualDisplay firstDisplay =
            firstDevice.createVirtualDisplay(
                getVirtualDisplayConfig("firstDisplay", surface, /* flags= */ 0),
                MoreExecutors.directExecutor(),
                mockDisplayCallback);
        ShadowLooper.idleMainLooper();

        assertThat(displayIdsDuringCallback.get()).isNotNull();
        assertThat(displayIdsDuringCallback.get())
            .asList()
            .contains(firstDisplay.getDisplay().getDisplayId());
        firstDisplay.release();
      }

      // Create a second virtual device and display after the first display was released so that
      // DisplayManagerGlobal's nextDisplayId is greater than max(getDisplayIds()) + 1.
      displayIdsDuringCallback.set(null);
      try (VirtualDevice secondDevice =
          virtualDeviceManager.createVirtualDevice(
              /* associationId= */ 0, new VirtualDeviceParams.Builder().setName("bar").build())) {
        VirtualDisplay secondDisplay =
            secondDevice.createVirtualDisplay(
                getVirtualDisplayConfig("secondDisplay", surface, /* flags= */ 0),
                MoreExecutors.directExecutor(),
                mockDisplayCallback);
        ShadowLooper.idleMainLooper();

        assertThat(displayIdsDuringCallback.get()).isNotNull();
        assertThat(displayIdsDuringCallback.get())
            .asList()
            .contains(secondDisplay.getDisplay().getDisplayId());
      }
    } finally {
      displayManager.unregisterDisplayListener(displayListener);
    }
  }

  @Test
  @Config(sdk = VANILLA_ICE_CREAM)
  public void testCreateVirtualCamera() {
    VirtualCameraCallback callback = mock(VirtualCameraCallback.class);

    VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(0, new VirtualDeviceParams.Builder().build());
    ShadowVirtualDevice shadowVirtualDevice = Shadow.extract(virtualDevice);
    VirtualCamera camera =
        virtualDevice.createVirtualCamera(
            new VirtualCameraConfig.Builder("name")
                .setLensFacing(CameraMetadata.LENS_FACING_FRONT)
                .addStreamConfig(200, 300, ImageFormat.YUV_420_888, 30)
                .setVirtualCameraCallback(MoreExecutors.directExecutor(), callback)
                .build());

    assertThat(shadowVirtualDevice.getVirtualCameras()).containsExactly(camera);

    ShadowVirtualCamera shadowCamera = Shadow.extract(camera);
    shadowCamera.getVirtualCameraCallback().onStreamClosed(99);
    verify(callback).onStreamClosed(99);

    // Verify closing the camera removes it from the camera list.
    camera.close();
    assertThat(shadowVirtualDevice.getVirtualCameras()).isEmpty();
  }

  @Test
  public void virtualDeviceManager_activityContextEnabled_retrievesSameVirtualDevices() {
    setSystemPropertyRule.set("robolectric.createActivityContexts", "true");

    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      VirtualDeviceManager applicationVirtualDeviceManager =
          (VirtualDeviceManager)
              RuntimeEnvironment.getApplication().getSystemService(Context.VIRTUAL_DEVICE_SERVICE);
      Activity activity = controller.get();
      VirtualDeviceManager activityVirtualDeviceManager =
          (VirtualDeviceManager) activity.getSystemService(Context.VIRTUAL_DEVICE_SERVICE);

      assertThat(applicationVirtualDeviceManager).isNotNull();
      List<android.companion.virtual.VirtualDevice> applicationVirtualDevices =
          applicationVirtualDeviceManager.getVirtualDevices();
      assertThat(activityVirtualDeviceManager).isNotNull();
      List<android.companion.virtual.VirtualDevice> activityVirtualDevices =
          activityVirtualDeviceManager.getVirtualDevices();

      assertThat(applicationVirtualDevices).isNotNull();
      assertThat(activityVirtualDevices).isNotNull();
      assertThat(activityVirtualDevices).isEqualTo(applicationVirtualDevices);
    }
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void testReset_closesActiveDevicesAndPreservesListenersForSubsequentCreations() {
    DisplayManager displayManager = context.getSystemService(DisplayManager.class);
    VirtualDeviceListener deviceListener = mock(VirtualDeviceListener.class);
    DisplayListener displayListener = mock(DisplayListener.class);
    virtualDeviceManager.registerVirtualDeviceListener(
        MoreExecutors.directExecutor(), deviceListener);
    displayManager.registerDisplayListener(displayListener, /* handler= */ null);

    try {
      VirtualDevice firstDevice =
          virtualDeviceManager.createVirtualDevice(
              /* associationId= */ 0, new VirtualDeviceParams.Builder().setName("foo").build());
      Surface surface = new Surface(new SurfaceTexture(/* texName= */ 0));
      VirtualDisplay firstDisplay =
          firstDevice.createVirtualDisplay(
              getVirtualDisplayConfig("firstDisplay", surface, /* flags= */ 0),
              MoreExecutors.directExecutor(),
              mockDisplayCallback);
      int firstDisplayId = firstDisplay.getDisplay().getDisplayId();
      ShadowLooper.idleMainLooper();

      verify(deviceListener).onVirtualDeviceCreated(/* deviceId= */ 1);
      verify(displayListener).onDisplayAdded(firstDisplayId);

      ShadowVirtualDevice.reset();
      ShadowLooper.idleMainLooper();

      ShadowVirtualDevice shadowFirstDevice = Shadow.extract(firstDevice);
      assertThat(shadowFirstDevice.isClosed()).isTrue();
      assertThat(virtualDeviceManager.getVirtualDevices()).isEmpty();
      verify(deviceListener).onVirtualDeviceClosed(/* deviceId= */ 1);
      verify(displayListener).onDisplayRemoved(firstDisplayId);
      // Operations on a display after its VirtualDevice was closed should be safe no-ops.
      firstDisplay.setSurface(null);
      firstDisplay.resize(DISPLAY_WIDTH, DISPLAY_HEIGHT, DISPLAY_DPI);
      firstDisplay.release();

      try (VirtualDevice secondDevice =
          virtualDeviceManager.createVirtualDevice(
              /* associationId= */ 0, new VirtualDeviceParams.Builder().setName("bar").build())) {
        assertThat(secondDevice.getDeviceId()).isEqualTo(1);
        verify(deviceListener, times(2)).onVirtualDeviceCreated(/* deviceId= */ 1);
      }
    } finally {
      displayManager.unregisterDisplayListener(displayListener);
    }
  }

  @Test
  public void testCreateVirtualAudioDevice_audioCapture_readsFromShadowAudioRecord() {
    Shadows.shadowOf((Application) context)
        .grantPermissions(Manifest.permission.MODIFY_AUDIO_ROUTING);
    AudioFormat captureFormat = createAudioCaptureFormat();
    AtomicBoolean observedIsBlocking = new AtomicBoolean(true);
    byte[] expectedBytes = new byte[] {10, 20, 30, 40};
    ShadowAudioRecord.setSourceProvider(audioRecord ->
        new AudioRecordSource() {
          @Override
          public int readInByteArray(
              byte[] audioData, int offsetInBytes, int sizeInBytes, boolean isBlocking) {
            observedIsBlocking.set(isBlocking);
            int toCopy = Math.min(expectedBytes.length, sizeInBytes);
            System.arraycopy(expectedBytes, 0, audioData, offsetInBytes, toCopy);
            return toCopy;
          }

          @Override
          public int readInDirectBuffer(ByteBuffer buffer, int sizeInBytes, boolean isBlocking) {
            observedIsBlocking.set(isBlocking);
            int maxBytes = Math.min(buffer.remaining(), sizeInBytes);
            int toCopy = Math.min(expectedBytes.length, maxBytes);
            buffer.put(expectedBytes, 0, toCopy);
            return toCopy;
          }
        });

    try (VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName("foo").build())) {
      VirtualDisplay virtualDisplay =
          virtualDevice.createVirtualDisplay(
              getVirtualDisplayConfig("audioDisplay", /* surface= */ null, /* flags= */ 0),
              /* executor= */ null,
              /* callback= */ null);
      VirtualAudioDevice virtualAudioDevice =
          virtualDevice.createVirtualAudioDevice(
              virtualDisplay, /* executor= */ null, /* callback= */ null);
      assertThat(
              virtualDevice.createVirtualAudioDevice(
                  virtualDisplay, /* executor= */ null, /* callback= */ null))
          .isSameInstanceAs(virtualAudioDevice);

      AudioCapture audioCapture = virtualAudioDevice.startAudioCapture(captureFormat);
      assertThat(audioCapture.getFormat()).isEqualTo(captureFormat);
      assertThat(audioCapture.getRecordingState()).isEqualTo(RECORDSTATE_RECORDING);
      assertThat(virtualAudioDevice.getAudioCapture()).isSameInstanceAs(audioCapture);
      // Before any app UIDs are routed, the backing AudioRecord has not been created yet.
      assertThat(audioCapture.read(new byte[8], 0, 8)).isEqualTo(0);

      ShadowVirtualDevice shadowVirtualDevice = Shadow.extract(virtualDevice);
      shadowVirtualDevice.notifyAppsNeedingAudioRoutingChanged(10100);

      byte[] byteOutput = new byte[8];
      int arrayBytesRead = audioCapture.read(byteOutput, 1, 7, READ_NON_BLOCKING);
      assertThat(arrayBytesRead).isEqualTo(4);
      assertThat(observedIsBlocking.get()).isFalse();
      assertThat(Arrays.copyOfRange(byteOutput, 1, 5)).isEqualTo(expectedBytes);

      ByteBuffer directBuffer = ByteBuffer.allocateDirect(8);
      int bufferBytesRead = audioCapture.read(directBuffer, 8, READ_BLOCKING);
      assertThat(bufferBytesRead).isEqualTo(4);
      assertThat(observedIsBlocking.get()).isTrue();
      assertThat(directBuffer.position()).isEqualTo(0);
      byte[] copiedFromBuffer = new byte[4];
      directBuffer.get(copiedFromBuffer);
      assertThat(copiedFromBuffer).isEqualTo(expectedBytes);

      audioCapture.stop();
      assertThat(audioCapture.getRecordingState()).isEqualTo(RECORDSTATE_STOPPED);

      audioCapture.startRecording();
      assertThat(audioCapture.getRecordingState()).isEqualTo(RECORDSTATE_RECORDING);
      assertThat(audioCapture.read(new byte[8], 0, 8)).isEqualTo(4);

      // Clearing routed app UIDs releases the backing AudioRecord stream.
      shadowVirtualDevice.notifyAppsNeedingAudioRoutingChanged();
      assertThat(audioCapture.read(new byte[8], 0, 8)).isEqualTo(0);
    }
  }

  @Test
  @Config(minSdk = VANILLA_ICE_CREAM)
  public void testCreateVirtualAudioDevice_customAudioDevicePolicy_createsAudioCapture() {
    try (VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0,
            new VirtualDeviceParams.Builder()
                .setName("customAudio")
                .setDevicePolicy(
                    VirtualDeviceParams.POLICY_TYPE_AUDIO, VirtualDeviceParams.DEVICE_POLICY_CUSTOM)
                .build())) {
      // DEVICE_POLICY_CUSTOM uses Context.createDeviceContext(deviceId) for AudioPolicy.
      Shadows.shadowOf((Application) context)
          .grantPermissions(virtualDevice.getDeviceId(), Manifest.permission.MODIFY_AUDIO_ROUTING);
      VirtualDisplay virtualDisplay =
          virtualDevice.createVirtualDisplay(
              getVirtualDisplayConfig("audioDisplay", /* surface= */ null, /* flags= */ 0),
              /* executor= */ null,
              /* callback= */ null);
      VirtualAudioDevice virtualAudioDevice =
          virtualDevice.createVirtualAudioDevice(
              virtualDisplay, /* executor= */ null, /* callback= */ null);
      AudioCapture audioCapture = virtualAudioDevice.startAudioCapture(createAudioCaptureFormat());
      ShadowVirtualDevice shadowVirtualDevice = Shadow.extract(virtualDevice);
      shadowVirtualDevice.notifyAppsNeedingAudioRoutingChanged(10100);

      ByteBuffer directBuffer = ByteBuffer.allocateDirect(16);
      assertThat(audioCapture.read(directBuffer, 16)).isEqualTo(16);
    }
  }

  @Test
  public void testCreateVirtualAudioDevice_audioInjection_writesToAudioTrack() {
    Shadows.shadowOf((Application) context)
        .grantPermissions(Manifest.permission.MODIFY_AUDIO_ROUTING);
    AudioFormat injectionFormat =
        new AudioFormat.Builder()
            .setSampleRate(48000)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .build();

    try (VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName("foo").build())) {
      VirtualDisplay virtualDisplay =
          virtualDevice.createVirtualDisplay(
              getVirtualDisplayConfig("audioDisplay", /* surface= */ null, /* flags= */ 0),
              /* executor= */ null,
              /* callback= */ null);
      VirtualAudioDevice virtualAudioDevice =
          virtualDevice.createVirtualAudioDevice(
              virtualDisplay, /* executor= */ null, /* callback= */ null);
      AudioInjection audioInjection = virtualAudioDevice.startAudioInjection(injectionFormat);
      ShadowVirtualDevice shadowVirtualDevice = Shadow.extract(virtualDevice);
      shadowVirtualDevice.notifyAppsNeedingAudioRoutingChanged(10100);

      ByteBuffer buffer = ByteBuffer.allocateDirect(16);
      int bytesWritten = audioInjection.write(buffer, 16, WRITE_BLOCKING);

      assertThat(bytesWritten).isEqualTo(16);
    }
  }

  @Test
  public void
      testNotifyAppsNeedingAudioRoutingChanged_withoutAudioSession_throwsIllegalStateException() {
    try (VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName("foo").build())) {
      ShadowVirtualDevice shadowVirtualDevice = Shadow.extract(virtualDevice);

      assertThrows(
          IllegalStateException.class,
          () -> shadowVirtualDevice.notifyAppsNeedingAudioRoutingChanged(10100));
    }
  }

  @Test
  public void testCreateVirtualAudioDevice_closeLifecycle_clearsAudioCaptureAndAllowsRecreation() {
    Shadows.shadowOf((Application) context)
        .grantPermissions(Manifest.permission.MODIFY_AUDIO_ROUTING);
    AudioFormat captureFormat = createAudioCaptureFormat();

    VirtualDevice virtualDevice =
        virtualDeviceManager.createVirtualDevice(
            0, new VirtualDeviceParams.Builder().setName("foo").build());
    ShadowVirtualDevice shadowVirtualDevice = Shadow.extract(virtualDevice);

    VirtualDisplay virtualDisplay =
        virtualDevice.createVirtualDisplay(
            getVirtualDisplayConfig("audioDisplay", /* surface= */ null, /* flags= */ 0),
            /* executor= */ null,
            /* callback= */ null);
    VirtualAudioDevice firstAudioDevice =
        virtualDevice.createVirtualAudioDevice(
            virtualDisplay, /* executor= */ null, /* callback= */ null);
    AudioCapture firstCapture = firstAudioDevice.startAudioCapture(captureFormat);
    shadowVirtualDevice.notifyAppsNeedingAudioRoutingChanged(10100);
    assertThat(firstCapture.read(new byte[16], 0, 16)).isEqualTo(16);

    firstAudioDevice.close();
    assertThat(firstAudioDevice.getAudioCapture()).isNull();
    assertThat(firstCapture.read(new byte[16], 0, 16)).isEqualTo(0);
    // Closing the VirtualAudioDevice ends the active audio session, so routing notifications
    // should fail until a new VirtualAudioDevice is created and starts a session.
    assertThrows(
        IllegalStateException.class,
        () -> shadowVirtualDevice.notifyAppsNeedingAudioRoutingChanged(10100));

    VirtualAudioDevice secondAudioDevice =
        virtualDevice.createVirtualAudioDevice(
            virtualDisplay, /* executor= */ null, /* callback= */ null);
    assertThat(secondAudioDevice).isNotSameInstanceAs(firstAudioDevice);
    AudioCapture secondCapture = secondAudioDevice.startAudioCapture(captureFormat);
    shadowVirtualDevice.notifyAppsNeedingAudioRoutingChanged(10100);
    assertThat(secondCapture.read(new byte[16], 0, 16)).isEqualTo(16);

    virtualDevice.close();
    assertThat(secondAudioDevice.getAudioCapture()).isNull();
    assertThat(secondCapture.read(new byte[16], 0, 16)).isEqualTo(0);
  }

  private static AudioFormat createAudioCaptureFormat() {
    return new AudioFormat.Builder()
        .setSampleRate(48000)
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
        .build();
  }

  private VirtualDisplayConfig getVirtualDisplayConfig(String name, Surface surface, int flags) {
    return new VirtualDisplayConfig.Builder(name, DISPLAY_WIDTH, DISPLAY_HEIGHT, DISPLAY_DPI)
        .setSurface(surface)
        .setFlags(flags)
        .build();
  }
}

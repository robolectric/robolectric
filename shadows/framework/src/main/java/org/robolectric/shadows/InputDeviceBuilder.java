package org.robolectric.shadows;

import android.view.InputDevice;
import android.view.KeyCharacterMap;

/**
 * Builder for {@link android.view.InputDevice}.
 *
 * <p>This exposes the setters for public InputDevice attributes. Its implemented by wrapping the
 * hidden android.view.InputDevice.Builder. Tests building against the android platform source
 * should just use that API instead.
 *
 * <p>Only supported when running on SDKs >= 34
 */
public class InputDeviceBuilder {

  private InputDeviceBuilder() {}

  public static InputDeviceBuilder newBuilder() {
    return new InputDeviceBuilder();
  }

  private final InputDevice.Builder delegate = new InputDevice.Builder();

  /**
   * @see InputDevice#getId()
   */
  public InputDeviceBuilder setId(int id) {
    delegate.setId(id);
    return this;
  }

  /**
   * @see InputDevice#getControllerNumber()
   */
  public InputDeviceBuilder setControllerNumber(int controllerNumber) {
    delegate.setControllerNumber(controllerNumber);
    return this;
  }

  /**
   * @see InputDevice#getName()
   */
  public InputDeviceBuilder setName(String name) {
    delegate.setName(name);
    return this;
  }

  /**
   * @see InputDevice#getVendorId()
   */
  public InputDeviceBuilder setVendorId(int vendorId) {
    delegate.setVendorId(vendorId);
    return this;
  }

  /**
   * @see InputDevice#getProductId()
   */
  public InputDeviceBuilder setProductId(int productId) {
    delegate.setProductId(productId);
    return this;
  }

  /**
   * @see InputDevice#getDescriptor()
   */
  public InputDeviceBuilder setDescriptor(String descriptor) {
    delegate.setDescriptor(descriptor);
    return this;
  }

  /**
   * @see InputDevice#isExternal()
   */
  public InputDeviceBuilder setExternal(boolean external) {
    delegate.setExternal(external);
    return this;
  }

  /**
   * @see InputDevice#getSources()
   */
  public InputDeviceBuilder setSources(int sources) {
    delegate.setSources(sources);
    return this;
  }

  /**
   * @see InputDevice#getKeyboardType()
   */
  public InputDeviceBuilder setKeyboardType(int keyboardType) {
    delegate.setKeyboardType(keyboardType);
    return this;
  }

  /**
   * @see InputDevice#getKeyCharacterMap()
   */
  public InputDeviceBuilder setKeyCharacterMap(KeyCharacterMap keyCharacterMap) {
    delegate.setKeyCharacterMap(keyCharacterMap);
    return this;
  }

  /**
   * @see InputDevice#getVibrator()
   */
  public InputDeviceBuilder setHasVibrator(boolean hasVibrator) {
    delegate.setHasVibrator(hasVibrator);
    return this;
  }

  /**
   * @see InputDevice#hasMicrophone()
   */
  public InputDeviceBuilder setHasMicrophone(boolean hasMicrophone) {
    delegate.setHasMicrophone(hasMicrophone);
    return this;
  }

  /**
   * @see InputDevice#getMotionRanges()
   */
  public InputDeviceBuilder addMotionRange(
      int axis, int source, float min, float max, float flat, float fuzz, float resolution) {
    delegate.addMotionRange(axis, source, min, max, flat, fuzz, resolution);
    return this;
  }

  public InputDevice build() {
    return delegate.build();
  }
}

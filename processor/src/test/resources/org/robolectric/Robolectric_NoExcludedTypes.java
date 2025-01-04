package org.robolectric;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import org.robolectric.internal.ShadowProvider;
import org.robolectric.shadow.api.Shadow;

/**
 * Shadow mapper. Automatically generated by the Robolectric Annotation Processor.
 */
@Generated("org.robolectric.annotation.processing.RobolectricProcessor")
@SuppressWarnings({"unchecked","deprecation"})
public class Shadows implements ShadowProvider {
  private static final List<Map.Entry<String, String>> SHADOWS = new ArrayList<>(1);

  static {
    SHADOWS.add(new AbstractMap.SimpleImmutableEntry<>("com.example.objects.Dummy", "org.robolectric.annotation.processing.shadows.ShadowExcludedFromAndroidSdk"));
  }

  @Override
  public void reset() {
  }

  @Override
  public Collection<Map.Entry<String, String>> getShadows() {
    return SHADOWS;
  }

  @Override
  public String[] getProvidedPackageNames() {
    return new String[] {
        "com.example.objects"
    };
  }

}
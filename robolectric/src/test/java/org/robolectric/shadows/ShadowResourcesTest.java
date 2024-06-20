package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.S;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.widget.RemoteViews;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.Range;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowResourcesTest {
  private static final int FIRST_RESOURCE_COLOR_ID = android.R.color.system_neutral1_0;
  private static final int LAST_RESOURCE_COLOR_ID = android.R.color.system_accent3_1000;

  /**
   * Some green/blue colors, from system_neutral1_0 to system_accent3_1000, extracted using 0xB1EBFF
   * as seed color and "FRUIT_SALAD" as theme style.
   */
  private static final int[] greenBlueColorBase = {
    -1, -393729, -1641480, -2562838, -4405043, -6181454, -7892073, -9668483, -11181979, -12760755,
    -14208458, -15590111, -16777216, -1, -393729, -2296322, -3217680, -4994349, -6770760, -8547171,
    -10257790, -11836822, -13350318, -14863301, -16376283, -16777216, -1, -720905, -4456478,
    -8128307, -10036302, -12075112, -14638210, -16742810, -16749487, -16756420, -16762839,
    -16768746, -16777216, -1, -720905, -4456478, -5901613, -7678281, -9454947, -11231613, -13139095,
    -15111342, -16756420, -16762839, -16768746, -16777216, -1, -393729, -2361857, -5051393,
    -7941655, -9783603, -11625551, -13729642, -16750723, -16757153, -16763326, -16769241, -16777216
  };

  private Resources resources;

  @Before
  public void setup() throws Exception {
    resources = ApplicationProvider.getApplicationContext().getResources();
  }

  @Test
  @Config(qualifiers = "fr")
  public void testGetValuesResFromSpecificQualifiers() {
    assertThat(resources.getString(R.string.hello)).isEqualTo("Bonjour");
  }

  /**
   * Public framework symbols are defined here:
   * https://android.googlesource.com/platform/frameworks/base/+/master/core/res/res/values/public.xml
   * Private framework symbols are defined here:
   * https://android.googlesource.com/platform/frameworks/base/+/master/core/res/res/values/symbols.xml
   *
   * <p>These generate android.R and com.android.internal.R respectively, when Framework Java code
   * does not need to reference a framework resource it will not have an R value generated.
   * Robolectric is then missing an identifier for this resource so we must generate a placeholder
   * ourselves.
   */
  @Test
  @Config(
      sdk =
          Build.VERSION_CODES
              .LOLLIPOP) // android:color/secondary_text_material_dark was added in API 21
  public void shouldGenerateIdsForResourcesThatAreMissingRValues() {
    int identifier_missing_from_r_file =
        resources.getIdentifier("secondary_text_material_dark", "color", "android");

    // We expect Robolectric to generate a placeholder identifier where one was not generated in the
    // android R files.
    assertThat(identifier_missing_from_r_file).isNotEqualTo(0);

    // We expect to be able to successfully android:color/secondary_text_material_dark to a
    // ColorStateList.
    assertThat(resources.getColorStateList(identifier_missing_from_r_file)).isNotNull();
  }

  @Test
  @Config(qualifiers = "fr")
  public void openRawResource_shouldLoadDrawableWithQualifiers() {
    InputStream resourceStream = resources.openRawResource(R.drawable.an_image);
    Bitmap bitmap = BitmapFactory.decodeStream(resourceStream);
    assertThat(bitmap.getHeight()).isEqualTo(100);
    assertThat(bitmap.getWidth()).isEqualTo(100);
  }

  @Test
  public void openRawResourceFd_shouldReturnsValidFdForUnCompressFile() throws Exception {
    try (AssetFileDescriptor afd = resources.openRawResourceFd(R.raw.raw_resource)) {
      assertThat(afd).isNotNull();
    }
  }

  @Test
  @Config
  public void themeResolveAttribute_shouldSupportDereferenceResource() {
    TypedValue out = new TypedValue();

    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.MyBlackTheme, false);

    theme.resolveAttribute(android.R.attr.windowBackground, out, true);
    assertThat(out.type).isNotEqualTo(TypedValue.TYPE_REFERENCE);
    assertThat(out.type)
        .isIn(Range.closed(TypedValue.TYPE_FIRST_COLOR_INT, TypedValue.TYPE_LAST_COLOR_INT));

    int value = resources.getColor(android.R.color.black);
    assertThat(out.data).isEqualTo(value);
  }

  @Test
  public void themeResolveAttribute_shouldSupportNotDereferencingResource() {
    TypedValue out = new TypedValue();

    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.MyBlackTheme, false);

    theme.resolveAttribute(android.R.attr.windowBackground, out, false);
    assertThat(out.type).isEqualTo(TypedValue.TYPE_REFERENCE);
    assertThat(out.data).isEqualTo(android.R.color.black);
  }

  @Test
  public void obtainStyledAttributes_shouldCheckXmlFirst_fromAttributeSetBuilder() {

    // This simulates a ResourceProvider built from a 21+ SDK as viewportHeight / viewportWidth were
    // introduced in API 21 but the public ID values they are assigned clash with private
    // com.android.internal.R values on older SDKs. This test ensures that even on older SDKs, on
    // calls to obtainStyledAttributes() Robolectric will first check for matching resource ID
    // values in the AttributeSet before checking the theme.

    AttributeSet attributes =
        Robolectric.buildAttributeSet()
            .addAttribute(android.R.attr.viewportWidth, "12.0")
            .addAttribute(android.R.attr.viewportHeight, "24.0")
            .build();

    TypedArray typedArray =
        ApplicationProvider.getApplicationContext()
            .getTheme()
            .obtainStyledAttributes(
                attributes,
                new int[] {android.R.attr.viewportWidth, android.R.attr.viewportHeight},
                0,
                0);
    assertThat(typedArray.getFloat(0, 0)).isEqualTo(12.0f);
    assertThat(typedArray.getFloat(1, 0)).isEqualTo(24.0f);
    typedArray.recycle();
  }

  @Test
  @Config(minSdk = N_MR1)
  public void obtainAttributes() {
    TypedArray typedArray =
        resources.obtainAttributes(
            Robolectric.buildAttributeSet()
                .addAttribute(R.attr.styleReference, "@xml/shortcuts")
                .build(),
            new int[] {R.attr.styleReference});
    assertThat(typedArray).isNotNull();
    assertThat(typedArray.peekValue(0).resourceId).isEqualTo(R.xml.shortcuts);
  }

  @Test
  public void obtainAttributes_shouldUseReferencedIdFromAttributeSet() {
    // android:id/mask was introduced in API 21, but it's still possible for apps built against API
    // 21 to refer to it in older runtimes because referenced resource ids are compiled (by aapt)
    // into the binary XML format.
    AttributeSet attributeSet =
        Robolectric.buildAttributeSet().addAttribute(android.R.attr.id, "@android:id/mask").build();
    TypedArray typedArray = resources.obtainAttributes(attributeSet, new int[] {android.R.attr.id});
    assertThat(typedArray.getResourceId(0, -9)).isEqualTo(android.R.id.mask);
  }

  @Test
  public void obtainAttributes_shouldReturnValuesFromAttributeSet() {
    AttributeSet attributes =
        Robolectric.buildAttributeSet()
            .addAttribute(android.R.attr.title, "A title!")
            .addAttribute(android.R.attr.width, "12px")
            .addAttribute(android.R.attr.height, "1in")
            .build();
    TypedArray typedArray =
        resources.obtainAttributes(
            attributes,
            new int[] {android.R.attr.height, android.R.attr.width, android.R.attr.title});

    assertThat(typedArray.getDimension(0, 0)).isEqualTo(160f);
    assertThat(typedArray.getDimension(1, 0)).isEqualTo(12f);
    assertThat(typedArray.getString(2)).isEqualTo("A title!");
    typedArray.recycle();
  }

  @Test
  public void obtainStyledAttributesShouldCheckXmlFirst_andFollowReferences() {
    // This simulates a ResourceProvider built from a 21+ SDK as viewportHeight / viewportWidth were
    // introduced in API 21 but the public ID values they are assigned clash with private
    // com.android.internal.R values on older SDKs. This test ensures that even on older SDKs,
    // on calls to obtainStyledAttributes() Robolectric will first check for matching
    // resource ID values in the AttributeSet before checking the theme.
    AttributeSet attributes =
        Robolectric.buildAttributeSet()
            .addAttribute(android.R.attr.viewportWidth, "@integer/test_integer1")
            .addAttribute(android.R.attr.viewportHeight, "@integer/test_integer2")
            .build();

    TypedArray typedArray =
        ApplicationProvider.getApplicationContext()
            .getTheme()
            .obtainStyledAttributes(
                attributes,
                new int[] {android.R.attr.viewportWidth, android.R.attr.viewportHeight},
                0,
                0);
    assertThat(typedArray.getFloat(0, 0)).isEqualTo(2000);
    assertThat(typedArray.getFloat(1, 0)).isEqualTo(9);
    typedArray.recycle();
  }

  @Test
  public void getAttributeSetSourceResId() {
    XmlResourceParser xmlResourceParser = resources.getXml(R.xml.preferences);

    int sourceRedId = ShadowResources.getAttributeSetSourceResId(xmlResourceParser);

    assertThat(sourceRedId).isEqualTo(R.xml.preferences);
  }

  @Test
  public void addConfigurationChangeListener_callsOnConfigurationChange() {
    AtomicBoolean listenerWasCalled = new AtomicBoolean();
    shadowOf(resources)
        .addConfigurationChangeListener(
            (oldConfig, newConfig, newMetrics) -> {
              listenerWasCalled.set(true);
              assertThat(newConfig.fontScale).isEqualTo(oldConfig.fontScale * 2);
            });

    Configuration newConfig = new Configuration(resources.getConfiguration());
    newConfig.fontScale *= 2;
    resources.updateConfiguration(newConfig, resources.getDisplayMetrics());

    assertThat(listenerWasCalled.get()).isTrue();
  }

  @Test
  public void removeConfigurationChangeListener_doesNotCallOnConfigurationChange() {
    AtomicBoolean listenerWasCalled = new AtomicBoolean();
    ShadowResources.OnConfigurationChangeListener listener =
        (oldConfig, newConfig, newMetrics) -> listenerWasCalled.set(true);
    Configuration newConfig = new Configuration(resources.getConfiguration());
    newConfig.fontScale *= 2;

    shadowOf(resources).addConfigurationChangeListener(listener);
    shadowOf(resources).removeConfigurationChangeListener(listener);
    resources.updateConfiguration(newConfig, resources.getDisplayMetrics());

    assertThat(listenerWasCalled.get()).isFalse();
  }

  @Test
  public void subclassWithNpeGetConfiguration_constructsCorrectly() {
    // Simulate the behavior of ResourcesWrapper during construction which will throw an NPE if
    // getConfiguration is called, on lower SDKs the Configuration constructor calls
    // updateConfiguration(), the ShadowResources will attempt to call getConfiguration during this
    // method call and shouldn't fail.
    Resources resourcesSubclass =
        new Resources(
            resources.getAssets(), resources.getDisplayMetrics(), resources.getConfiguration()) {
          @Override
          public Configuration getConfiguration() {
            throw new NullPointerException();
          }
        };

    assertThat(resourcesSubclass).isNotNull();
  }

  @Test
  @Config(minSdk = S)
  public void getColor_shouldReturnCorrectMaterialYouColor() throws Exception {
    SparseIntArray sparseArray =
        new SparseIntArray(LAST_RESOURCE_COLOR_ID - FIRST_RESOURCE_COLOR_ID + 1);
    IntStream.range(0, greenBlueColorBase.length)
        .forEach(i -> sparseArray.put(FIRST_RESOURCE_COLOR_ID + i, greenBlueColorBase[i]));
    int basicColor = android.R.color.system_neutral1_10;
    Context context = ApplicationProvider.getApplicationContext();
    RemoteViews.ColorResources colorResources =
        RemoteViews.ColorResources.create(context, sparseArray);
    assertThat(colorResources).isNotNull();

    colorResources.apply(context);

    assertThat(basicColor).isNotEqualTo(0);
    assertThat(resources.getColor(basicColor, /* theme= */ null)).isEqualTo(-393729);
  }

  @Ignore("Re-enable when performing benchmarks")
  @Test
  @Config(sdk = Q)
  public void benchmarkUpdateConfiguration() {
    long startTime = System.nanoTime();
    Resources systemResources = Resources.getSystem();
    for (int i = 0; i < 10_000; i++) {
      Configuration oldConfig = resources.getConfiguration();
      Configuration newConfig = new Configuration(oldConfig);
      // This change triggers RebuildFilterList in CppAssetManager2
      newConfig.colorMode = 3;
      systemResources.updateConfiguration(newConfig, resources.getDisplayMetrics());
      systemResources.updateConfiguration(oldConfig, resources.getDisplayMetrics());
    }
    long endTime = System.nanoTime();
    long elapsedNs = endTime - startTime;
    System.err.println(
        "updateConfiguration benchmark took " + TimeUnit.NANOSECONDS.toMillis(elapsedNs) + " ms");
  }
}

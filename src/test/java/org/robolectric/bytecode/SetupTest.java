package org.robolectric.bytecode;

import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SetupTest {
  private Setup setup;

  @Before
  public void setUp() throws Exception {
    setup = new Setup();
  }

  @Test
  public void shouldInstrumentDefaultRequestDirector() throws Exception {
    assertTrue(setup.shouldInstrument(wrap("org.apache.http.impl.client.DefaultRequestDirector")));
  }

  @Test
  public void shouldInstrumentGoogleMapsClasses() throws Exception {
    assertTrue(setup.shouldInstrument(wrap("com.google.android.maps.SomeMapsClass")));
  }

  @Test
  public void shouldInstrumentGooglePlayServicesClasses() throws Exception {
    assertTrue(setup.shouldInstrument(wrap("com.google.android.gms.auth.GoogleAuthUtil")));
  }

  @Test
  public void shouldNotInstrumentAndroidAppClasses() throws Exception {
    assertFalse(setup.shouldInstrument(wrap("com.google.android.apps.Foo")));
  }

  @Test
  public void shouldInstrumentDalvikClasses() {
    assertTrue(setup.shouldInstrument(wrap("dalvik.system.DexFile")));
  }

  @Test
  public void shouldNotInstrumentCoreJdkClasses() throws Exception {
    assertFalse(setup.shouldInstrument(wrap("java.lang.Object")));
    assertFalse(setup.shouldInstrument(wrap("java.lang.String")));
  }

  @Test
  public void shouldInstrumentAndroidCoreClasses() throws Exception {
    assertTrue(setup.shouldInstrument(wrap("android.content.Intent")));
    assertTrue(setup.shouldInstrument(wrap("android.and.now.for.something.completely.different")));
  }

  @Test
  public void shouldNotAcquireRClasses() throws Exception {
    assertTrue(setup.shouldAcquire("com.whatever.Rfoo"));
    assertTrue(setup.shouldAcquire("com.whatever.fooR"));
    assertFalse(setup.shouldAcquire("com.whatever.R"));
    assertFalse(setup.shouldAcquire("com.whatever.R$anything"));
    assertTrue(setup.shouldAcquire("com.whatever.R$anything$else"));
  }

  ClassInfo wrap(final String className) {
    return new ClassInfo() {
      @Override public String getName() {
        return className;
      }

      @Override public boolean isInterface() {
        return false;
      }

      @Override public boolean isAnnotation() {
        return false;
      }

      @Override public boolean hasAnnotation(Class<? extends Annotation> annotationClass) {
        return false;
      }
    };
  }
}

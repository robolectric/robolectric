package org.robolectric.android.internal;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.shadows.ClassNameResolver;
import org.robolectric.shadows.testing.TestApplication;

@RunWith(JUnit4.class)
public class ClassNameResolverTest {
  @Test
  public void shouldResolveClassesBySimpleName() throws Exception {
    assertEquals(TestApplication.class, ClassNameResolver.resolve("org.robolectric.shadows.testing", "TestApplication"));
  }

  @Test
  public void shouldResolveClassesByDottedSimpleName() throws Exception {
    assertEquals(TestApplication.class, ClassNameResolver.resolve("org.robolectric.shadows.testing", ".TestApplication"));
  }

  @Test
  public void shouldResolveClassesByFullyQualifiedName() throws Exception {
    assertEquals(TestApplication.class, ClassNameResolver.resolve("org.robolectric.shadows.testing", "org.robolectric.shadows.testing.TestApplication"));
  }

  @Test
  public void shouldResolveClassesByPartiallyQualifiedName() throws Exception {
    assertEquals(TestApplication.class, ClassNameResolver.resolve("org", ".robolectric.shadows.testing.TestApplication"));
  }

  @Test(expected = ClassNotFoundException.class)
  public void shouldNotResolveClassesByUndottedPartiallyQualifiedNameBecauseAndroidDoesnt() throws Exception {
    ClassNameResolver.resolve("org", "robolectric.shadows.testing.TestApplication");
  }
}

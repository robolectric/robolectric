package org.robolectric.internal.dependency;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.internal.dependency.CachedDependencyResolver.Cache;

@RunWith(JUnit4.class)
public class CachedDependencyResolverFileCacheTest {

  private final String ID = "id";

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void shouldLoadNullWhenCacheIsEmpty() throws Exception {
    Cache cache = new CachedDependencyResolver.FileCache(temporaryFolder.getRoot(), 1000);

    String value = cache.load(ID, String.class);

    assertNull(value);
  }

  @Test
  public void shouldLoadObjectWhenCacheExists() throws Exception {
    Cache cache = new CachedDependencyResolver.FileCache(temporaryFolder.getRoot(), 1000);

    String expectedValue = "some string";

    writeToCacheFile(expectedValue);

    String value = cache.load(ID, String.class);

    assertEquals(expectedValue, value);
  }

  @Test
  public void shouldLoadNullWhenObjectInCacheHaveBadType() throws Exception {
    Cache cache = new CachedDependencyResolver.FileCache(temporaryFolder.getRoot(), 1000);

    writeToCacheFile(123L);

    assertNull(cache.load(ID, String.class));
  }

  @Test
  public void shouldWriteObjectToFile() throws Exception {
    Cache cache = new CachedDependencyResolver.FileCache(temporaryFolder.getRoot(), 1000);

    Long expectedValue = 421L;

    assertTrue(cache.write(ID, expectedValue));

    Object actual = readFromCacheFile();

    assertEquals(expectedValue, actual);
  }

  @Test
  public void shouldWriteUrlArrayToFile() throws Exception {
    Cache cache = new CachedDependencyResolver.FileCache(temporaryFolder.getRoot(), 1000);

    URL[] urls = { new URL("http://localhost") };

    assertTrue(cache.write(ID, urls));

    Object actual = readFromCacheFile();

    assertArrayEquals(urls, (URL[]) actual);
  }

  private Object readFromCacheFile() throws ClassNotFoundException, IOException {
    File dir = temporaryFolder.getRoot();

    File dest = new File(dir, ID);

    ObjectInputStream in = new ObjectInputStream(new FileInputStream(dest));

    try {
      return in.readObject();
    } finally {
      in.close();
    }
  }

  private void writeToCacheFile(Object expectedValue) throws IOException {
    File dir = temporaryFolder.getRoot();

    File dest = new File(dir, ID);

    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dest));

    try {
      out.writeObject(expectedValue);
    } finally {
      out.close();
    }
  }

}

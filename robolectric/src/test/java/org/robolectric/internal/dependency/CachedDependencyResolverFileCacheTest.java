package org.robolectric.internal.dependency;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;

import org.junit.Rule;
import org.junit.Test;
import org.robolectric.internal.dependency.CachedDependencyResolver.Cache;
import org.robolectric.test.TemporaryFolder;

import static org.junit.Assert.*;

public class CachedDependencyResolverFileCacheTest {

  private final String ID = "id";
  private final String DIR = "tmp";

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void shouldLoadNullWhenCacheIsEmpty() {
    Cache cache = createCache();

    String value = cache.load(ID, String.class);

    assertNull(value);
  }

  @Test
  public void shouldLoadObjectWhenCacheExists() throws Exception {
    Cache cache = createCache();

    String expectedValue = "some string";

    writeToCacheFile(expectedValue);

    String value = cache.load(ID, String.class);

    assertEquals(expectedValue, value);
  }

  @Test
  public void shouldLoadNullWhenObjectInCacheHaveBadType() throws Exception {
    Cache cache = createCache();

    writeToCacheFile(123L);

    assertNull(cache.load(ID, String.class));
  }

  @Test
  public void shouldWriteObjectToFile() throws Exception {
    Cache cache = createCache();

    Long expectedValue = 421L;

    assertTrue(cache.write(ID, expectedValue));

    Object actual = readFromCacheFile();

    assertEquals(expectedValue, actual);
  }

  @Test
  public void shouldWriteUrlArrayToFile() throws Exception {
    Cache cache = createCache();

    URL[] urls = { new URL("http://localhost") };

    assertTrue(cache.write(ID, urls));

    Object actual = readFromCacheFile();

    assertArrayEquals(urls, (URL[]) actual);
  }

  private Object readFromCacheFile() throws ClassNotFoundException, IOException {
    File dir = temporaryFolder.newFolder(DIR);

    File dest = new File(dir, ID);

    ObjectInputStream in = new ObjectInputStream(new FileInputStream(dest));

    try {
      return in.readObject();
    } finally {
      in.close();
    }
  }

  private void writeToCacheFile(Object expectedValue) throws FileNotFoundException, IOException {
    File dir = temporaryFolder.newFolder(DIR);

    File dest = new File(dir, ID);

    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dest));

    try {
      out.writeObject(expectedValue);
    } finally {
      out.close();
    }
  }

  private Cache createCache() {
    return new CachedDependencyResolver.FileCache(temporaryFolder.newFolder(DIR), 1000);
  }
}

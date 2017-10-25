package org.robolectric.internal.dependency;

import com.google.common.base.Preconditions;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.zip.CRC32;

public class CachedDependencyResolver implements DependencyResolver {
  private final static String CACHE_PREFIX = "localArtifactUrl";

  private DependencyProperties depsProp;
  private final DependencyResolver dependencyResolver;
  private final CacheNamingStrategy cacheNamingStrategy;
  private final CacheValidationStrategy cacheValidationStrategy;
  private final Cache cache;

  public CachedDependencyResolver(DependencyProperties depsProp, DependencyResolver dependencyResolver, File cacheDir,
                                  long cacheValidTime) {
    this(depsProp, dependencyResolver, new FileCache(cacheDir, cacheValidTime), new DefaultCacheNamingStrategy(),
        new DefaultCacheValidationStrategy());
  }

  public CachedDependencyResolver(DependencyProperties depsProp, DependencyResolver dependencyResolver, Cache cache,
                                  CacheNamingStrategy cacheNamingStrategy,
                                  CacheValidationStrategy cacheValidationStrategy) {
    this.depsProp = depsProp;
    this.dependencyResolver = dependencyResolver;
    this.cache = cache;
    this.cacheNamingStrategy = cacheNamingStrategy;
    this.cacheValidationStrategy = cacheValidationStrategy;
  }

  public URL getLocalArtifactUrl(int apiLevel) {
    String artifact = depsProp.getDependencyName(apiLevel);
    final String cacheName = cacheNamingStrategy.getName(CACHE_PREFIX, artifact);
    final URL urlFromCache = cache.load(cacheName, URL.class);

    if (urlFromCache != null && cacheValidationStrategy.isValid(urlFromCache)) {
      return urlFromCache;
    }

    final URL url = dependencyResolver.getLocalArtifactUrl(apiLevel);
    cache.write(cacheName, url);
    return url;

  }

  interface CacheNamingStrategy {
    String getName(String prefix, String... dependencyName);
  }

  interface CacheValidationStrategy {
    boolean isValid(URL url);

    boolean isValid(URL[] urls);
  }

  static class DefaultCacheValidationStrategy implements CacheValidationStrategy {
    @Override
    public boolean isValid(URL url) {
      return new File(url.getPath()).exists();
    }

    @Override
    public boolean isValid(URL[] urls) {
      for (URL url : urls) {
        if (!isValid(url)) {
          return false;
        }
      }
      return true;
    }
  }

  static class DefaultCacheNamingStrategy implements CacheNamingStrategy {
    @Override public String getName(String prefix, String... dependencyNames) {
      StringBuilder sb = new StringBuilder();

      sb.append(prefix)
          .append("#");

      for (String dependency : dependencyNames) {
        sb.append(dependency)
            .append(",");
      }

      CRC32 crc = new CRC32();
      crc.update(sb.toString().getBytes(UTF_8));
      return crc.getValue() + "";
    }
  }

  interface Cache {
    <T extends Serializable> T load(String id, Class<T> type);
    <T extends Serializable> boolean write(String id, T object);
  }

  static class FileCache implements Cache {
    private final File dir;
    private final long validTime;

    FileCache(File dir, long validTime) {
      this.dir = dir;
      this.validTime = validTime;
    }

    @Override
    public <T extends Serializable> T load(String id, Class<T> type) {
      try {
        File file = new File(dir, id);
        if (!file.exists() || (validTime > 0 && file.lastModified() < new Date().getTime() - validTime)) {
          return null;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
          Object o = in.readObject();
          return o.getClass() == type ? (T) o : null;
        }
      } catch (IOException | ClassNotFoundException e) {
        return null;
      }
    }

    @Override
    public <T extends Serializable> boolean write(String id, T object) {
      try {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(dir, id)))) {
          out.writeObject(object);
          return true;
        }
      } catch (IOException e) {
        return false;
      }
    }
  }
}

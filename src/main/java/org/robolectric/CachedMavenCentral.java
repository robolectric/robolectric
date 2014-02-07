package org.robolectric;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

import org.apache.maven.model.Dependency;

class CachedMavenCentral implements MavenCentral {

  private final static String CACHE_PREFIX_1 = "localArtifactUrls";
  private final static String CACHE_PREFIX_2 = "localArtifactUrl";

  private final MavenCentral mavenCentral;
  private final CacheNamingStrategy cacheNamingStrategy;
  private final Cache cache;

  CachedMavenCentral(MavenCentral mavenCentral, File cacheDir, long cacheValidTime) {
    this(mavenCentral, new FileCache(cacheDir, cacheValidTime), new DefaultCacheNamingStrategy());
  }

  CachedMavenCentral(MavenCentral mavenCentral, Cache cache, CacheNamingStrategy cacheNamingStrategy) {
    this.mavenCentral = mavenCentral;
    this.cache = cache;
    this.cacheNamingStrategy = cacheNamingStrategy;
  }

  @Override
  public Map<String, URL> getLocalArtifactUrls(RobolectricTestRunner robolectricTestRunner, Dependency... dependencies) {

    String cacheName = cacheNamingStrategy.getName(CACHE_PREFIX_1, dependencies);

    HashMap<String, URL> urlsFromCache = cache.load(cacheName, HashMap.class);

    if(urlsFromCache != null) {
      return urlsFromCache;
    }

    Map<String, URL> urls = mavenCentral.getLocalArtifactUrls(robolectricTestRunner, dependencies);

    cache.write(cacheName, new HashMap<String, URL>(urls));

    return urls;
  }

  @Override
  public URL getLocalArtifactUrl(RobolectricTestRunner robolectricTestRunner, Dependency dependency) {

    String cacheName = cacheNamingStrategy.getName(CACHE_PREFIX_2, dependency);

    URL urlFromCache = cache.load(cacheName, URL.class);

    if(urlFromCache != null) {
      return urlFromCache;
    }

    URL url = mavenCentral.getLocalArtifactUrl(robolectricTestRunner, dependency);
    cache.write(cacheName, url);

    return url;
  }

  interface CacheNamingStrategy {
    String getName(String prefix, Dependency... dependencies);
  }

  static class DefaultCacheNamingStrategy implements CacheNamingStrategy {
    public String getName(String prefix, Dependency... dependencies) {
      StringBuilder sb = new StringBuilder();

      sb.append(prefix)
        .append("#");

      for(Dependency dependency : dependencies) {
        sb.append(dependency.getGroupId())
          .append(":")
          .append(dependency.getArtifactId())
          .append(":")
          .append(dependency.getVersion())
          .append(",");
      }

      CRC32 crc = new CRC32();
      crc.update(sb.toString().getBytes());
      return crc.getValue()+"";
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

        if(!file.exists() || validTime > 0 && file.lastModified() < new Date().getTime() - validTime) {
          return null;
        }

        ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));

        try {
          Object o = in.readObject();

          return o.getClass() == type ? (T) o : null;
        } finally {
          in.close();
        }
      } catch (FileNotFoundException e) {
        return null;
      } catch (IOException e) {
        return null;
      } catch (ClassNotFoundException e) {
        return null;
      }
    }

    @Override
    public <T extends Serializable> boolean write(String id, T object) {
      try {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(dir, id)));

        try {
          out.writeObject(object);
          return true;
        } finally {
          out.close();
        }
      } catch (FileNotFoundException e) {
        return false;
      } catch (IOException e) {
        return false;
      }
    }
  }
}

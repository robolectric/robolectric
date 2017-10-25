package org.robolectric.internal.dependency;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import android.os.Build;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.model.InitializationError;
import org.robolectric.internal.dependency.CachedDependencyResolver.Cache;
import org.robolectric.internal.dependency.CachedDependencyResolver.CacheNamingStrategy;
import org.robolectric.internal.dependency.CachedDependencyResolver.CacheValidationStrategy;

@RunWith(JUnit4.class)
public class CachedDependencyResolverTest {
  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private static final String CACHE_NAME = "someName";
  private DependencyResolver internalResolver = mock(DependencyResolver.class);
  private CacheNamingStrategy cacheNamingStrategy = new CacheNamingStrategy() {
    @Override
    public String getName(String prefix, String... dependencies) {
      return CACHE_NAME;
    }
  };
  private CacheValidationStrategy cacheValidationStrategy = new CacheValidationStrategy() {
    @Override
    public boolean isValid(URL url) {
      return true;
    }

    @Override
    public boolean isValid(URL[] urls) {
      return true;
    }
  };

  private URL url;
  private URL[] urls;
  private Cache cache = new CacheStub();
  private DependencyJar[] dependencies;
  private DependencyJar dependency;
  private final int apiLevel = Build.VERSION_CODES.N;

  private Properties depsProps;

  @Before
  public void setUp() throws InitializationError, MalformedURLException {
    urls = new URL[] { new URL("http://localhost") };
    url = new URL("http://localhost");
    depsProps = new Properties();
    dependencies = new DependencyJar[]{
        createDependency(Build.VERSION_CODES.N, "group1", "artifact1"),
        createDependency(Build.VERSION_CODES.M, "group2", "artifact2"),
    };
    dependency = dependencies[0];
  }

  @Test
  public void getLocalArtifactUrl_shouldWriteLocalArtifactUrlWhenCacheMiss() throws Exception{
    DependencyResolver res = createResolver();

    when(internalResolver.getLocalArtifactUrl(apiLevel)).thenReturn(url);

    URL url = res.getLocalArtifactUrl(apiLevel);

    assertEquals(this.url, url);
    assertCacheContents(url);
  }

  @Test
  public void getLocalArtifactUrl_shouldReadLocalArtifactUrlFromCacheIfExists() throws Exception {
    DependencyResolver res = createResolver();
    cache.write(CACHE_NAME, url);

    URL url = res.getLocalArtifactUrl(apiLevel);

    verify(internalResolver, never()).getLocalArtifactUrl(apiLevel);

    assertEquals(this.url, url);
  }

  @Test
  public void getLocalArtifactUrl_whenCacheInvalid_shouldFetchDependencyInformation() {
    CacheValidationStrategy failStrategy = mock(CacheValidationStrategy.class);
    when(failStrategy.isValid(any(URL.class))).thenReturn(false);

    DependencyResolver res = new CachedDependencyResolver(new DependencyProperties(depsProps), internalResolver, cache, cacheNamingStrategy, failStrategy);
    cache.write(CACHE_NAME, this.url);

    res.getLocalArtifactUrl(apiLevel);

    verify(internalResolver).getLocalArtifactUrl(apiLevel);
  }

  private void assertCacheContents(URL[] urls) {
    assertArrayEquals(urls, cache.load(CACHE_NAME, URL[].class));
  }

  private void assertCacheContents(URL url) {
    assertEquals(url, cache.load(CACHE_NAME, URL.class));
  }

  private DependencyResolver createResolver() {
    return new CachedDependencyResolver(new DependencyProperties(depsProps), internalResolver, cache, cacheNamingStrategy, cacheValidationStrategy);
  }

  private DependencyJar createDependency(final int apiLevel, final String groupId, final String artifactId) {
    DependencyJar dependencyJar = new DependencyJar(groupId, artifactId, null, "") {

      @Override
      public boolean equals(Object o) {
        if(!(o instanceof DependencyJar)) return false;

        DependencyJar d = (DependencyJar) o;

        return this.getArtifactId().equals(d.getArtifactId()) && this.getGroupId().equals(groupId);
      }
    };
    depsProps.setProperty(Integer.toString(apiLevel), dependencyJar.getShortName());
    return dependencyJar;
  }

  private static class CacheStub implements CachedDependencyResolver.Cache {
    private Map<String, Serializable> map = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Serializable> T load(String id, Class<T> type) {
      Serializable o = map.get(id);

      return o != null && o.getClass() == type ? (T) o : null;
    }

    @Override
    public <T extends Serializable> boolean write(String id, T object) {
      map.put(id, object);
      return true;
    }
  }
}

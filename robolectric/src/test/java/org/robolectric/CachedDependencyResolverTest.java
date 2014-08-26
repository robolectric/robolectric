package org.robolectric;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.model.Dependency;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.InitializationError;
import org.robolectric.CachedDependencyResolver.Cache;
import org.robolectric.CachedDependencyResolver.CacheNamingStrategy;
import org.robolectric.test.TemporaryFolder;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class CachedDependencyResolverTest {

  private static final String CACHE_NAME = "someName";

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private DependencyResolver internalResolver = mock(DependencyResolver.class);
  private CacheNamingStrategy cacheNamingStrategy = new CacheNamingStrategy() {
    @Override
    public String getName(String prefix, Dependency... dependencies) {
      return CACHE_NAME;
    }
  };

  private URL[] urls;
  private Cache cache = new CacheStub();
  private Dependency[] dependencies = new Dependency[]{
      createDependency("group1", "artifact1"),
      createDependency("group2", "artifact2"),
  };
  private Dependency dependency = dependencies[0];
  private URL url;

  @Before
  public void setUp() throws InitializationError, MalformedURLException {
    urls = new URL[] { new URL("http://localhost") };
    url = new URL("http://localhost");
  }

  @Test
  public void shouldWriteLocalArtifactsUrlsWhenCacheMiss() throws Exception {
    DependencyResolver res = createResolver();

    when(internalResolver.getLocalArtifactUrls(dependencies)).thenReturn(urls);

    URL[] urls = res.getLocalArtifactUrls(dependencies);

    assertArrayEquals(this.urls, urls);
    assertCacheContents(urls);
  }

  @Test
  public void shouldReadLocalArtifactUrlsFromCacheIfExists() throws Exception {

    DependencyResolver res = createResolver();

    cache.write(CACHE_NAME, urls);

    URL[] urls = res.getLocalArtifactUrls(dependencies);

    verify(internalResolver, never()).getLocalArtifactUrls(dependencies);

    assertArrayEquals(this.urls, urls);
  }

  @Test
  public void shouldWriteLocalArtifactUrlWhenCacheMiss() throws Exception{
    DependencyResolver res = createResolver();

    when(internalResolver.getLocalArtifactUrl(dependency)).thenReturn(url);

    URL url = res.getLocalArtifactUrl(dependency);

    assertEquals(this.url, url);
    assertCacheContents(url);
  }

  @Test
  public void shouldReadLocalArtifactUrlFromCacheIfExists() throws Exception {
    DependencyResolver res = createResolver();

    cache.write(CACHE_NAME, url);

    URL url = res.getLocalArtifactUrl(dependency);

    verify(internalResolver, never()).getLocalArtifactUrl(dependency);

    assertEquals(this.url, url);
  }

  private void assertCacheContents(URL[] urls) {
    assertArrayEquals(urls, cache.load(CACHE_NAME, URL[].class));
  }

  private void assertCacheContents(URL url) {
    assertEquals(url, cache.load(CACHE_NAME, URL.class));
  }

  private DependencyResolver createResolver() {
    return new CachedDependencyResolver(internalResolver, cache, cacheNamingStrategy);
  }

  private Dependency createDependency(final String groupId, final String artifactId) {
    return new Dependency(){
      {
        setGroupId(groupId);
        setArtifactId(artifactId);
      }

      @Override
      public boolean equals(Object o) {
        if(!(o instanceof Dependency)) return false;

        Dependency d = (Dependency) o;

        return this.getArtifactId().equals(d.getArtifactId()) && this.getGroupId().equals(groupId);
      }
    };
  }

  private static class CacheStub implements CachedDependencyResolver.Cache {

    private Map<String, Serializable> map = new HashMap<String, Serializable>();

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

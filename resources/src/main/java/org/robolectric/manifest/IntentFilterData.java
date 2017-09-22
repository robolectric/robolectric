package org.robolectric.manifest;

import java.util.ArrayList;
import java.util.List;

public class IntentFilterData {
  private final List<String> actions;
  private final List<String> categories;
  private final List<String> schemes;
  private final List<String> mimeTypes;
  private final List<DataAuthority> authorities;
  private final List<String> paths;
  private final List<String> pathPatterns;
  private final List<String> pathPrefixes;

  public IntentFilterData(List<String> actions, List<String> categories) {
    this.actions = actions;
    this.categories = new ArrayList<>(categories);
    this.schemes = new ArrayList<>();
    this.mimeTypes = new ArrayList<>();
    this.authorities = new ArrayList<>();
    this.paths = new ArrayList<>();
    this.pathPatterns = new ArrayList<>();
    this.pathPrefixes = new ArrayList<>();
  }

  public List<String> getActions() {
    return actions;
  }

  public List<String> getCategories() {
    return categories;
  }

  public List<String> getSchemes() {
    return schemes;
  }

  public List<String> getMimeTypes() {
    return mimeTypes;
  }

  public List<DataAuthority> getAuthorities() {
    return authorities;
  }

  public List<String> getPaths() {
    return paths;
  }

  public List<String> getPathPatterns() {
    return pathPatterns;
  }

  public List<String> getPathPrefixes() {
    return pathPrefixes;
  }

  public void addScheme(String scheme) {
    if (scheme != null) {
      schemes.add(scheme);
    }
  }

  public void addMimeType(String mimeType) {
    if (mimeType != null) {
      mimeTypes.add(mimeType);
    }
  }

  public void addPath(String path) {
    if (path != null) {
      paths.add(path);
    }
  }

  public void addPathPattern(String pathPattern) {
    if (pathPattern != null) {
      pathPatterns.add(pathPattern);
    }
  }

  public void addPathPrefix(String pathPrefix) {
    if (pathPrefix != null) {
      pathPrefixes.add(pathPrefix);
    }
  }

  public void addAuthority(String host, String port) {
    if (host != null) {
      authorities.add(new DataAuthority(host, port));
    }
  }

  public static class DataAuthority {
    private String host;
    private String port;

    public DataAuthority(String host, String port) {
      this.host = host;
      this.port = port;
    }

    public String getHost() {
      return host;
    }

    public String getPort() {
      return port;
    }
  }
}

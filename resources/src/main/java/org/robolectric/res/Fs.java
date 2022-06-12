package org.robolectric.res;

import com.google.errorprone.annotations.InlineMe;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.util.Util;

@SuppressWarnings({"NewApi", "AndroidJdkLibsChecker"})
abstract public class Fs {

  @GuardedBy("ZIP_FILESYSTEMS")
  private static final Map<Path, FsWrapper> ZIP_FILESYSTEMS = new HashMap<>();

  /**
   * @deprecated Use {@link File#toPath()} instead.
   */
  @Deprecated
  @InlineMe(replacement = "file.toPath()")
  public static Path newFile(File file) {
    return file.toPath();
  }

  /**
   * @deprecated Use {@link #fromUrl(String)} instead.
   */
  @Deprecated
  @InlineMe(replacement = "Fs.fromUrl(path)", imports = "org.robolectric.res.Fs")
  public static Path fileFromPath(String path) {
    return Fs.fromUrl(path);
  }

  public static FileSystem forJar(URL url) {
    return forJar(Paths.get(toUri(url)));
  }

  public static FileSystem forJar(Path jarFile) {
    try {
      return getJarFs(jarFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Use this method instead of {@link Paths#get(String, String...)} or {@link Paths#get(URI)}.
   *
   * <p>Supports "file:path", "jar:file:jarfile.jar!/path", and plain old paths.
   *
   * <p>For JAR files, automatically open and cache filesystems.
   */
  public static Path fromUrl(String urlString) {
    if (urlString.startsWith("file:") || urlString.startsWith("jar:")) {
      URL url;
      try {
        url = new URL(urlString);
      } catch (MalformedURLException e) {
        throw new RuntimeException("Failed to resolve path from " + urlString, e);
      }
      return fromUrl(url);
    } else {
      return Paths.get(urlString);
    }
  }

  /** Isn't this what {@link Paths#get(URI)} should do? */
  public static Path fromUrl(URL url) {
    try {
      switch (url.getProtocol()) {
        case "file":
          return Paths.get(url.toURI());
        case "jar":
          String[] parts = url.getPath().split("!", 0);
          Path jarFile = Paths.get(new URI(parts[0]).toURL().getFile());
          FileSystem fs = getJarFs(jarFile);
          return fs.getPath(parts[1].substring(1));
        default:
          throw new IllegalArgumentException("unsupported fs type for '" + url + "'");
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to resolve path from " + url, e);
    }
  }

  public static URI toUri(URL url) {
    try {
      return url.toURI();
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("invalid URL: " + url, e);
    }
  }

  static String baseNameFor(Path path) {
    String name = path.getFileName().toString();
    int dotIndex = name.indexOf(".");
    return dotIndex >= 0 ? name.substring(0, dotIndex) : name;
  }

  public static InputStream getInputStream(Path path) throws IOException {
    // otherwise we get ClosedByInterruptException, meh
    if (path.toUri().getScheme().equals("file")) {
      return new BufferedInputStream(new FileInputStream(path.toFile()));
    }
    return new BufferedInputStream(Files.newInputStream(path));
  }

  public static byte[] getBytes(Path path) throws IOException {
    return Util.readBytes(getInputStream(path));
  }

  public static Path[] listFiles(Path path) throws IOException {
    try (Stream<Path> list = Files.list(path)) {
      return list.toArray(Path[]::new);
    }
  }

  public static Path[] listFiles(Path path, final Predicate<Path> filter) throws IOException {
    try (Stream<Path> list = Files.list(path)) {
      return list.filter(filter).toArray(Path[]::new);
    }
  }

  public static String[] listFileNames(Path path) {
    File[] files = path.toFile().listFiles();
    if (files == null) return null;
    String[] strings = new String[files.length];
    for (int i = 0; i < files.length; i++) {
      strings[i] = files[i].getName();
    }
    return strings;
  }

  public static Path join(Path path, String... pathParts) {
    for (String pathPart : pathParts) {
      path = path.resolve(pathPart);
    }
    return path;
  }

  public static String externalize(Path path) {
    if (path.getFileSystem().provider().getScheme().equals("file")) {
      return path.toString();
    } else {
      return path.toUri().toString();
    }
  }

  /** Returns a reference-counted Jar FileSystem, possibly one that was previously returned. */
  private static FileSystem getJarFs(Path jarFile) throws IOException {
    Path key = jarFile.toAbsolutePath();

    synchronized (ZIP_FILESYSTEMS) {
      FsWrapper fs = ZIP_FILESYSTEMS.get(key);
      if (fs == null) {
        fs = new FsWrapper(FileSystems.newFileSystem(key, (ClassLoader) null), key);
        fs.incrRefCount();

        ZIP_FILESYSTEMS.put(key, fs);
      } else {
        fs.incrRefCount();
      }

      return fs;
    }
  }

  @SuppressWarnings("NewApi")
  private static class FsWrapper extends FileSystem {
    private final FileSystem delegate;
    private final Path jarFile;

    @GuardedBy("this")
    private int refCount;

    public FsWrapper(FileSystem delegate, Path jarFile) {
      this.delegate = delegate;
      this.jarFile = jarFile;
    }

    synchronized void incrRefCount() {
      refCount++;
    }

    synchronized void decrRefCount() throws IOException {
      if (--refCount == 0) {
        synchronized (ZIP_FILESYSTEMS) {
          ZIP_FILESYSTEMS.remove(jarFile);
        }
        delegate.close();
      }
    }

    @Override
    public FileSystemProvider provider() {
      return delegate.provider();
    }

    @Override
    public void close() throws IOException {
      decrRefCount();
    }

    @Override
    public boolean isOpen() {
      return delegate.isOpen();
    }

    @Override
    public boolean isReadOnly() {
      return delegate.isReadOnly();
    }

    @Override
    public String getSeparator() {
      return delegate.getSeparator();
    }

    @Override
    public Iterable<Path> getRootDirectories() {
      return delegate.getRootDirectories();
    }

    @Override
    public Iterable<FileStore> getFileStores() {
      return delegate.getFileStores();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
      return delegate.supportedFileAttributeViews();
    }

    @Override
    public Path getPath(String first, String... more) {
      return delegate.getPath(first, more);
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
      return delegate.getPathMatcher(syntaxAndPattern);
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
      return delegate.getUserPrincipalLookupService();
    }

    @Override
    public WatchService newWatchService() throws IOException {
      return delegate.newWatchService();
    }
  }
}

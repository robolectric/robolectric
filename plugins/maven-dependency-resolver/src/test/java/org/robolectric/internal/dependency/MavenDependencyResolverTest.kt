package org.robolectric.internal.dependency

import com.google.common.hash.Hashing
import com.google.common.io.Files
import com.google.common.truth.Truth.assertThat
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutorService
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MavenDependencyResolverTest {
  private lateinit var localRepositoryDir: File
  private lateinit var executorService: ExecutorService
  private lateinit var mavenDependencyResolver: MavenDependencyResolver
  private lateinit var mavenArtifactFetcher: TestMavenArtifactFetcher

  @Before
  @Throws(Exception::class)
  fun setUp() {
    executorService = MoreExecutors.newDirectExecutorService()
    localRepositoryDir = Files.createTempDir()
    localRepositoryDir.deleteOnExit()
    mavenArtifactFetcher =
      TestMavenArtifactFetcher(
        REPOSITORY_URL,
        REPOSITORY_USERNAME,
        REPOSITORY_PASSWORD,
        PROXY_HOST,
        PROXY_PORT,
        localRepositoryDir,
        executorService,
      )
    mavenDependencyResolver = TestMavenDependencyResolver()
  }

  @Throws(Exception::class)
  @Test
  fun localArtifactUrl_placesFilesCorrectlyForSingleURL() {
    val dependencyJar = successCases[0]
    mavenDependencyResolver.getLocalArtifactUrl(dependencyJar)
    assertThat(mavenArtifactFetcher.numRequests).isEqualTo(4)
    val artifact = MavenJarArtifact(dependencyJar)
    checkJarArtifact(artifact)
  }

  @Throws(Exception::class)
  @Test
  fun localArtifactUrl_placesFilesCorrectlyForMultipleURL() {
    mavenDependencyResolver.getLocalArtifactUrls(*successCases)
    assertThat(mavenArtifactFetcher.numRequests).isEqualTo(4 * successCases.size)
    for (dependencyJar in successCases) {
      val artifact = MavenJarArtifact(dependencyJar)
      checkJarArtifact(artifact)
    }
  }

  /** Checks the case where the existing artifact directory is valid. */
  @Throws(Exception::class)
  @Test
  fun localArtifactUrl_handlesExistingArtifactDirectory() {
    val dependencyJar = DependencyJar("group", "artifact", "1")
    val mavenJarArtifact = MavenJarArtifact(dependencyJar)
    val jarFile = File(localRepositoryDir, mavenJarArtifact.jarPath())
    Files.createParentDirs(jarFile)
    assertThat(jarFile.parentFile.isDirectory).isTrue()
    mavenDependencyResolver.getLocalArtifactUrl(dependencyJar)
    checkJarArtifact(mavenJarArtifact)
  }

  /**
   * Checks the case where there is some existing artifact metadata in the artifact directory, but
   * not the JAR.
   */
  @Throws(Exception::class)
  @Test
  fun localArtifactUrl_handlesExistingMetadataFile() {
    val dependencyJar = DependencyJar("group", "artifact", "1")
    val mavenJarArtifact = MavenJarArtifact(dependencyJar)
    val pomFile = File(localRepositoryDir, mavenJarArtifact.pomPath())
    pomFile.parentFile.mkdirs()
    Files.write(ByteArray(0), pomFile)
    assertThat(pomFile.exists()).isTrue()
    mavenDependencyResolver.getLocalArtifactUrl(dependencyJar)
    checkJarArtifact(mavenJarArtifact)
  }

  @Throws(Exception::class)
  private fun checkJarArtifact(artifact: MavenJarArtifact) {
    val jar = File(localRepositoryDir, artifact.jarPath())
    val pom = File(localRepositoryDir, artifact.pomPath())
    val jarSha512 = File(localRepositoryDir, artifact.jarSha512Path())
    val pomSha512 = File(localRepositoryDir, artifact.pomSha512Path())
    assertThat(jar.exists()).isTrue()
    assertThat(readFile(jar)).isEqualTo("$artifact jar contents")
    assertThat(pom.exists()).isTrue()
    assertThat(readFile(pom)).isEqualTo("$artifact pom contents")
    assertThat(jarSha512.exists()).isTrue()
    assertThat(readFile(jarSha512)).isEqualTo(sha512("$artifact jar contents"))
    assertThat(pom.exists()).isTrue()
    assertThat(readFile(pomSha512)).isEqualTo(sha512("$artifact pom contents"))
  }

  @Throws(Exception::class)
  @Test
  fun localArtifactUrl_doesNotFetchWhenArtifactsExist() {
    val dependencyJar = DependencyJar("group", "artifact", "1")
    val mavenJarArtifact = MavenJarArtifact(dependencyJar)
    val artifactFile = File(localRepositoryDir, mavenJarArtifact.jarPath())
    artifactFile.parentFile.mkdirs()
    Files.write(ByteArray(0), artifactFile)
    assertThat(artifactFile.exists()).isTrue()
    mavenDependencyResolver.getLocalArtifactUrl(dependencyJar)
    assertThat(mavenArtifactFetcher.numRequests).isEqualTo(0)
  }

  @Throws(Exception::class)
  @Test
  fun localArtifactUrl_handlesFileNotFound() {
    val dependencyJar = DependencyJar("group", "missing-artifact", "1")
    Assert.assertThrows(AssertionError::class.java) {
      mavenDependencyResolver.getLocalArtifactUrl(dependencyJar)
    }
  }

  @Throws(Exception::class)
  @Test
  fun localArtifactUrl_handlesInvalidSha512() {
    val dependencyJar = DependencyJar("group", "artifact-invalid-sha512", "1")
    addTestArtifactInvalidSha512(dependencyJar)
    Assert.assertThrows(AssertionError::class.java) {
      mavenDependencyResolver.getLocalArtifactUrl(dependencyJar)
    }
  }

  internal inner class TestMavenDependencyResolver : MavenDependencyResolver() {
    override fun createMavenFetcher(
      repositoryUrl: String?,
      repositoryUserName: String?,
      repositoryPassword: String?,
      proxyHost: String?,
      proxyPort: Int,
      localRepositoryDir: File,
      executorService: ExecutorService,
    ): MavenArtifactFetcher {
      return mavenArtifactFetcher
    }

    override fun createExecutorService(): ExecutorService {
      return executorService
    }

    override fun createLockFile(): File {
      return try {
        File.createTempFile("MavenDependencyResolverTest", null)
      } catch (e: IOException) {
        throw AssertionError(e)
      }
    }
  }

  @Suppress("LongParameterList")
  internal class TestMavenArtifactFetcher(
    repositoryUrl: String?,
    repositoryUserName: String?,
    repositoryPassword: String?,
    proxyHost: String?,
    proxyPort: Int,
    localRepositoryDir: File,
    private val executorService: ExecutorService,
  ) :
    MavenArtifactFetcher(
      repositoryUrl,
      repositoryUserName,
      repositoryPassword,
      proxyHost,
      proxyPort,
      localRepositoryDir,
      executorService,
    ) {
    var numRequests = 0
      private set

    override fun createFetchToFileTask(remoteUrl: URL, tempFile: File): ListenableFuture<Void> {
      return Futures.submitAsync(
        object : FetchToFileTask(remoteUrl, tempFile, null, null, null, 0) {
          @Throws(Exception::class)
          override fun call(): ListenableFuture<Void> {
            numRequests += 1
            return super.call()
          }
        },
        executorService,
      )
    }
  }

  companion object {
    private var REPOSITORY_DIR: File
    private var REPOSITORY_URL: String
    private const val REPOSITORY_USERNAME = "username"
    private const val REPOSITORY_PASSWORD = "password"
    private const val PROXY_HOST = "123.4.5.678"
    private const val PROXY_PORT = 9000
    private val SHA512 = Hashing.sha512()
    private val successCases =
      arrayOf(
        DependencyJar("group", "artifact", "1"),
        DependencyJar("org.group2", "artifact2-name", "2.4.5"),
        DependencyJar("org.robolectric", "android-all", "10-robolectric-5803371"),
      )

    init {
      try {
        REPOSITORY_DIR = Files.createTempDir()
        REPOSITORY_DIR.deleteOnExit()
        REPOSITORY_URL = REPOSITORY_DIR.toURI().toURL().toString()
        for (dependencyJar in successCases) {
          addTestArtifact(dependencyJar)
        }
      } catch (e: Exception) {
        throw AssertionError(e)
      }
    }

    @Throws(IOException::class)
    fun addTestArtifact(dependencyJar: DependencyJar?) {
      val mavenJarArtifact = MavenJarArtifact(dependencyJar)
      try {
        Files.createParentDirs(File(REPOSITORY_DIR, mavenJarArtifact.jarPath()))
        val jarContents = "$mavenJarArtifact jar contents"
        Files.write(
          jarContents.toByteArray(StandardCharsets.UTF_8),
          File(REPOSITORY_DIR, mavenJarArtifact.jarPath()),
        )
        Files.write(
          sha512(jarContents).toByteArray(),
          File(REPOSITORY_DIR, mavenJarArtifact.jarSha512Path()),
        )
        val pomContents = "$mavenJarArtifact pom contents"
        Files.write(
          pomContents.toByteArray(StandardCharsets.UTF_8),
          File(REPOSITORY_DIR, mavenJarArtifact.pomPath()),
        )
        Files.write(
          sha512(pomContents).toByteArray(),
          File(REPOSITORY_DIR, mavenJarArtifact.pomSha512Path()),
        )
      } catch (e: MalformedURLException) {
        throw AssertionError(e)
      }
    }

    @Throws(IOException::class)
    fun addTestArtifactInvalidSha512(dependencyJar: DependencyJar?) {
      val mavenJarArtifact = MavenJarArtifact(dependencyJar)
      try {
        Files.createParentDirs(File(REPOSITORY_DIR, mavenJarArtifact.jarPath()))
        val jarContents = "$mavenJarArtifact jar contents"
        Files.write(jarContents.toByteArray(), File(REPOSITORY_DIR, mavenJarArtifact.jarPath()))
        Files.write(
          sha512("No the same content").toByteArray(),
          File(REPOSITORY_DIR, mavenJarArtifact.jarSha512Path()),
        )
        val pomContents = "$mavenJarArtifact pom contents"
        Files.write(pomContents.toByteArray(), File(REPOSITORY_DIR, mavenJarArtifact.pomPath()))
        Files.write(
          sha512("Really not the same content").toByteArray(),
          File(REPOSITORY_DIR, mavenJarArtifact.pomSha512Path()),
        )
      } catch (e: MalformedURLException) {
        throw AssertionError(e)
      }
    }

    fun sha512(contents: String): String {
      return SHA512.hashString(contents, StandardCharsets.UTF_8).toString()
    }

    @Throws(IOException::class)
    fun readFile(file: File): String {
      return String(Files.asByteSource(file).read(), StandardCharsets.UTF_8)
    }
  }
}

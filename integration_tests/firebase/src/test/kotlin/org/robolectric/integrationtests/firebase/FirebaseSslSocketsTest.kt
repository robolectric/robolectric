package org.robolectric.integrationtests.firebase

import android.net.ssl.SSLSockets
import android.os.Build.VERSION_CODES.Q
import com.google.common.truth.Truth.assertThat
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import org.conscrypt.OpenSSLProvider
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Firebase Firestore reaches its backend through gRPC's OkHttp transport. Its
 * `io.grpc.okhttp.OkHttpProtocolNegotiator$AndroidNegotiator` decides whether to enable TLS session
 * tickets by *reflectively* resolving [android.net.ssl.SSLSockets] in its static initializer
 * (`Class.forName` + `getMethod`); there is no `Build.VERSION.SDK_INT` check. When the class
 * resolves, it calls [android.net.ssl.SSLSockets.setUseSessionTickets], which in turn calls
 * [android.net.ssl.SSLSockets.isSupportedSocket].
 *
 * [SSLSockets] has existed since API 29. Under a standard [RobolectricTestRunner] (real android-all
 * on the classpath) at API 29+, that reflection resolves to the genuine instrumented android-all
 * class and the path works — which is what these tests assert.
 *
 * The "Method isSupportedSocket in android.net.ssl.SSLSockets not mocked" failure arises in a
 * different setup: the Robolectric runtime SDK is below 29 (so android-all has no [SSLSockets])
 * while an AGP "mockable" `android.jar` compiled against a newer SDK sits on the classpath.
 * Robolectric's class loader then falls back to that stub, so gRPC's reflective probe succeeds
 * against a "not mocked" method and invokes it. This module does not reproduce that stub leak; it
 * guards the android-all path instead.
 */
@RunWith(RobolectricTestRunner::class)
class FirebaseSslSocketsTest {

  /**
   * At API 29+, gRPC's reflective probe must resolve to the real android-all class, not the stub.
   */
  @Test
  @Config(sdk = [Q, Config.NEWEST_SDK])
  fun isSupportedSocket_runsRealImplementation() {
    newSslSocket().use { socket ->
      // A plain conscrypt-openjdk socket is not an Android conscrypt socket, so the real
      // android-all
      // implementation returns false. The point is that it executes at all rather than throwing the
      // "...not mocked" RuntimeException from the android.jar stub.
      assertThat(SSLSockets.isSupportedSocket(socket)).isFalse()
    }
  }

  /**
   * gRPC calls [SSLSockets.setUseSessionTickets]; for an unsupported socket the real implementation
   * throws [IllegalArgumentException] (not the "not mocked" stub error), proving android-all code
   * is what runs.
   */
  @Test
  @Config(sdk = [Q, Config.NEWEST_SDK])
  fun setUseSessionTickets_unsupportedSocket_throwsIllegalArgument() {
    newSslSocket().use { socket ->
      assertThrows(IllegalArgumentException::class.java) {
        SSLSockets.setUseSessionTickets(socket, true)
      }
    }
  }

  companion object {
    // Conscrypt calls into native libraries and, per the JNI spec, can only be loaded once.
    // Robolectric loads non-instrumented classes such as org.conscrypt.* with the shared system
    // classloader, so a single provider instance is reused across SDK sandboxes. See
    // SecurityProvidersTest for context.
    private val CONSCRYPT_PROVIDER = OpenSSLProvider()

    private val SSL_CONTEXT by lazy {
      SSLContext.getInstance("TLS", CONSCRYPT_PROVIDER).apply { init(null, null, null) }
    }

    private fun newSslSocket(): SSLSocket {
      return SSL_CONTEXT.socketFactory.createSocket() as SSLSocket
    }
  }
}

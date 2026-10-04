package org.robolectric.kotest

import android.app.Application
import android.os.Build
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.robolectric.annotation.Config

/** Tests that a spec is created, and its tests run, in the Android environment. */
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [34], qualifiers = "fr")
class RobolectricExtensionTest :
  FunSpec({
    val application = ApplicationProvider.getApplicationContext<Application>()
    val creationThread = Thread.currentThread()

    test("the body of the spec runs in the Android environment") {
      application.packageName.shouldNotBeEmpty()
    }

    test("a test runs on the configured SDK") { Build.VERSION.SDK_INT shouldBe 34 }

    test("a test runs with the configuration of the spec") {
      application.resources.configuration.locales[0].language shouldBe "fr"
    }

    test("a test runs on Android's main thread, as the body of the spec does") {
      Looper.myLooper() shouldBeSameInstanceAs Looper.getMainLooper()
      Thread.currentThread() shouldBeSameInstanceAs creationThread
    }

    test("a test runs in the environment that the spec was created in") {
      ApplicationProvider.getApplicationContext<Application>() shouldBeSameInstanceAs application
    }

    context("a container") {
      Looper.myLooper() shouldBeSameInstanceAs Looper.getMainLooper()

      test("runs its tests in the environment too") {
        Looper.myLooper() shouldBeSameInstanceAs Looper.getMainLooper()
      }
    }
  })

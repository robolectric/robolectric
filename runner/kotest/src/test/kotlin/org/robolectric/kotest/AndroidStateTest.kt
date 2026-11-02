package org.robolectric.kotest

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.core.extensions.ApplyExtension
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.robolectric.annotation.Config

private fun preferences() =
  ApplicationProvider.getApplicationContext<Application>()
    .getSharedPreferences("state", Context.MODE_PRIVATE)

/** Tests that the tests of a spec instance share its Android state. */
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [34])
class SharedAndroidStateTest :
  FunSpec({
    test("the first test changes the state of the application") {
      preferences().edit().putString("written", "by the first test").commit()
    }

    test("the second test sees it") {
      preferences().getString("written", null) shouldBe "by the first test"
    }
  })

/** Tests that each spec instance has an Android environment of its own. */
@ApplyExtension(RobolectricExtension::class)
@Config(sdk = [34])
class AndroidStatePerRootTest :
  FunSpec({
    isolationMode = IsolationMode.InstancePerRoot

    test("the first test changes the state of the application") {
      preferences().edit().putString("written", "by the first test").commit()
    }

    test("the second test runs in a spec, and so an application, of its own") {
      preferences().getString("written", null) shouldBe null
    }

    context("the tests of a container") {
      test("the first one changes the state") {
        preferences().edit().putString("written", "in the container").commit()
      }

      test("the second one sees it, as they share the instance") {
        preferences().getString("written", null) shouldBe "in the container"
      }
    }
  })

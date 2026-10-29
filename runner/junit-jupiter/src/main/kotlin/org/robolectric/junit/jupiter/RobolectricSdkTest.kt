package org.robolectric.junit.jupiter

import org.junit.jupiter.api.ClassTemplate
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.robolectric.junit.jupiter.internal.SdkTestInvocationContextProvider

/**
 * Reports a test, or all tests of a class, once for each Android SDK that it runs on, in a class
 * that uses [RobolectricExtension], where a `@Test` reports all its SDKs as one test.
 *
 * On a test method, it is used in place of `@Test`:
 * ```
 * @RobolectricSdkTest
 * @Config(sdk = [33, 34])
 * fun worksOnBothSdks() {
 *   assertThat(Build.VERSION.SDK_INT).isAnyOf(33, 34)
 * }
 * ```
 *
 * The runs are named as `RobolectricTestRunner` names them: `worksOnBothSdks[33]` and, for the last
 * SDK, `worksOnBothSdks`.
 *
 * On a class, it makes the class run once for each SDK that is selected for its tests, as `SDK 33`
 * and `SDK 34`, each with the tests that are configured for that SDK.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@TestTemplate
@ClassTemplate
@ExtendWith(SdkTestInvocationContextProvider::class)
public annotation class RobolectricSdkTest

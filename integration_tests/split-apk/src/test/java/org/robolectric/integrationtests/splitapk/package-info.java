/**
 * Integration tests for Android App Bundle (AAB) split APK support in Robolectric.
 *
 * <p>These tests are modeled after the official Android App Bundle samples from
 * https://github.com/android/app-bundle-samples/tree/main/DynamicFeatures
 *
 * <p>The DynamicFeatures sample demonstrates:
 *
 * <ul>
 *   <li>{@code MyApplication} - SplitCompat initialization in attachBaseContext()
 *   <li>{@code MainActivity} - SplitInstallManager for on-demand feature delivery
 *   <li>{@code BaseSplitActivity} - SplitCompat.installActivity() for feature activities
 *   <li>On-demand features: kotlin, java, native, assets, maxSdk
 *   <li>Install-time feature: initialInstall
 *   <li>Asset-only module: assets (android:hasCode="false")
 *   <li>Config splits: density, ABI, language
 * </ul>
 *
 * <p>Since Robolectric tests run on the JVM (not on a real device with Google Play), we simulate
 * the same patterns using Robolectric's shadow APIs. These tests verify that the split APK
 * infrastructure works correctly for the same scenarios the official sample targets.
 */
package org.robolectric.integrationtests.splitapk;

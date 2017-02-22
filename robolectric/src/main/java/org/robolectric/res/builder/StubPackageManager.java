package org.robolectric.res.builder;

/**
 * @deprecated Use {@link org.robolectric.shadows.ShadowPackageManager} instead.
 * <pre>
 *   ShadowPackageManager shadowPackageManager = shadowOf(context.getPackageManager());
 * </pre>
 *
 * If there is functionality you are missing you can extend ShadowPackageManager.
 */
@Deprecated
public class StubPackageManager extends org.robolectric.android.StubPackageManager {
}

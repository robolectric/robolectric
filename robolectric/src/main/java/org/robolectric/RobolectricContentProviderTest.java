/*
 * Copyright (c) 2015. by Rainer Burgstaller, All rights reserved.
 *
 * This software is the confidential and proprietary information of Rainer Burgstaller ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only in accordance with the terms of the license agreement you entered into with Rainer Burgstaller.
 */

package org.robolectric;

import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;

import org.junit.Before;
import org.robolectric.shadows.ShadowContentResolver;

/**
 * A Test Baseclass to support Content Provider Testing. Use it as a drop in replacement for
 * {@code ProviderTestCase2}.
 * <p>
 * To use annotate your test class as follows:
 * <pre>
 * {@code
 *     @RunWith(RobolectricGradleTestRunner.class)
 *     @Config(constants = BuildConfig.class)
 *     public class MyTest extends RobolectricContentProviderTest {
 *         ...
 *     }
 * }
 * </pre>
 * </p>
 *
 * @see <a href="http://developer.android.com/tools/testing/contentprovider_testing.html">
 * Android Content Provider testing</a>
 */
public class RobolectricContentProviderTest {
  private final Class<? extends ContentProvider> m_contentProviderClass;
  private final String m_authority;

  private ContentProvider m_contentProvider;
  private Application m_context;
  private ContentResolver m_contentResolver;
  private ShadowContentResolver m_shadowContentResolver;

  public RobolectricContentProviderTest(final Class<? extends ContentProvider> providerClass, final String authority) {
    m_contentProviderClass = providerClass;
    m_authority = authority;
  }

  @Before
  public void setupContentProviderTest() throws IllegalAccessException, InstantiationException {
    m_contentProvider = m_contentProviderClass.newInstance();
    m_context = RuntimeEnvironment.application;

    m_contentResolver = m_context.getContentResolver();

    m_shadowContentResolver = Shadows.shadowOf(m_contentResolver);
    m_contentProvider.onCreate();
    ShadowContentResolver.registerProvider(m_authority, m_contentProvider);
  }


  public Context getMockContext() {
    return m_context;
  }

  public ContentResolver getMockContentResolver() {
    return m_contentResolver;
  }
}

package org.robolectric.shadows.support.v4;

import static org.robolectric.internal.Shadow.directlyOn;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import org.robolectric.Robolectric;
import org.robolectric.ShadowsAdapter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowViewGroup;

@Implements(ViewPager.class)
public class ShadowViewPager extends ShadowViewGroup {
  @RealObject ViewPager realViewPager;

  @Implementation
  public void setAdapter(final PagerAdapter adapter) {
    ShadowsAdapter shadowsAdapter = Robolectric.getShadowsAdapter();
    shadowsAdapter
        .getMainLooper()
        .runPaused(() -> directlyOn(realViewPager, ViewPager.class).setAdapter(adapter));
  }

  @Implementation
  public void setOffscreenPageLimit(final int limit) {
    ShadowsAdapter shadowsAdapter = Robolectric.getShadowsAdapter();
    shadowsAdapter
        .getMainLooper()
        .runPaused(() -> directlyOn(realViewPager, ViewPager.class).setOffscreenPageLimit(limit));
  }
}

package org.robolectric.shadows;

import android.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(TabHost.class)
public class ShadowTabHost extends ShadowFrameLayout {
  private List<TabHost.TabSpec> tabSpecs = new ArrayList<>();
  private TabHost.OnTabChangeListener listener;
  private int currentTab = -1;

  @RealObject
  private TabHost realObject;

  @Implementation
  public android.widget.TabHost.TabSpec newTabSpec(java.lang.String tag) {
    TabSpec realTabSpec = Shadow.newInstanceOf(TabHost.TabSpec.class);
    Shadows.shadowOf(realTabSpec).setTag(tag);
    return realTabSpec;
  }

  @Implementation
  public void addTab(android.widget.TabHost.TabSpec tabSpec) {
    tabSpecs.add(tabSpec);
    View indicatorAsView = Shadows.shadowOf(tabSpec).getIndicatorAsView();
    if (indicatorAsView != null) {
      realObject.addView(indicatorAsView);
    }
  }

  @Implementation
  public void setCurrentTab(int index) {
    currentTab = index;
    if (listener != null) {
      listener.onTabChanged(getCurrentTabTag());
    }
  }

  @Implementation
  public void setCurrentTabByTag(String tag) {
    for (int x = 0; x < tabSpecs.size(); x++) {
      TabSpec tabSpec = tabSpecs.get(x);
      if (tabSpec.getTag().equals(tag)) {
        currentTab = x;
      }
    }
    if (listener != null) {
      listener.onTabChanged(getCurrentTabTag());
    }
  }

  @Implementation
  public int getCurrentTab() {
    if (currentTab == -1 && tabSpecs.size() > 0) currentTab = 0;
    return currentTab;
  }

  public TabSpec getCurrentTabSpec() {
    return tabSpecs.get(getCurrentTab());
  }

  @Implementation
  public String getCurrentTabTag() {
    int i = getCurrentTab();
    if (i >= 0 && i < tabSpecs.size()) {
      return tabSpecs.get(i).getTag();
    }
    return null;
  }

  @Implementation
  public void setOnTabChangedListener(android.widget.TabHost.OnTabChangeListener listener) {
    this.listener = listener;
  }

  @Implementation
  public View getCurrentView() {
    ShadowTabSpec ts = Shadows.shadowOf(getCurrentTabSpec());
    View v = ts.getContentView();
    if (v == null) {
      int viewId = ts.getContentViewId();
      if (realView.getContext() instanceof Activity) {
        v = ((Activity) realView.getContext()).findViewById(viewId);
      } else {
        return null;
      }
    }
    return v;
  }

  @Implementation
  public TabWidget getTabWidget() {
    Context context = realView.getContext();
    if (context instanceof Activity) {
      return (TabWidget) ((Activity)context).findViewById(R.id.tabs);
    } else {
      return null;
    }
  }

  public TabHost.TabSpec getSpecByTag(String tag) {
    for (TabHost.TabSpec tabSpec : tabSpecs) {
      if (tag.equals(tabSpec.getTag())) {
        return tabSpec;
      }
    }
    return null;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  @Implements(TabSpec.class)
  public static class ShadowTabSpec {

    @RealObject
    TabSpec realObject;
    private String tag;
    private View indicatorView;
    private Intent intent;
    private int viewId;
    private View contentView;
    private CharSequence label;
    private Drawable icon;

    /**
     * Sets the tag on the TabSpec.
     *
     * @param tag The tag.
     */
    public void setTag(String tag) {
      this.tag = tag;
    }

    @Implementation
    public String getTag() {
      return tag;
    }

    /**
     * @return the view object set in a call to {@code TabSpec#setIndicator(View)}
     */
    public View getIndicatorAsView() {
      return this.indicatorView;
    }

    public String getIndicatorLabel() {
      return this.label.toString();
    }

    public Drawable getIndicatorIcon() {
      return this.icon;
    }

    /**
     * Same as GetIndicatorLabel()
     *
     * @return Tab text.
     */
    public String getText() {
      return label.toString();
    }

    @Implementation
    public TabSpec setIndicator(View view) {
      this.indicatorView = view;
      return realObject;
    }

    @Implementation
    public TabSpec setIndicator(CharSequence label) {
      this.label = label;
      return realObject;
    }

    @Implementation
    public TabSpec setIndicator(CharSequence label, Drawable icon) {
      this.label = label;
      this.icon = icon;
      return realObject;
    }

    /**
     * @return the intent object set in a call to {@code TabSpec#setContent(Intent)}
     */
    public Intent getContentAsIntent() {
      return intent;
    }

    @Implementation
    public TabSpec setContent(Intent intent) {
      this.intent = intent;
      return realObject;
    }

    @Implementation
    public TabSpec setContent(TabHost.TabContentFactory factory) {
      contentView = factory.createTabContent(this.tag);
      return realObject;
    }

    @Implementation
    public TabSpec setContent(int viewId) {
      this.viewId = viewId;
      return realObject;
    }

    public int getContentViewId() {
      return viewId;
    }

    public View getContentView() {
      return contentView;
    }
  }
}

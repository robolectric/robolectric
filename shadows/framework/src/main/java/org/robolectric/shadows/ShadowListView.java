package org.robolectric.shadows;

import android.view.View;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(ListView.class)
public class ShadowListView extends ShadowAbsListView {
  @RealObject private ListView realListView;

  public List<View> getHeaderViews() {
    HeaderViewListAdapter adapter = (HeaderViewListAdapter) realListView.getAdapter();
    ArrayList<View> headerViews = new ArrayList<>();
    int headersCount = adapter.getHeadersCount();
    for (int i = 0; i < headersCount; i++) {
      headerViews.add(adapter.getView(i, null, realListView));
    }
    return headerViews;
  }

  public List<View> getFooterViews() {
    HeaderViewListAdapter adapter = (HeaderViewListAdapter) realListView.getAdapter();
    ArrayList<View> footerViews = new ArrayList<>();
    int offset = adapter.getHeadersCount() + adapter.getCount() - adapter.getFootersCount();
    int itemCount = adapter.getCount();
    for (int i = offset; i < itemCount; i++) {
      footerViews.add(adapter.getView(i, null, realListView));
    }
    return footerViews;
  }
}

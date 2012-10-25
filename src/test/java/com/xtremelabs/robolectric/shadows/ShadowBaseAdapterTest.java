package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.database.DataSetObserver;

import org.junit.Before;
import org.junit.Test;

public class ShadowBaseAdapterTest {
  private ShadowBaseAdapter mAdapter;

  @Before
  public void setUp() {
    mAdapter = new ShadowBaseAdapter();
  }

  @Test
  public void testGetViewTypeCount() {
    assertThat(mAdapter.getViewTypeCount(), is(1));
  }

  @Test
  public void testIsEnabled() {
    assertThat(mAdapter.isEnabled(0), is(true));
  }

  @Test
  public void testAreAllItemsEnabled() {
    assertThat(mAdapter.areAllItemsEnabled(), is(true));
  }

  @Test
  public void testDataSetObserver() {
    DataSetObserver mockObserver = mock(DataSetObserver.class);
    mAdapter.registerDataSetObserver(mockObserver);

    mAdapter.notifyDataSetChanged();
    verify(mockObserver).onChanged();
    mAdapter.notifyDataSetInvalidated();
    verify(mockObserver).onInvalidated();

    mAdapter.unregisterDataSetObserver(mockObserver);
    mAdapter.notifyDataSetChanged();
    mAdapter.notifyDataSetInvalidated();
    verifyNoMoreInteractions(mockObserver);
  }
}

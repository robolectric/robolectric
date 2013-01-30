package com.xtremelabs.robolectric.shadows;

import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.util.Transcript;

import static com.xtremelabs.robolectric.Robolectric.*;
import static com.xtremelabs.robolectric.matchers.TextViewHasTextMatcher.hasText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.CombinableMatcher.both;
import static org.junit.Assert.assertThat;

public class AdapterViewBehavior {
    public static void shouldActAsAdapterView(AdapterView adapterView) throws Exception {
        Robolectric.shadowOf(Looper.getMainLooper()).pause();
        
        testSetAdapter_ShouldCauseViewsToBeRenderedAsynchronously(adapterView);
        testSetAdapter_ShouldSelectFirstItemAsynchronously(adapterView);
        testSetAdapter_ShouldFireOnNothingSelectedWhenAdapterCountIsReducedToZero(adapterView);
        
        shouldIgnoreSetSelectionCallsWithInvalidPosition(adapterView);
        shouldOnlyUpdateOnceIfInvalidatedMultipleTimes(adapterView);
        
        testSetEmptyView_ShouldHideAdapterViewIfAdapterIsNull(adapterView);
        testSetEmptyView_ShouldHideAdapterViewIfAdapterViewIsEmpty(adapterView);
        testSetEmptyView_ShouldHideEmptyViewIfAdapterViewIsNotEmpty(adapterView);
        testSetEmptyView_ShouldHideEmptyViewWhenAdapterGetsNewItem(adapterView);
    }

    private static void shouldIgnoreSetSelectionCallsWithInvalidPosition(AdapterView adapterView) {
        final Transcript transcript = new Transcript();

        adapterView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                transcript.add("onItemSelected fired");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        
        ShadowHandler.idleMainLooper();
        transcript.assertNoEventsSoFar();
        adapterView.setSelection(AdapterView.INVALID_POSITION);
        ShadowHandler.idleMainLooper();
        transcript.assertNoEventsSoFar();
    }
    
    private static void testSetAdapter_ShouldCauseViewsToBeRenderedAsynchronously(AdapterView adapterView) throws Exception {
        adapterView.setAdapter(new CountingAdapter(2));

        assertThat(adapterView.getCount(), equalTo(2));
        assertThat(adapterView.getChildCount(), equalTo(0));

        ShadowHandler.idleMainLooper();
        assertThat(adapterView.getChildCount(), equalTo(2));
        assertThat((TextView) adapterView.getChildAt(0), hasText("Item 0"));
        assertThat((TextView) adapterView.getChildAt(1), hasText("Item 1"));
    }

    private static void testSetAdapter_ShouldSelectFirstItemAsynchronously(final AdapterView adapterView) throws Exception {
        final Transcript transcript = new Transcript();

        adapterView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                assertThat(parent, sameInstance(adapterView));
                assertThat(view, both(sameInstance(adapterView.getChildAt(position))).and(not(nullValue())));
                assertThat(id, equalTo(adapterView.getAdapter().getItemId(position)));
                transcript.add("selected item " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        adapterView.setAdapter(new CountingAdapter(2));
        transcript.assertNoEventsSoFar();
        ShadowHandler.idleMainLooper();
        transcript.assertEventsSoFar("selected item 0");
    }
    
    private static void testSetAdapter_ShouldFireOnNothingSelectedWhenAdapterCountIsReducedToZero(final AdapterView adapterView) throws Exception {
        final Transcript transcript = new Transcript();

        adapterView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                transcript.add("onNothingSelected fired");
            }
        });
        CountingAdapter adapter = new CountingAdapter(2);
        adapterView.setAdapter(adapter);
        ShadowHandler.idleMainLooper();
        transcript.assertNoEventsSoFar();
        adapter.setCount(0);
        ShadowHandler.idleMainLooper();
        transcript.assertEventsSoFar("onNothingSelected fired");
    }
    
    private static void testSetEmptyView_ShouldHideAdapterViewIfAdapterIsNull(final AdapterView adapterView) throws Exception {
    	adapterView.setAdapter(null);
    	
    	View emptyView = new View(adapterView.getContext());
		adapterView.setEmptyView(emptyView);
		
		assertThat(adapterView.getVisibility(), is(View.GONE));
		assertThat(emptyView.getVisibility(), is(View.VISIBLE));
    }
    
    private static void testSetEmptyView_ShouldHideAdapterViewIfAdapterViewIsEmpty(final AdapterView adapterView) throws Exception {
    	adapterView.setAdapter(new CountingAdapter(0));
    	
    	View emptyView = new View(adapterView.getContext());
		adapterView.setEmptyView(emptyView);
		
		assertThat(adapterView.getVisibility(), is(View.GONE));
		assertThat(emptyView.getVisibility(), is(View.VISIBLE));
    }

    private static void testSetEmptyView_ShouldHideEmptyViewIfAdapterViewIsNotEmpty(final AdapterView adapterView) throws Exception {
    	adapterView.setAdapter(new CountingAdapter(1));
    	
    	View emptyView = new View(adapterView.getContext());
		adapterView.setEmptyView(emptyView);
		
		assertThat(adapterView.getVisibility(), is(View.VISIBLE));
		assertThat(emptyView.getVisibility(), is(View.GONE));
    }
    
    private static void testSetEmptyView_ShouldHideEmptyViewWhenAdapterGetsNewItem(final AdapterView adapterView) throws Exception {
    	CountingAdapter adapter = new CountingAdapter(0);
		adapterView.setAdapter(adapter);
    	
    	View emptyView = new View(adapterView.getContext());
		adapterView.setEmptyView(emptyView);
		
		assertThat(adapterView.getVisibility(), is(View.GONE));
		assertThat(emptyView.getVisibility(), is(View.VISIBLE));
		
		adapter.setCount(1);
		
		ShadowHandler.idleMainLooper();
		
		assertThat(adapterView.getVisibility(), is(View.VISIBLE));
		assertThat(emptyView.getVisibility(), is(View.GONE));
    }
    
    private static void testSetEmptyView_ShouldHideAdapterViewWhenAdapterBecomesEmpty(final AdapterView adapterView) throws Exception {
    	CountingAdapter adapter = new CountingAdapter(1);
		adapterView.setAdapter(adapter);
    	
    	View emptyView = new View(adapterView.getContext());
		adapterView.setEmptyView(emptyView);
		
		assertThat(adapterView.getVisibility(), is(View.GONE));
		assertThat(emptyView.getVisibility(), is(View.VISIBLE));
		
		adapter.setCount(0);
		
		ShadowHandler.idleMainLooper();
		
		assertThat(adapterView.getVisibility(), is(View.VISIBLE));
		assertThat(emptyView.getVisibility(), is(View.GONE));
    }

    private static void shouldOnlyUpdateOnceIfInvalidatedMultipleTimes(final AdapterView adapterView) {
        final Transcript transcript = new Transcript();
        CountingAdapter adapter = new CountingAdapter(2) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                transcript.add("getView for " + position);
                return super.getView(position, convertView, parent);
            }
        };
        adapterView.setAdapter(adapter);

        transcript.assertNoEventsSoFar();

        adapter.notifyDataSetChanged();
        adapter.notifyDataSetChanged();

        ShadowHandler.idleMainLooper();

        transcript.assertEventsSoFar("getView for 0", "getView for 1");
    }
}

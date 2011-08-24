package com.xtremelabs.robolectric.shadows;

import static com.xtremelabs.robolectric.Robolectric.shadowOf_;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.util.Strings;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(OverlayItem.class)
public class ShadowOverlayItem {
    private GeoPoint geoPoint;
	private String title;
	private String snippet;

    public void __constructor__(GeoPoint geoPoint, String title, String snippet) {
        this.geoPoint = geoPoint;
		this.title = title;
		this.snippet = snippet;
    }

    @Implementation
    public GeoPoint getPoint() {
        return geoPoint;
    }
    
    @Implementation
    public String getTitle() {
		return title;
	}
    
    @Implementation
    public String getSnippet() {
		return snippet;
	}
    
    @Override @Implementation
    public boolean equals(Object o) {
    	if (o == null) return false;
        o = shadowOf_(o);
        if (o == null) return false;
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;

        ShadowOverlayItem that = (ShadowOverlayItem) o;

        return Strings.equals(title, that.title)
        	&& Strings.equals(snippet, that.snippet)
        	&& geoPoint == null ? that.geoPoint == null :
        		geoPoint.equals(that.geoPoint);
    }
    
    @Override @Implementation
    public int hashCode() {
    	int result = 13;
    	result = title == null ? result : 19 * result + title.hashCode();
    	result = snippet == null ? result : 19 * result + snippet.hashCode();
    	result = geoPoint == null ? result : 19 * result + geoPoint.hashCode();
    	return result;
    }
}

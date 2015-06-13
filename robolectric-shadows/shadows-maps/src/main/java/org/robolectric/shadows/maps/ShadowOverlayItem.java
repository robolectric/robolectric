package org.robolectric.shadows.maps;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.util.Strings;

/**
 * Shadow for {@link com.google.android.maps.OverlayItem}.
 */
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
    o = ShadowExtractor.extract(o);
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

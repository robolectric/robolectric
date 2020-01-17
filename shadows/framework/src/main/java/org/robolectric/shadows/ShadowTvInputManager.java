package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.media.tv.ITvInputManager;
import android.media.tv.TvContentRatingSystemInfo;
import android.media.tv.TvInputManager;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** A shadow implementation of {@link android.media.tv.TvInputManager}. */
@Implements(value = TvInputManager.class, minSdk = LOLLIPOP)
public class ShadowTvInputManager {

  @RealObject protected TvInputManager tvInputManager;

  private List<TvContentRatingSystemInfo> tvContentRatingSystemList = new ArrayList<>();

  @Implementation
  public List<TvContentRatingSystemInfo> getTvContentRatingSystemList() {
    return tvContentRatingSystemList;
  }

  @Implementation
  protected void __constructor__(ITvInputManager iTvInputManager, int userId) {}

  public void setTvContentRatingSystemList(
      List<TvContentRatingSystemInfo> tvContentRatingSystemList) {
    this.tvContentRatingSystemList = tvContentRatingSystemList;
  }
}

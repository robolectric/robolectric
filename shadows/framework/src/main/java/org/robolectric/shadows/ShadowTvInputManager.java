package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.annotation.NonNull;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = TvInputManager.class, minSdk = LOLLIPOP)
public class ShadowTvInputManager {
  private static Map<String, TvInputInfo> tvInputInfoMap = new HashMap<>();
  private static Map<String, Integer> tvInputStateMap = new HashMap<>();

  @Implementation
  public TvInputInfo getTvInputInfo(@NonNull String inputId) {
    return tvInputInfoMap.get(inputId);
  }

  @Implementation
  public List<TvInputInfo> getTvInputList() {
    return new ArrayList<>(tvInputInfoMap.values());
  }

  public static void setTvInputInfo(String inputId, TvInputInfo tvInputInfo) {
    tvInputInfoMap.put(inputId, tvInputInfo);
  }

  @Implementation
  public int getInputState(@NonNull String inputId) {
    return tvInputStateMap.get(inputId);
  }

  public static void setInputState(String inputId, int state) {
    tvInputStateMap.put(inputId, state);
  }

}
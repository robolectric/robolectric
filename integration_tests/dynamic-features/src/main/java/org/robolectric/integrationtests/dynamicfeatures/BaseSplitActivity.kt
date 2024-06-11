package org.robolectric.integrationtests.dynamicfeatures

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.splitcompat.SplitCompat

abstract class BaseSplitActivity : AppCompatActivity() {
  override fun attachBaseContext(newBase: Context?) {
    super.attachBaseContext(newBase)
    SplitCompat.installActivity(this)
  }
}

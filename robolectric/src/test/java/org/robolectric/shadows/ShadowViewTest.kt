package org.robolectric.shadows

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Build.VERSION_CODES.JELLY_BEAN_MR2
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.View.MeasureSpec
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.ViewParent
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import org.robolectric.R
import org.robolectric.Robolectric
import org.robolectric.Robolectric.buildActivity
import org.robolectric.Robolectric.setupActivity
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.DeviceConfig
import org.robolectric.annotation.AccessibilityChecks
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers
import org.robolectric.util.TestRunnable
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class ShadowViewTest {
  private lateinit var view: View
  private lateinit var transcript: MutableList<String>
  private lateinit var context: Application

  @Before
  @Throws(Exception::class)
  fun setUp() {
    transcript = ArrayList()
    context = ApplicationProvider.getApplicationContext()
    view = View(context)
  }

  @Test
  @Throws(Exception::class)
  fun testHasNullLayoutParamsUntilAddedToParent() {
    assertThat(view.layoutParams).isNull()
    LinearLayout(context).addView(view)
    assertThat(view.layoutParams).isNotNull()
  }

  @Test
  @Throws(Exception::class)
  fun layout_shouldAffectWidthAndHeight() {
    assertThat(view.width).isEqualTo(0)
    assertThat(view.height).isEqualTo(0)

    view.layout(100, 200, 303, 404)
    assertThat(view.width).isEqualTo(303 - 100)
    assertThat(view.height).isEqualTo(404 - 200)
  }

  @Test
  @Throws(Exception::class)
  fun measuredDimensions() {
    val view1 = object : View(context) {
      init {
        setMeasuredDimension(123, 456)
      }
    }
    assertThat(view1.measuredWidth).isEqualTo(123)
    assertThat(view1.measuredHeight).isEqualTo(456)
  }

  @Test
  @Throws(Exception::class)
  fun layout_shouldCallOnLayoutOnlyIfChanged() {
    val view1 = object : View(context) {
      override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        transcript.add(
            "onLayout $changed $left $top $right $bottom")
      }
    }
    view1.layout(0, 0, 0, 0)
    assertThat(transcript).isEmpty()
    view1.layout(1, 2, 3, 4)
    assertThat(transcript).containsExactly("onLayout true 1 2 3 4")
    transcript.clear()
    view1.layout(1, 2, 3, 4)
    assertThat(transcript).isEmpty()
  }

  @Test
  @Throws(Exception::class)
  fun shouldFocus() {
    val transcript = ArrayList<String>()

    view.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus -> transcript.add(if (hasFocus) "Gained focus" else "Lost focus") }

    assertFalse(view.isFocused)
    assertFalse(view.hasFocus())
    assertThat(transcript).isEmpty()

    view.requestFocus()
    assertFalse(view.isFocused)
    assertFalse(view.hasFocus())
    assertThat(transcript).isEmpty()

    view.isFocusable = true
    view.requestFocus()
    assertTrue(view.isFocused)
    assertTrue(view.hasFocus())
    assertThat(transcript).containsExactly("Gained focus")
    transcript.clear()

    shadowOf(view)
        .setMyParent(LinearLayout(context)) // we can never lose focus unless a parent can
    // take it

    view.clearFocus()
    assertFalse(view.isFocused)
    assertFalse(view.hasFocus())
    assertThat(transcript).containsExactly("Lost focus")
  }

  @Test
  @Throws(Exception::class)
  fun shouldNotBeFocusableByDefault() {
    assertFalse(view.isFocusable)

    view.isFocusable = true
    assertTrue(view.isFocusable)
  }

  @Test
  @Throws(Exception::class)
  fun shouldKnowIfThisOrAncestorsAreVisible() {
    assertThat(view.isShown).named("view isn't considered shown unless it has a view root").isFalse()
    shadowOf(view).setMyParent(ReflectionHelpers.createNullProxy(ViewParent::class.java))
    assertThat(view.isShown).isTrue()
    shadowOf(view).setMyParent(null)

    val parent = LinearLayout(context)
    parent.addView(view)

    val grandParent = LinearLayout(context)
    grandParent.addView(parent)

    grandParent.visibility = View.GONE

    assertFalse(view.isShown)
  }

  @Test
  @Throws(Exception::class)
  fun shouldInflateMergeRootedLayoutAndNotCreateReferentialLoops() {
    val root = LinearLayout(context)
    LinearLayout.inflate(context, R.layout.inner_merge, root)
    for (i in 0 until root.childCount) {
      val child = root.getChildAt(i)
      assertNotSame(root, child)
    }
  }

  @Test
  @Throws(Exception::class)
  fun performLongClick_shouldClickOnView() {
    val clickListener = mock(OnLongClickListener::class.java)
    shadowOf(view).setMyParent(ReflectionHelpers.createNullProxy(ViewParent::class.java))
    view.setOnLongClickListener(clickListener)
    view.performLongClick()

    verify(clickListener).onLongClick(view)
  }

  @Test
  @Throws(Exception::class)
  fun checkedClick_shouldClickOnView() {
    val clickListener = mock(OnClickListener::class.java)
    shadowOf(view).setMyParent(ReflectionHelpers.createNullProxy(ViewParent::class.java))
    view.setOnClickListener(clickListener)
    shadowOf(view).checkedPerformClick()

    verify(clickListener).onClick(view)
  }

  @Test(expected = RuntimeException::class)
  @Throws(Exception::class)
  fun checkedClick_shouldThrowIfViewIsNotVisible() {
    val grandParent = LinearLayout(context)
    val parent = LinearLayout(context)
    grandParent.addView(parent)
    parent.addView(view)
    grandParent.visibility = View.GONE

    shadowOf(view).checkedPerformClick()
  }

  @Test(expected = RuntimeException::class)
  @Throws(Exception::class)
  fun checkedClick_shouldThrowIfViewIsDisabled() {
    view.isEnabled = false
    shadowOf(view).checkedPerformClick()
  }

  /*
   * This test will throw an exception because the accessibility checks depend on the  Android
   * Support Library. If the support library is included at some point, a single test from
   * AccessibilityUtilTest could be moved here to make sure the accessibility checking is run.
   */
  @Test(expected = RuntimeException::class)
  @AccessibilityChecks
  @Throws(Exception::class)
  fun checkedClick_withA11yChecksAnnotation_shouldThrow() {
    shadowOf(view).checkedPerformClick()
  }

  @Test
  @Throws(Exception::class)
  fun getBackground_shouldReturnNullIfNoBackgroundHasBeenSet() {
    assertThat(view.background).isNull()
  }

  @Test
  fun shouldSetBackgroundColor() {
    val red = -0x10000
    view.setBackgroundColor(red)
    val background = view.background as ColorDrawable
    assertThat(background.color).isEqualTo(red)
  }

  @Test
  @Throws(Exception::class)
  fun shouldSetBackgroundResource() {
    view.setBackgroundResource(R.drawable.an_image)
    assertThat(shadowOf(view.background as BitmapDrawable).getCreatedFromResId())
        .isEqualTo(R.drawable.an_image)
  }

  @Test
  @Throws(Exception::class)
  fun shouldClearBackgroundResource() {
    view.setBackgroundResource(R.drawable.an_image)
    view.setBackgroundResource(0)
    assertThat(view.background).isEqualTo(null)
  }

  @Test
  fun shouldRecordBackgroundColor() {
    val colors = intArrayOf(R.color.black, R.color.clear, R.color.white)

    for (color in colors) {
      view.setBackgroundColor(color)
      val drawable = view.background as ColorDrawable
      assertThat(drawable.color).isEqualTo(color)
    }
  }

  @Test
  fun shouldRecordBackgroundDrawable() {
    val drawable = BitmapDrawable(BitmapFactory.decodeFile("some/fake/file"))
    view.setBackgroundDrawable(drawable)
    assertThat(view.background).isSameAs(drawable)
    assertThat(ShadowView.visualize(view)).isEqualTo("background:\nBitmap for file:some/fake/file")
  }

  @Test
  @Throws(Exception::class)
  fun shouldPostActionsToTheMessageQueue() {
    ShadowLooper.pauseMainLooper()

    val runnable = TestRunnable()
    view.post(runnable)
    assertFalse(runnable.wasRun)

    ShadowLooper.unPauseMainLooper()
    assertTrue(runnable.wasRun)
  }

  @Test
  @Throws(Exception::class)
  fun shouldPostInvalidateDelayed() {
    ShadowLooper.pauseMainLooper()

    view.postInvalidateDelayed(100)
    val shadowView = shadowOf(view)
    assertFalse(shadowView.wasInvalidated())

    ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
    assertTrue(shadowView.wasInvalidated())
  }

  @Test
  @Throws(Exception::class)
  fun shouldPostActionsToTheMessageQueueWithDelay() {
    ShadowLooper.pauseMainLooper()

    val runnable = TestRunnable()
    view.postDelayed(runnable, 1)
    assertFalse(runnable.wasRun)

    Robolectric.getForegroundThreadScheduler().advanceBy(1)
    assertTrue(runnable.wasRun)
  }

  @Test
  @Throws(Exception::class)
  fun shouldRemovePostedCallbacksFromMessageQueue() {
    val runnable = TestRunnable()
    view.postDelayed(runnable, 1)

    view.removeCallbacks(runnable)

    Robolectric.getForegroundThreadScheduler().advanceBy(1)
    assertThat(runnable.wasRun).isFalse()
  }

  @Test
  @Throws(Exception::class)
  fun shouldSupportAllConstructors() {
    View(context)
    View(context, null)
    View(context, null, 0)
  }

  @Test
  fun shouldRememberIsPressed() {
    view.isPressed = true
    assertTrue(view.isPressed)
    view.isPressed = false
    assertFalse(view.isPressed)
  }

  @Test
  @Throws(Exception::class)
  fun shouldAddOnClickListenerFromAttribute() {
    val attrs = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.onClick, "clickMe")
        .build()

    view = View(context, attrs)
    assertNotNull(shadowOf(view).onClickListener)
  }

  @Test
  @Throws(Exception::class)
  fun shouldCallOnClickWithAttribute() {
    val myActivity = buildActivity(MyActivity::class.java).create().get()

    val attrs = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.onClick, "clickMe")
        .build()

    view = View(myActivity, attrs)
    view.performClick()
    assertTrue("Should have been called", myActivity.called)
  }

  @Test(expected = RuntimeException::class)
  @Throws(Exception::class)
  fun shouldThrowExceptionWithBadMethodName() {
    val myActivity = buildActivity(MyActivity::class.java).create().get()

    val attrs = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.onClick, "clickYou")
        .build()

    view = View(myActivity, attrs)
    view.performClick()
  }

  @Test
  @Throws(Exception::class)
  fun shouldSetAnimation() {
    val anim = TestAnimation()
    view.animation = anim
    assertThat(view.animation).isSameAs(anim)
  }

  @Test
  fun shouldFindViewWithTag() {
    view.tag = "tagged"
    assertThat(view.findViewWithTag("tagged") as View).isSameAs(view)
  }

  @Test
  @Throws(Exception::class)
  fun scrollTo_shouldStoreTheScrolledCoordinates() {
    view.scrollTo(1, 2)
    assertThat(shadowOf(view).scrollToCoordinates).isEqualTo(Point(1, 2))
  }

  @Test
  @Throws(Exception::class)
  fun shouldScrollTo() {
    view.scrollTo(7, 6)

    assertEquals(7, view.scrollX.toLong())
    assertEquals(6, view.scrollY.toLong())
  }

  @Test
  @Throws(Exception::class)
  fun scrollBy_shouldStoreTheScrolledCoordinates() {
    view.scrollTo(4, 5)
    view.scrollBy(10, 20)
    assertThat(shadowOf(view).scrollToCoordinates).isEqualTo(Point(14, 25))

    assertThat(view.scrollX).isEqualTo(14)
    assertThat(view.scrollY).isEqualTo(25)
  }

  @Test
  fun shouldGetScrollXAndY() {
    assertEquals(0, view.scrollX.toLong())
    assertEquals(0, view.scrollY.toLong())
  }

  @Test
  @Throws(Exception::class)
  fun getViewTreeObserver_shouldReturnTheSameObserverFromMultipleCalls() {
    val observer = view.viewTreeObserver
    assertThat(observer).isInstanceOf(ViewTreeObserver::class.java)
    assertThat(view.viewTreeObserver).isSameAs(observer)
  }

  @Test
  @Throws(Exception::class)
  fun dispatchTouchEvent_sendsMotionEventToOnTouchEvent() {
    val touchableView = TouchableView(context)
    val event = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 12f, 34f, 0)
    touchableView.dispatchTouchEvent(event)
    assertThat(touchableView.event).isSameAs(event)
    view.dispatchTouchEvent(event)
    assertThat(shadowOf(view).lastTouchEvent).isSameAs(event)
  }

  @Test
  @Throws(Exception::class)
  fun dispatchTouchEvent_listensToFalseFromListener() {
    val called = AtomicBoolean(false)
    view.setOnTouchListener { view, motionEvent ->
      called.set(true)
      false
    }
    val event = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 12f, 34f, 0)
    view.dispatchTouchEvent(event)
    assertThat(shadowOf(view).lastTouchEvent).isSameAs(event)
    assertThat(called.get()).isTrue()
  }

  @Test
  @Throws(Exception::class)
  fun test_nextFocusDownId() {
    assertEquals(View.NO_ID.toLong(), view.nextFocusDownId.toLong())

    view.nextFocusDownId = R.id.icon
    assertEquals(R.id.icon.toLong(), view.nextFocusDownId.toLong())
  }

  @Test
  fun startAnimation() {
    val view = TestView(buildActivity(Activity::class.java).create().get())
    val animation = AlphaAnimation(0f, 1f)

    val listener = mock(Animation.AnimationListener::class.java)
    animation.setAnimationListener(listener)
    view.startAnimation(animation)

    verify<AnimationListener>(listener).onAnimationStart(animation)
    verify<AnimationListener>(listener).onAnimationEnd(animation)
  }

  @Test
  fun setAnimation() {
    val view = TestView(buildActivity(Activity::class.java).create().get())
    val animation = AlphaAnimation(0f, 1f)

    val listener = mock(Animation.AnimationListener::class.java)
    animation.setAnimationListener(listener)
    animation.startTime = 1000
    view.animation = animation

    verifyZeroInteractions(listener)

    Robolectric.getForegroundThreadScheduler().advanceToNextPostedRunnable()

    verify<AnimationListener>(listener).onAnimationStart(animation)
    verify<AnimationListener>(listener).onAnimationEnd(animation)
  }

  @Test
  fun setNullAnimation() {
    val view = TestView(buildActivity(Activity::class.java).create().get())
    view.setAnimation(null)
    assertThat(view.getAnimation()).isNull()
  }

  @Test
  fun test_measuredDimension() {
    // View does not provide its own onMeasure implementation
    val view1 = TestView(buildActivity(Activity::class.java).create().get())

    assertThat(view1.height).isEqualTo(0)
    assertThat(view1.width).isEqualTo(0)
    assertThat(view1.measuredHeight).isEqualTo(0)
    assertThat(view1.measuredWidth).isEqualTo(0)

    view1.measure(MeasureSpec.makeMeasureSpec(150, MeasureSpec.AT_MOST),
        MeasureSpec.makeMeasureSpec(300, MeasureSpec.AT_MOST))

    assertThat(view1.height).isEqualTo(0)
    assertThat(view1.width).isEqualTo(0)
    assertThat(view1.measuredHeight).isEqualTo(300)
    assertThat(view1.measuredWidth).isEqualTo(150)
  }

  @Test
  fun test_measuredDimensionCustomView() {
    // View provides its own onMeasure implementation
    val view2 = TestView2(buildActivity(Activity::class.java).create().get(), 300, 100)

    assertThat(view2.width).isEqualTo(0)
    assertThat(view2.height).isEqualTo(0)
    assertThat(view2.measuredWidth).isEqualTo(0)
    assertThat(view2.measuredHeight).isEqualTo(0)

    view2.measure(MeasureSpec.makeMeasureSpec(200, MeasureSpec.AT_MOST),
        MeasureSpec.makeMeasureSpec(50, MeasureSpec.AT_MOST))

    assertThat(view2.width).isEqualTo(0)
    assertThat(view2.height).isEqualTo(0)
    assertThat(view2.measuredWidth).isEqualTo(300)
    assertThat(view2.measuredHeight).isEqualTo(100)
  }

  @Test
  @Throws(Exception::class)
  fun shouldGetAndSetTranslations() {
    view = TestView(buildActivity(Activity::class.java).create().get())
    view.translationX = 8.9f
    view.translationY = 4.6f

    assertThat(view.translationX).isEqualTo(8.9f)
    assertThat(view.translationY).isEqualTo(4.6f)
  }

  @Test
  @Throws(Exception::class)
  fun shouldGetAndSetAlpha() {
    view = TestView(buildActivity(Activity::class.java).create().get())
    view.alpha = 9.1f

    assertThat(view.alpha).isEqualTo(9.1f)
  }

  @Test
  fun itKnowsIfTheViewIsShown() {
    shadowOf(view).setMyParent(ReflectionHelpers.createNullProxy(ViewParent::class.java)) // a view is only considered visible if it is added to a view root
    view.visibility = View.VISIBLE
    assertThat(view.isShown).isTrue()
  }

  @Test
  fun itKnowsIfTheViewIsNotShown() {
    view.visibility = View.GONE
    assertThat(view.isShown).isFalse()

    view.visibility = View.INVISIBLE
    assertThat(view.isShown).isFalse()
  }

  @Test
  @Throws(Exception::class)
  fun shouldTrackRequestLayoutCalls() {
    assertThat(shadowOf(view).didRequestLayout()).isFalse()
    view.requestLayout()
    assertThat(shadowOf(view).didRequestLayout()).isTrue()
    shadowOf(view).setDidRequestLayout(false)
    assertThat(shadowOf(view).didRequestLayout()).isFalse()
  }

  @Test
  @Throws(Exception::class)
  fun shouldClickAndNotClick() {
    assertThat(view.isClickable).isFalse()
    view.isClickable = true
    assertThat(view.isClickable).isTrue()
    view.isClickable = false
    assertThat(view.isClickable).isFalse()
    view.setOnClickListener { }
    assertThat(view.isClickable).isTrue()
  }

  @Test
  @Throws(Exception::class)
  fun shouldLongClickAndNotLongClick() {
    assertThat(view.isLongClickable).isFalse()
    view.isLongClickable = true
    assertThat(view.isLongClickable).isTrue()
    view.isLongClickable = false
    assertThat(view.isLongClickable).isFalse()
    view.setOnLongClickListener { false }
    assertThat(view.isLongClickable).isTrue()
  }

  @Test
  fun rotationX() {
    view.rotationX = 10f
    assertThat(view.rotationX).isEqualTo(10f)
  }

  @Test
  fun rotationY() {
    view.rotationY = 20f
    assertThat(view.rotationY).isEqualTo(20f)
  }

  @Test
  fun rotation() {
    view.rotation = 30f
    assertThat(view.rotation).isEqualTo(30f)
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  fun cameraDistance() {
    view.cameraDistance = 100f
    assertThat(view.cameraDistance).isEqualTo(100f)
  }

  @Test
  fun scaleX() {
    assertThat(view.scaleX).isEqualTo(1f)
    view.scaleX = 0.5f
    assertThat(view.scaleX).isEqualTo(0.5f)
  }

  @Test
  fun scaleY() {
    assertThat(view.scaleY).isEqualTo(1f)
    view.scaleY = 0.5f
    assertThat(view.scaleY).isEqualTo(0.5f)
  }

  @Test
  fun pivotX() {
    view.pivotX = 10f
    assertThat(view.pivotX).isEqualTo(10f)
  }

  @Test
  fun pivotY() {
    view.pivotY = 10f
    assertThat(view.pivotY).isEqualTo(10f)
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  fun elevation() {
    view.elevation = 10f
    assertThat(view.elevation).isEqualTo(10f)
  }

  @Test
  fun translationX() {
    view.translationX = 10f
    assertThat(view.translationX).isEqualTo(10f)
  }

  @Test
  fun translationY() {
    view.translationY = 10f
    assertThat(view.translationY).isEqualTo(10f)
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  fun translationZ() {
    view.translationZ = 10f
    assertThat(view.translationZ).isEqualTo(10f)
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  fun clipToOutline() {
    view.clipToOutline = true
    assertThat(view.clipToOutline).isTrue()
  }

  @Test
  @Throws(Exception::class)
  fun performHapticFeedback_shouldSetLastPerformedHapticFeedback() {
    assertThat(shadowOf(view).lastHapticFeedbackPerformed()).isEqualTo(-1)
    view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    assertThat(shadowOf(view).lastHapticFeedbackPerformed()).isEqualTo(HapticFeedbackConstants.LONG_PRESS)
  }

  @Test
  @Throws(Exception::class)
  fun canAssertThatSuperDotOnLayoutWasCalledFromViewSubclasses() {
    val view = TestView2(setupActivity(Activity::class.java), 1111, 1112)
    assertThat(shadowOf(view).onLayoutWasCalled()).isFalse()
    view.onLayout(true, 1, 2, 3, 4)
    assertThat(shadowOf(view).onLayoutWasCalled()).isTrue()
  }

  @Test
  @Throws(Exception::class)
  fun setScrolls_canBeAskedFor() {
    view.scrollX = 234
    view.scrollY = 544
    assertThat(view.scrollX).isEqualTo(234)
    assertThat(view.scrollY).isEqualTo(544)
  }

  @Test
  @Throws(Exception::class)
  fun setScrolls_firesOnScrollChanged() {
    val testView = TestView(buildActivity(Activity::class.java).create().get())
    testView.scrollX = 122
    testView.scrollY = 150
    testView.scrollX = 453
    assertThat(testView.oldl).isEqualTo(122)
    testView.scrollY = 54
    assertThat(testView.l).isEqualTo(453)
    assertThat(testView.t).isEqualTo(54)
    assertThat(testView.oldt).isEqualTo(150)
  }

  @Test
  @Throws(Exception::class)
  fun layerType() {
    assertThat(view.layerType).isEqualTo(View.LAYER_TYPE_NONE)
    view.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    assertThat(view.layerType).isEqualTo(View.LAYER_TYPE_SOFTWARE)
  }

  private class TestAnimation : Animation()

  private class TouchableView(context: Context) : View(context) {
    internal var event: MotionEvent? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
      this.event = event
      return false
    }
  }

  class TestView(context: Context) : View(context) {
    internal var onAnimationEndWasCalled: Boolean = false
    var l: Int = 0
    var t: Int = 0
    var oldl: Int = 0
    var oldt: Int = 0

    override fun onAnimationEnd() {
      super.onAnimationEnd()
      onAnimationEndWasCalled = true
    }

    public override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
      this.l = l
      this.t = t
      this.oldl = oldl
      this.oldt = oldt
    }
  }

  private class TestView2(context: Context, private val minWidth: Int, private val minHeight: Int) : View(context) {

    public override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
      super.onLayout(changed, l, t, r, b)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
      setMeasuredDimension(minWidth, minHeight)
    }
  }

  @Test
  @Throws(Exception::class)
  fun shouldCallOnAttachedToAndDetachedFromWindow() {
    val parent = MyView("parent", transcript)
    parent.addView(MyView("child", transcript))
    assertThat(transcript).isEmpty()

    val activity = Robolectric.buildActivity(ContentViewActivity::class.java).create().get()
    activity.windowManager.addView(parent, WindowManager.LayoutParams(100, 100))
    assertThat(transcript).containsExactly("parent attached", "child attached")
    transcript.clear()

    parent.addView(MyView("another child", transcript))
    assertThat(transcript).containsExactly("another child attached")
    transcript.clear()

    val temporaryChild = MyView("temporary child", transcript)
    parent.addView(temporaryChild)
    assertThat(transcript).containsExactly("temporary child attached")
    transcript.clear()
    assertTrue(shadowOf(temporaryChild).isAttachedToWindow)

    parent.removeView(temporaryChild)
    assertThat(transcript).containsExactly("temporary child detached")
    assertFalse(shadowOf(temporaryChild).isAttachedToWindow)
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  @Throws(Exception::class)
  fun getWindowId_shouldReturnValidObjectWhenAttached() {
    val parent = MyView("parent", transcript)
    val child = MyView("child", transcript)
    parent.addView(child)

    assertThat(parent.windowId).isNull()
    assertThat(child.windowId).isNull()

    val activity = Robolectric.buildActivity(ContentViewActivity::class.java).create().get()
    activity.windowManager.addView(parent, WindowManager.LayoutParams(100, 100))

    val windowId = parent.windowId
    assertThat(windowId).isNotNull()
    assertThat(child.windowId).isSameAs(windowId)
    assertThat(child.windowId).isEqualTo(windowId) // equals must work!

    val anotherChild = MyView("another child", transcript)
    parent.addView(anotherChild)
    assertThat(anotherChild.windowId).isEqualTo(windowId)

    parent.removeView(anotherChild)
    assertThat(anotherChild.windowId).isNull()
  }

  // todo looks like this is flaky...
  @Test
  @Throws(Exception::class)
  fun removeAllViews_shouldCallOnAttachedToAndDetachedFromWindow() {
    val parent = MyView("parent", transcript)
    val activity = Robolectric.buildActivity(ContentViewActivity::class.java).create().get()
    activity.windowManager.addView(parent, WindowManager.LayoutParams(100, 100))

    parent.addView(MyView("child", transcript))
    parent.addView(MyView("another child", transcript))
    ShadowLooper.runUiThreadTasks()
    transcript.clear()
    parent.removeAllViews()
    ShadowLooper.runUiThreadTasks()
    assertThat(transcript).containsExactly("another child detached", "child detached")
  }

  @Test
  @Throws(Exception::class)
  fun capturesOnSystemUiVisibilityChangeListener() {
    val testView = TestView(buildActivity(Activity::class.java).create().get())
    val changeListener = View.OnSystemUiVisibilityChangeListener { }
    testView.setOnSystemUiVisibilityChangeListener(changeListener)

    assertThat(changeListener).isEqualTo(shadowOf(testView).onSystemUiVisibilityChangeListener)
  }

  @Test
  @Throws(Exception::class)
  fun capturesOnCreateContextMenuListener() {
    val testView = TestView(buildActivity(Activity::class.java).create().get())
    assertThat(shadowOf(testView).onCreateContextMenuListener).isNull()

    val createListener = View.OnCreateContextMenuListener { contextMenu, view, contextMenuInfo -> }

    testView.setOnCreateContextMenuListener(createListener)
    assertThat(shadowOf(testView).onCreateContextMenuListener).isEqualTo(createListener)

    testView.setOnCreateContextMenuListener(null)
    assertThat(shadowOf(testView).onCreateContextMenuListener).isNull()
  }

  @Test
  fun setsGlobalVisibleRect() {
    val globalVisibleRect = Rect()
    shadowOf(view).setGlobalVisibleRect(Rect())
    assertThat(view.getGlobalVisibleRect(globalVisibleRect))
        .isFalse()
    assertThat(globalVisibleRect.isEmpty)
        .isTrue()
    assertThat(view.getGlobalVisibleRect(globalVisibleRect, Point(1, 1)))
        .isFalse()
    assertThat(globalVisibleRect.isEmpty)
        .isTrue()

    shadowOf(view).setGlobalVisibleRect(Rect(1, 2, 3, 4))
    assertThat(view.getGlobalVisibleRect(globalVisibleRect))
        .isTrue()
    assertThat(globalVisibleRect)
        .isEqualTo(Rect(1, 2, 3, 4))
    assertThat(view.getGlobalVisibleRect(globalVisibleRect, Point(1, 1)))
        .isTrue()
    assertThat(globalVisibleRect)
        .isEqualTo(Rect(0, 1, 2, 3))
  }

  @Test
  fun usesDefaultGlobalVisibleRect() {
    val activityController = Robolectric.buildActivity(Activity::class.java)
    val activity = activityController.get()
    activity.setContentView(view, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT))
    activityController.setup()

    val globalVisibleRect = Rect()
    assertThat(view.getGlobalVisibleRect(globalVisibleRect))
        .isTrue()
    assertThat(globalVisibleRect)
        .isEqualTo(Rect(0, 25,
            DeviceConfig.DEFAULT_SCREEN_SIZE.width, DeviceConfig.DEFAULT_SCREEN_SIZE.height))
  }

  class MyActivity : Activity() {
    var called: Boolean = false

    fun clickMe(view: View) {
      called = true
    }
  }

  class MyView(private val name: String, private val transcript: MutableList<String>) : LinearLayout(ApplicationProvider.getApplicationContext()) {

    override fun onAttachedToWindow() {
      transcript.add("$name attached")
      super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
      transcript.add("$name detached")
      super.onDetachedFromWindow()
    }
  }

  private class ContentViewActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle) {
      super.onCreate(savedInstanceState)
      setContentView(FrameLayout(this))
    }
  }
}

package android.view.animation;


import com.xtremelabs.robolectric.internal.DoNotInstrument;

@DoNotInstrument
public class ShadowAnimationBridge {

    private Animation realAnimation;

    public ShadowAnimationBridge(Animation realAnimation) {
        this.realAnimation = realAnimation;

    }


    public void applyTransformation(float interpolatedTime, Transformation t) {
        realAnimation.applyTransformation(interpolatedTime, t);
    }
}

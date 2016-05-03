package nl.svendubbeld.car.animation;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;

public class SpeakNotificationsIconAnimation extends Animation {

    private ImageView mIcon;
    private boolean mEnable;
    private int mAlphaEnabled;
    private int mAlphaDisabled;

    public SpeakNotificationsIconAnimation(ImageView icon, boolean enable, int alphaEnabled, int alphaDisabled) {
        mIcon = icon;
        mEnable = enable;
        mAlphaEnabled = alphaEnabled;
        mAlphaDisabled = alphaDisabled;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        if (mEnable) {
            mIcon.setImageAlpha((int) (interpolatedTime * mAlphaEnabled));
        } else {
            mIcon.setImageAlpha((int) (mAlphaEnabled - interpolatedTime * (mAlphaEnabled - mAlphaDisabled)));
        }
    }
}

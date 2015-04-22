/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Sven Dubbeld
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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

/*
 * Copyright (c) 2014, David Forsythe
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of Luchadeer nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.dforsyth.android.luchadeer.ui.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;

import org.dforsyth.android.luchadeer.R;


public class FadeNetworkImageView extends ObservableNetworkImageView {
    public FadeNetworkImageView(Context context) {
        super(context);
        setErrorImageResId(R.drawable.gb_no_image);
    }

    public FadeNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setErrorImageResId(R.drawable.gb_no_image);

    }

    public FadeNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setErrorImageResId(R.drawable.gb_no_image);
    }

    private final int FADE_IN_TIME_MS = 250;

    @Override
    public void setImageDrawable(Drawable drawable) {
        if (drawable == null) {
            super.setImageDrawable(drawable);
            return;
        }

        Drawable[] layers = new Drawable[]{
                new ColorDrawable(android.R.color.transparent),
                drawable
        };
        TransitionDrawable transitionDrawable = new TransitionDrawable(layers);
        transitionDrawable.setCrossFadeEnabled(true);
        transitionDrawable.startTransition(FADE_IN_TIME_MS);

        super.setImageDrawable(transitionDrawable);
    }

    @Override
    public void setImageResource(int resId) {
        try {
            setImageDrawable(getResources().getDrawable(resId));
        } catch (Resources.NotFoundException e) {
            super.setImageResource(resId);
        }
    }
}

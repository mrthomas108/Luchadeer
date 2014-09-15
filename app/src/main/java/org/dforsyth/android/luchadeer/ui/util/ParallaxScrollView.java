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
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;


public class ParallaxScrollView extends ObservableScrollView {
    private ArrayList<ParallaxListView.ParallaxableView> mSubscribedViews;
    private OnScrollChangedListener mOnScrollChangedListener;

    public ParallaxScrollView(Context context) {
        super(context);
        setup();
    }

    public ParallaxScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public ParallaxScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup();
    }

    private class ParallaxOnScrollChangedListener implements OnScrollChangedListener {

        @Override
        public void onScrollChanged(int l, int t, int oldl, int oldt) {
            int height = getHeight();
            for (ParallaxListView.ParallaxableView view : mSubscribedViews) {
                float percent = (float) getScrollY() / getHeight();
                View v = (View) view;
                if (getScrollY() + getHeight() > v.getTop()) {
                    view.setScrollPercent(percent);
                }
                // Log.d("ParallaxScrollView", "scrollpct: " + percent);
            }

            if (mOnScrollChangedListener != null) {
                mOnScrollChangedListener.onScrollChanged(l, t, oldl, oldt);
            }
        }
    }

    private void setup() {
        // we could just override onScrollChanged...
        super.setOnScrollChangedListener(new ParallaxOnScrollChangedListener());
        mSubscribedViews = new ArrayList<ParallaxListView.ParallaxableView>();
    }

    @Override
    public void setOnScrollChangedListener(OnScrollChangedListener onScrollChangedListener) {
        mOnScrollChangedListener = onScrollChangedListener;
    }

    public void subscribeView(ParallaxListView.ParallaxableView view) {
        mSubscribedViews.add(view);
    }
}

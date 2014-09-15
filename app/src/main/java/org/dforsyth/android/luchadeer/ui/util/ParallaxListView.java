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
import android.widget.AbsListView;


public class ParallaxListView extends PaginatedListView {
    private OnScrollListener mOnScrollListener;

    public ParallaxListView(Context context) {
        super(context);
        setup();
    }

    public ParallaxListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public ParallaxListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup();
    }

    private void setup() {
        // setCacheColorHint(0);
        // setScrollingCacheEnabled(false);
        super.setOnScrollListener(new ParallaxOnScrollListener());
    }

    // TODO: turn this into an abstract class with getParallaxableHeight()
    public interface ParallaxableView {
        public void setScrollPercent(float percent);
    }

    private class ParallaxOnScrollListener implements OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged(absListView, i);
            }
        }

        @Override
        public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (mOnScrollListener != null) {
                mOnScrollListener.onScroll(absListView, firstVisibleItem, visibleItemCount, totalItemCount);
            }

            int height = absListView.getHeight();

            for (int i = 0; i < visibleItemCount; ++i) {
                View v = absListView.getChildAt(i);
                if (v == getLoadingFooter()) {
                    continue;
                }

                int totalScrollHeight = height + v.getHeight();
                float scrollPercent = v.getTop() / (float)totalScrollHeight;

                if (v instanceof ParallaxableView) {
                    ParallaxableView parallaxableView = (ParallaxableView) v;
                    parallaxableView.setScrollPercent(scrollPercent);
                }
            }
        }
    }
}

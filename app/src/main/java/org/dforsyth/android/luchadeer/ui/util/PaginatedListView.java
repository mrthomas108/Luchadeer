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
import android.widget.ListAdapter;
import android.widget.ListView;


public class PaginatedListView extends ListView {
    private View mLoadingFooter;
    private OnScrollListener mOnScrollListener;
    private int mAvailableItemCount;
    private int mLoadingBoundary;
    private boolean mLoadingNextPage;
    private OnRequestNextPageListener mOnRequestNextPageListener;

    public PaginatedListView(Context context) {
        super(context);
        setup();
    }

    public PaginatedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public PaginatedListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup();
    }

    private void setup() {
        // setup our scroll listener that will allow up to load more
        mLoadingBoundary = 1;
        mAvailableItemCount = 0;

        // XXX how does this feel?
        setVerticalScrollBarEnabled(false);

        super.setOnScrollListener(new PaginatedListViewOnScrollListener());
    }

    public void setLoadingFooter(View loadingFooter) {
        mLoadingFooter = loadingFooter;
    }

    public View getLoadingFooter() {
        return mLoadingFooter;
    }

    public void setAvailableItemCount(int availableItemCount) {
        mAvailableItemCount = availableItemCount;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (mLoadingFooter != null && getFooterViewsCount() == 0) {
            addFooterView(mLoadingFooter, null, false);
        }
        super.setAdapter(adapter);
    }

    public void setLoadingBoundary(int loadingBoundary) {
        mLoadingBoundary = loadingBoundary;
    }

    public void setLoadingNextPage(boolean loadingNextPage) {
        if (mLoadingNextPage != loadingNextPage) {
            mLoadingNextPage = loadingNextPage;
        }
    }

    public interface OnRequestNextPageListener {
        public void onRequestNextPage();
    }

    public void setOnRequestNextPageListener(OnRequestNextPageListener onRequestNextPageListener) {
        mOnRequestNextPageListener = onRequestNextPageListener;
    }

    @Override
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        // override so inheriter can set a listener
        mOnScrollListener = onScrollListener;
    }

    private class PaginatedListViewOnScrollListener implements OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged(absListView, i);
            }
        }

        @Override
        public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (totalItemCount < mAvailableItemCount && firstVisibleItem >= totalItemCount - visibleItemCount - mLoadingBoundary) {

                if (!mLoadingNextPage) {
                    mLoadingNextPage = true;
                    if (mOnRequestNextPageListener != null) {
                        mOnRequestNextPageListener.onRequestNextPage();
                    }
                }
            }

            if (totalItemCount >= mAvailableItemCount && getFooterViewsCount() > 0) {
                removeFooterView(mLoadingFooter);
            }

            if (mOnScrollListener != null) {
                mOnScrollListener.onScroll(absListView, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        }
    }
}

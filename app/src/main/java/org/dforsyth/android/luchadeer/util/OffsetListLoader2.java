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

package org.dforsyth.android.luchadeer.util;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.dforsyth.android.luchadeer.net.LuchadeerApi;

import java.util.ArrayList;

public abstract class OffsetListLoader2<T> extends AsyncTaskLoader<LoaderListResult<T>> {
    private int mOffset;
    private int mTotalResults;
    private Context mContext;
    private LoaderListResult<T> mLoaded;

    public OffsetListLoader2(Context context, int initialOffset) {
        super(context);
        mOffset = initialOffset;
    }

    public abstract LuchadeerApi.GiantBombResponse<ArrayList<T>> doRequest(int offset) throws Exception;

    @Override
    public LoaderListResult<T> loadInBackground() {

        LuchadeerApi.GiantBombResponse<ArrayList<T>> response = null;
        Exception error = null;
        try {
            response = doRequest(mOffset);
            mTotalResults = response.getNumberOfTotalResults();
        } catch (Exception e) {
            e.printStackTrace();
            error = e;
        }

        return new LoaderListResult<T>(response == null ? null : response.getResults(), error);
    }

    @Override
    public void deliverResult(LoaderListResult<T> result) {
        if (result != null && result.getError() == null) {
            mLoaded = result;
        }

        super.deliverResult(result);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (mLoaded != null) {
            // because i handle nulls as errors, this should probably deliver an empty arraylist?
            deliverResult(null);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        mLoaded = null;
    }

    public int getAvailableItemCount() {
        return mTotalResults;
    }
}

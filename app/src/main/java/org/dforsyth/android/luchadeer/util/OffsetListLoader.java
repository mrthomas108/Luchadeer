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
import android.util.Log;

import com.android.volley.toolbox.RequestFuture;

import org.dforsyth.android.luchadeer.net.LuchadeerApi;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public abstract class OffsetListLoader<T> extends AsyncTaskLoader<LoaderListResult<T>> {
    private static final String TAG = OffsetListLoader.class.getName();

    private LoaderListResult<T> mLoaded;
    private static int mOffset;
    private int mTotalResults;

    public OffsetListLoader(Context context, int initialOffset) {
        super(context);
        mOffset = initialOffset;
    }

    public abstract void makeRequest(RequestFuture<LuchadeerApi.GiantBombResponse<ArrayList<T>>> future, int offset);

    @Override
    public LoaderListResult<T> loadInBackground() {
        RequestFuture<LuchadeerApi.GiantBombResponse<ArrayList<T>>> future = RequestFuture.newFuture();

        Log.d("loadInBackground", "loading with offset " + mOffset);

        makeRequest(future, mOffset);

        Log.d(TAG, "waiting for response");
        LuchadeerApi.GiantBombResponse<ArrayList<T>> response = null;
        Exception error = null;
        try {
            response = future.get();

            mTotalResults = response.getNumberOfTotalResults();
        } catch (InterruptedException e) {
            e.printStackTrace();
            error = e;
        } catch (ExecutionException e) {
            e.printStackTrace();
            error = e;
        }

        return new LoaderListResult<T>(response == null ? null : response.getResults(), error);
    }


    @Override
    public void deliverResult(LoaderListResult<T> result) {
        if (result != null) {
            if (result.getError() == null) {
                mLoaded = result;
            }
        }

        super.deliverResult(result);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        Log.d(TAG, "onStartLoading");
        if (mLoaded != null) {
            // because i handle nulls as errors, this should probably deliver an empty arraylist?
            deliverResult(null);
        } else {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        Log.d(TAG, "onStopLoading");
        cancelLoad();
    }

    @Override
    protected void onReset() {
        Log.d(TAG, "onReset");
        super.onReset();
        onStopLoading();
        mLoaded = null;
    }

    public int getAvailableItemCount() {
        return mTotalResults;
    }
}

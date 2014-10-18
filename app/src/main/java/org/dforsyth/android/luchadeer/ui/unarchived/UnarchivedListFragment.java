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

package org.dforsyth.android.luchadeer.ui.unarchived;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.android.volley.toolbox.RequestFuture;

import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.model.youtube.YouTubeVideo;
import org.dforsyth.android.luchadeer.net.LuchadeerApi;
import org.dforsyth.android.luchadeer.ui.util.ContentListFragment;
import org.dforsyth.android.luchadeer.ui.util.PaginatedListView;
import org.dforsyth.android.luchadeer.util.LoaderResult;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class UnarchivedListFragment extends ContentListFragment implements
        DrawerLayout.DrawerListener,
        LoaderManager.LoaderCallbacks<LoaderResult<LuchadeerApi.YouTubeListResponse>>{

    private static final String TAG = UnarchivedListFragment.class.getName();

    private ContentListAdapter<YouTubeVideo> mAdapter;
    private ArrayList<YouTubeVideo> mVideos;
    private OnUnarchivedSelectedListener mListener;

    private ActionBar mActionBar;

    private PaginatedListView mListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private String mNextPageToken;

    private static final int UNARCHIVED_LIST_LOADER_ID = 4;

    private static final String STATE_VIDEOS = "videos";
    private static final String STATE_TOKEN = "token";

    public static UnarchivedListFragment newInstance() {
        return new UnarchivedListFragment();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mActionBar = getActivity().getActionBar();
        mActionBar.setTitle("Unarchived");

        mListView = (PaginatedListView) getListView();
        mSwipeRefreshLayout = getSwipeRefreshLayout();

        mVideos = new ArrayList<YouTubeVideo>();

        ArrayList<YouTubeVideo> videos = null;
        String nextPageToken = null;
        if (savedInstanceState != null) {
            videos = savedInstanceState.getParcelableArrayList(STATE_VIDEOS);
            nextPageToken = savedInstanceState.getString(STATE_TOKEN);
        }

        if (videos == null) {
            getLoaderManager().initLoader(UNARCHIVED_LIST_LOADER_ID, null, this);
        } else {
            onYouTubeVideoRequestCompleted(videos, nextPageToken);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_VIDEOS, mVideos);
        outState.putString(STATE_TOKEN, mNextPageToken);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnUnarchivedSelectedListener) activity;
    }

    @Override
    public void onDrawerSlide(View view, float v) {

    }

    @Override
    public void onDrawerOpened(View view) {
    }

    @Override
    public void onDrawerClosed(View view) {
    }

    @Override
    public void onDrawerStateChanged(int i) {

    }

    private static class YouTubeChannelLoader extends AsyncTaskLoader<LoaderResult<LuchadeerApi.YouTubeListResponse>> {
        private String mPageToken;
        private LoaderResult<LuchadeerApi.YouTubeListResponse> mLoaded;

        public YouTubeChannelLoader(Context context, String pageToken) {
            super(context);
            mPageToken = pageToken;
        }

        @Override
        public void deliverResult(LoaderResult<LuchadeerApi.YouTubeListResponse> result) {
            if (result != null && result.getError() == null) {
                mLoaded = result;
            }

            super.deliverResult(result);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            if (mLoaded != null) {
                deliverResult(null);
            } else {
                forceLoad();
            }
        }

        @Override
        public LoaderResult<LuchadeerApi.YouTubeListResponse> loadInBackground() {
            RequestFuture<LuchadeerApi.YouTubeListResponse> future = RequestFuture.newFuture();

            LuchadeerApi api = LuchadeerApi.getInstance(null);

            api.unarchivedVideos(this, future, future, mPageToken, null);

            LuchadeerApi.YouTubeListResponse response = null;
            Exception error = null;
            try {
                response = future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                error = e;
            } catch (ExecutionException e) {
                e.printStackTrace();
                error = e;
            }

            return new LoaderResult<LuchadeerApi.YouTubeListResponse>(response == null ? null : response, error);
        }
    }

    @Override
    public SwipeRefreshLayout.OnRefreshListener getOnRefreshListener() {
        return new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // mNextPageToken = null;
                mSwipeRefreshLayout.setEnabled(false);
                getLoaderManager().restartLoader(UNARCHIVED_LIST_LOADER_ID, null, UnarchivedListFragment.this);
            }
        };
    }

    @Override
    public PaginatedListView.OnRequestNextPageListener getRequestNextPageListener() {
        return new PaginatedListView.OnRequestNextPageListener() {
            @Override
            public void onRequestNextPage() {
                getLoaderManager().restartLoader(UNARCHIVED_LIST_LOADER_ID, null, UnarchivedListFragment.this);
            }
        };
    }


    @Override
    public Loader<LoaderResult<LuchadeerApi.YouTubeListResponse>> onCreateLoader(int i, Bundle bundle) {
        return new YouTubeChannelLoader(getActivity(), isRefreshing() ? null : mNextPageToken);
    }

    private void onYouTubeVideoRequestCompleted(ArrayList<YouTubeVideo> videos, String nextPageToken) {
        if (mAdapter == null) {
            mAdapter = new YouTubeVideosAdapter(getActivity());
            setListAdapter(mAdapter);
        }

        if (videos == null) {
            return;
        }

        if (isRefreshing()) {
            mVideos.clear();
        }

        mVideos.addAll(videos);
        mNextPageToken = nextPageToken;

        if (nextPageToken == null) {
            Log.d(TAG, "got " + videos.size() + " results " + mAdapter.getCount());
            mListView.setAvailableItemCount(mVideos.size());
        } else {
            Log.d(TAG, "adding 10");
            mListView.setAvailableItemCount(mVideos.size() + 10);
        }

        mAdapter.notifyDataSetChanged();

        mListView.setLoadingNextPage(false);
        mSwipeRefreshLayout.setEnabled(true);
        setRefreshing(false);
        setListShown(true);
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<LuchadeerApi.YouTubeListResponse>> loader, LoaderResult<LuchadeerApi.YouTubeListResponse> result) {
        if (result == null) {
            return;
        }
        LuchadeerApi.YouTubeListResponse response = result.getResult();
        onYouTubeVideoRequestCompleted(response.getItems(), response.getNextPageToken());
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<LuchadeerApi.YouTubeListResponse>> loader) {

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        YouTubeVideo video = mAdapter.getItem(position);
        // Toast.makeText(getActivity(), "video is " + video.getVideoId(), Toast.LENGTH_SHORT).show();
        mListener.onUnarchivedSelected(video);
    }

    public interface OnUnarchivedSelectedListener {
        public void onUnarchivedSelected(YouTubeVideo video);
    }

    private class YouTubeVideosAdapter extends ContentListAdapter<YouTubeVideo> {

        public YouTubeVideosAdapter(Context context) {
            super(context, R.layout.video_list_item);
        }

        @Override
        public int getCount() {
            return mVideos.size();
        }

        @Override
        public YouTubeVideo getItem(int position) {
            return mVideos.get(position);
        }
    }
}

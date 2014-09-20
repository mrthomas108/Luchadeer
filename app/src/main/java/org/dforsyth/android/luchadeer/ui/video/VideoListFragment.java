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

package org.dforsyth.android.luchadeer.ui.video;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;

import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.net.LuchadeerApi;
import org.dforsyth.android.luchadeer.model.giantbomb.Video;
import org.dforsyth.android.luchadeer.model.giantbomb.VideoType;
import org.dforsyth.android.luchadeer.persist.LuchadeerPersist;
import org.dforsyth.android.luchadeer.persist.LuchadeerPreferences;
import org.dforsyth.android.luchadeer.persist.db.LuchadeerContract;
import org.dforsyth.android.luchadeer.persist.provider.BatchContentObserver;
import org.dforsyth.android.luchadeer.ui.util.ContentListFragment;
import org.dforsyth.android.luchadeer.ui.util.PaginatedListView;
import org.dforsyth.android.luchadeer.ui.util.ParallaxListView;
import org.dforsyth.android.luchadeer.util.LoaderListResult;
import org.dforsyth.android.luchadeer.util.OffsetListLoader;
import org.dforsyth.android.luchadeer.util.Util;
import org.dforsyth.android.luchadeer.util.VideoUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class VideoListFragment extends ContentListFragment implements
        DrawerLayout.DrawerListener, LoaderManager.LoaderCallbacks<LoaderListResult<Video>> {
    private static final String TAG = VideoListFragment.class.getName();

    private ArrayList<VideoType> mVideoTypes;
    private ArrayList<Video> mVideos;
    private ArrayList<Video> mVideosTmp;
    private int mOffset;
    private int mOffsetTmp;
    private int mTotalResults;

    private VideoArrayAdapter mVideoArrayAdapter;
    private String mCategoryId;
    private ParallaxListView mListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private LuchadeerApi mApi;
    private LuchadeerPersist mPersist;
    private LuchadeerPreferences mPreferences;

    private ActionBar mActionBar;

    private OnVideoSelectedListener mOnVideoSelectedListener;

    private final static String ARG_CATEGORY_ID = "category_id";
    private static final String STATE_VIDEO_TYPES = "video_types";
    private static final String STATE_VIDEOS = "videos";
    private static final String STATE_TOTAL_RESULTS = "total_results";
    private static final String STATE_CATEGORY_ID = "category_id";
    private static final String STATE_OFFSET = "offset";

    private static final int VIDEOS_LIST_LOADER_ID = 1;

    public VideoListFragment() {

    }

    public static VideoListFragment newInstance(String categoryId) {

        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);

        VideoListFragment fragment = new VideoListFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mApi = LuchadeerApi.getInstance(activity.getApplicationContext());
        mPersist = LuchadeerPersist.getInstance(activity.getApplicationContext());
        mPreferences = LuchadeerPreferences.getInstance(activity.getApplicationContext());

        setOnVideoSelectedListener((OnVideoSelectedListener) activity);
    }

    @Override
    public SwipeRefreshLayout.OnRefreshListener getOnRefreshListener() {
        return new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setEnabled(false);
                getLoaderManager().restartLoader(VIDEOS_LIST_LOADER_ID, null, VideoListFragment.this);
            }
        };
    }

    @Override
    public PaginatedListView.OnRequestNextPageListener getRequestNextPageListener() {
        return new PaginatedListView.OnRequestNextPageListener() {
            @Override
            public void onRequestNextPage() {
                getLoaderManager().restartLoader(VIDEOS_LIST_LOADER_ID, null, VideoListFragment.this);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // just always say retry, there wont be empty categories
        setEmptyText(getString(R.string.video_list_empty));

        mActionBar = getActivity().getActionBar();

        // override
        mListView = (ParallaxListView) getListView();
        mSwipeRefreshLayout = getSwipeRefreshLayout();

        mVideos = new ArrayList<Video>();
        mOffset = 0;

        if (savedInstanceState != null) {
            // load old state
            mVideoTypes = (ArrayList<VideoType>) savedInstanceState.getSerializable(STATE_VIDEO_TYPES);
            mVideosTmp = savedInstanceState.getParcelableArrayList(STATE_VIDEOS);
            mTotalResults = savedInstanceState.getInt(STATE_TOTAL_RESULTS);
            mCategoryId = savedInstanceState.getString(STATE_CATEGORY_ID);
            mOffsetTmp = savedInstanceState.getInt(STATE_OFFSET);
        }

        // this should actually check mVideosTmp
        if (mVideoTypes == null) {
            requestVideoTypes();
        } else {
            onVideoTypesRequestCompleted(mVideoTypes);
        }
    }

    private BatchContentObserver observer;

    @Override
    public void onStart() {
        super.onStart();

        observer = new BatchContentObserver(new BatchContentObserver.OnChangeListener() {
            @Override
            public void onChange(boolean selfChange) {
                if (mVideoArrayAdapter != null) {
                    mVideoArrayAdapter.notifyDataSetChanged();
                }
            }
        });

        getActivity().getContentResolver().registerContentObserver(
                LuchadeerContract.VideoViewStatus.CONTENT_URI,
                false,
                observer
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVideoArrayAdapter != null) {
            mVideoArrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(STATE_VIDEO_TYPES, mVideoTypes);
        outState.putParcelableArrayList(STATE_VIDEOS, mVideos);
        outState.putInt(STATE_TOTAL_RESULTS, mTotalResults);
        outState.putString(STATE_CATEGORY_ID, mCategoryId);
        outState.putInt(STATE_OFFSET, mOffset);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().getContentResolver().unregisterContentObserver(observer);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // this will kill video types requests
        mApi.cancelRequests(this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mOnVideoSelectedListener != null) {
            mOnVideoSelectedListener.onVideoSelected(mVideoArrayAdapter.getItem(position));
        }
    }

    private void setupActionBarForFragment() {
        Log.d(TAG, "setupActionBarForFragment");
        if (mVideoTypes != null && mVideoTypes.size() > 0) {
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            mActionBar.setTitle("");
        }
        mActionBar.setSubtitle(null);
    }

    private void setupActionBarForDrawer() {
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        mActionBar.setTitle(getString(R.string.app_name));
        mActionBar.setSubtitle(null);
    }

    @Override
    public void onDrawerSlide(View view, float slideOffset) {
    }

    @Override
    public void onDrawerOpened(View view) {
        setupActionBarForDrawer();
    }

    @Override
    public void onDrawerClosed(View view) {
        setupActionBarForFragment();
    }

    @Override
    public void onDrawerStateChanged(int i) {

    }

    @Override
    public Loader<LoaderListResult<Video>> onCreateLoader(int i, Bundle bundle) {
        return new VideosListLoader(getActivity(), isRefreshing() ? 0 : mOffset, mCategoryId);
    }

    @Override
    public void onLoadFinished(Loader<LoaderListResult<Video>> arrayListLoader, LoaderListResult<Video> result) {
        // discard null
        if (result == null) {
            return;
        }

        ArrayList<Video> videos = result.getResult();
        Exception error = result.getError();

        if (videos != null) {
            VideosListLoader loader = (VideosListLoader) arrayListLoader;
            onVideosRequestCompleted(videos, loader.getAvailableItemCount(), videos.size());
        } else {
            Util.handleVolleyError(getActivity(), error);
            onRequestVideosFailed();
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderListResult<Video>> arrayListLoader) {
    }

    private class VideoArrayAdapter extends ContentListAdapter<Video> {
        private final static String LAST_WATCHED_KEY = "lastWatched";

        public VideoArrayAdapter(Context context) {
            super(context, R.layout.video_list_item);
        }

        @Override
        public int getCount() {
            return mVideos.size();
        }

        @Override
        public Video getItem(int position) {
            return mVideos.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            final ContentViewHolder holder = (ContentViewHolder) view.getTag();

            TextView lastWatched = (TextView) holder.extraViews.putIfAbsent(LAST_WATCHED_KEY, view.findViewById(R.id.last_watched));
            if (lastWatched == null) {
                lastWatched = (TextView) holder.extraViews.get(LAST_WATCHED_KEY);
            }

            Video video = getItem(position);

            lastWatched.setVisibility(View.GONE);
            lastWatched.clearAnimation();

            mPersist.getVideoWatched(getLoaderManager(), video.getId(), new LuchadeerPersist.VideoWatchedResultListener() {
                @Override
                public void onVideoWatchedResult(long watched) {
                    if (watched > 0) {
                        TextView held = (TextView) holder.extraViews.get(LAST_WATCHED_KEY);

                        held.setVisibility(View.VISIBLE);
                        // fade this in for now...
                        held.setAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
                        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");
                        held.setText(String.format("Last watched: %s", sdf.format(new Date(watched))));
                    }
                }
            });

            return view;
        }
    }

    private class VideoTypeSpinnerAdapter extends ArrayAdapter<String> {

        public VideoTypeSpinnerAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.category_list_header, parent, false);
            }

            TextView subTitle = (TextView) convertView.findViewById(R.id.sub_title);
            subTitle.setText(getItem(position));

            return convertView;
        }
    }

    public void reloadVideosList() {
        setListShown(false);
        // we clear so that we have the offset we want. The other option was to use setRefreshing
        resetVideosList();
        // setRefreshing(true);
        mSwipeRefreshLayout.setEnabled(false);
        getLoaderManager().restartLoader(VIDEOS_LIST_LOADER_ID, null, VideoListFragment.this);
    }

    public void onRequestVideosFailed() {
        mListView.setLoadingNextPage(false);
        setRefreshing(false);

        if (mVideoArrayAdapter == null) {
            mVideoArrayAdapter = new VideoArrayAdapter(getActivity());
            setListAdapter(mVideoArrayAdapter);
        }

        // enable refresh so that the user can try again?
        mSwipeRefreshLayout.setEnabled(true);
    }

    public void onVideosRequestCompleted(ArrayList<Video> videos, int totalNumberOfResults, int offset) {
        if (mVideoArrayAdapter == null) {
            mVideoArrayAdapter = new VideoArrayAdapter(getActivity());
            setListAdapter(mVideoArrayAdapter);
        }

        if (videos == null) {
            return;
        }

        mTotalResults = totalNumberOfResults;
        mListView.setAvailableItemCount(mTotalResults);

        if (isRefreshing()) {
            resetVideosList();
        }

        mOffset += offset; // videos.size();
        mVideos.addAll(filterTrailers(videos));

        mVideoArrayAdapter.notifyDataSetChanged();

        mListView.setLoadingNextPage(false);
        mSwipeRefreshLayout.setEnabled(true);
        setRefreshing(false);
        setListShown(true);
    }

    private ArrayList<Video> filterTrailers(ArrayList<Video> videos) {
        ArrayList<Video> filtered = new ArrayList<Video>();

        if (!mPreferences.getFilterTrailers()) {
            return videos;
        }

        for (Video video : videos) {
            if (video.getVideoType() != null && !video.getVideoType().equals(VideoUtil.VIDEO_TYPE_TRAILERS)) {
                filtered.add(video);
            }
        }

        return filtered;
    }

    private void resetVideosList() {
        mVideos.clear();
        mOffset = 0;
    }

    private void requestVideoTypes() {
        mApi.videoTypes(
                this,
                new Response.Listener<LuchadeerApi.GiantBombResponse<ArrayList<VideoType>>>() {
                    @Override
                    public void onResponse(LuchadeerApi.GiantBombResponse<ArrayList<VideoType>> response) {
                        onVideoTypesRequestCompleted(response.getResults());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        onRequestVideoTypesFailed();
                    }
                });
    }

    private void onRequestVideoTypesFailed() {
        mCategoryId = null;
        getLoaderManager().initLoader(VIDEOS_LIST_LOADER_ID, null, this);
    }

    private void onVideoTypesRequestCompleted(ArrayList<VideoType> videoTypes) {
        mVideoTypes = videoTypes;

        // use strings for now because its easier to add "Latest"
        ArrayList<String> videoTypeNames = new ArrayList<String>();
        for (VideoType videoType : videoTypes) {
            videoTypeNames.add(videoType.getName());
        }

        // incase a latest category is ever added.
        if (!videoTypeNames.contains("Latest")) {
            videoTypeNames.add(0, "Latest");
        }

        VideoTypeSpinnerAdapter adapter = new VideoTypeSpinnerAdapter(getActivity(), R.layout.category_list_item);
        adapter.addAll(videoTypeNames);

        mActionBar.setListNavigationCallbacks(
                adapter,
                new ActionBar.OnNavigationListener() {
                    boolean ready = false;
                    @Override
                    public boolean onNavigationItemSelected(int i, long l) {
                        // handle initial set
                        if (!ready) {
                            ready = true;
                            return true;
                        }

                        String selected;
                        if (i != 0) {
                            selected = mVideoTypes.get(i - 1).getId();
                        } else {
                            selected = null;
                        }

                        if ((selected == null && mCategoryId == null) || (selected != null && selected.equals(mCategoryId))) {
                            return true;
                        }

                        Log.d(TAG, "old: " + mCategoryId + " new: " + selected);
                        mCategoryId = selected;

                        mVideoArrayAdapter = new VideoArrayAdapter(getActivity());
                        setListAdapter(mVideoArrayAdapter);
                        resetVideosList();
                        setListShown(false);

                        getLoaderManager().restartLoader(VIDEOS_LIST_LOADER_ID, null, VideoListFragment.this);

                        return true;
                    }
                }
        );

        setupActionBarForFragment();

        mPreferences.setVideoTypeCache(videoTypes);

        if (mVideosTmp == null || mVideosTmp.isEmpty()) {
            getLoaderManager().initLoader(VIDEOS_LIST_LOADER_ID, null, this);
        } else {
            onVideosRequestCompleted(mVideosTmp, mTotalResults, mOffsetTmp);
        }
    }

    public interface OnVideoSelectedListener {
        public void onVideoSelected(Video mVideo);
    }

    public void setOnVideoSelectedListener(OnVideoSelectedListener onVideoSelectedListener) {
        mOnVideoSelectedListener = onVideoSelectedListener;
    }

    private static class VideosListLoader extends OffsetListLoader<Video> {
        private String mCategory;

        public VideosListLoader(Context context, int initialOffset, String category) {
            super(context, initialOffset);
            mCategory = category;
        }

        @Override
        public void makeRequest(RequestFuture<LuchadeerApi.GiantBombResponse<ArrayList<Video>>> future, int offset) {
            LuchadeerApi.getInstance(getContext()).videos(this, mCategory, offset, future, future);
        }
    }
}

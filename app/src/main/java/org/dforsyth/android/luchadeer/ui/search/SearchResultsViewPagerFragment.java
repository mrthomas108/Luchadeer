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

package org.dforsyth.android.luchadeer.ui.search;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.RequestFuture;

import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.SearchActivity;
import org.dforsyth.android.luchadeer.net.LuchadeerApi;
import org.dforsyth.android.luchadeer.model.giantbomb.SearchResult;
import org.dforsyth.android.luchadeer.util.LoaderListResult;
import org.dforsyth.android.luchadeer.util.Util;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by dforsyth on 7/24/14.
 */
public class SearchResultsViewPagerFragment extends Fragment implements
        SearchActivity.OnSearchQueryUpdatedListener,
        ViewPager.OnPageChangeListener,
        LoaderManager.LoaderCallbacks<LoaderListResult<SearchResult>> {
    private static final String TAG = SearchResultsViewPagerFragment.class.getName();

    private static final String ARG_QUERY = "query";
    private static final String STATE_QUERY = "query";
    private static final String STATE_SEARCH_RESULTS = "search_results";

    private static final int SEARCH_RESULTS_LOADER_ID = 3;

    private Activity mActivity;
    private ActionBar mActionBar;

    private View rootView;
    private ViewPager mViewPager;

    private String mQuery;
    private ArrayList<SearchResult> mResults;
    private ArrayList<SearchResult> mGameResults;
    private ArrayList<SearchResult> mVideoResults;

    private static final String RESOURCE_TYPE_GAME = "game";
    private static final String RESOURCE_TYPE_VIDEO = "video";
    private static final String[] SEARCH_RESOURCE_TYPES = new String[]{RESOURCE_TYPE_GAME, RESOURCE_TYPE_VIDEO};

    public SearchResultsViewPagerFragment() {

    }

    public static SearchResultsViewPagerFragment newInstance(String query) {
        SearchResultsViewPagerFragment fragment = new SearchResultsViewPagerFragment();

        Bundle args = new Bundle();
        args.putString(ARG_QUERY, query);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int i) {
        mActionBar.setSelectedNavigationItem(i);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private class SearchResultsTabListener implements ActionBar.TabListener {

        private int mItem;
        private ViewPager mPager;

        public SearchResultsTabListener(ViewPager pager, int item) {
            mItem = item;
            mPager = pager;
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            mPager.setCurrentItem(mItem);
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search_results_view_pager, container, false);

        mViewPager  = (ViewPager) rootView.findViewById(R.id.results_pager);

        mActivity = getActivity();
        mActionBar = mActivity.getActionBar();
        mQuery = getArguments().getString(ARG_QUERY);

        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mActionBar.addTab(
                mActionBar.newTab()
                        .setText(getString(R.string.tab_videos))
                        .setTabListener(new SearchResultsTabListener(mViewPager, 0)));

        mActionBar.addTab(
                mActionBar.newTab()
                        .setText(getString(R.string.tab_games))
                        .setTabListener(new SearchResultsTabListener(mViewPager, 1)));


        mViewPager.setOnPageChangeListener(this);
        mViewPager.setAdapter(new PagerAdapter(getChildFragmentManager()));

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onviewcreated");
        String query = null;
        if (savedInstanceState != null) {
            query = savedInstanceState.getString(STATE_QUERY);
            mResults = savedInstanceState.getParcelableArrayList(STATE_SEARCH_RESULTS);
        }
        // we need results
        if (mResults == null) {
            getLoaderManager().initLoader(SEARCH_RESULTS_LOADER_ID, null, this);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_QUERY, mQuery);
        outState.putParcelableArrayList(STATE_SEARCH_RESULTS, mResults);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        mActionBar.removeAllTabs();
    }

    @Override
    public void onSearchQueryUpdated(String newQuery) {
        mQuery = newQuery;
        mResults = null;
        hideResultLists();
        Log.d(TAG, "calling restart loader");
        getLoaderManager().restartLoader(SEARCH_RESULTS_LOADER_ID, null, this);
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        private FragmentManager mFragmentManager;

        public PagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
            case (0):
                return VideoSearchResultsListFragment.newInstance(mVideoResults);
            case (1):
                return GameSearchResultListFragment.newInstance(mGameResults);
            default:
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        public Fragment getFragmentAtPosition(int position) {
            Fragment fragment = mFragmentManager.findFragmentByTag("android:switcher:" + R.id.results_pager + ":" + position);

            return fragment;
        }
    }

    private void onSearchResultsRequestCompleted(ArrayList<SearchResult> response) {
        mResults = response;
        mVideoResults = new ArrayList<SearchResult>();
        mGameResults = new ArrayList<SearchResult>();

        for (SearchResult result : response) {
            String resourceType = result.getResourceType();
            if (resourceType.equals(RESOURCE_TYPE_GAME)) {
                mGameResults.add(result);
            } else if (resourceType.equals(RESOURCE_TYPE_VIDEO)) {
                mVideoResults.add(result);
            }
        }

        PagerAdapter adapter = (PagerAdapter) mViewPager.getAdapter();
        SearchResultListFragment fragment;
        fragment = (SearchResultListFragment) adapter.getFragmentAtPosition(0);
        if (fragment != null) {
            fragment.setSearchResults(mVideoResults);
        }
        fragment = (SearchResultListFragment) adapter.getFragmentAtPosition(1);
        if (fragment != null) {
            fragment.setSearchResults(mGameResults);
        }
    }

    private void onSearchResultsRequestFailed() {
        onSearchResultsRequestCompleted(new ArrayList<SearchResult>());
    }

    private void hideResultLists() {
        PagerAdapter adapter = (PagerAdapter) mViewPager.getAdapter();
        SearchResultListFragment fragment;
        fragment = (SearchResultListFragment) adapter.getFragmentAtPosition(0);
        if (fragment != null) {
            if (fragment.getView() != null) {
                fragment.clearResults();
            }
        }
        fragment = (SearchResultListFragment) adapter.getFragmentAtPosition(1);
        if (fragment != null) {
            if (fragment.getView() != null) {
                fragment.clearResults();
            }
        }
    }

    private static class SearchResultsLoader extends AsyncTaskLoader<LoaderListResult<SearchResult>> {
        private String mQuery;
        private LoaderListResult<SearchResult> mResults;

        public SearchResultsLoader(Context context, String query) {
            super(context);
            mQuery = query;
        }

        @Override
        public LoaderListResult<SearchResult> loadInBackground() {
            RequestFuture<LuchadeerApi.GiantBombResponse<ArrayList<SearchResult>>> future = RequestFuture.newFuture();

            LuchadeerApi.getInstance(getContext()).search(this, mQuery, SEARCH_RESOURCE_TYPES, future, future);

            LuchadeerApi.GiantBombResponse<ArrayList<SearchResult>> response = null;
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

            return new LoaderListResult<SearchResult>(response == null ? null : response.getResults(), error);
        }

        @Override
        public void deliverResult(LoaderListResult<SearchResult> result) {
            // when in reset state, we don't deliver
            if (isReset()) {
                return;
            }
            mResults = result;
            if (isStarted()) {
                super.deliverResult(result);
            }
        }

        @Override
        protected void onStartLoading() {
            if (mResults != null) {
                deliverResult(mResults);
            } else {
                forceLoad();
            }
        }

        @Override
        protected void onStopLoading() {
            super.onStopLoading();
            cancelLoad();
        }
    }


    @Override
    public Loader<LoaderListResult<SearchResult>> onCreateLoader(int i, Bundle bundle) {
        Log.d(TAG, "oncCreateLoader");
        return new SearchResultsLoader(getActivity(), mQuery);
    }

    @Override
    public void onLoadFinished(Loader<LoaderListResult<SearchResult>> loader, LoaderListResult<SearchResult> result) {
        ArrayList<SearchResult> searchResults = result.getResult();
        Exception error = result.getError();

        if (searchResults != null) {
            onSearchResultsRequestCompleted(searchResults);
        } else {
            Util.handleVolleyError(getActivity(), error);
            onSearchResultsRequestFailed();
        }
        getLoaderManager().destroyLoader(SEARCH_RESULTS_LOADER_ID);
    }

    @Override
    public void onLoaderReset(Loader<LoaderListResult<SearchResult>> loader) {
    }

}

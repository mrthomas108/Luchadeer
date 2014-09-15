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

package org.dforsyth.android.luchadeer.ui.game;


import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.toolbox.RequestFuture;

import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.SearchActivity;
import org.dforsyth.android.luchadeer.net.LuchadeerApi;
import org.dforsyth.android.luchadeer.model.giantbomb.Game;
import org.dforsyth.android.luchadeer.ui.util.ContentListFragment;
import org.dforsyth.android.luchadeer.ui.util.PaginatedListView;
import org.dforsyth.android.luchadeer.ui.util.ParallaxListView;
import org.dforsyth.android.luchadeer.util.LoaderListResult;
import org.dforsyth.android.luchadeer.util.OffsetListLoader;
import org.dforsyth.android.luchadeer.util.Util;

import java.util.ArrayList;


public class GameListFragment extends ContentListFragment implements
        DrawerLayout.DrawerListener, LoaderManager.LoaderCallbacks<LoaderListResult<Game>> {
    private static final String TAG = GameListFragment.class.getName();

    private GameArrayAdapter mArrayAdapter;
    private ParallaxListView mListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ActionBar mActionBar;

    private OnGameSelectedListener mOnGameSelectedListener;

    private ArrayList<Game> mGames;
    private int mTotalResults;

    private static final String STATE_GAMES = "games";
    private static final String STATE_TOTAL_RESULTS = "total_results";

    private static final int GAMES_LIST_LOADER_ID = 1;

    public GameListFragment() {
        // Required empty public constructor
    }

    public static GameListFragment newInstance() {
        return new GameListFragment();
    }

    @Override
    public SwipeRefreshLayout.OnRefreshListener getOnRefreshListener() {
        return new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setEnabled(false);
                getLoaderManager().restartLoader(GAMES_LIST_LOADER_ID, null, GameListFragment.this);
            }
        };
    }

    @Override
    public PaginatedListView.OnRequestNextPageListener getRequestNextPageListener() {
        return new PaginatedListView.OnRequestNextPageListener() {
            @Override
            public void onRequestNextPage() {
                getLoaderManager().restartLoader(GAMES_LIST_LOADER_ID, null, GameListFragment.this);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setEmptyText(getString(R.string.game_list_empty));

        mActionBar = getActivity().getActionBar();

        mListView = (ParallaxListView) getListView();
        mSwipeRefreshLayout = getSwipeRefreshLayout();

        setupActionBarForFragment();

        mGames = new ArrayList<Game>();

        ArrayList<Game> games = null;
        int totalResults = 0;
        if (savedInstanceState != null) {
            games = savedInstanceState.getParcelableArrayList(STATE_GAMES);
            totalResults = savedInstanceState.getInt(STATE_TOTAL_RESULTS);
        }

        if (games == null || games.size() == 0) {
            getLoaderManager().initLoader(GAMES_LIST_LOADER_ID, null, this);
        } else {
            onGamesRequestCompleted(games, totalResults);
        }
    }

    private void setupActionBarForFragment() {
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        mActionBar.setTitle(getResources().getString(R.string.app_name));
        mActionBar.setSubtitle("Games");
    }

    private void setupActionBarForDrawer() {
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        mActionBar.setTitle(getResources().getString(R.string.app_name));
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
    public Loader<LoaderListResult<Game>> onCreateLoader(int i, Bundle bundle) {
        Log.d(TAG, "onCreateLoader");
        return new GamesListLoader(getActivity(), isRefreshing() ? 0 : mGames.size());
    }

    @Override
    public void onLoadFinished(Loader<LoaderListResult<Game>> arrayListloader, LoaderListResult<Game> result) {
        Log.d(TAG, "onLoadFinished");

        // discard null
        if (result == null) {
            return;
        }

        ArrayList<Game> games = result.getResult();
        Exception error = result.getError();

        if (games != null) {
            GamesListLoader loader = (GamesListLoader) arrayListloader;
            onGamesRequestCompleted(games, loader.getAvailableItemCount());
        } else {
            Util.handleVolleyError(getActivity(), error);
            onGamesRequestFailed();
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderListResult<Game>> arrayListLoader) {
    }

    private class GameArrayAdapter extends ContentListAdapter<Game> {
        public GameArrayAdapter(Context context) { super(context, R.layout.game_list_item);}

        @Override
        public int getCount() {
            return mGames.size();
        }

        @Override
        public Game getItem(int position) {
            return mGames.get(position);
        }
    }

    private void onGamesRequestFailed() {
        mListView.setLoadingNextPage(false);
        setRefreshing(false);

        if (mArrayAdapter == null) {
            mArrayAdapter = new GameArrayAdapter(getActivity());
            setListAdapter(mArrayAdapter);
        }

        mSwipeRefreshLayout.setEnabled(true);
    }

    private void onGamesRequestCompleted(ArrayList<Game> games, int totalResults) {
        if (mArrayAdapter == null) {
            mArrayAdapter = new GameArrayAdapter(getActivity());
            setListAdapter(mArrayAdapter);
        }

        if (games == null) {
            return;
        }

        mTotalResults = totalResults;
        mListView.setAvailableItemCount(mTotalResults);

        if (isRefreshing()) {
            mGames.clear();
        }
        mGames.addAll(games);
        mArrayAdapter.notifyDataSetChanged();

        mListView.setLoadingNextPage(false);
        mSwipeRefreshLayout.setEnabled(true);
        setRefreshing(false);
        setListShown(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mOnGameSelectedListener.onGameSelected(mArrayAdapter.getItem(position).getId());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.search_menu_item) {
            Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
            startActivity(searchIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public interface OnGameSelectedListener {
        public void onGameSelected(int gameId);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mOnGameSelectedListener = (OnGameSelectedListener) activity;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_GAMES, mGames);
        outState.putInt(STATE_TOTAL_RESULTS, mTotalResults);
    }

    private static class GamesListLoader extends OffsetListLoader<Game> {

        public GamesListLoader(Context context, int initialOffset) {
            super(context, initialOffset);
        }

        @Override
        public void makeRequest(RequestFuture<LuchadeerApi.GiantBombResponse<ArrayList<Game>>> future, int offset) {
            LuchadeerApi.getInstance(getContext()).games(this, offset, future, future);
        }
    }
}

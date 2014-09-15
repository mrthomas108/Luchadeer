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

package org.dforsyth.android.luchadeer;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.SearchView;

import org.dforsyth.android.luchadeer.ui.search.SearchResultsViewPagerFragment;

public class SearchActivity extends BaseActivity {
    private final static String TAG = SearchActivity.class.getName();

    private final static String RESULT_PAGER_FRAGMENT_TAG = "result_pager_fragment";
    private final static String STATE_QUERY = "query";

    private SearchResultsViewPagerFragment mSearchResultsViewPagerFragment;

    private String mQuery;
    private SearchView mSearchView;

    public interface OnSearchQueryUpdatedListener {
        public void onSearchQueryUpdated(String newQuery);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        if (savedInstanceState != null) {
            mQuery = savedInstanceState.getString(STATE_QUERY);
        }

        getActionBar().setDisplayHomeAsUpEnabled(true);
        if (mQuery == null || mQuery.equals("")) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        mSearchResultsViewPagerFragment = (SearchResultsViewPagerFragment) getSupportFragmentManager()
                .findFragmentByTag(RESULT_PAGER_FRAGMENT_TAG);
        if (mSearchResultsViewPagerFragment == null) {
            mSearchResultsViewPagerFragment = SearchResultsViewPagerFragment
                    .newInstance(intent.getStringExtra(SearchManager.QUERY));

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, mSearchResultsViewPagerFragment, RESULT_PAGER_FRAGMENT_TAG)
                    .commit();
        } else {
            mSearchResultsViewPagerFragment.onSearchQueryUpdated(intent.getStringExtra(SearchManager.QUERY));
        }

        mSearchView.clearFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);

        MenuItem i = menu.findItem(R.id.search_item);

        SearchManager m = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) MenuItemCompat.getActionView(i);
        mSearchView.setIconifiedByDefault(false);
        // mSearchView.setIconified(false);
        mSearchView.setSearchableInfo(m.getSearchableInfo(getComponentName()));

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.isEmpty()) {
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag(RESULT_PAGER_FRAGMENT_TAG);
                    if (fragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .remove(fragment)
                                .commit();
                    }
                }
                mQuery = s;
                return true;
            }
        });

        mSearchView.setQuery(mQuery, false);
        if (mQuery.isEmpty()) {
            mSearchView.requestFocus();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_QUERY, mQuery);
        super.onSaveInstanceState(outState);
    }
}

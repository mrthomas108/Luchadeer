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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.net.LuchadeerApi;
import org.dforsyth.android.luchadeer.model.giantbomb.Image;
import org.dforsyth.android.luchadeer.model.giantbomb.SearchResult;
import org.dforsyth.android.luchadeer.ui.util.FadeNetworkImageView;

import java.util.ArrayList;

/**
 * Created by dforsyth on 7/25/14.
 */
public abstract class SearchResultListFragment extends ListFragment {
    private static final String TAG = SearchResultListFragment.class.getName();

    protected static final String ARG_RESULTS = "results";
    private static final String STATE_RESULTS = "results";

    private LuchadeerApi mApi;
    private ArrayList<SearchResult> mResults;
    private ResultAdapter mAdapter;

    public void setSearchResults(ArrayList<SearchResult> results) {
        Log.d(TAG, "setting results");

        if (results == null) {
            setListShown(false);
            return;
        }

        mResults = results;
        // make an adapter if we don't already have one
        if (mAdapter == null) {
            mAdapter = new ResultAdapter(getActivity());
            setListAdapter(mAdapter);
        }
        // notifiy
        mAdapter.notifyDataSetChanged();

        // give it to the user
        setListShown(true);
    }

    public void clearResults() {
        // clear the results
        mResults = null;

        // reset position
        mAdapter = new ResultAdapter(getActivity());
        setListAdapter(mAdapter);

        // hide list
        setListShown(false);
    }

    @Override
    public abstract void onListItemClick(ListView l, View v, int position, long id);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApi = LuchadeerApi.getInstance(getActivity());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText("No results");

        ArrayList<SearchResult> results;
        if (savedInstanceState == null) {
            results = getArguments().getParcelableArrayList(ARG_RESULTS);
        } else {
            results = savedInstanceState.getParcelableArrayList(STATE_RESULTS);
        }
        setSearchResults(results);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_RESULTS, mResults);
    }

    private class ResultAdapter extends ArrayAdapter<SearchResult> {

        private LayoutInflater mInflater;

        public ResultAdapter(Context context) {
            super(context, 0);
            mInflater = LayoutInflater.from(getActivity());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.simple_list_item, null);
            }

            TextView t = (TextView) convertView.findViewById(R.id.result_name);
            FadeNetworkImageView iv = (FadeNetworkImageView) convertView.findViewById(R.id.result_image);

            SearchResult result = getItem(position);

            t.setText(result.getName());

            Image i = result.getImage();
            if (i != null) {
                iv.setImageUrl(i.getSuperUrl(), mApi.getImageLoader());
            } else {
                iv.setImageUrl(null, null);
            }

            return convertView;
        }

        @Override
        public SearchResult getItem(int position) {
            return mResults.get(position);
        }

        @Override
        public int getCount() {
            return mResults != null ? mResults.size() : 0;
        }
    }
}

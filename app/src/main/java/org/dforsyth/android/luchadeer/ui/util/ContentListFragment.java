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
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.net.LuchadeerApi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public abstract class ContentListFragment extends SwipeRefreshListFragment {

    public abstract SwipeRefreshLayout.OnRefreshListener getOnRefreshListener();
    public abstract PaginatedListView.OnRequestNextPageListener getRequestNextPageListener();

    private static final int LOADING_NEXT_PAGE_BOUNDARY = 10;

    private ParallaxListView mListView;
    private LinearLayout mLoadingView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mListView = new ParallaxListView(getActivity());
        UiUtil.overrideListView(view, mListView);

        mLoadingView = (LinearLayout) inflater.inflate(R.layout.list_loading_item, null);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SwipeRefreshLayout swipeRefreshLayout = getSwipeRefreshLayout();
        swipeRefreshLayout.setColorSchemeResources(
                R.color.luchadeer_green,
                R.color.luchadeer_red,
                R.color.luchadeer_grey,
                R.color.luchadeer_gold);
        swipeRefreshLayout.setEnabled(false);

        setOnRefreshListener(getOnRefreshListener());

        mListView.setOnRequestNextPageListener(getRequestNextPageListener());
        mListView.setLoadingFooter(mLoadingView);
        mListView.setLoadingBoundary(LOADING_NEXT_PAGE_BOUNDARY);
    }

    public interface Content {
        public String getName();
        public String getImageUrl();
    }

    public static class ContentViewHolder {
        public TextView name;
        public NetworkImageView image;
        public ConcurrentMap<String, View> extraViews = new ConcurrentHashMap<String, View>();
    }

    public class ContentListAdapter<T extends Content> extends ArrayAdapter<T> {

        private LayoutInflater mLayoutInflater;
        private int mResource;
        private LuchadeerApi mApi;

        public ContentListAdapter(Context context, int resource) {
            super(context, 0);
            mResource = resource;
            mLayoutInflater = LayoutInflater.from(getActivity());
            mApi = LuchadeerApi.getInstance(context.getApplicationContext());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ContentViewHolder holder;

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(mResource, parent, false);

                holder = new ContentViewHolder();

                holder.name = (TextView) convertView.findViewById(R.id.content_name);
                holder.image = (NetworkImageView) convertView.findViewById(R.id.content_image);

                // we size these differently for the parallax effect
                ViewGroup.LayoutParams cparams = convertView.getLayoutParams();
                cparams.height = (int) (parent.getHeight() * .35);
                convertView.setLayoutParams(cparams);

                ViewGroup.LayoutParams params = holder.image.getLayoutParams();
                params.height = parent.getHeight() / 2;
                holder.image.setLayoutParams(params);

                convertView.setTag(holder);
            } else {
                holder = (ContentViewHolder) convertView.getTag();
            }

            Content content = getItem(position);

            holder.name.setText(content.getName());

            String imageUrl = content.getImageUrl();
            if (imageUrl != null) {
                holder.image.setImageUrl(imageUrl, mApi.getImageLoader());
            } else {
                holder.image.setImageUrl(null, null);
            }

            return convertView;
        }
    }
}

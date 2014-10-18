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

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.model.youtube.YouTubeVideo;
import org.dforsyth.android.luchadeer.net.LuchadeerApi;
import org.dforsyth.android.luchadeer.ui.util.FadeNetworkImageView;
import org.dforsyth.android.luchadeer.ui.util.UiUtil;

import java.text.SimpleDateFormat;
import java.util.HashMap;

public class UnarchivedDetailFragment extends Fragment implements DrawerLayout.DrawerListener {
    private static final String ARG_YOUTUBE_VIDEO = "youtube_video";

    private YouTubeVideo mYouTubeVideo;
    private ImageView mPlay;
    private View mQuality;
    private View mFavorite;
    private View mDownload;
    private ImageView mViewOnWeb;
    private TextView mDeck;
    private TextView mName;
    private TextView mPublished;
    private TextView mUser;
    private View mRuntime;
    private FadeNetworkImageView mImage;
    private View mShareView;

    public static UnarchivedDetailFragment newInstance(YouTubeVideo youTubeVideo) {
        UnarchivedDetailFragment fragment = new UnarchivedDetailFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_YOUTUBE_VIDEO, youTubeVideo);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_video_detail, container, false);

        mPlay = (ImageView) rootView.findViewById(R.id.video_play);
        mQuality = rootView.findViewById(R.id.video_quality_spinner);
        mFavorite = rootView.findViewById(R.id.video_favorite);
        mDownload = rootView.findViewById(R.id.video_download);
        mViewOnWeb = (ImageView) rootView.findViewById(R.id.video_view_on_web);
        mDeck = (TextView) rootView.findViewById(R.id.video_deck_text);
        mName = (TextView) rootView.findViewById(R.id.video_name_text);
        mPublished = (TextView) rootView.findViewById(R.id.video_publish_date);
        mUser = (TextView) rootView.findViewById(R.id.video_user);
        mRuntime = rootView.findViewById(R.id.video_length_section);
        mImage = (FadeNetworkImageView) rootView.findViewById(R.id.video_image);

        mQuality.setVisibility(View.GONE);
        mFavorite.setVisibility(View.GONE);
        mDownload.setVisibility(View.GONE);
        mRuntime.setVisibility(View.GONE);

        mYouTubeVideo = getArguments().getParcelable(ARG_YOUTUBE_VIDEO);

        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent youtube = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + mYouTubeVideo.getVideoId()));
                startActivity(youtube);
            }
        });

        mViewOnWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewOnWeb = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + mYouTubeVideo.getVideoId()));
                startActivity(viewOnWeb);
            }
        });

        mName.setText(mYouTubeVideo.getName());

        YouTubeVideo.Snippet snippet = mYouTubeVideo.getSnippet();
        if (mDeck != null) {
            mDeck.setText(snippet.getDescription());
        }
        // mPublished.setText(snippet.getPublishedAt().toString());

        SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy h:mm a");
        mPublished.setText(format.format(snippet.getPublishedAt()));

        mUser.setText(String.format("Posted by: %s", snippet.getChannelTitle()));

        HashMap<String, YouTubeVideo.Thumbnail> thumbnails = snippet.getThumbnails();
        if (thumbnails != null && thumbnails.get("high") != null) {
            mImage.setImageUrl(thumbnails.get("high").getUrl(), LuchadeerApi.getInstance(getActivity()).getImageLoader());
        }

        setHasOptionsMenu(true);

        return rootView;
    }

    ShareActionProvider mShareProvider;

    private void setupShareProviderIntent() {
        if (mYouTubeVideo == null || mShareProvider == null) {
            return;
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                String.format("Check out \"%s\" on Giant Bomb Unarchived! %s", mYouTubeVideo.getName(), "http://www.youtube.com/watch?v=" + mYouTubeVideo.getVideoId()));
        shareIntent.setType("text/plain");
        mShareProvider.setShareIntent(shareIntent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.video_detail, menu);

        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        setupShareProviderIntent();

        mShareView = MenuItemCompat.getActionView(item);

        // For now, lets not activate the media router for this fragment. It might be misleading since the video isn't
        // going to play within this app.
        // mActivity.getVideoCastManager().addMediaRouterButton(menu, R.id.media_route_menu_item);
    }

    public void onDrawerSlide(View view, float slideOffset) {
        UiUtil.actionBarItemOnDrawerSlide(mShareView, slideOffset);
    }

    @Override
    public void onDrawerOpened(View view) {
        UiUtil.actionBarItemOnDrawerOpened(mShareView);
    }

    @Override
    public void onDrawerClosed(View view) {
        UiUtil.actionBarItemOnDrawerClosed(mShareView);
    }

    @Override
    public void onDrawerStateChanged(int i) {

    }
}

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

package org.dforsyth.android.luchadeer.ui.player;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;

import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.VideoPlayerActivity;
import org.dforsyth.android.luchadeer.persist.LuchadeerPersist;
import org.dforsyth.android.luchadeer.ui.util.ObservableVideoView;
import org.dforsyth.android.luchadeer.util.VideoUtil;

/**
 * Created by dforsyth on 8/20/14.
 */
public class VideoPlayerFragment extends Fragment implements MediaPlayer.OnCompletionListener, View.OnSystemUiVisibilityChangeListener {
    private final static String TAG = VideoPlayerFragment.class.getName();

    private final static String ARG_VIDEO_URI = "video_uri";
    private final static String ARG_VIDEO_NAME = "video_name";
    private final static String ARG_GIANT_BOMB_ID = "giantbomb_id";

    private Activity mActivity;
    private ActionBar mActionBar;
    private View mDecorView;
    private int mNavigationBarHeight;
    private int mNavigationBarWidth;
    private boolean mMarkable;

    private Handler mHandler;

    private ObservableVideoView mVideoView;
    private MediaController mController;
    private ProgressBar mProgressBar;

    private Uri mVideoUri;
    private String mName;
    private int mGiantBombId;

    private int mPosition;

    public static VideoPlayerFragment newInstance(String name, String uri, int giantBombId) {
        Bundle args = new Bundle();

        args.putString(ARG_VIDEO_URI, uri);
        args.putString(ARG_VIDEO_NAME, name);
        args.putInt(ARG_GIANT_BOMB_ID, giantBombId);

        VideoPlayerFragment fragment = new VideoPlayerFragment();

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();
        mActionBar = mActivity.getActionBar();

        mDecorView = mActivity.getWindow().getDecorView();


        // mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        mHandler = new Handler(Looper.getMainLooper());

        Bundle arguments = getArguments();

        mName = arguments.getString(ARG_VIDEO_NAME);

        String uri = arguments.getString(ARG_VIDEO_URI);
        mVideoUri = Uri.parse(uri);

        mGiantBombId = arguments.getInt(ARG_GIANT_BOMB_ID);

        mActionBar.setTitle(mName);

        Resources resources = getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height_landscape", "dimen", "android");
        int resourceIdB = resources.getIdentifier("navigation_bar_width", "dimen", "android");
        if (resourceId > 0 && (resources.getConfiguration().screenLayout  & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
            mNavigationBarHeight = getResources().getDimensionPixelSize(resourceId);
            mNavigationBarWidth = 0;
        } else {
            mNavigationBarHeight = 0;
            mNavigationBarWidth = getResources().getDimensionPixelSize(resourceIdB);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_video_player, container, false);

        mVideoView = (ObservableVideoView) rootView.findViewById(R.id.video_player);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressbar);

        if (savedInstanceState != null) {
            mPosition = savedInstanceState.getInt("position");
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mVideoView.setVideoURI(mVideoUri);
        mVideoView.setOnCompletionListener(this);
        mDecorView.setOnSystemUiVisibilityChangeListener(this);

        mController = new GBMediaController(mActivity) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    mActivity.finish();
                }
                return super.dispatchKeyEvent(event);
            }
        };

        mController.setAnchorView(mVideoView);
        mController.setPadding(0, 0, mNavigationBarWidth, mNavigationBarHeight);

        mVideoView.setMediaController(mController);

        hideSystemUi();

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mProgressBar.setVisibility(View.GONE);
                mDecorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                | View.SYSTEM_UI_FLAG_IMMERSIVE
                );
                mMarkable = true;
            }
        });

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int error, int extra) {
                if (!mVideoView.isPlaying()) {
                    // if the video is playing, leave the resolution up to the user for now
                    Intent results = new Intent();
                    results.putExtra(VideoPlayerActivity.EXTRA_VIDEO_URI, mVideoUri.toString());
                    mActivity.setResult(VideoUtil.RESULT_CODE_ERROR, results);
                    mActivity.finish();
                }
                return true;
            }
        });

        mVideoView.requestFocus();
        mVideoView.start();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mPosition > 0) {
            mVideoView.seekTo(mPosition);
            mController.show(3000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mPosition = mVideoView.getCurrentPosition();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        // set video as watched
        LuchadeerPersist.getInstance(mActivity).markVideoWatched(mGiantBombId);
        Log.d(TAG, mGiantBombId + " marked as completed");

        mActivity.finish();
    }

    @Override
    public void onSystemUiVisibilityChange(int i) {
        if (i == View.SYSTEM_UI_FLAG_VISIBLE) {
            startSystemUiHideCountdown();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int position = mVideoView.getCurrentPosition();
        outState.putInt("position", position);

        Log.d("VideoPlayerFragment", "onSaveInstanceState");
    }

    private class GBMediaController extends MediaController {

        public GBMediaController(Context context) {
            super(context);
        }

        @Override
        public void show(int timeout) {
            super.show(timeout);
            showSystemUi();
        }

        @Override
        public void hide() {
            super.hide();
            hideSystemUi();
        }
    }

    private void hideSystemUi() {
        mActionBar.hide();
        if (mDecorView != null) {
            mDecorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
            );
        }
    }

    private void showSystemUi() {
        mActionBar.show();
    }

    Runnable delayedHide = new Runnable() {
        @Override
        public void run() {
            hideSystemUi();
        }
    };

    public void startSystemUiHideCountdown() {
        mHandler.removeCallbacks(delayedHide);
        mHandler.postDelayed(delayedHide, 3000);
    }
}

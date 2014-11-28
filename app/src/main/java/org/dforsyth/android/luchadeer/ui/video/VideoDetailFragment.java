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

import android.app.Activity;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.RequestFuture;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;

import org.dforsyth.android.luchadeer.BaseActivity;
import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.net.LuchadeerApi;
import org.dforsyth.android.luchadeer.model.giantbomb.Video;
import org.dforsyth.android.luchadeer.persist.LuchadeerPersist;
import org.dforsyth.android.luchadeer.persist.db.LuchadeerContract;
import org.dforsyth.android.luchadeer.persist.provider.BatchContentObserver;
import org.dforsyth.android.luchadeer.ui.util.FadeNetworkImageView;
import org.dforsyth.android.luchadeer.ui.util.UiUtil;
import org.dforsyth.android.luchadeer.util.CastUtil;
import org.dforsyth.android.luchadeer.util.IntentUtil;
import org.dforsyth.android.luchadeer.util.LoaderResult;
import org.dforsyth.android.luchadeer.util.Util;
import org.dforsyth.android.luchadeer.util.VideoUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;


public class VideoDetailFragment extends Fragment implements
        DrawerLayout.DrawerListener, LoaderManager.LoaderCallbacks<LoaderResult<Video>> {
    private final static String TAG = VideoDetailFragment.class.getName();

    private final static String CANT_CAST_LOCAL_DIALOG_FRAGMENT = "cant_cast_local_dialog_fragment";
    private final static String CANT_PLAY_UNFINISHED_DOWNLOAD_DIALOG_FRAGMENT = "cant_play_unfinished_download_dialog_fragment";

    private final static String ARG_VIDEO_OBJ = "video_obj";
    private final static String ARG_VIDEO_ID = "video_id";

    private final static int VIDEO_LOADER_ID = 1;

    private BaseActivity mActivity;

    private FadeNetworkImageView mImage;
    private ImageView mPlay;
    private TextView mName;
    private TextView mDeck;
    private TextView mUser;
    private TextView mPublishDate;
    private TextView mRuntime;

    private ImageView mOpenOnWeb;
    private ImageView mFavorite;
    private ImageView mDownload;

    private View mLoadingView;
    private View mVerticalLayout;

    private Spinner mSpinner;

    private Video mVideo;
    private int mVideoId;

    private ShareActionProvider mShareProvider;
    private View mShareView;

    private boolean mIsFavorite;
    private boolean mDetailsShown;

    private HashMap<String, DownloadInfo> mDownloadMap;

    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private CountDownLatch mCountDown;
    private BatchContentObserver mObserver;

    private LuchadeerApi mApi;
    private LuchadeerPersist mPersist;

    private OnDetailLoadFailedListener mOnDetailLoadFailedListener;

    public VideoDetailFragment() {

    }

    public static VideoDetailFragment newInstance(Video video) {
        VideoDetailFragment fragment = new VideoDetailFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_VIDEO_OBJ, video);
        fragment.setArguments(args);

        return fragment;
    }

    public static VideoDetailFragment newInstance(int videoId) {
        VideoDetailFragment fragment = new VideoDetailFragment();
        Bundle args = new Bundle();

        args.putInt(ARG_VIDEO_ID, videoId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (BaseActivity) getActivity();
        mApi = LuchadeerApi.getInstance(mActivity.getApplicationContext());
        mPersist = LuchadeerPersist.getInstance(mActivity.getApplicationContext());

        mOnDetailLoadFailedListener = (VideoDetailFragment.OnDetailLoadFailedListener) mActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FrameLayout rootView = (FrameLayout) inflater.inflate(R.layout.fragment_video_detail, container, false);

        mLoadingView = inflater.inflate(R.layout.list_loading_item, null, false);
        mVerticalLayout = rootView.findViewById(R.id.vertical_layout);

        mVideo = getArguments().getParcelable(ARG_VIDEO_OBJ);
        mVideoId = getArguments().getInt(ARG_VIDEO_ID);

        mImage = (FadeNetworkImageView) rootView.findViewById(R.id.video_image);
        mPlay = (ImageView) rootView.findViewById(R.id.video_play);
        mName = (TextView) rootView.findViewById(R.id.video_name_text);
        mDeck = (TextView) rootView.findViewById(R.id.video_deck_text);
        mUser = (TextView) rootView.findViewById(R.id.video_user);
        mPublishDate = (TextView) rootView.findViewById(R.id.video_publish_date);
        mRuntime = (TextView) rootView.findViewById(R.id.video_length_seconds);

        mOpenOnWeb = (ImageView) rootView.findViewById(R.id.video_view_on_web);
        mFavorite = (ImageView) rootView.findViewById(R.id.video_favorite);
        mDownload = (ImageView) rootView.findViewById(R.id.video_download);

        mPlay.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View view) {
                String uri = Util.addApiKey(
                        getActivity(),
                        VideoUtil.getQualityUrl(mVideo, (String) mSpinner.getSelectedItem()));
                DownloadInfo info;
                if ((info = mDownloadMap.get(mSpinner.getSelectedItem())) != null) {
                    uri = info.contentURI;
                }
                startActivity(IntentUtil.getExternalVideoPlayerIntent(uri));
                return true;
            }
        });

        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playVideo();
            }
        });

        mOpenOnWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mVideo.getSiteDetailUrl()));
                startActivity(browserIntent);
            }
        });

        mDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disableVideoBarButton(mDownload);
                startDownload();
            }
        });

        mFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mIsFavorite) {
                    addFavorite();
                } else {
                    removeFavorite();
                }
            }
        });

        mSpinner = (Spinner) rootView.findViewById(R.id.video_quality_spinner);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String item = (String) mSpinner.getItemAtPosition(i);
                if (item.equals(VideoUtil.VIDEO_SOURCE_YOUTUBE) || mDownloadMap.containsKey(item)) {
                    disableVideoBarButton(mDownload);
                } else {
                    enableVideoBarButton(mDownload);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        setHasOptionsMenu(true);

        mVerticalLayout.setVisibility(View.GONE);

        rootView.addView(mLoadingView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // api call, favorite load, download load
        mCountDown = new CountDownLatch(3);

        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // we do this here because it needs mvideo populated.
                setupQualitySpinner();

                showDetails(true);
            }
        };

        new Handler(mHandlerThread.getLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    mCountDown.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "sending message");
                mHandler.sendEmptyMessage(0);
            }
        });

        if (mVideo != null) {
            mVideoId = mVideo.getId();
            onVideoRequestComplete(mVideo);
        } else {
            getLoaderManager().initLoader(VIDEO_LOADER_ID, null, this);
        }

        loadFavorite();
        loadDownloads();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mActivity = (BaseActivity) activity;

        mObserver = new BatchContentObserver(new BatchContentObserver.OnChangeListener() {
            @Override
            public void onChange(boolean selfChange) {
                // make sure we still have our activity -- this can get fired late for missing videos
                if (getActivity() == null) {
                    return;
                }

                loadDownloads();
            }
        });

        activity.getContentResolver().registerContentObserver(
                LuchadeerContract.VideoDownload.CONTENT_URI,
                true,
                mObserver
        );

    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().getContentResolver().unregisterContentObserver(mObserver);
    }

    private void onVideoRequestFailed() {
        mOnDetailLoadFailedListener.onDetailLoadFailed();
    }

    private void onVideoRequestComplete(Video video) {
        mVideo = video;

        mImage.setImageUrl(mVideo.getImage().getSuperUrl(), mApi.getImageLoader());

        mName.setText(mVideo.getName());

        if (mDeck != null) {
            mDeck.setText(mVideo.getDeck());
        }

        mUser.setText(String.format("Posted by: %s", mVideo.getUser()));

        SimpleDateFormat format = new SimpleDateFormat("MMM d, yyyy h:mm a");
        mPublishDate.setText(format.format(mVideo.getPublishDate()));

        int lengthSeconds = mVideo.getLengthSeconds();

        int totalMins = lengthSeconds / 60;
        int mins = totalMins % 60;
        int hours = totalMins / 60;
        int secs = lengthSeconds % 60;

        mRuntime.setText(String.format("Runtime: %02d:%02d:%02d", hours, mins, secs));

        setupShareProviderIntent();

        Log.d(TAG, "video request complete countdown");
        mCountDown.countDown();
    }

    @Override
    public Loader<LoaderResult<Video>> onCreateLoader(int i, Bundle bundle) {
        return new VideoLoader(getActivity(), mVideoId);
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<Video>> videoLoader, LoaderResult<Video> result) {
        Video video = result.getResult();
        Exception error = result.getError();

        if (video != null) {
            onVideoRequestComplete(video);
        } else {
            Util.handleVolleyError(getActivity(), error);
            onVideoRequestFailed();
        }
        // done with this loader
        getLoaderManager().destroyLoader(VIDEO_LOADER_ID);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<Video>> videoLoader) {
    }

    private static class DownloadInfo {
        public String quality;
        public int downloadStatus;
        public String contentURI;
    }

    // load downloads and populate the downloads map
    private void loadDownloads() {
        mPersist.getVideoDownloads(getLoaderManager(), mVideoId, new LuchadeerPersist.LoadFinishedListener() {
        // LuchadeerPersist.getInstance(getActivity()).getVideoDownloads2(mVideoId, new LuchadeerPersist.OnVideoDownloadsQueryCompleteListener() {
            @Override
            public void onLoadFinished(Cursor cursor) {
                mDownloadMap = new HashMap<String, DownloadInfo>();
                if (cursor.moveToFirst()) {

                    while (!cursor.isAfterLast()) {
                        String quality = cursor.getString(cursor.getColumnIndex(LuchadeerContract.VideoDownload.QUALITY));
                        String contentUri = cursor.getString(cursor.getColumnIndex(LuchadeerContract.VideoDownload.LOCAL_LOCATION));
                        int downloadStatus = cursor.getInt(cursor.getColumnIndex(LuchadeerContract.VideoDownload.DOWNLOAD_STATUS));

                        DownloadInfo q = new DownloadInfo();
                        q.quality = quality;
                        q.contentURI = contentUri;
                        q.downloadStatus = downloadStatus;
                        mDownloadMap.put(quality, q);

                        cursor.moveToNext();
                    }
                }

                // this is nasty
                if (mDetailsShown) {
                    setupQualitySpinner();
                } else {
                    // download map is ready
                    mCountDown.countDown();
                }
            }
        });
    }

    private void setupShareProviderIntent() {
        if (mVideo == null || mShareProvider == null) {
            return;
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                String.format("Check out \"%s\" on Giant Bomb! %s", mVideo.getName(), mVideo.getSiteDetailUrl()));
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

        mActivity.getVideoCastManager().addMediaRouterButton(menu, R.id.media_route_menu_item);
    }

    private void setupQualitySpinner() {
        // XXX: this is just a terrible way to handle all of this. come back later to fix it.
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.quality_spinner_item) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.quality_spinner_item, parent, false);
                }
                TextView tv = (TextView) convertView.findViewById(android.R.id.text1);

                String quality = getItem(position);

                tv.setText(quality);
                DownloadInfo info = mDownloadMap.get(quality);
                if (info != null) {
                    tv.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_get_app_white_24dp,
                            0,
                            0,
                            0);
                }

                return convertView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
                }
                TextView tv = (TextView) convertView.findViewById(android.R.id.text1);

                String quality = getItem(position);

                tv.setText(quality);

                DownloadInfo info = mDownloadMap.get(quality);
                if (info != null) {
                    Log.d(TAG, "found info for " + getItem(position) + "(" + position + ")");
                    tv.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_get_app_white_24dp,
                            0,
                            0,
                            0);
                } else {
                    tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }

                return convertView;
            }
        };

        if (mVideo.getHDUrl() != null) {
            adapter.add(VideoUtil.VIDEO_SOURCE_HD);
        }
        if (mVideo.getHighUrl() != null) {
            adapter.add(VideoUtil.VIDEO_SOURCE_HIGH);
        }
        if (mVideo.getLowUrl() != null) {
            adapter.add(VideoUtil.VIDEO_SOURCE_LOW);
        }
        if (mVideo.getYouTubeId() != null) {
            adapter.add(VideoUtil.VIDEO_SOURCE_YOUTUBE);
        }

        int oldSelection = mSpinner.getSelectedItemPosition();

        // adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);

        if (oldSelection != AdapterView.INVALID_POSITION) {
            mSpinner.setSelection(oldSelection);
        }

        // spinner is ready
        mCountDown.countDown();
    }

    private void setFavorite(boolean favorite) {
        if (favorite == mIsFavorite) {
            return;
        }

        mIsFavorite = favorite;
        if (mFavorite != null) {
            if (mIsFavorite) {
                mFavorite.setImageResource(R.drawable.ic_favorite_white_24dp);
            } else {
                mFavorite.setImageResource(R.drawable.ic_favorite_outline_white_24dp);
            }
        }
    }

    private void loadFavorite() {
        mPersist.videoIsFavorite(getLoaderManager(), mVideoId, new LuchadeerPersist.ExistsResultListener() {
            @Override
            public void onExistsResult(boolean exists) {
                setFavorite(exists);
                Log.d(TAG, "favorite countdown");
                mCountDown.countDown();
            }
        });
    }

    private void addFavorite() {
        mPersist.addVideoFavorite(mVideo);
        setFavorite(true);
        Toast.makeText(mActivity, getString(R.string.favorite_added), Toast.LENGTH_SHORT).show();
    }

    private void removeFavorite() {
        mPersist.removeVideoFavorite(mVideoId);
        setFavorite(false);
        Toast.makeText(mActivity, getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show();
    }

    private void startDownload() {
        String quality = (String) mSpinner.getSelectedItem();
        String url = Util.addApiKey(getActivity(), VideoUtil.getQualityUrl(mVideo, quality));
        if (url == null) {
            return;
        }

        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(mVideo.getName());
        request.setVisibleInDownloadsUi(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

        request.setDestinationUri(
                Uri.fromFile(new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
                        + "/" + mVideo.getId() + "-" + quality
                ))
        );

        request.allowScanningByMediaScanner();

        DownloadManager manager = (DownloadManager) mActivity.getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = manager.enqueue(request);

        Log.d(TAG, "Started download: " + url);

        // the observer will see this and update the ui
        mPersist.addVideoDownload(mVideo, quality, downloadId);
    }

    private void showDetails(boolean showDetails) {
        if (showDetails == mDetailsShown) {
            return;
        }

        mDetailsShown = showDetails;

        // TODO: we don't animation these becuase we cant animate on game details yet.
        if (mDetailsShown) {
            mLoadingView.setVisibility(View.GONE);
            mVerticalLayout.setVisibility(View.VISIBLE);
        } else {
            mLoadingView.setVisibility(View.VISIBLE);
            mVerticalLayout.setVisibility(View.GONE);
        }
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

    private void disableVideoBarButton(View view) {
        mDownload.setClickable(false);
        mDownload.setAlpha(0.2f);
    }

    private void enableVideoBarButton(View view) {
        mDownload.setClickable(true);
        mDownload.setAlpha(1f);
    }

    public interface OnDetailLoadFailedListener {
        public void onDetailLoadFailed();
    }

    private void playVideo() {
        String selected = (String) mSpinner.getSelectedItem();

        // if it's youtube, just send the user there immediately
        if (selected.equals(VideoUtil.VIDEO_SOURCE_YOUTUBE)) {
            Intent youtube = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + mVideo.getYouTubeId()));
            startActivity(youtube);
            return;
        }

        DownloadInfo info = mDownloadMap.get(selected);

        VideoCastManager castManager = CastUtil.getVideoCastManager(getActivity());
        boolean castConnected = castManager.isConnected();

        if (info != null) {
            // we want to play local content
            if (info.downloadStatus != DownloadManager.STATUS_SUCCESSFUL) {
                CantPlayUnfinishedDownloadDialogFragment popup = CantPlayUnfinishedDownloadDialogFragment.newInstance(
                        mVideo.getName(),
                        Util.addApiKey(getActivity(), VideoUtil.getQualityUrl(mVideo, selected)),
                        mVideoId,
                        ""
                );
                popup.show(getFragmentManager(), CANT_PLAY_UNFINISHED_DOWNLOAD_DIALOG_FRAGMENT);
            } else if (castConnected) {
                // cast is connected, we need to warn the user about limitations
                CantCastLocalDialogFragment popup = CantCastLocalDialogFragment.newInstance(
                        info.contentURI,
                        Util.addApiKey(getActivity(), VideoUtil.getQualityUrl(mVideo, selected)),
                        mVideo.getName(),
                        "",
                        mVideoId
                );
                popup.show(getFragmentManager(), CANT_CAST_LOCAL_DIALOG_FRAGMENT);
            } else {
                VideoUtil.playVideo(
                        getActivity(),
                        mVideo.getName(),
                        info.contentURI,
                        mVideo.getId(),
                        "",
                        true
                );
            }
        } else {
            // fall back to simple playvideo call
            VideoUtil.playVideo(getActivity(), mVideo, selected);
        }
    }

    private static class VideoLoader extends AsyncTaskLoader<LoaderResult<Video>> {
        private int mVideoId;

        public VideoLoader(Context context, int videoId) {
            super(context);
            mVideoId = videoId;
        }

        @Override
        public LoaderResult<Video> loadInBackground() {
            RequestFuture<LuchadeerApi.GiantBombResponse<Video>> future = RequestFuture.newFuture();

            LuchadeerApi.getInstance(getContext()).video(this, mVideoId, future, future);

            LuchadeerApi.GiantBombResponse<Video> response = null;
            Exception error = null;
            try {
                response = future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                error = e;
            } catch (ExecutionException e) {
                e.printStackTrace();
                error = e;
                // Util.handleVolleyError(getContext(), (VolleyError) e.getCause());
            }

            return new LoaderResult<Video>(response == null ? null : response.getResults(), error);
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();
            forceLoad();
        }
    }
}

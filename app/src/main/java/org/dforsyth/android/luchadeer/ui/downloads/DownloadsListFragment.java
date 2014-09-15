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

package org.dforsyth.android.luchadeer.ui.downloads;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.net.LuchadeerApi;
import org.dforsyth.android.luchadeer.persist.LuchadeerPersist;
import org.dforsyth.android.luchadeer.persist.db.LuchadeerContract;
import org.dforsyth.android.luchadeer.persist.provider.BatchContentObserver;
import org.dforsyth.android.luchadeer.ui.util.BezelImageView;
import org.dforsyth.android.luchadeer.util.IntentUtil;


public class DownloadsListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final static String TAG = DownloadsListFragment.class.getName();

    private Activity mActivity;
    private SimpleCursorAdapter mAdapter;
    private LuchadeerApi mApi;
    private ListView mListView;

    private boolean mActionEnabled;

    BatchContentObserver mDownloadsObserver;
    OnStartVideoListener mOnStartVideoListener;

    private static String[] CURSOR_FROM = {
            LuchadeerContract.ContentColumns.NAME,
            LuchadeerContract.ContentColumns.SUPER_IMAGE_URL,
            LuchadeerContract.VideoDownload.QUALITY,
    };

    private static int[] CURSOR_TO = {
            R.id.video_name,
            R.id.image,
            R.id.video_quality,
    };

    public DownloadsListFragment() {

    }

    public static DownloadsListFragment newInstance() {
        return new DownloadsListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = getActivity();

        mApi = LuchadeerApi.getInstance(mActivity.getApplicationContext());

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView = getListView();

        setEmptyText(getString(R.string.no_downloads));

        setupContextMenu();
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(0, null, this);
    }

    public Uri getContentUri() {
        return LuchadeerContract.VideoDownload.CONTENT_URI;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mDownloadsObserver = new BatchContentObserver(new BatchContentObserver.OnChangeListener() {
            @Override
            public void onChange(boolean selfChange) {
                // TODO: it would be cooler if we kept our scroll position
                setListAdapter(null);

                restartLoader();
            }
        });

        activity.getContentResolver().registerContentObserver(
                getContentUri(),
                true,
                mDownloadsObserver);

        mOnStartVideoListener = (OnStartVideoListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        getActivity().getContentResolver().unregisterContentObserver(mDownloadsObserver);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        // play item
        Cursor cursor = (Cursor) mAdapter.getItem(position);

        int idx = cursor.getColumnIndex(LuchadeerContract.VideoDownload.GIANT_BOMB_ID);
        startActivity(IntentUtil.getVideoDetailActivityIntent(mActivity, cursor.getInt(idx)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mActivity.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(
                mActivity,
                LuchadeerContract.VideoDownload.CONTENT_URI,
                LuchadeerContract.VideoDownload.PROJECTION,
                null,
                null,
                null);
    }

    private class DownloadCursorAdapter extends SimpleCursorAdapter {
        private ImageLoader mImageLoader;
        private Cursor mCursor;
        private Activity mActivity;

        public DownloadCursorAdapter(Activity activity, Cursor cursor, ImageLoader imageLoader) {
            super(activity, R.layout.download_list_item, cursor, CURSOR_FROM, CURSOR_TO, 0);
            mImageLoader = imageLoader;
            mCursor = cursor;
            mActivity = activity;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            String statusText = "Downloading";
            TextView videoStatus = (TextView) view.findViewById(R.id.video_status);
            FrameLayout indicator = (FrameLayout) view.findViewById(R.id.indicator);

            int statusIdx = mCursor.getColumnIndex(LuchadeerContract.VideoDownload.DOWNLOAD_STATUS);
            int status = mCursor.getInt(statusIdx);

            final String name = mCursor.getString(mCursor.getColumnIndex(LuchadeerContract.VideoDownload.NAME));
            final String uri = mCursor.getString(mCursor.getColumnIndex(LuchadeerContract.VideoDownload.LOCAL_LOCATION));
            final int giantBombId = mCursor.getInt(mCursor.getColumnIndex(LuchadeerContract.VideoDownload.GIANT_BOMB_ID));
            final String imageUrl = mCursor.getString(mCursor.getColumnIndex(LuchadeerContract.VideoDownload.SUPER_IMAGE_URL));

            switch (status) {
                case (DownloadManager.STATUS_SUCCESSFUL):
                    statusText = "Complete";
                    ImageView playButton = new ImageView(mActivity);
                    playButton.setImageResource(R.drawable.ic_action_play);
                    indicator.addView(playButton);

                    indicator.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (mActionEnabled) {
                                mOnStartVideoListener.onStartVideo(name, uri, giantBombId, imageUrl);
                            }
                        }
                    });

                    indicator.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            startActivity(IntentUtil.getExternalVideoPlayerIntent(uri));
                            return true;
                        }
                    });
                    break;
                case (DownloadManager.STATUS_FAILED):
                    statusText = "Failed";
                    break;
                case (DownloadManager.STATUS_PAUSED):
                    // statusText = "Paused";
                case (DownloadManager.STATUS_PENDING):
                    // statusText = "Pending";
                case (DownloadManager.STATUS_RUNNING):
                    statusText = "Downloading";
                default:
                    ProgressBar progress = new ProgressBar(mActivity);
                    indicator.addView(progress);
            }

            videoStatus.setText(statusText);

            return view;
        }

        @Override
        public void setViewImage(ImageView v, String value) {
            // super.setViewImage(v, value);
            BezelImageView riv = (BezelImageView) v;
            riv.setImageUrl(value, mImageLoader);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter = new DownloadCursorAdapter(mActivity, cursor, mApi.getImageLoader());
        setListAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        setListAdapter(null);
        mAdapter.swapCursor(null);
    }

    private void setupContextMenu() {
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                // mListView.setItemChecked(i, b);
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.list_remove_context, menu);

                mActionEnabled = false;

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_item_remove) {
                    setListShown(false);

                    SparseBooleanArray array = mListView.getCheckedItemPositions();

                    for (int i = 0; i < array.size(); ++i) {
                        if (array.valueAt(i)) {
                            int key = array.keyAt(i);
                            Cursor cursor = (Cursor) mListView.getItemAtPosition(key);
                            int idx = cursor.getColumnIndex(LuchadeerContract.VideoDownload.DOWNLOAD_ID);
                            int downloadId = cursor.getInt(idx);

                            LuchadeerPersist.getInstance(getActivity()).removeVideoDownload(downloadId);

                            DownloadManager manager = (DownloadManager) mActivity.getSystemService(Context.DOWNLOAD_SERVICE);
                            manager.remove(downloadId);
                        }
                    }

                    actionMode.finish();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                mActionEnabled = true;
            }
        });

        mActionEnabled = true;
    }

    public interface OnStartVideoListener {
        public void onStartVideo(String name, String uri, int giantBombId, String imageUrl);
    }
}

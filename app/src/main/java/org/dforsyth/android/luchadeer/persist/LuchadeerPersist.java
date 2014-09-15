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

package org.dforsyth.android.luchadeer.persist;

import android.app.DownloadManager;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.dforsyth.android.luchadeer.model.giantbomb.Game;
import org.dforsyth.android.luchadeer.model.giantbomb.Image;
import org.dforsyth.android.luchadeer.model.giantbomb.Video;
import org.dforsyth.android.luchadeer.persist.db.LuchadeerContract;

import java.util.Calendar;


public class LuchadeerPersist {
    private static final String TAG = LuchadeerPersist.class.getName();

    private static final String ARG_GIANT_BOMB_ID = "giantbomb_id";

    private static LuchadeerPersist sInstance;

    private Context mContext;

    public static LuchadeerPersist getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new LuchadeerPersist(context);
        }

        return sInstance;
    }

    private LuchadeerPersist(Context context) {
        mContext = context.getApplicationContext();
    }

    public void addGameFavorite(Game game) {
        ContentValues values = new ContentValues();

        values.put(LuchadeerContract.GameFavorite.GIANT_BOMB_ID, game.getId());
        values.put(LuchadeerContract.GameFavorite.NAME, game.getName());

        String superImageUrl = null;
        Image image;
        image = game.getImage();
        if (image != null) {
            superImageUrl = image.getSuperUrl();
        }
        values.put(LuchadeerContract.GameFavorite.SUPER_IMAGE_URL, superImageUrl);

        Calendar calendar = Calendar.getInstance();
        values.put(LuchadeerContract.GameFavorite.DATE_CREATED, calendar.getTimeInMillis());

        mContext.getContentResolver().insert(
                LuchadeerContract.GameFavorite.CONTENT_URI,
                values);
    }

    public void addVideoFavorite(Video video) {
        ContentValues values = new ContentValues();

        values.put(LuchadeerContract.GameFavorite.GIANT_BOMB_ID, video.getId());
        values.put(LuchadeerContract.GameFavorite.NAME, video.getName());

        String superImageUrl = null;
        Image image;
        image = video.getImage();
        if (image != null) {
            superImageUrl = image.getSuperUrl();
        }
        values.put(LuchadeerContract.GameFavorite.SUPER_IMAGE_URL, superImageUrl);

        Calendar calendar = Calendar.getInstance();
        values.put(LuchadeerContract.GameFavorite.DATE_CREATED, calendar.getTimeInMillis());

        mContext.getContentResolver().insert(
                LuchadeerContract.VideoFavorite.CONTENT_URI,
                values);
    }

    public void removeFavorite(Uri uri, int giantBombId) {
        mContext.getContentResolver().delete(
                uri,
                LuchadeerContract.ContentColumns.GIANT_BOMB_ID + " = ?",
                new String[]{Integer.toString(giantBombId)});
        Log.d(TAG, "removed favorite: " + giantBombId);
    }

    public void removeGameFavorite(int gameId) {
        removeFavorite(
                LuchadeerContract.GameFavorite.CONTENT_URI,
                gameId);
    }

    public void removeVideoFavorite(int videoId) {
        removeFavorite(
                LuchadeerContract.VideoFavorite.CONTENT_URI,
                videoId);
    }

    private static final int GAME_FAVORITE_ID = 1;
    private static final int VIDEO_FAVORITE_ID = 2;
    private static final int VIDEO_WATCHED_ID = 3;
    private static final int VIDEO_DOWNLOAD_ID = 4;

    public interface ExistsResultListener {
        public void onExistsResult(boolean exists);
    }

    private static class ExistsLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        private ExistsResultListener mExistsResultListener;
        private int mGiantBombId;
        private Context mContext;

        public ExistsLoaderCallbacks(Context context, int giantBombId, ExistsResultListener existsResultListener) {
            mExistsResultListener = existsResultListener;
            mGiantBombId = giantBombId;
            mContext = context;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
            Uri uri;
            String[] projection;

            switch (id) {
                case (GAME_FAVORITE_ID):
                    uri = LuchadeerContract.GameFavorite.CONTENT_URI;
                    projection = LuchadeerContract.GameFavorite.PROJECTION;
                    break;
                case (VIDEO_FAVORITE_ID):
                    uri = LuchadeerContract.VideoFavorite.CONTENT_URI;
                    projection = LuchadeerContract.VideoFavorite.PROJECTION;
                    break;
                default:
                    throw new RuntimeException("Unknown Id");
            }

            return new CursorLoader(
                    mContext,
                    uri,
                    projection,
                    LuchadeerContract.ContentColumns.GIANT_BOMB_ID + " = ?",
                    new String[]{Integer.toString(mGiantBombId)},
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            int loaderId = loader.getId();
            if (loaderId == GAME_FAVORITE_ID || loaderId == VIDEO_FAVORITE_ID) {
                mExistsResultListener.onExistsResult(cursor.moveToFirst());
            }
            cursor.close();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    public void gameIsFavorite(LoaderManager manager, int gameId, ExistsResultListener existsResultListener) {
        manager.restartLoader(
                GAME_FAVORITE_ID,
                null,
                new ExistsLoaderCallbacks(mContext, gameId, existsResultListener));
    }

    public void videoIsFavorite(LoaderManager manager, int videoId, ExistsResultListener existsResultListener) {
        manager.restartLoader(
                VIDEO_FAVORITE_ID,
                null,
                new ExistsLoaderCallbacks(mContext, videoId, existsResultListener));
    }

    public void addVideoDownload(Video video, String quality, long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        Cursor cursor = ((DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE)).query(query);
        if (!cursor.moveToFirst()) {
            // its not there...
            return;
        }

        ContentValues contentValues = new ContentValues();

        contentValues.put(LuchadeerContract.VideoDownload.GIANT_BOMB_ID, video.getId());
        contentValues.put(LuchadeerContract.VideoDownload.NAME, video.getName());

        Calendar calendar = Calendar.getInstance();
        contentValues.put(LuchadeerContract.VideoDownload.DATE_CREATED, calendar.getTimeInMillis());

        contentValues.put(LuchadeerContract.VideoDownload.QUALITY, quality);
        contentValues.put(LuchadeerContract.VideoDownload.DOWNLOAD_ID, downloadId);

        Image image = video.getImage();
        if (image != null) {
            contentValues.put(LuchadeerContract.VideoDownload.SUPER_IMAGE_URL, image.getSuperUrl());
        }

        int idxStatus = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int status = cursor.getInt(idxStatus);

        cursor.close();

        contentValues.put(LuchadeerContract.VideoDownload.DOWNLOAD_STATUS, status);

        mContext.getContentResolver().insert(
                LuchadeerContract.VideoDownload.CONTENT_URI,
                contentValues);
    }

    public void removeVideoDownload(long downloadId) {
        mContext.getContentResolver().delete(
                LuchadeerContract.VideoDownload.CONTENT_URI,
                LuchadeerContract.VideoDownload.DOWNLOAD_ID + " = (?)",
                new String[]{Long.toString(downloadId)});
    }

    public void removeVideoDownloadByURI(String uri) {
        mContext.getContentResolver().delete(
                LuchadeerContract.VideoDownload.CONTENT_URI,
                LuchadeerContract.VideoDownload.LOCAL_LOCATION + " = (?)",
                new String[]{uri});
    }

    public int updateDownloadStatus(long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = ((DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE)).query(query);
        if (!cursor.moveToFirst()) {
            // its not there...
            return -1;
        }

        ContentValues values = new ContentValues();

        int downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
        String contentLocalUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

        values.put(LuchadeerContract.VideoDownload.DOWNLOAD_STATUS, downloadStatus);
        values.put(LuchadeerContract.VideoDownload.LOCAL_LOCATION, contentLocalUri);

        mContext.getContentResolver().update(
            LuchadeerContract.VideoDownload.CONTENT_URI,
            values,
            LuchadeerContract.VideoDownload.DOWNLOAD_ID + " = (?)",
            new String[]{Long.toString(downloadId)});

        cursor.close();

        return downloadStatus;
    }

    public void markVideoWatched(int giantBombId) {
        ContentValues values = new ContentValues();

        values.put(LuchadeerContract.VideoViewStatus.GIANT_BOMB_ID, giantBombId);
        Calendar calendar = Calendar.getInstance();
        values.put(LuchadeerContract.VideoViewStatus.LAST_COMPLETED, calendar.getTimeInMillis());

        mContext.getContentResolver().insert(
                LuchadeerContract.VideoViewStatus.CONTENT_URI,
                values);
    }

    public interface VideoWatchedResultListener {
        public void onVideoWatchedResult(long watched);
    }

    private static class VideoWatchedLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        private Context mContext;
        private VideoWatchedResultListener mVideoWatchedResultListener;

        public VideoWatchedLoaderCallbacks(Context context, VideoWatchedResultListener videoWatchedResultListener) {
            mContext = context;
            mVideoWatchedResultListener = videoWatchedResultListener;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return new CursorLoader(
                    mContext,
                    LuchadeerContract.VideoViewStatus.CONTENT_URI,
                    LuchadeerContract.VideoViewStatus.PROJECTION,
                    LuchadeerContract.VideoViewStatus.GIANT_BOMB_ID + " = ?",
                    new String[]{Integer.toString(bundle.getInt(ARG_GIANT_BOMB_ID))},
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            long value;

            if (!cursor.moveToFirst()) {
                value = -1;
            }

            if (cursor.getCount() == 0) {
                value = -1;
            } else {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(LuchadeerContract.VideoViewStatus.LAST_COMPLETED);
                if (cursor.isNull(idx)) {
                    value = -1;
                } else {
                    value = cursor.getLong(idx);
                }
            }
            cursor.close();

            mVideoWatchedResultListener.onVideoWatchedResult(value);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {

        }
    }

    public void getVideoWatched(LoaderManager manager, int giantBombId, VideoWatchedResultListener videoWatchedResultListener) {
        Bundle args = new Bundle();
        args.putInt(ARG_GIANT_BOMB_ID, giantBombId);
        manager.restartLoader(
                VIDEO_WATCHED_ID + giantBombId,
                args,
                new VideoWatchedLoaderCallbacks(mContext, videoWatchedResultListener));
    }

    public interface LoadFinishedListener {
        public void onLoadFinished(Cursor cursor);
    }

    public void getVideoDownloads(LoaderManager manager, int giantBombId, LoadFinishedListener loadFinishedListener) {
        Bundle args = new Bundle();
        args.putInt(ARG_GIANT_BOMB_ID, giantBombId);
        manager.restartLoader(
                VIDEO_DOWNLOAD_ID + giantBombId,
                args,
                new VideoDownloadsLoaderCallbacks(mContext, loadFinishedListener));
    }

    private static class VideoDownloadsLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        private Context mContext;
        private LoadFinishedListener mLoadFinishedListener;

        public VideoDownloadsLoaderCallbacks(Context context, LoadFinishedListener loadFinishedListener) {
            mContext = context;
            mLoadFinishedListener = loadFinishedListener;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            Log.d(TAG, "loader created!");
            return new CursorLoader(
                    mContext,
                    LuchadeerContract.VideoDownload.CONTENT_URI,
                    LuchadeerContract.VideoDownload.PROJECTION,
                    LuchadeerContract.VideoDownload.GIANT_BOMB_ID + " = ?",
                    new String[]{Integer.toString(bundle.getInt(ARG_GIANT_BOMB_ID))},
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            Log.d(TAG, "load finished video downloads");
            mLoadFinishedListener.onLoadFinished(cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
        }
    }
}

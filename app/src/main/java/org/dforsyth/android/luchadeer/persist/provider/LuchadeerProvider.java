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

package org.dforsyth.android.luchadeer.persist.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import org.dforsyth.android.luchadeer.persist.db.LuchadeerContract;
import org.dforsyth.android.luchadeer.persist.db.LuchadeerDatabase;


public class LuchadeerProvider extends ContentProvider {
    private static final String TAG = LuchadeerProvider.class.getName();

    public static final String AUTHORITY = "org.dforsyth.android.luchadeer.provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private static final int GAME_FAVORITE = 1;
    private static final int VIDEO_FAVORITE = 2;
    private static final int VIDEO_DOWNLOAD = 3;
    private static final int VIDEO_VIEW_STATUS = 4;
    // private static final int VIDEO_DOWNLOAD_SINGLE = 4;

    private static LuchadeerDatabase mDatabase;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, LuchadeerContract.GameFavorite.TABLE_NAME, GAME_FAVORITE);
        sUriMatcher.addURI(AUTHORITY, LuchadeerContract.VideoFavorite.TABLE_NAME, VIDEO_FAVORITE);
        sUriMatcher.addURI(AUTHORITY, LuchadeerContract.VideoDownload.TABLE_NAME, VIDEO_DOWNLOAD);
        sUriMatcher.addURI(AUTHORITY, LuchadeerContract.VideoViewStatus.TABLE_NAME, VIDEO_VIEW_STATUS);
    }

    @Override
    public boolean onCreate() {
        mDatabase = LuchadeerDatabase.getInstance(getContext());
        return true;
    }

    private String getTableName(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case (GAME_FAVORITE):
                return LuchadeerContract.GameFavorite.TABLE_NAME;
            case (VIDEO_FAVORITE):
                return LuchadeerContract.VideoFavorite.TABLE_NAME;
            case (VIDEO_DOWNLOAD):
                return LuchadeerContract.VideoDownload.TABLE_NAME;
            case (VIDEO_VIEW_STATUS):
                return LuchadeerContract.VideoViewStatus.TABLE_NAME;
        }
        throw new RuntimeException("uri did not match any tables");
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDatabase.getReadableDatabase();

        String tableName = getTableName(uri);

        return database.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = mDatabase.getWritableDatabase();

        String tableName = getTableName(uri);

        long id =  database.insert(
                tableName,
                null,
                values
        );

        Log.d(TAG, "inserting into " + tableName);

        // notify observers
        getContext().getContentResolver().notifyChange(uri, null);

        return uri.buildUpon().appendPath(Long.toString(id)).build();
    }

    @Override
    public int delete(Uri uri, String whereClause, String[] whereArgs) {
        SQLiteDatabase database = mDatabase.getWritableDatabase();
        int rval = database.delete(getTableName(uri), whereClause, whereArgs);

        // notify observers
        getContext().getContentResolver().notifyChange(uri, null);

        return rval;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String whereClause, String[] whereArgs) {
        SQLiteDatabase database = mDatabase.getWritableDatabase();

        int rval = database.update(getTableName(uri), contentValues, whereClause, whereArgs);

        getContext().getContentResolver().notifyChange(uri, null);

        return rval;
    }
}

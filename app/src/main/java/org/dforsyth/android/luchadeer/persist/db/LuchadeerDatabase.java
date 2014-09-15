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

package org.dforsyth.android.luchadeer.persist.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class LuchadeerDatabase extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "org.dforsyth.android.luchadeer.db";

    private static LuchadeerDatabase sInstance;

    private Context mContext;

    public static LuchadeerDatabase getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new LuchadeerDatabase(context);
        }

        return sInstance;
    }

    private LuchadeerDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // create the favorites tables
        database.execSQL(
            "CREATE TABLE " + LuchadeerContract.GameFavorite.TABLE_NAME + " (" +
            LuchadeerContract.GameFavorite._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            LuchadeerContract.GameFavorite.GIANT_BOMB_ID + " INTEGER NOT NULL," +
            LuchadeerContract.GameFavorite.NAME + " TEXT NOT NULL," +
            LuchadeerContract.GameFavorite.SUPER_IMAGE_URL + " TEXT," +
            LuchadeerContract.GameFavorite.DATE_CREATED + " INTEGER NOT NULL," +
            "UNIQUE (" + LuchadeerContract.GameFavorite.GIANT_BOMB_ID + ") ON CONFLICT REPLACE)");

        database.execSQL(
            "CREATE TABLE " + LuchadeerContract.VideoFavorite.TABLE_NAME + " (" +
            LuchadeerContract.VideoFavorite._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            LuchadeerContract.VideoFavorite.GIANT_BOMB_ID + " INTEGER NOT NULL," +
            LuchadeerContract.VideoFavorite.NAME + " TEXT NOT NULL," +
            LuchadeerContract.VideoFavorite.SUPER_IMAGE_URL + " TEXT," +
            LuchadeerContract.VideoFavorite.DATE_CREATED + " INTEGER NOT NULL," +
            "UNIQUE (" + LuchadeerContract.VideoFavorite.GIANT_BOMB_ID + ") ON CONFLICT REPLACE)");

        // create downloads table
        database.execSQL(
                "CREATE TABLE " + LuchadeerContract.VideoDownload.TABLE_NAME + " (" +
                LuchadeerContract.VideoDownload._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                LuchadeerContract.VideoDownload.GIANT_BOMB_ID + " INTEGER NOT NULL," +
                LuchadeerContract.VideoDownload.NAME + " TEXT NOT NULL," +
                LuchadeerContract.VideoDownload.SUPER_IMAGE_URL + " TEXT," +
                LuchadeerContract.VideoDownload.DATE_CREATED + " INTEGER NOT NULL," +
                LuchadeerContract.VideoDownload.DOWNLOAD_ID + " INTEGER NOT NULL," +
                LuchadeerContract.VideoDownload.LOCAL_LOCATION + " TEXT," + // NOT NULL," +
                LuchadeerContract.VideoDownload.QUALITY + " TEXT NOT NULL," +
                LuchadeerContract.VideoDownload.DOWNLOAD_STATUS + " INTEGER NOT NULL," +
                "UNIQUE (" + LuchadeerContract.VideoDownload.GIANT_BOMB_ID + "," +
                LuchadeerContract.VideoDownload.QUALITY + ") ON CONFLICT REPLACE)");

        // create watch tracker table
        database.execSQL(
                "CREATE TABLE " + LuchadeerContract.VideoViewStatus.TABLE_NAME + " (" +
                LuchadeerContract.VideoViewStatus._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                LuchadeerContract.VideoViewStatus.GIANT_BOMB_ID + " INTEGER NOT NULL," +
                LuchadeerContract.VideoViewStatus.POSITION + " INTEGER," +
                LuchadeerContract.VideoViewStatus.LAST_COMPLETED + " INTEGER," +
                "UNIQUE (" + LuchadeerContract.VideoViewStatus.GIANT_BOMB_ID + ") ON CONFLICT REPLACE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

    }

    public void deleteDatabase() {
        mContext.deleteDatabase(DATABASE_NAME);
    }
}

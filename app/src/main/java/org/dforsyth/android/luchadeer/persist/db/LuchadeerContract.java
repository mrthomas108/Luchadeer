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

import android.net.Uri;
import android.provider.BaseColumns;

import org.dforsyth.android.luchadeer.persist.provider.LuchadeerProvider;


public class LuchadeerContract {
    private static final String contentTypeFmt = "vnd.android.cursor.dir/vnd.org.dforsyth.android.luchadeer.%s";

    public interface ContentColumns {
        public static final String GIANT_BOMB_ID = "giantbomb_id";
        public static final String NAME = "name";
        public static final String SUPER_IMAGE_URL = "super_image_url";
        public static final String DATE_CREATED = "date_created";
    }

    public static class GameFavorite implements ContentColumns, BaseColumns {
        public static final String CONTENT_TYPE = String.format(contentTypeFmt, "gamefavorite");

        public static final String TABLE_NAME = "game_favorite";

        public static final Uri CONTENT_URI = LuchadeerProvider.BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        public static final String[] PROJECTION = {
                _ID,
                GIANT_BOMB_ID,
                NAME,
                SUPER_IMAGE_URL,
                DATE_CREATED,
        };

        public static int getGiantBombId(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }
    }

    public static abstract class VideoFavorite implements ContentColumns, BaseColumns {
        public static final String CONTENT_TYPE = String.format(contentTypeFmt, "videofavorite");

        public static final String TABLE_NAME = "video_favorite";

        public static final Uri CONTENT_URI = LuchadeerProvider.BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        public static final String[] PROJECTION = {
                _ID,
                GIANT_BOMB_ID,
                NAME,
                SUPER_IMAGE_URL,
                DATE_CREATED,
        };


        public static int getGiantBombId(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }
    }

    public static abstract class VideoDownload implements ContentColumns, BaseColumns {
        public static final String CONTENT_TYPE = String.format(contentTypeFmt, "videodownload");

        public static final String TABLE_NAME = "video_download";

        public static final Uri CONTENT_URI = LuchadeerProvider.BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        public static final String DOWNLOAD_ID = "download_id";
        public static final String QUALITY = "quality";
        public static final String LOCAL_LOCATION = "local_location";
        public static final String DOWNLOAD_STATUS = "download_status";

        public static final String[] PROJECTION = {
                _ID,
                GIANT_BOMB_ID,
                NAME,
                SUPER_IMAGE_URL,
                DATE_CREATED,
                DOWNLOAD_ID,
                QUALITY,
                LOCAL_LOCATION,
                DOWNLOAD_STATUS,
        };

        public static int getGiantBombId(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }
    }

    public static abstract class VideoViewStatus implements BaseColumns {
        public static final String GIANT_BOMB_ID = "giantbomb_id";
        public static final String POSITION = "position";
        public static final String LAST_COMPLETED = "last_completed";

        public static final String TABLE_NAME = "video_view_status";

        public static final Uri CONTENT_URI = LuchadeerProvider.BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        public static final String[] PROJECTION = {
                _ID,
                GIANT_BOMB_ID,
                POSITION,
                LAST_COMPLETED,
        };
    }
}

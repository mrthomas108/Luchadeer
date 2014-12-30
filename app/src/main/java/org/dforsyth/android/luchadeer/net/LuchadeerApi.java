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

package org.dforsyth.android.luchadeer.net;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.util.LruCache;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.model.giantbomb.ApiKey;
import org.dforsyth.android.luchadeer.model.giantbomb.Game;
import org.dforsyth.android.luchadeer.model.giantbomb.SearchResult;
import org.dforsyth.android.luchadeer.model.giantbomb.Video;
import org.dforsyth.android.luchadeer.model.giantbomb.VideoType;
import org.dforsyth.android.luchadeer.model.luchadeer.Preferences;
import org.dforsyth.android.luchadeer.model.youtube.YouTubeVideo;
import org.dforsyth.android.luchadeer.persist.LuchadeerPreferences;
import org.dforsyth.android.ravioli.Ravioli;
import org.dforsyth.android.ravioli.RavioliRequest;
import org.dforsyth.android.ravioli.encoders.GsonEncoder;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class LuchadeerApi {
    private static final String TAG = LuchadeerApi.class.getName();

    private static LuchadeerApi sInstance = null;

    private static String LUCHADEER_BASE_URL; // so we can initialize it later.
    private static String UNLINKED_BASE_URL;
    private static final String LINKED_BASE_URL = "https://www.giantbomb.com/api";


    private static final int IMAGE_CACHE_SIZE = 10; // lazy
    private static final int GAME_LIST_RETRIES = 3;

    public static final int GB_STATUS_OK = 1;
    public static final int GB_STATUS_NOT_FOUND = 101;
    public static final int GB_STATUS_URL_FORMAT = 102;
    public static final int GB_STATUS_FILTER = 104;
    public static final int GB_STATUS_SUBSCRIBER_CONTENT = 105;
    public static final int GB_STATUS_RATE_LIMIT = 107;

    public static final int LUCHA_STATUS_QUOTA_ERROR1 = 503;
    public static final int LUCHA_STATUS_QUOTA_ERROR2 = 403;

    private String mApiKey;

    private String mBaseUrl;

    private Context mContext;

    private LuchadeerPreferences mPreferences;

    // XXX having the imageloader in here isn't great, we should consider refactoring it out
    private ImageLoader mImageLoader;
    private RequestQueue mImageRequestQueue;

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public class GiantBombResponse<T> {
        // TODO: typedadapterfactory so that this can handle errors properly when expecting a single object?
        @SerializedName("results") private T mResults;
        @SerializedName("number_of_total_results") private int mNumberOfTotalResults;
        @SerializedName("error") private String mError;
        @SerializedName("status_code") private int mStatusCode;

        public T getResults() {
            return mResults;
        }

        public int getNumberOfTotalResults() {
            return mNumberOfTotalResults;
        }

        public String getError() {
            return mError;
        }

        public boolean statusIsOK() {
            return mStatusCode == GB_STATUS_OK;
        }

        public int getStatusCode() {
            return mStatusCode;
        }
    }

    private static Ravioli mGiantBombClient;
    private static Ravioli mYouTubeClient;
    private static Ravioli mValidateClient;

    private LuchadeerApi(Context context) {
        // VolleyLog.DEBUG = true;
        mContext = context.getApplicationContext();

        mPreferences = LuchadeerPreferences.getInstance(mContext);

        mImageRequestQueue = Volley.newRequestQueue(mContext);

        mImageLoader = new ImageLoader(mImageRequestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(IMAGE_CACHE_SIZE);
            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }
            public Bitmap getBitmap(String url) {
                return mCache.get(url);
            }
        });

        // set up uris and read api key out of preferences
        setupURIs();
        reloadApiKey(); // this will init the rail clients

        mImageRequestQueue.start();
    }

    private Ravioli makeGiantBombClient() {
        return new Ravioli.Builder(
                mContext,
                GiantBombUriBuilder().build()
        )
        .setEncoder(new GsonEncoder(new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()))
        .build();
    }

    private Ravioli makeYouTubeClient() {
        return new Ravioli.Builder(
                mContext,
                Uri.parse(LUCHADEER_BASE_URL).buildUpon()
                        .appendPath("youtube")
                        .appendPath("unarchived_videos")
                        .build()
        )
        .setEncoder(new GsonEncoder(new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000Z'").create()))
        .build();
    }

    private Ravioli makeValidateClient() {
        return new Ravioli.Builder(
            mContext,
            Uri.parse(LINKED_BASE_URL)
        )
        .build();
    }

    public LuchadeerApi(Context context, HttpStack stack, LuchadeerPreferences preferences) {
        mContext = context;

        mPreferences = preferences;
        mImageRequestQueue = Volley.newRequestQueue(mContext, stack);
        mImageLoader = new ImageLoader(mImageRequestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(IMAGE_CACHE_SIZE);
            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }
            public Bitmap getBitmap(String url) {
                return mCache.get(url);
            }
        });

        // setup uris and read api key out of preferences
        setupURIs();
        reloadApiKey();

        mImageRequestQueue.start();
    }

    public static LuchadeerApi getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new LuchadeerApi(context);
        }

        return sInstance;
    }

    // this method exists just so that I can late load secret uris
    private void setupURIs() {
        LUCHADEER_BASE_URL = mContext.getString(R.string.luchadeer_base_uri);
        UNLINKED_BASE_URL = LUCHADEER_BASE_URL + "/giantbomb";
    }

    // reread api key out of preferences
    public void reloadApiKey() {
        mApiKey = mPreferences.getApiKey();
        mBaseUrl = mApiKey.isEmpty() ? UNLINKED_BASE_URL : LINKED_BASE_URL;

        mGiantBombClient = makeGiantBombClient();
        mYouTubeClient = makeYouTubeClient();
        mValidateClient = makeValidateClient();
    }

    public String getApiKey() {
        return mApiKey;
    }

    private Uri.Builder GiantBombUriBuilder() {
        return Uri.parse(mBaseUrl).buildUpon()
            .appendQueryParameter("format", "json")
            .appendQueryParameter("api_key", mApiKey);
    }

    public RavioliRequest<GiantBombResponse<ArrayList<Video>>> getVideos(String categoryId, int offset) {
        RavioliRequest.Builder <GiantBombResponse<ArrayList<Video>>> builder = new RavioliRequest.Builder<GiantBombResponse<ArrayList<Video>>>(
                mGiantBombClient,
                new TypeToken<GiantBombResponse<ArrayList<Video>>>(){}.getType()
        )
        .addPath("videos")
        .addQueryParameter("offset", Integer.toString(offset));

        if (categoryId != null) {
            builder = builder.addQueryParameter("video_type", categoryId);
        }

        return builder.build();
    }

    public RavioliRequest<GiantBombResponse<Video>> getVideo(int videoId) {
        return new RavioliRequest.Builder<GiantBombResponse<Video>>(
                mGiantBombClient,
                new TypeToken<GiantBombResponse<Video>>(){}.getType()
        )
        .addPath("video/")
        .addPath(Integer.toString(videoId) + "/")
        .build();
    }

    public RavioliRequest<ApiKey> getValidate(String linkCode) {
        return new RavioliRequest.Builder<ApiKey>(
            mValidateClient,
            ApiKey.class
        )
        .addPath("validate")
        .addQueryParameter("link_code", linkCode)
        .addQueryParameter("format", "json")
        .build();
    }

    public RavioliRequest<GiantBombResponse<ArrayList<VideoType>>> getVideoTypes() {
        return new RavioliRequest.Builder<GiantBombResponse<ArrayList<VideoType>>>(
                mGiantBombClient,
            new TypeToken<GiantBombResponse<ArrayList<VideoType>>>(){}.getType()
        )
        .addPath("video_types/")
        .build();
    }

    public RavioliRequest<GiantBombResponse<ArrayList<Game>>> getGames(int offset) {
        return new RavioliRequest.Builder<GiantBombResponse<ArrayList<Game>>>(
                mGiantBombClient,
            new TypeToken<GiantBombResponse<ArrayList<Game>>>(){}.getType()
        )
        .addPath("games/")
        .addQueryParameter("sort", "date_added:desc")
        .addQueryParameter("offset", Integer.toString(offset))
        .setRetryPolicy(new DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            GAME_LIST_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ))
        .build();
    }

    public RavioliRequest<GiantBombResponse<Game>> getGame(int gameId) {
        return new RavioliRequest.Builder<GiantBombResponse<Game>>(
                mGiantBombClient,
            new TypeToken<GiantBombResponse<Game>>(){}.getType()
        )
        .addPath("game/")
        .addPath(Integer.toString(gameId) + "/")
        .build();
    }

    public RavioliRequest<GiantBombResponse<ArrayList<SearchResult>>> getSearch(String query, String[] resources) {
        StringBuilder resourcesBuilder = new StringBuilder();
        if (resources != null) {
            for (String resource : resources) {
                resourcesBuilder.append(resource);
                resourcesBuilder.append(",");
            }
        }

        RavioliRequest.Builder<GiantBombResponse<ArrayList<SearchResult>>> builder = new RavioliRequest.Builder<GiantBombResponse<ArrayList<SearchResult>>>(
                mGiantBombClient,
            new TypeToken<GiantBombResponse<ArrayList<SearchResult>>>(){}.getType()
        )
        .addPath("search/")
        .addQueryParameter("query", query)
        .setRetryPolicy(new DefaultRetryPolicy(
            DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
            GAME_LIST_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        if (resourcesBuilder.length() > 0) {
            builder = builder.addQueryParameter("resources", resourcesBuilder.toString());
        }

        return builder.build();
    }

    public void cancelRequests(Object tag) {
        mGiantBombClient.cancel(tag);
        Log.d(TAG, "canceled requests for " + tag);
    }

    public RavioliRequest<String> setPreferencesFuture(final Preferences preferences) {
        Ravioli mPreferenceClient = new Ravioli.Builder(
                mContext,
                Uri.parse(LUCHADEER_BASE_URL)
        ).build();

        byte[] bytes;
        try {
            bytes = new Gson().toJson(preferences).getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            // then the caller will npe, but that's fine in this case because something is really wrong.
            return null;
        }

        return new RavioliRequest.Builder<String>(
            mPreferenceClient,
            String.class
        )
        .addPath("preferences")
        .setMethod(Request.Method.POST)
        .setBody(bytes)
        .build();
    }

    public class YouTubeListResponse {
        @SerializedName("items") private ArrayList<YouTubeVideo> mItems;
        @SerializedName("nextPageToken") private String mNextPageToken;
        @SerializedName("pageInfo") private PageInfo mPageInfo;

        public String getNextPageToken() {
            return mNextPageToken;
        }

        public PageInfo getPageInfo() {
            return mPageInfo;
        }

        public class PageInfo {
            @SerializedName("totalResults") private int mTotalResults;

            public int getTotalResults() {
                return mTotalResults;
            }
        }

        public ArrayList<YouTubeVideo> getItems() {
            return mItems;
        }
    }

    public RavioliRequest<YouTubeListResponse> getUnarchivedVideos(String nextPageToken, String query) {
        RavioliRequest.Builder<YouTubeListResponse> builder = new RavioliRequest.Builder<>(
            mYouTubeClient,
            new TypeToken<YouTubeListResponse>(){}.getType()
        );
        if (query != null) {
            builder = builder.addQueryParameter("q", query);
        }
        if (nextPageToken != null) {
            builder = builder.addQueryParameter("pageToken", nextPageToken);
        }

        return builder.build();
    }
}

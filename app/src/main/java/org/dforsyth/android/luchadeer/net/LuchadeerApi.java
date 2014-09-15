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

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.model.giantbomb.ApiKey;
import org.dforsyth.android.luchadeer.model.giantbomb.Game;
import org.dforsyth.android.luchadeer.model.giantbomb.SearchResult;
import org.dforsyth.android.luchadeer.model.giantbomb.Video;
import org.dforsyth.android.luchadeer.model.giantbomb.VideoType;
import org.dforsyth.android.luchadeer.model.luchadeer.Preferences;
import org.dforsyth.android.luchadeer.persist.LuchadeerPreferences;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
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
    private RequestQueue mRequestQueue;
    private RequestQueue mImageRequestQueue;

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }


    private class GsonRequest<T> extends Request<T> {
        private Type mClazz;
        private Response.Listener<T> mListener;
        private Gson mGson;

        public GsonRequest(int method, String url, TypeToken<T> clazz, Response.Listener<T> listener,
                           Response.ErrorListener errorListener) {
            super(method, url, errorListener);
            mClazz = clazz.getType();
            mListener = listener;
            mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        }

        public Gson getGson() {
            return mGson;
        }

        @Override
        protected void deliverResponse(T response) {
            mListener.onResponse(response);
        }

        @Override
        protected Response<T> parseNetworkResponse(NetworkResponse networkResponse) {
            Log.d(TAG, getUrl());

            try {
                String json = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
                return Response.success((T)mGson.fromJson(json, mClazz), HttpHeaderParser.parseCacheHeaders(networkResponse));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return Response.error(new ParseError(networkResponse));
            }
        }
    }

    // special case request so we can parse error responses -- the api returns the wrong result type when state code != 1
    public class GiantBombRequest<T> extends GsonRequest<GiantBombResponse<T>> {
        private Type mClazz;

        public GiantBombRequest(int method, String url, TypeToken<GiantBombResponse<T>> clazz, Response.Listener<GiantBombResponse<T>> listener, Response.ErrorListener errorListener) {
            super(method, url, clazz, listener, errorListener);
            mClazz = clazz.getType();
        }

        @Override
        protected Response<GiantBombResponse<T>> parseNetworkResponse(NetworkResponse networkResponse) {
            Log.d(TAG, getUrl());

            String json;
            try {
                json = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
                // wrap this in an IllegalStateException catch so that we can catch errors from the api with an error response
                // (since error always returns a dictionary result instead of a list).
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return Response.error(new ParseError(networkResponse));
            }

            GiantBombResponse<T> response;
            try {
                response = (GiantBombResponse<T>)getGson().fromJson(json, mClazz);
            } catch (JsonSyntaxException e) {
                return Response.error(new GiantBombVolleyError(getGson(), json));
            }

            if (response == null) {
                return Response.error(new GiantBombVolleyError(getGson(), json));
            }

            if (response.getStatusCode() != GB_STATUS_OK) {
                return Response.error(new GiantBombVolleyError(response));
            }

            return Response.success(response, HttpHeaderParser.parseCacheHeaders(networkResponse));
        }
    }

    public static class GiantBombVolleyError extends VolleyError {
        private  GiantBombResponse mResponse;

        public GiantBombVolleyError(Gson gson, String json) {
            mResponse = gson.fromJson(json, new TypeToken<GiantBombResponse>(){}.getType());
        }

        public GiantBombVolleyError(GiantBombResponse response) {
            mResponse = response;
        }

        public GiantBombResponse<Object> getResponse() {
            return mResponse;
        }
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

    private LuchadeerApi(Context context) {
        // VolleyLog.DEBUG = true;
        mContext = context.getApplicationContext();

        mPreferences = LuchadeerPreferences.getInstance(mContext);

        mRequestQueue = Volley.newRequestQueue(mContext);
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
        reloadApiKey();

        mRequestQueue.start();
        mImageRequestQueue.start();
    }

    public LuchadeerApi(Context context, HttpStack stack, LuchadeerPreferences preferences) {
        mContext = context;

        mPreferences = preferences;
        mRequestQueue = Volley.newRequestQueue(mContext, stack);
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

        mRequestQueue.start();
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
        LUCHADEER_BASE_URL = mContext.getString(R.string.unlinked_base_uri);
        UNLINKED_BASE_URL = LUCHADEER_BASE_URL + "/giantbomb";
    }

    // reread api key out of preferences
    public void reloadApiKey() {
        mApiKey = mPreferences.getApiKey();
        mBaseUrl = mApiKey.isEmpty() ? UNLINKED_BASE_URL : LINKED_BASE_URL;
    }

    public String getApiKey() {
        return mApiKey;
    }

    private Uri.Builder GiantBombUriBuilder() {
        return Uri.parse(mBaseUrl).buildUpon()
            .appendQueryParameter("format", "json")
            .appendQueryParameter("api_key", mApiKey);
    }

    public void videos(Object tag,
                       String categoryId,
                       int offset,
                       Response.Listener<GiantBombResponse<ArrayList<Video>>> responseListener,
                       Response.ErrorListener errorListener) {

        Uri.Builder builder = GiantBombUriBuilder();
        builder = builder.appendEncodedPath("videos/")
                .appendQueryParameter("offset", Integer.toString(offset));

        if (categoryId != null && !categoryId.isEmpty()) {
            builder = builder.appendQueryParameter("video_type", categoryId);
        }

        String url = builder.build().toString();

        GiantBombRequest<ArrayList<Video>> r = new GiantBombRequest<ArrayList<Video>>(
                Request.Method.GET,
                url,
                new TypeToken<GiantBombResponse<ArrayList<Video>>>(){},
                responseListener,
                errorListener);
        r.setTag(tag);

        mRequestQueue.add(r);
    }

    public void video(Object tag,
                      int videoId,
                      Response.Listener<GiantBombResponse<Video>> responseListener,
                      Response.ErrorListener errorListener) {

        String url = GiantBombUriBuilder()
                .appendEncodedPath("video/")
                .appendPath(Integer.toString(videoId) + "/")
                .build().toString();

        GiantBombRequest<Video> r = new GiantBombRequest<Video>(
                Request.Method.GET,
                url,
                new TypeToken<GiantBombResponse<Video>>(){},
                responseListener,
                errorListener);
        r.setTag(tag);

        mRequestQueue.add(r);
    }

    public void validate(String linkCode, Response.Listener<ApiKey> responseListener,
                         Response.ErrorListener errorListener) {

        // this one breaks if there is a trailing slash...
        String url = Uri.parse(LINKED_BASE_URL).buildUpon()
                .appendPath("validate")
                .appendQueryParameter("link_code", linkCode)
                .appendQueryParameter("format", "json").build().toString();

        GsonRequest<ApiKey> r = new GsonRequest<ApiKey>(
                Request.Method.GET,
                url,
                new TypeToken<ApiKey>(){},
                responseListener,
                errorListener);

        mRequestQueue.add(r);
    }

    public void videoTypes(Object tag,
                           Response.Listener<GiantBombResponse<ArrayList<VideoType>>> responseListener,
                           Response.ErrorListener errorListener) {

        String url = GiantBombUriBuilder().appendEncodedPath("video_types/").build().toString();

        Log.d(TAG, url);


        GiantBombRequest<ArrayList<VideoType>> r = new GiantBombRequest<ArrayList<VideoType>>(
                Request.Method.GET,
                url,
                new TypeToken<GiantBombResponse<ArrayList<VideoType>>>(){},
                responseListener,
                errorListener);

        r.setTag(tag);

        mRequestQueue.add(r);
    }

    public void games(Object tag,
                      int offset,
                      Response.Listener<GiantBombResponse<ArrayList<Game>>> responseListener,
                      Response.ErrorListener errorListener) {

        String url = GiantBombUriBuilder()
                .appendEncodedPath("games/")
                .appendQueryParameter("sort", "date_added:desc")
                .appendQueryParameter("offset", Integer.toString(offset))
                .build().toString();

        GiantBombRequest<ArrayList<Game>> r = new GiantBombRequest<ArrayList<Game>>(
                Request.Method.GET,
                url,
                new TypeToken<GiantBombResponse<ArrayList<Game>>>(){},
                responseListener,
                errorListener);

        // this is request can be crazy slow from giantbomb -- we retry a few times but we don't back off
        r.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                GAME_LIST_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        r.setTag(tag);

        mRequestQueue.add(r);
    }

    public void game(Object tag,
                     int gameId,
                     Response.Listener<GiantBombResponse<Game>> responseListener,
                     Response.ErrorListener errorListener) {

        String url = GiantBombUriBuilder()
                .appendEncodedPath("game/")
                .appendPath(Integer.toString(gameId) + "/")
                .build().toString();

        GiantBombRequest<Game> r = new GiantBombRequest<Game>(
                Request.Method.GET,
                url,
                new TypeToken<GiantBombResponse<Game>>(){},
                responseListener,
                errorListener);

        r.setTag(tag);

        mRequestQueue.add(r);
    }

    public void search(Object tag,
                       String query,
                       String[] resources,
                       Response.Listener<GiantBombResponse<ArrayList<SearchResult>>> responseListener,
                       Response.ErrorListener errorListener) {

        // dont have StringJoiner
        String resourceList = "";
        if (resources != null) {
            for (String resource : resources) {
                resourceList += resource + ",";
            }
        }

        Uri.Builder builder = GiantBombUriBuilder()
                .appendEncodedPath("search/")
                .appendQueryParameter("query", query);
        if (!resourceList.isEmpty()) {
            builder.appendQueryParameter("resources", resourceList);
        }

        String url = builder.build().toString();

        GiantBombRequest<ArrayList<SearchResult>> r = new GiantBombRequest<ArrayList<SearchResult>>(
                Request.Method.GET,
                url,
                new TypeToken<GiantBombResponse<ArrayList<SearchResult>>>(){},
                responseListener,
                errorListener);

        // this is request can be crazy slow from giantbomb -- we retry a few times
        r.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                GAME_LIST_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        r.setTag(tag);

        mRequestQueue.add(r);
    }

    public void cancelRequests(Object tag) {
        mRequestQueue.cancelAll(tag);
        Log.d(TAG, "canceled requests for " + tag);
    }

    public RequestFuture<String> setPreferencesFuture(final Preferences preferences) {
        RequestFuture<String> future = RequestFuture.newFuture();
        String url = LUCHADEER_BASE_URL + "/preferences";
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                future,
                future) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return new Gson().toJson(preferences).getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        mRequestQueue.add(request);

        return future;
    }
}

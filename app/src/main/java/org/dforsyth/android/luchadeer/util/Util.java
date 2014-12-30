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

package org.dforsyth.android.luchadeer.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.net.LuchadeerApi;
import org.dforsyth.android.luchadeer.persist.LuchadeerPreferences;
import org.dforsyth.android.luchadeer.service.PreferenceSyncService;
import org.dforsyth.android.ravioli.encoders.DecodeError;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class Util {

    private final static String TAG = Util.class.getName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static int GCM_REREGISTER_DAYS = 7;

    public static boolean postDelayed(Handler handler, Runnable runnable, Object token, int delayMillis) {
        return handler.postAtTime(runnable, token, SystemClock.uptimeMillis() + delayMillis);
    }

    public static boolean checkGooglePlayServices(Activity activity) {
        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (result != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(result)) {
                GooglePlayServicesUtil.getErrorDialog(
                        result,
                        activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
                return false;
            }
        }

        return true;
    }

    public static int getApplicationVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static void registerGCMIfNecessary(Context context) {
        LuchadeerPreferences preferences = LuchadeerPreferences.getInstance(context);
        String gcmRegistrationId = preferences.getGCMRegistrationId();
        int registeredAppVersion = preferences.getGCMRegistrationVersion();
        long registrationTime = preferences.getGCMRegistrationTime();

        long registrationDelta = System.currentTimeMillis() - registrationTime;

        int appVersion = getApplicationVersion(context);

        if (gcmRegistrationId == null || registeredAppVersion != appVersion || TimeUnit.MILLISECONDS.toDays(registrationDelta) > GCM_REREGISTER_DAYS) {
            registerGCM(context);
        }
    }

    public static void registerGCM(final Context context) {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void[] objects) {
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                String senderId = context.getString(R.string.gcm_sender_id);

                String registrationId = null;
                try {
                    registrationId = gcm.register(senderId);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "registration id: " + registrationId);

                return registrationId;
            }

            @Override
            protected void onPostExecute(String registrationId) {
                super.onPostExecute(registrationId);
                if (registrationId == null || registrationId.isEmpty()) {
                    Log.d(TAG, "registration id is null or empty");
                    return;
                }

                // store registration id
                LuchadeerPreferences preferences = LuchadeerPreferences.getInstance(context);
                preferences.setGCMRegistrationId(registrationId);
                preferences.setGCMRegistrationVersion(getApplicationVersion(context));
                preferences.setGCMRegistrationTime(System.currentTimeMillis());

                // start preference sync service
                context.startService(new Intent(context, PreferenceSyncService.class));
            }
        };
        task.execute(null, null, null);
    }

    public static boolean giantBombAccountLinked(Context context) {
        LuchadeerPreferences preferences = LuchadeerPreferences.getInstance(context);
        return !preferences.apiKeyIsDefault();
    }

    /*
    public static String downloadStatusToText(int downloadStatus) {
        switch (downloadStatus) {
            case (DownloadManager.STATUS_RUNNING):
            case (DownloadManager.STATUS_PENDING):
            case (DownloadManager.STATUS_PAUSED):
                return "Downloading";
            case (DownloadManager.STATUS_SUCCESSFUL):
                return "Downloaded";
            case (DownloadManager.STATUS_FAILED):
                return "Failed";
        }
        return "Downloading";
    }
    */

    // XXX all of this is garbage.

    // for now these handlers will just show toasts
    public static void handleGiantBombError(Context context, LuchadeerApi.GiantBombResponse gbr) {
        int statusCode = gbr.getStatusCode();
        switch (statusCode) {
            case (LuchadeerApi.GB_STATUS_OK):
                return;
            case (LuchadeerApi.GB_STATUS_SUBSCRIBER_CONTENT):
                Toast.makeText(context, R.string.subscriber_content_error, Toast.LENGTH_LONG).show();
                return;
            case (LuchadeerApi.GB_STATUS_RATE_LIMIT):
                Toast.makeText(context, R.string.rate_limit_error, Toast.LENGTH_SHORT).show();
                return;
            case (LuchadeerApi.GB_STATUS_NOT_FOUND):
                Toast.makeText(context, R.string.giantbomb_not_found_error, Toast.LENGTH_SHORT).show();
                return;
            case (LuchadeerApi.GB_STATUS_FILTER):
            case (LuchadeerApi.GB_STATUS_URL_FORMAT):
            default:
                Toast.makeText(context, R.string.giantbomb_request_error, Toast.LENGTH_SHORT).show();
        }
    }

    public static void handleRequestError(Context context, Exception error) {
        if (error instanceof ExecutionException) {
            handleVolleyError(context, (VolleyError) error.getCause());
            return;
        }

        if (error instanceof VolleyError) {
            handleVolleyError(context, (VolleyError) error);
            return;
        }
    }

    public static void handleVolleyError(Context context, VolleyError error) {
        Exception cause = (Exception) error.getCause();

        if (cause instanceof TimeoutError) {
            Toast.makeText(context, R.string.luchadeer_request_error, Toast.LENGTH_SHORT).show();
            return;
        }

        if (cause instanceof DecodeError) {
            // try to handle a giantbomb error
            // TODO: dont instantiate this here
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
            try {
                LuchadeerApi.GiantBombResponse gbr = gson.fromJson(
                        ((DecodeError) cause).getData(),
                        LuchadeerApi.GiantBombResponse.class
                );
                handleGiantBombError(context, gbr);
                return;
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }

        int statusCode = 0;
        if (error.networkResponse != null) {
            statusCode = error.networkResponse.statusCode;
        }
        switch (statusCode) {
            case (LuchadeerApi.LUCHA_STATUS_QUOTA_ERROR1):
            case (LuchadeerApi.LUCHA_STATUS_QUOTA_ERROR2):
                Toast.makeText(context, R.string.luchadeer_quota_error, Toast.LENGTH_SHORT).show();
                return;
            default:
                Toast.makeText(context, R.string.luchadeer_request_error, Toast.LENGTH_SHORT).show();
                return;
        }
    }

    public static String addApiKey(Context context, String uri) {
        String apiKey = LuchadeerPreferences.getInstance(context.getApplicationContext()).getApiKey();
        return Uri.parse(uri).buildUpon().appendQueryParameter("api_key", apiKey).build().toString();
    }
}

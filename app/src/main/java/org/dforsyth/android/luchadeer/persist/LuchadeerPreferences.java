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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dforsyth.android.luchadeer.R;
import org.dforsyth.android.luchadeer.model.giantbomb.VideoType;
import org.dforsyth.android.luchadeer.model.luchadeer.Preferences;

import java.util.ArrayList;
import java.util.HashSet;


public class LuchadeerPreferences {
    private static LuchadeerPreferences sInstance;
    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private Gson mGson;
    private Resources mResources;

    private LuchadeerPreferences(Context context) {
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mGson = new Gson();
        mResources = mContext.getResources();
    }

    public LuchadeerPreferences(Context context, String name) {
        mContext = context;
        mGson = new Gson();
        mResources = mContext.getResources();
        mSharedPreferences = mContext.getSharedPreferences(name, 0);
    }

    public static LuchadeerPreferences getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new LuchadeerPreferences(context);
        }

        return sInstance;
    }

    public String getApiKey() {
        return mSharedPreferences.getString(
                mResources.getString(R.string.pref_api_key),
                "");
    }

    public void setApiKey(String apiKey) {
        mSharedPreferences.edit().putString(
                mResources.getString(R.string.pref_api_key),
                apiKey)
                .commit();
    }

    public boolean apiKeyIsDefault() {
        return getApiKey().equals("");
    }

    public void removeApiKey() {
        mSharedPreferences.edit().remove(
                mResources.getString(R.string.pref_api_key))
                .commit();
    }

    public String getPreferredQuality() {
        return mSharedPreferences.getString(mResources.getString(R.string.pref_quality), "high");
    }

    public void setPreferredQuality(String quality) {
        mSharedPreferences.edit().putString(
                mResources.getString(R.string.pref_quality),
                quality)
                .commit();
    }


    public void setVideoTypeCache(ArrayList<VideoType> videoTypes) {
        String json = mGson.toJson(videoTypes);
        mSharedPreferences.edit().putString(
                mResources.getString(R.string.pref_video_type_cache),
                json)
                .apply();
    }

    public ArrayList<VideoType> getVideoTypeCache() {
        String json = mSharedPreferences.getString(
                mResources.getString(R.string.pref_video_type_cache),
                null);

        if (json == null) {
            return new ArrayList<VideoType>();
        }

        TypeToken<ArrayList<VideoType>> token = new TypeToken<ArrayList<VideoType>>(){};
        return mGson.fromJson(json, token.getType());
    }

    public String getGCMRegistrationId() {
        return mSharedPreferences.getString(
                mResources.getString(R.string.pref_gcm_registration_id),
                null);
    }

    public void setGCMRegistrationId(String registrationId) {
        mSharedPreferences.edit().putString(
                mResources.getString(R.string.pref_gcm_registration_id),
                registrationId).commit();
    }

    public int getGCMRegistrationVersion() {
        return mSharedPreferences.getInt(
                mResources.getString(R.string.pref_gcm_registration_version),
                Integer.MIN_VALUE);
    }

    public void setGCMRegistrationVersion(int version) {
        mSharedPreferences.edit().putInt(
                mResources.getString(R.string.pref_gcm_registration_version),
                version).commit();
    }

    public Preferences forUpload() {
        return new Preferences(
                getGCMRegistrationId(),
                mSharedPreferences.getStringSet(
                        mResources.getString(R.string.pref_push_notification_categories),
                        new HashSet<String>())
        );
    }

    public boolean getNotificationsEnabled() {
        return mSharedPreferences.getBoolean(
                mResources.getString(R.string.pref_push_notification),
                false
        );
    }

    public boolean getNotificationVibrationEnabled() {
        return mSharedPreferences.getBoolean(
                mResources.getString(R.string.pref_push_notification_vibrate),
                false
        );
    }

    public boolean getNotificationLEDEnabled() {
        return mSharedPreferences.getBoolean(
                mResources.getString(R.string.pref_push_notification_led),
                false
        );
    }

    public void clearPreferences() {
        mSharedPreferences.edit().clear().commit();
    }

    public boolean getFilterTrailers() {
        return mSharedPreferences.getBoolean(
                mResources.getString(R.string.pref_filter_trailers),
                false
        );
    }
}

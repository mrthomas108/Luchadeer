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

package org.dforsyth.android.luchadeer.model.giantbomb;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class VideoType implements Serializable, Parcelable {
    @SerializedName("api_detail_url") private String mApiDetailUrl;
    @SerializedName("deck") private String mDeck;
    @SerializedName("id") private String mId;
    @SerializedName("name") private String mName;
    @SerializedName("site_detail_url") private String mSiteDetailUrl;

    public String getApiDetailUrl() {
        return mApiDetailUrl;
    }

    public String getDeck() {
        return mDeck;
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getSiteDetailUrl() {
        return mSiteDetailUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mApiDetailUrl);
        parcel.writeString(mDeck);
        parcel.writeString(mId);
        parcel.writeString(mName);
        parcel.writeString(mSiteDetailUrl);
    }

    public static final Parcelable.Creator<VideoType> CREATOR
            = new Parcelable.Creator<VideoType>() {
        public VideoType createFromParcel(Parcel in) {
            return new VideoType(in);
        }

        public VideoType[] newArray(int size) {
            return new VideoType[size];
        }
    };

    private VideoType(Parcel in) {
        mApiDetailUrl = in.readString();
        mDeck = in.readString();
        mId = in.readString();
        mName = in.readString();
        mSiteDetailUrl = in.readString();
    }
}

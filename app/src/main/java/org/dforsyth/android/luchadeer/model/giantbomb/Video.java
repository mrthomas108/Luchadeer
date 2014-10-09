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

import org.dforsyth.android.luchadeer.ui.util.ContentListFragment;

import java.util.Date;


public class Video implements /* Serializable, */ ContentListFragment.Content, Parcelable {
    @SerializedName("id") private int mId;
    @SerializedName("api_detail_url") private String mApiDetailUrl;
    @SerializedName("name") private String mName;
    @SerializedName("image") private Image mImage;
    @SerializedName("deck") private String mDeck;
    @SerializedName("high_url") private String mHighUrl;
    @SerializedName("low_url") private String mLowUrl;
    @SerializedName("hd_url") private String mHDUrl;
    @SerializedName("length_seconds") private int mLengthSeconds;
    @SerializedName("user") private String mUser;
    @SerializedName("publish_date") private Date mPublishDate;
    @SerializedName("site_detail_url") private String mSiteDetailUrl;
    @SerializedName("youtube_id") private String mYouTubeId;
    @SerializedName("video_type") private String mVideoType;

    public String getApiDetailUrl() {
        return mApiDetailUrl;
    }

    public Image getImage() {
        return mImage;
    }

    public String getImageUrl() {
        if (mImage == null) {
            return null;
        }

        return mImage.getSuperUrl();
    }

    public String getName() {
        return mName;
    }

    public String getDeck() {
        return mDeck;
    }

    public String getHighUrl() {
        return mHighUrl;
    }

    public Date getPublishDate() {
        return mPublishDate;
    }

    public String getUser() {
        return mUser;
    }

    public int getLengthSeconds() {
        return mLengthSeconds;
    }

    public String getLowUrl() {
        return mLowUrl;
    }

    public String getHDUrl() {
        return mHDUrl;
    }

    public String getSiteDetailUrl() {
        return mSiteDetailUrl;
    }

    public String getYouTubeId() {
        return mYouTubeId;
    }

    public int getId() {
        return mId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mId);
        parcel.writeString(mApiDetailUrl);
        parcel.writeString(mName);
        parcel.writeParcelable(mImage, i);
        parcel.writeString(mDeck);
        parcel.writeString(mHighUrl);
        parcel.writeString(mLowUrl);
        parcel.writeString(mHDUrl);
        parcel.writeInt(mLengthSeconds);
        parcel.writeString(mUser);
        parcel.writeLong(mPublishDate != null ? mPublishDate.getTime() : -1);
        parcel.writeString(mSiteDetailUrl);
        parcel.writeString(mYouTubeId);
        parcel.writeString(mVideoType);
    }

    public static final Parcelable.Creator<Video> CREATOR
            = new Parcelable.Creator<Video>() {
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

    private Video(Parcel in) {
        mId = in.readInt();
        mApiDetailUrl = in.readString();
        mName = in.readString();
        mImage = in.readParcelable(Image.class.getClassLoader());
        mDeck = in.readString();
        mHighUrl = in.readString();
        mLowUrl = in.readString();
        mHDUrl = in.readString();
        mLengthSeconds = in.readInt();
        mUser = in.readString();
        long date = in.readLong();
        mPublishDate = date != -1 ? new Date(date) : null;
        mSiteDetailUrl = in.readString();
        mYouTubeId = in.readString();
        mVideoType = in.readString();
    }

    public String getVideoType() {
        return mVideoType;
    }
}
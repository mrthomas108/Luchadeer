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

package org.dforsyth.android.luchadeer.model.youtube;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import org.dforsyth.android.luchadeer.ui.util.ContentListFragment;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class YouTubeVideo implements ContentListFragment.Content, Parcelable {
    @SerializedName("id") private Id mId;
    @SerializedName("snippet") private Snippet mSnippet;

    @Override
    public String getName() {
        return mSnippet.getTitle();
    }

    @Override
    public String getImageUrl() {
        if (mSnippet.getThumbnails() == null) {
            return null;
        }

        if (mSnippet.getThumbnails().get("high") == null) {
            return null;
        }

        return mSnippet.getThumbnails().get("high").getUrl();
    }

    public Snippet getSnippet() {
        return mSnippet;
    }

    public String getVideoId() {
        return mId.getVideoId();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeSerializable(mId);
        parcel.writeSerializable(mSnippet);
    }

    public static final Parcelable.Creator<YouTubeVideo> CREATOR
            = new Parcelable.Creator<YouTubeVideo>() {
        public YouTubeVideo createFromParcel(Parcel in) {
            return new YouTubeVideo(in);
        }

        public YouTubeVideo[] newArray(int size) {
            return new YouTubeVideo[size];
        }
    };

    private YouTubeVideo(Parcel in) {
        mId = (Id) in.readSerializable();
        mSnippet = (Snippet) in.readSerializable();
    }

    public class Id implements Serializable {
        @SerializedName("videoId") private String mVideoId;

        public String getVideoId() {
            return mVideoId;
        }
    }

    public class Snippet implements Serializable {
        @SerializedName("title") private String mTitle;
        @SerializedName("description") private String mDescription;
        @SerializedName("publishedAt") private Date mPublishedAt;
        @SerializedName("channelTitle") private String mChannelTitle;
        @SerializedName("thumbnails") private HashMap<String, Thumbnail> mThumbnails;

        public String getTitle() {
            return mTitle;
        }

        public String getDescription() {
            return mDescription;
        }

        public Date getPublishedAt() {
            return mPublishedAt;
        }

        public String getChannelTitle() {
            return mChannelTitle;
        }

        public HashMap<String, Thumbnail> getThumbnails() {
            return mThumbnails;
        }
    }

    public class Thumbnail implements Serializable {
        @SerializedName("url") private String mUrl;

        public String getUrl() {
            return mUrl;
        }
    }
}

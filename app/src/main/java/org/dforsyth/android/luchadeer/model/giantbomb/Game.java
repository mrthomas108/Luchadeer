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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class Game implements ContentListFragment.Content, Parcelable {
    @SerializedName("id") private int mId;
    @SerializedName("name") private String mName;
    @SerializedName("image") private Image mImage;
    @SerializedName("images") private ArrayList<Image> mImages;
    @SerializedName("deck") private String mDeck;
    @SerializedName("description") private String mDescription;
    @SerializedName("platforms") private ArrayList<Platform> mPlatforms;
    @SerializedName("videos") private ArrayList<Video> mVideos;
    @SerializedName("original_release_date") private Date mOriginalReleaseDate;
    @SerializedName("developers") private ArrayList<SimpleNamedObject> mDevelopers;
    @SerializedName("publishers") private ArrayList<SimpleNamedObject> mPublishers;
    @SerializedName("site_detail_url") private String mSiteDetailUrl;
    // dealing with null is easier.
    @SerializedName("expected_release_day") Integer mExpectedReleaseDay;
    @SerializedName("expected_release_month") Integer mExpectedReleaseMonth;
    @SerializedName("expected_release_quarter") Integer mExpectedReleaseQuarter;
    @SerializedName("expected_release_year") Integer mExpectedReleaseYear;
    @SerializedName("similar_games") private ArrayList<Game> mSimilarGames;

    public String getName() {
        return mName;
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

    public void setImage(Image image) {
        mImage = image;
    }

    public ArrayList<Image> getImages() {
        return mImages;
    }

    public int getId() {
        return mId;
    }

    public String getDeck() {
        return mDeck;
    }

    public String getDescription() {
        return mDescription;
    }

    public ArrayList<Platform> getPlatforms() {
        return mPlatforms;
    }

    public ArrayList<Video> getVideos() {
        return mVideos;
    }

    public Date getOriginalReleaseDate() {
        return mOriginalReleaseDate;
    }

    public ArrayList<SimpleNamedObject> getDevelopers() {
        return mDevelopers;
    }

    public ArrayList<SimpleNamedObject> getPublishers() {
        return mPublishers;
    }

    public String getSiteDetailUrl() {
        return mSiteDetailUrl;
    }

    public String getExpectedReleaseDateString() {
        Calendar instance = Calendar.getInstance();
        String formatString = "";

        if (mExpectedReleaseMonth != null) {
            instance.set(Calendar.MONTH, mExpectedReleaseMonth - 1);
            formatString += "MMM";
        }

        if (mExpectedReleaseDay != null) {
            instance.set(Calendar.DAY_OF_MONTH, mExpectedReleaseDay);
            formatString += (formatString.isEmpty() ? "" : " ") + "d";
        }

        if (mExpectedReleaseYear != null) {
            instance.set(Calendar.YEAR, mExpectedReleaseYear);
            formatString += (formatString.isEmpty() ? "" : ", ") + "yyyy";
        }

        if (mExpectedReleaseQuarter != null) {
            formatString = "'(Q" + mExpectedReleaseQuarter + ")'" + (formatString.isEmpty() ? "" : " ") + formatString;
        }

        if (formatString.isEmpty()) {
            return null;
        }

        return new SimpleDateFormat(formatString).format(instance.getTime());
    }

    public int getExpectedReleaseYear() {
        return mExpectedReleaseYear;
    }

    public int getExpectedReleaseQuarter() {
        return mExpectedReleaseQuarter;
    }

    public int getExpectedReleaseMonth() {
        return mExpectedReleaseMonth;
    }

    public int getExpectedReleaseDay() {
        return mExpectedReleaseDay;
    }

    public ArrayList<Game> getSimilarGames() {
        return mSimilarGames;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mId);
        parcel.writeString(mName);
        parcel.writeParcelable(mImage, i);
        parcel.writeList(mImages);
        parcel.writeString(mDeck);
        parcel.writeString(mDescription);
        parcel.writeList(mPlatforms);
        parcel.writeList(mVideos);
        parcel.writeLong(mOriginalReleaseDate != null ? mOriginalReleaseDate.getTime() : -1);
        parcel.writeList(mDevelopers);
        parcel.writeList(mPublishers);
        parcel.writeString(mSiteDetailUrl);
        parcel.writeInt(mExpectedReleaseDay != null ? mExpectedReleaseDay : -1);
        parcel.writeInt(mExpectedReleaseMonth != null ? mExpectedReleaseMonth : -1);
        parcel.writeInt(mExpectedReleaseQuarter != null ? mExpectedReleaseQuarter : -1);
        parcel.writeInt(mExpectedReleaseYear != null ? mExpectedReleaseYear : -1);
        parcel.writeList(mSimilarGames);
    }

    public static final Parcelable.Creator<Game> CREATOR
            = new Parcelable.Creator<Game>() {
        public Game createFromParcel(Parcel in) {
            return new Game(in);
        }

        public Game[] newArray(int size) {
            return new Game[size];
        }
    };

    private Game(Parcel in) {
        mId = in.readInt();
        mName = in.readString();
        mImage = in.readParcelable(Image.class.getClassLoader());
        mImages = in.readArrayList(Image.class.getClassLoader());
        mDeck = in.readString();
        mDescription = in.readString();
        mPlatforms = in.readArrayList(Image.class.getClassLoader());
        mVideos = in.readArrayList(Video.class.getClassLoader());
        long date = in.readLong();
        mOriginalReleaseDate = date != -1 ? new Date(date) : null;
        mDevelopers = in.readArrayList(SimpleNamedObject.class.getClassLoader());
        mPublishers = in.readArrayList(SimpleNamedObject.class.getClassLoader());
        mSiteDetailUrl = in.readString();
        int day = in.readInt();
        mExpectedReleaseDay = day != -1 ? day : null;
        int month = in.readInt();
        mExpectedReleaseMonth = month != -1 ? month : null;
        int quarter = in.readInt();
        mExpectedReleaseQuarter = quarter != -1 ? quarter : null;
        int year = in.readInt();
        mExpectedReleaseYear = year != -1 ? year : null;
        mSimilarGames = in.readArrayList(Game.class.getClassLoader());
    }
}

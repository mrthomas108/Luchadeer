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
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;

import org.dforsyth.android.luchadeer.model.giantbomb.Video;


public class VideoUtil {
    private static final String TAG = VideoUtil.class.getName();

    public static int REQUEST_CODE_VIDEO_PLAYBACK = 2;
    public static int RESULT_CODE_ERROR = Activity.RESULT_FIRST_USER + 0;

    public static final String VIDEO_TYPE_LIVE = "live";

    public static final String VIDEO_SOURCE_HD = "HD";
    public static final String VIDEO_SOURCE_HIGH = "High";
    public static final String VIDEO_SOURCE_LOW = "Low";
    public static final String VIDEO_SOURCE_YOUTUBE = "YouTube";

    public static void playVideo(Activity activity, Video video, String quality) {
        String url = getQualityUrl(video, quality);

        playVideo(activity, video.getName(), Util.addApiKey(activity, url), video.getId(), video.getImage().getSuperUrl(), false);
    }

    public static void playVideo(Activity activity, String videoName, String videoUri, int giantBombId, String imageUrl, boolean forceLocal) {
        VideoCastManager castManager = CastUtil.getVideoCastManager(activity);

        if (castManager.isConnected() && !forceLocal) {
            MediaMetadata md = new MediaMetadata();
            md.putString(MediaMetadata.KEY_TITLE, videoName);
            md.addImage(new WebImage(Uri.parse(imageUrl)));
            md.addImage(new WebImage(Uri.parse(imageUrl)));
            MediaInfo info = new MediaInfo.Builder(videoUri)
                    .setMetadata(md)
                    .setContentType("video/mp4")
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .build();

            castManager.startCastControllerActivity(activity, info, 0, true);
        } else {
            Log.d(TAG, "starting video player activity");
            activity.startActivityForResult(
                    IntentUtil.getVideoPlayerActivityIntent(
                            activity,
                            videoName,
                            videoUri,
                            giantBombId),
                    REQUEST_CODE_VIDEO_PLAYBACK);
        }
    }

    public static String getQualityUrl(Video video, String quality) {
        // TODO: should this account for local (getQualityUri)?

        if (quality.equals(VIDEO_SOURCE_HD)) {
            return video.getHDUrl();
        } else if (quality.equals(VIDEO_SOURCE_HIGH)) {
            return video.getHighUrl();
        } else if (quality.equals(VIDEO_SOURCE_LOW))   {
            return video.getLowUrl();
        } else {
            return null;
        }
    }
}
